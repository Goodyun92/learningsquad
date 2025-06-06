package com.example.learningsquad.answer.controller;

import com.example.learningsquad.answer.model.CreateAnswerRequestDto;
import com.example.learningsquad.answer.service.AnswerService;
import com.example.learningsquad.global.common.controller.Controller;
import com.example.learningsquad.global.common.model.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


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
    public ApiResponse getAllUserAnswers(@PathVariable Long questionId, @RequestParam(required = false, defaultValue = "RECENT") String sort) {
        return success(answerService.getAnswers(questionId, sort));
    }

    /**
     * questionId에 해당하는 사용자 답변 생성
     * @param requestDto (Long questionId, Integer SimilarityScore, String answer)
     * @return
     */
    @PostMapping()
    public ApiResponse createAnswer(@RequestBody CreateAnswerRequestDto requestDto) {
        return success(answerService.createAnswer(requestDto));
    }

}
