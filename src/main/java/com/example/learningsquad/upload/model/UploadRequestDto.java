package com.example.learningsquad.upload.model;

import lombok.Data;

@Data
public class UploadRequestDto {

    String documentUrl;

    String csvUrl;

    String title;
}
