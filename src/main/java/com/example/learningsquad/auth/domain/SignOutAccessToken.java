package com.example.learningsquad.auth.domain;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Data
@Builder
@RedisHash("sign_out_access_token")
@NoArgsConstructor
@AllArgsConstructor
public class SignOutAccessToken {
    @Id
    private String id;

    private String signInId;

    @TimeToLive
    private Long expiration;

    public static SignOutAccessToken from(String signInId, String accessToken, Long expirationTime) {
        return SignOutAccessToken.builder()
                .id(accessToken)
                .signInId(signInId)
                .expiration(expirationTime / 1000)
                .build();
    }
}