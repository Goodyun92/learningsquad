package com.example.learningsquad.auth.domain;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@RedisHash(value = "refresh_token")
public class RefreshToken {

    @Id
    private String id;

    @Indexed
    private String refreshToken;

    private String role;

    @TimeToLive
    private Long expiration;

    public RefreshToken update(String token, long ttl) {
        this.refreshToken = token;
        this.expiration = ttl;
        return this;
    }

    public static RefreshToken from(String signInId, String refreshToken, Long expirationTime) {
        return RefreshToken.builder()
                .id(signInId)
                .refreshToken(refreshToken)
                .expiration(expirationTime / 1000)
                .build();
    }

}