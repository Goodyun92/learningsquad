package com.example.learningsquad.answer.service;

import com.example.learningsquad.answer.domain.AnswerEntity;
import com.example.learningsquad.answer.model.CreateAnswerRequestDto;
import com.example.learningsquad.answer.model.GetAnswerResponseDto;
import com.example.learningsquad.answer.model.GetSimilarityRequestDto;
import com.example.learningsquad.answer.repository.AnswerRepository;
import com.example.learningsquad.global.common.model.ResultCode;
import com.example.learningsquad.global.exception.AppException;
import com.example.learningsquad.question.domain.QuestionEntity;
import com.example.learningsquad.question.service.QuestionService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnswerService {

    private final WebClient.Builder webClientBuilder;
    private final AnswerRepository answerRepository;
    private final QuestionService questionService;
    private ReactiveTransactionManager transactionManager;  // Reactive DB 트랜잭션 매니저

    @Value("${flask.similarity.base-url}")
    private String baseUrl;

    private static final String NAME = "lsls";
    private static final String FALLBACKMETHOD = "getSimilarityFallback";

    public List<GetAnswerResponseDto> getAnswers(Long questionId, String sort) {
        List<AnswerEntity> answers;
        switch (sort.toUpperCase()) {
            case "RECENT" ->
                answers = answerRepository.findByQuestionEntity_ObjectIdOrderByCreatedOnDesc(questionId);
            case "OLD" ->
                answers = answerRepository.findByQuestionEntity_ObjectIdOrderByCreatedOnAsc(questionId);
            case "SCORE_LOW" ->
                answers = answerRepository.findByQuestionEntity_ObjectIdOrderByScoreAsc(questionId);
            case "SCORE_HIGH" ->
                answers = answerRepository.findByQuestionEntity_ObjectIdOrderByScoreDesc(questionId);
            default ->
                throw new AppException(ResultCode.FAILURE, "정렬 기준이 잘못되었습니다. RECENT, OLD, SCORE_LOW, SCORE_HIGH 중 하나를 선택하세요.");
        }

        return answers.stream()
                .map(GetAnswerResponseDto::of)
                .toList();
    }

    public Mono<GetAnswerResponseDto> createAnswer(CreateAnswerRequestDto requestDto) {
        final String correctAnswer = questionService.getModelAnswer(requestDto.getQuestionId());
        final String userAnswer = requestDto.getAnswer();

        // Reactor 환경에서도 트랜잭션 컨텍스트를 유지
        // 비동기 스레드에서 트랜잭션 유지
        final TransactionalOperator txOperator = TransactionalOperator.create(transactionManager);

        // transactional
        return getSimilarity(correctAnswer, userAnswer)
                .flatMap(newScore -> {
                    if (newScore < 0) {
                        return Mono.error(new AppException(ResultCode.FAILURE, "모델 서버 응답 에러, 잠시 후 다시 시도해주세요."));
                    }
                    return Mono.defer(() -> saveAnswer(newScore, requestDto));  // Mono.defer()로 트랜잭션 컨텍스트 유지
                })
                .as(txOperator::transactional)
                .onErrorResume(e -> Mono.error(new AppException(ResultCode.FAILURE, "답변 생성 실패 : " + e.getMessage())));
    }

    @CircuitBreaker(name = NAME, fallbackMethod = FALLBACKMETHOD)
    private Mono<Integer> getSimilarity(String correctAnswer, String userAnswer) {
        // 모델 서버로 전송
        // webflux 사용
        final WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();

        final GetSimilarityRequestDto requestDto = GetSimilarityRequestDto.builder()
                .sentence1(correctAnswer)
                .sentence2(userAnswer)
                .build();

        return webClient.post()
                .uri("/get-similarity")
                .body(BodyInserters.fromValue(requestDto))
                .retrieve()
                .bodyToMono(Integer.class)
                .onErrorResume(e -> Mono.error(new AppException(ResultCode.FAILURE, "모델 서버 응답 에러, 잠시 후 다시 시도해주세요.")));

    }

    /**
     * circuit breaker getSimilarity fallback method
     * @param correctAnswer
     * @param userAnswer
     * @param t
     * @return
     */
    private Mono<Integer> getSimilarityFallback(String correctAnswer, String userAnswer, Throwable t) {
        log.error("Fallback : " + t.getMessage());
        return Mono.just(-1);  // fallback data
    }

    private Mono<GetAnswerResponseDto> saveAnswer(Integer newScore, CreateAnswerRequestDto requestDto) {
        return Mono.fromCallable(() -> {
                    final QuestionEntity questionEntity = questionService.getQuestionEntity(requestDto.getQuestionId());

                    // 최적 답변 여부 확인 및 저장
                    if (newScore > questionEntity.getBestScore()) {
                        questionEntity.setBestAnswer(requestDto.getAnswer());
                        questionEntity.setBestScore(newScore);
                        questionService.saveQuestion(questionEntity);  // 블로킹 JPA 호출
                    }

                    // 새로운 답변 저장
                    final AnswerEntity answerEntity = AnswerEntity.builder()
                            .questionEntity(questionEntity)
                            .answer(requestDto.getAnswer())
                            .score(newScore)
                            .build();
                    answerRepository.save(answerEntity);  // 블로킹 JPA 호출

                    return GetAnswerResponseDto.of(answerEntity);
                })
                .subscribeOn(Schedulers.boundedElastic());  // 블로킹 작업 별도 스레드 처리
    }
}
