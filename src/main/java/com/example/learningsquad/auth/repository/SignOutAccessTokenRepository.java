package com.example.learningsquad.auth.repository;

import com.example.learningsquad.auth.domain.SignOutAccessToken;
import org.springframework.data.repository.CrudRepository;

public interface SignOutAccessTokenRepository extends CrudRepository<SignOutAccessToken, String> {
}
