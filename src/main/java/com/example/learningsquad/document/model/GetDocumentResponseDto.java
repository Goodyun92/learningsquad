package com.example.learningsquad.document.model;

import com.example.learningsquad.document.domain.DocumentEntity;
import com.example.learningsquad.question.domain.QuestionEntity;
import com.example.learningsquad.question.model.GetQuestionResponseDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.util.List;

@Builder
public class GetDocumentResponseDto {
    private Long objectId;
    private String url;
    private String title;
    private Integer questionSize;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<GetQuestionResponseDto> questions;

    public static GetDocumentResponseDto of(DocumentEntity documentEntity) {
        return GetDocumentResponseDto.builder()
                .objectId(documentEntity.getObjectId())
                .url(documentEntity.getUrl())
                .title(documentEntity.getTitle())
                .questionSize(documentEntity.getQuestionSize())
                .build();
    }

    public static GetDocumentResponseDto of(DocumentEntity documentEntity, List<QuestionEntity> questionEntities) {
        return GetDocumentResponseDto.builder()
                .objectId(documentEntity.getObjectId())
                .url(documentEntity.getUrl())
                .title(documentEntity.getTitle())
                .questionSize(documentEntity.getQuestionSize())
                .questions(questionEntities.stream()
                        .map(GetQuestionResponseDto::of)
                        .toList())
                .build();
    }
}
