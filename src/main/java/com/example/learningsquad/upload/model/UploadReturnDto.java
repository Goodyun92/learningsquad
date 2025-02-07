package com.example.learningsquad.upload.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UploadReturnDto {
    Long id;

    String title;

    Integer questionSize;
}
