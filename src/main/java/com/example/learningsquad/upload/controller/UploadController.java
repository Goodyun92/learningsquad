package com.example.learningsquad.upload.controller;

import com.example.learningsquad.global.common.controller.Controller;
import com.example.learningsquad.global.common.model.ApiResponse;
import com.example.learningsquad.upload.model.UploadRequestDto;
import com.example.learningsquad.upload.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/upload")
public class UploadController extends Controller {
    private final UploadService uploadService;

    @PostMapping()
    public ApiResponse upload(@RequestBody UploadRequestDto requestDto) {
        return success(uploadService.upload(requestDto));
    }

}
