package com.example.learningsquad.upload.service;

import com.example.learningsquad.document.domain.DocumentEntity;
import com.example.learningsquad.document.service.DocumentService;
import com.example.learningsquad.global.common.model.ResultCode;
import com.example.learningsquad.global.exception.AppException;
import com.example.learningsquad.question.domain.QuestionEntity;
import com.example.learningsquad.question.service.QuestionService;
import com.example.learningsquad.upload.model.GetCsvRequestDto;
import com.example.learningsquad.upload.model.GetCsvReturnDto;
import com.example.learningsquad.upload.model.UploadRequestDto;
import com.example.learningsquad.upload.model.UploadReturnDto;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static com.example.learningsquad.global.exception.ExceptionCode.INVALID_EXTENSION;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadService {

    private final WebClient.Builder webClientBuilder;
    private final DocumentService documentService;
    private final QuestionService questionService;
    private ReactiveTransactionManager transactionManager;  // Reactive DB 트랜잭션 매니저

    @Value("${flask.upload.base-url}")
    private String baseUrl;

    private static final String NAME = "lsls";
    private static final String FALLBACKMETHOD = "getCsvUrlFallback";

    /**
     * 1. PDF 파일 다운로드 및 검증
     * 2. PDF 모델 서버로 업로드
     * 3. 모델서버로 부터 CSV url 반환
     * 4. CSV 파일 다운로드 및 처리 후 QnA 저장
     * @param requestDto
     * @return
     */
    public Mono<UploadReturnDto> upload(UploadRequestDto requestDto) {

        // Reactor 환경에서도 트랜잭션 컨텍스트를 유지
        // 비동기 스레드에서 트랜잭션 유지
        final TransactionalOperator txOperator = TransactionalOperator.create(transactionManager);

        return downloadAndValidatePdf(requestDto)  // 비동기적으로 PDF 다운로드 및 검증
                .flatMap(documentEntity ->  // 반환된 DocumentEntity를 사용
                        getCsvUrl(documentEntity.getUrl())  // 비동기적으로 CSV URL을 가져옴
                                .flatMap(csvUrl ->
                                        Mono.defer(() -> processCsvAndSaveQnA(csvUrl, documentEntity))  // CSV 처리 후 QnA 저장
                                )
                )
                .as(txOperator::transactional);  // 트랜잭션 컨텍스트 적용
    }

    /**
     * PDF 파일 다운로드 및 유효성 검증
     * 파일 다운로드는 webClient 사용하여 비동기 처리
     * Mono.fromCallable, subscribeOn(Schedulers.boundedElastic()) : 차단 i/o 작업을 별도의 스레드 풀에서 처리하여 비동기 환경에서 기아 상태를 방지
     * 차단 i/o 작업 : 파일 처리, JPA 저장
     * @param requestDto
     * @return
     */
    private Mono<DocumentEntity> downloadAndValidatePdf(UploadRequestDto requestDto) {
        // PDF 확장자 검증
        try {
            final String fileExtension = getFileExtension(requestDto.getDocumentUrl());
            if (!fileExtension.equalsIgnoreCase("pdf")) {
                throw new AppException(INVALID_EXTENSION);
            }
        } catch (AppException e) {
            return Mono.error(new AppException(INVALID_EXTENSION));
        }

        // WebClient를 사용하여 PDF 파일 다운로드 (비동기 처리)
        final WebClient webClient = webClientBuilder.baseUrl(requestDto.getDocumentUrl()).build();

        return webClient.get()
                .retrieve()
                .bodyToMono(byte[].class)
                .flatMap(pdfData -> {
                    // PDF 처리와 DB 저장은 차단 작업이므로 fromCallable을 사용하여 별도의 스레드에서 처리
                    return Mono.fromCallable(() -> {
                        try {
                            // byte[]로부터 PDF 문서 로드
                            final PDDocument document = PDDocument.load(pdfData);

                            // PDF 유효성 검증
                            if (!validateDocument(document)) {
                                log.info("PDF 길이가 너무 짧습니다.");
                                throw new AppException(ResultCode.FAILURE, "PDF 길이가 너무 짧습니다.");
                            }

                            // PDF 유효성 통과 후 DB에 저장
                            return documentService.saveDocumentFromUpload(requestDto);  // 비동기적으로 DB 저장
                        } catch (IOException e) {
                            log.error("PDF 처리 과정에서 오류가 발생했습니다.");
                            throw new AppException(ResultCode.FAILURE, "PDF 처리 과정에서 오류가 발생했습니다.");
                        }
                    }).subscribeOn(Schedulers.boundedElastic());  // 별도의 스레드에서 차단 작업 처리
                });
    }

    @CircuitBreaker(name = NAME, fallbackMethod = FALLBACKMETHOD)
    private Mono<String> getCsvUrl(String documentUrl) {
        final GetCsvRequestDto getCsvRequestDto = GetCsvRequestDto.builder()
                .s3_url(documentUrl)
                .build();

        final WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();
        return webClient.post()
                .uri("/test-s3-url")
                .bodyValue(getCsvRequestDto)
                .retrieve()
                .bodyToMono(GetCsvReturnDto.class)
                .flatMap(response -> Mono.just(response.getCsv_url()));
    }

    /**
     * circuit breaker fallback 메서드
     * @param documentUrl
     * @param t
     * @return
     */
    private Mono<String> getCsvUrlFallback(String documentUrl, Throwable t) {
        log.error("Fallback : " + t.getMessage());
        return Mono.just("Fallback"); // fallback data
    }

    /**
     * 블로킹 작업(파일 처리, JPA 저장) 처리
     * 블로킹 I/O는 boundedElastic 스레드 풀에서 실행
     * @param csvUrl
     * @param documentEntity
     * @return
     */
    private Mono<UploadReturnDto> processCsvAndSaveQnA(String csvUrl, DocumentEntity documentEntity) {
        if (csvUrl.equals("Fallback")) {
            return Mono.error(new AppException(ResultCode.FAILURE, "모델 서버 응답 에러, 잠시 후 다시 시도해주세요."));
        }

        final WebClient webClient = webClientBuilder.baseUrl(csvUrl).build();

        return webClient.get()  // WebClient를 사용하여 CSV 파일 다운로드
                .retrieve()
                .bodyToMono(String.class)  // CSV 파일을 문자열로 받아옴
                .flatMap(csvContent ->
                        Mono.fromCallable(() -> {
                                    int questionCount = 0;

                                    try {
                                        // CSV 문자열을 BufferedReader로 변환
                                        final BufferedReader reader = new BufferedReader(new StringReader(csvContent));
                                        final CSVReader csvReader = new CSVReader(reader);

                                        // 헤더 행 건너뛰기
                                        csvReader.readNext();

                                        String[] nextLine;
                                        while ((nextLine = csvReader.readNext()) != null) {
                                            String questionRead = nextLine[0];
                                            String answerRead = nextLine[1];

                                            log.info("question:{}", questionRead);
                                            log.info("answer:{}", answerRead);

                                            QuestionEntity qe = QuestionEntity.builder()
                                                    .documentEntity(documentEntity)
                                                    .content(questionRead)
                                                    .modelAnswer(answerRead)
                                                    .bestScore(0)
                                                    .questionIndex(++questionCount)
                                                    .build();

                                            questionService.saveQuestion(qe);  // 블로킹 JPA 호출
                                        }

                                        documentService.updateDocumentQuestionSize(documentEntity, questionCount);  // 블로킹 JPA 호출

                                        csvReader.close();
                                        reader.close();

                                    } catch (IOException | CsvValidationException e) {
                                        throw new AppException(ResultCode.FAILURE, "CSV 처리 과정에서 오류가 발생했습니다.");
                                    }

                                    // 업로드 결과 반환
                                    return UploadReturnDto.builder()
                                            .id(documentEntity.getObjectId())
                                            .title(documentEntity.getTitle())
                                            .questionSize(questionCount)
                                            .build();
                                })
                                .onErrorResume(e -> {
                                    log.error("csv 처리 중 에러 발생: {}", e.getMessage());
                                    return Mono.error(e);
                                })
                                .subscribeOn(Schedulers.boundedElastic())  // 별도의 스레드에서 JPA 작업 처리
                );
    }

    // 파일 확장자 추출 메서드
    private String getFileExtension(String filePath) {
        final int lastIndex = filePath.lastIndexOf('.');
        if (lastIndex == -1 || lastIndex == filePath.length() - 1) {
            throw new AppException(INVALID_EXTENSION);
        }
        return filePath.substring(lastIndex + 1);
    }

    private boolean validateDocument(PDDocument document) throws IOException{
        if (document.getNumberOfPages() < 1) {
            log.info("PDF 파일에 페이지가 없습니다.");
            return false;
        }

        // PDF 문서에서 텍스트 추출
        final PDFTextStripper pdfStripper = new PDFTextStripper();
        final String text = pdfStripper.getText(document);

        // PDF 파일에서 공백을 포함하여 글자 수 계산
        final int charCount = text.length();

        // PDF 파일의 글자 수가 2000자 이하인지 확인하여 응답 반환
        return charCount > 2000;
    }
}
