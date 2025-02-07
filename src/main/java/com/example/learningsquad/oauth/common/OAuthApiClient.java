package com.example.learningsquad.oauth.common;

public interface OAuthApiClient {

    /**
     * oAuthProvider(): Client 의 타입 반환
     * requestAccessToken: Authorization Code 를 기반으로 인증 API 를 요청해서 Access Token 을 획득
     * requestOauthInfo: Access Token 을 기반으로 Email, Nickname 이 포함된 프로필 정보를 획득
     */

    OAuthProvider oAuthProvider();
    String requestAccessToken(OAuthLoginParams params);
    OAuthInfoResponse requestOauthInfo(String accessToken);
}
