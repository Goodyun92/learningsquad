package com.example.learningsquad.global.exception;

import com.example.learningsquad.global.common.model.ResultCode;
import lombok.Getter;

@Getter
public class AppException extends RuntimeException {
    private ExceptionCode exceptionCode;
    private ResultCode resultCode;
    private String resultMessage;

    // exceptionCode로 생성
    public AppException(ExceptionCode exceptionCode) {
        this.exceptionCode = exceptionCode;
    }

    // 직접 메시지 생성
    public AppException(ResultCode resultCode, String resultMessage) {
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
    }
}
