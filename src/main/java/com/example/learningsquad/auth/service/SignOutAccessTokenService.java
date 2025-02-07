package com.example.learningsquad.auth.service;

import com.example.learningsquad.auth.domain.SignOutAccessToken;
import com.example.learningsquad.auth.repository.SignOutAccessTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignOutAccessTokenService {
    private final SignOutAccessTokenRepository signOutAccessTokenRepository;

    public void saveSignOutAccessToken(SignOutAccessToken signOutAccessToken) {
        signOutAccessTokenRepository.save(signOutAccessToken);
    }

    public boolean existsSignOutAccessTokenById(String token) {
        return signOutAccessTokenRepository.existsById(token);
    }
}
