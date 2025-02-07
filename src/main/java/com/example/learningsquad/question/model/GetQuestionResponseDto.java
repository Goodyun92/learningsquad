package com.example.learningsquad.question.model;

import com.example.learningsquad.question.domain.QuestionEntity;
import lombok.Builder;

@Builder
public class GetQuestionResponseDto {
    private Long objectId;
    private Long documentId;
    private String content;
    private Integer questionIndex;
    private String modelAnswer;
    private String bestAnswer;
    private Integer bestScore;

    public static GetQuestionResponseDto of(QuestionEntity questionEntity) {
        return GetQuestionResponseDto.builder()
                .objectId(questionEntity.getObjectId())
                .documentId(questionEntity.getDocumentEntity().getObjectId())
                .content(questionEntity.getContent())
                .questionIndex(questionEntity.getQuestionIndex())
                .modelAnswer(questionEntity.getModelAnswer())
                .bestAnswer(questionEntity.getBestAnswer())
                .bestScore(questionEntity.getBestScore())
                .build();
    }
}
