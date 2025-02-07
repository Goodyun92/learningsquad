package com.example.learningsquad.question.service;

import com.example.learningsquad.account.domain.AccountEntity;
import com.example.learningsquad.account.service.AccountService;
import com.example.learningsquad.global.exception.AppException;
import com.example.learningsquad.global.exception.ExceptionCode;
import com.example.learningsquad.question.domain.QuestionEntity;
import com.example.learningsquad.question.model.GetQuestionResponseDto;
import com.example.learningsquad.question.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final AccountService accountService;

    public void saveQuestion(QuestionEntity questionEntity) {
        questionRepository.save(questionEntity);
    }

    public List<QuestionEntity> getQuestionsByDocumentId(Long documentId) {
        return questionRepository.findByDocumentEntity_ObjectId(documentId);
    }

    public GetQuestionResponseDto getQuestion(Long id) {
        final String signInId = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        final Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        final AccountEntity accountEntity = accountService.findAccountBySignInId(signInId);

        final QuestionEntity questionEntity = questionRepository.findById(id)
                .orElseThrow(() -> new AppException(ExceptionCode.NON_EXISTENT_QUESTION));

        if (!questionEntity.getDocumentEntity().getAccountEntity().getObjectId().equals(accountEntity.getObjectId())
                && authorities.stream().noneMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AppException(ExceptionCode.UNAUTHORIZED_QUESTION);
        }

        return GetQuestionResponseDto.of(questionEntity);
    }

    public String getModelAnswer(Long questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new AppException(ExceptionCode.NON_EXISTENT_QUESTION))
                .getModelAnswer();
    }

    public QuestionEntity getQuestionEntity(Long questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new AppException(ExceptionCode.NON_EXISTENT_QUESTION));
    }
}
