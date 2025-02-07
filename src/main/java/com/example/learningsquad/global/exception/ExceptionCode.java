package com.example.learningsquad.global.exception;

import com.example.learningsquad.global.common.model.ResultCode;
import lombok.Getter;

@Getter
public enum ExceptionCode {
    NON_EXISTENT_USER(ResultCode.FAILURE, "존재하지 않는 사용자"),
    NON_EXISTENT_SIGNINID(ResultCode.FAILURE, "존재하지 않는 아이디입니다."),
    INVALID_PASSWORD(ResultCode.FAILURE, "비밀번호가 일치하지 않습니다."),
    INVALID_TOKEN(ResultCode.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(ResultCode.UNAUTHORIZED, "이미 만료된 토큰입니다."),
    INVALID_REFRESH_TOKEN(ResultCode.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    EXPIRED_REFRESH_TOKEN(ResultCode.UNAUTHORIZED, "만료된 리프레시 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(ResultCode.UNAUTHORIZED, "리프레시 토큰이 존재하지 않습니다."),
    USERDETAILS_NOT_FOUND(ResultCode.FAILURE, "사용자 인증 정보를 찾을 수 없습니다."),
    MISMATCH_SIGNINID_TOKEN(ResultCode.UNAUTHORIZED, "토큰의 signInId와 일치하지 않습니다."),
    ALREADY_LOGOUT_USER(ResultCode.UNAUTHORIZED, "이미 로그아웃된 사용자입니다."),
    ALREADY_EXIST_SIGNINID(ResultCode.FAILURE, "이미 존재하는 아이디입니다."),
    NOT_AUTHORIZED_ACCESS(ResultCode.UNAUTHORIZED, "접근 권한이 없습니다."),
    AUTHENTICATION_FAILED(ResultCode.FAILURE, "사용자 인증과정에서 실패했습니다."),
    INVALID_EXTENSION(ResultCode.FAILURE, "지원하지 않는 확장자입니다."),
    NON_EXISTENT_DOCUMENT(ResultCode.FAILURE, "존재하지 않는 문서입니다."),
    UNAUTHORIZED_DOCUMENT(ResultCode.UNAUTHORIZED, "해당 문서에 대한 권한이 없습니다."),
    NON_EXISTENT_QUESTION(ResultCode.FAILURE, "존재하지 않는 문제입니다."),
    UNAUTHORIZED_QUESTION(ResultCode.UNAUTHORIZED, "해당 문제에 대한 권한이 없습니다."),
    ;

    private final ResultCode resultCode;
    private final String resultMessage;

    ExceptionCode(ResultCode resultCode, String resultMessage) {
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
    }
}
