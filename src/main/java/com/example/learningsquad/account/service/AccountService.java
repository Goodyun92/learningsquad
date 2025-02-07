package com.example.learningsquad.account.service;

import com.example.learningsquad.account.domain.AccountEntity;
import com.example.learningsquad.account.repository.AccountRepository;
import com.example.learningsquad.global.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.example.learningsquad.global.exception.ExceptionCode.NON_EXISTENT_SIGNINID;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountEntity findAccountBySignInId(String signInId) {
        return accountRepository.findBySignInId(signInId)
                .orElseThrow(() -> new AppException(NON_EXISTENT_SIGNINID));
    }

    public Optional<AccountEntity> findAccountBySignInIdOptional(String signInId) {
        return accountRepository.findBySignInId(signInId);
    }

    public Boolean existsBySignInId(String signInId) {
        return accountRepository.existsBySignInId(signInId);
    }

    public AccountEntity saveAccount(AccountEntity accountEntity) {
        return accountRepository.save(accountEntity);
    }

    public void updateLastSignInAt(AccountEntity accountEntity) {
        accountEntity.setLastSignInAt(LocalDateTime.now());
        accountRepository.save(accountEntity);
    }
}
