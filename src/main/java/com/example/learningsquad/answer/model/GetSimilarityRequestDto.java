package com.example.learningsquad.answer.model;

import lombok.Builder;

@Builder
public class GetSimilarityRequestDto {
    String sentence1;
    String sentence2;
}
