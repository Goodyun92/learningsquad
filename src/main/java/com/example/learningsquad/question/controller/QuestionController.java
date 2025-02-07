package com.example.learningsquad.question.controller;

import com.example.learningsquad.global.common.controller.Controller;
import com.example.learningsquad.global.common.model.ApiResponse;
import com.example.learningsquad.question.model.GetQuestionResponseDto;
import com.example.learningsquad.question.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/question")
@RequiredArgsConstructor
public class QuestionController extends Controller {
    private final QuestionService questionService;

    @GetMapping("{id}")
    public ApiResponse getQuestion(@PathVariable Long id) {
        return success(questionService.getQuestion(id));
    }
}
