package com.example.learningsquad.global.exception;

import com.example.learningsquad.global.common.model.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestControllerAdvice
public class ExceptionManager {
    // exceptionHandelr를 통해 예외 전역 관리
    @ExceptionHandler(AppException.class)
    public ApiResponse handleAppException(AppException exception) {
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();

        if (exception.getExceptionCode() != null) {
            // ExceptionCode를 사용하는 경우
            final ExceptionCode exceptionCode = exception.getExceptionCode();
            response.setStatus(exceptionCode.getResultCode().getHttpStatus().value());
            return new ApiResponse(exceptionCode.getResultCode(), exceptionCode.getResultMessage(), null);
        } else {
            // 직접 코드 ,메시지 넣은 경우
            response.setStatus(exception.getResultCode().getHttpStatus().value());
            return new ApiResponse(exception.getResultCode(), exception.getResultMessage(), null);
        }
    }
}
