package com.example.learningsquad.upload.service;

import com.example.learningsquad.document.domain.DocumentEntity;
import com.example.learningsquad.document.service.DocumentService;
import com.example.learningsquad.global.common.model.ResultCode;
import com.example.learningsquad.global.exception.AppException;
import com.example.learningsquad.question.domain.QuestionEntity;
import com.example.learningsquad.question.service.QuestionService;
import com.example.learningsquad.upload.model.UploadRequestDto;
import com.example.learningsquad.upload.model.UploadReturnDto;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadService {

    private final DocumentService documentService;
    private final QuestionService questionService;
    private final RestTemplate restTemplate;

    /**
     * CSV 파일 다운로드 및 처리 후 QnA 저장
     * @param requestDto (String documentUrl, String, csvUrl, String title)
     * @return
     */
    public UploadReturnDto upload(UploadRequestDto requestDto) {

        final DocumentEntity de = documentService.saveDocumentFromUpload(requestDto);

        return processCsvAndSaveQnA(requestDto.getCsvUrl(), de);
    }

    /**
     * @param csvUrl
     * @param documentEntity
     * @return
     */
    public UploadReturnDto processCsvAndSaveQnA(String csvUrl, DocumentEntity documentEntity) {

        int questionCount = 0;

        try {
            // 1. RestTemplate로 CSV 파일 다운로드
            final ResponseEntity<String> response = restTemplate.getForEntity(csvUrl, String.class);
            final String csvContent = response.getBody();

            // 2. CSV 문자열을 BufferedReader로 변환
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

                questionService.saveQuestion(qe);
            }

            documentService.updateDocumentQuestionSize(documentEntity, questionCount);

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
    }

}
