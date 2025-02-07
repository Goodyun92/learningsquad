package com.example.learningsquad.auth.service;

import com.example.learningsquad.auth.domain.RefreshToken;
import com.example.learningsquad.auth.repository.RefreshTokenRepository;
import com.example.learningsquad.global.exception.AppException;
import com.example.learningsquad.security.jwt.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static com.example.learningsquad.global.exception.ExceptionCode.REFRESH_TOKEN_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenUtil jwtTokenUtil;

    public RefreshToken saveRefreshToken(String signInId, Long accountId, Collection<? extends GrantedAuthority> athorities, long expirationTime) {
        return refreshTokenRepository.save(RefreshToken.from(
                signInId, jwtTokenUtil.generateToken(signInId, accountId, athorities, expirationTime), expirationTime)
        );
    }

    public RefreshToken findRefreshTokenById(String username) {
        return refreshTokenRepository.findById(username).orElseThrow(() -> new AppException(REFRESH_TOKEN_NOT_FOUND));
    }

    public void deleteRefreshTokenById(String signInId) {
        refreshTokenRepository.deleteById(signInId);
    }
}
