package com.example.learningsquad.answer.service;

import com.example.learningsquad.answer.domain.AnswerEntity;
import com.example.learningsquad.answer.model.CreateAnswerRequestDto;
import com.example.learningsquad.answer.model.GetAnswerResponseDto;
import com.example.learningsquad.answer.repository.AnswerRepository;
import com.example.learningsquad.global.common.model.ResultCode;
import com.example.learningsquad.global.exception.AppException;
import com.example.learningsquad.question.domain.QuestionEntity;
import com.example.learningsquad.question.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionService questionService;

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

    public GetAnswerResponseDto createAnswer(CreateAnswerRequestDto requestDto) {
        final Integer newScore = requestDto.getSimilarityScore();

        final QuestionEntity questionEntity = questionService.getQuestionEntity(requestDto.getQuestionId());

        // 최적 답변 여부 확인 및 저장
        if (newScore > questionEntity.getBestScore()) {
            questionEntity.setBestAnswer(requestDto.getAnswer());
            questionEntity.setBestScore(newScore);
            questionService.saveQuestion(questionEntity);
        }

        // 새로운 답변 저장
        final AnswerEntity answerEntity = AnswerEntity.builder()
                .questionEntity(questionEntity)
                .answer(requestDto.getAnswer())
                .score(newScore)
                .build();
        answerRepository.save(answerEntity);

        return GetAnswerResponseDto.of(answerEntity);

    }
}
