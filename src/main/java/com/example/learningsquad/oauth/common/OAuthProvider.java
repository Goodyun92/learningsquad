package com.example.learningsquad.oauth.common;

import com.example.learningsquad.global.common.model.BaseEnum;

public enum OAuthProvider implements BaseEnum {
    KAKAO("KAKAO"),
    ;

    private final String value;

    OAuthProvider(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}
