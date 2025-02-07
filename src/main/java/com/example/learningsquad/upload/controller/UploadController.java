package com.example.learningsquad.upload.controller;

import com.example.learningsquad.global.common.controller.Controller;
import com.example.learningsquad.global.common.model.ApiResponse;
import com.example.learningsquad.upload.model.UploadRequestDto;
import com.example.learningsquad.upload.model.UploadReturnDto;
import com.example.learningsquad.upload.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/upload")
public class UploadController extends Controller {

    private final WebClient.Builder webClientBuilder;
    private final UploadService uploadService;

    @PostMapping()
    public Mono<ApiResponse<UploadReturnDto>> upload(@RequestBody UploadRequestDto requestDto) {
        return uploadService.upload(requestDto)
                .flatMap(this::successMono)
                .onErrorResume(e -> Mono.just(failure("Upload Failed : " + e.getMessage())));
    }

}
