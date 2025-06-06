package com.example.learningsquad.answer.model;

import lombok.Getter;

@Getter
public class CreateAnswerRequestDto {
    private Long questionId;
    private String answer;
    private Integer similarityScore;
}
