package com.example.learningsquad.answer.model;

import com.example.learningsquad.answer.domain.AnswerEntity;
import lombok.Builder;

@Builder
public class GetAnswerResponseDto {
    private Long objectId;
    private Long questionId;
    private String answer;
    private Integer score;

    public static GetAnswerResponseDto of(AnswerEntity answerEntity) {
        return GetAnswerResponseDto.builder()
                .objectId(answerEntity.getObjectId())
                .questionId(answerEntity.getQuestionEntity().getObjectId())
                .answer(answerEntity.getAnswer())
                .score(answerEntity.getScore())
                .build();
    }
}
