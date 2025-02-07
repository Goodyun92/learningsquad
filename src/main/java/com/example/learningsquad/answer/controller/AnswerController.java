package com.example.learningsquad.answer.controller;

import com.example.learningsquad.answer.model.CreateAnswerRequestDto;
import com.example.learningsquad.answer.model.GetAnswerResponseDto;
import com.example.learningsquad.answer.service.AnswerService;
import com.example.learningsquad.global.common.controller.Controller;
import com.example.learningsquad.global.common.model.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/answer")
public class AnswerController extends Controller {

    private final AnswerService answerService;

    /**
     * questionId에 해당하는 모든 사용자 답변 조회
     * @param questionId
     * @param sort 정렬 기준: RECENT(최신순), OLD(오래된순), SCORE_LOW(낮은 점수순), SCORE_HIGH(높은 점수순)
     * @return
     */
    @GetMapping("/all/{questionId}")
    public ApiResponse getAnswers(@PathVariable Long questionId, @RequestParam(required = false, defaultValue = "RECENT") String sort) {
        return success(answerService.getAnswers(questionId, sort));
    }

    /**
     * questionId에 해당하는 사용자 답변 생성
     * @param requestDto (Long questionId, String answer)
     * @return
     */
    @PostMapping()
    public Mono<ApiResponse<GetAnswerResponseDto>> createAnswer(@RequestBody CreateAnswerRequestDto requestDto) {
        return answerService.createAnswer(requestDto)
                .flatMap(this::successMono)
                .onErrorResume(e -> Mono.just(failure("Create Answer Failed : " + e.getMessage())));
    }

}
