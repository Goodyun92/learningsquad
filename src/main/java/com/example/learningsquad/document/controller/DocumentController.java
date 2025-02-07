package com.example.learningsquad.document.controller;

import com.example.learningsquad.document.service.DocumentService;
import com.example.learningsquad.global.common.controller.Controller;
import com.example.learningsquad.global.common.model.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/document")
public class DocumentController extends Controller {

    private final DocumentService documentService;

    /**
     * 사용자의 전체 문서 목록 조회
     * @param includeQuestions 문제 정보 포함 여부
     * @return
     */
    @GetMapping
    public ApiResponse getDocuments(@RequestParam(required = false, defaultValue = "false") Boolean includeQuestions) {
        return success(documentService.getDocumentList(includeQuestions));
    }

    /**
     * 문서 상세 조회
     * 문서의 문제 정보 포함
     * @param id 문서 ID
     * @return
     */
    @GetMapping("{id}")
    public ApiResponse getDocument(@PathVariable Long id) {
        return success(documentService.getDocument(id));
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return success();
    }
}
