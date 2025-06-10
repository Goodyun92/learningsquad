package com.example.learningsquad.document.service;

import com.example.learningsquad.account.domain.AccountEntity;
import com.example.learningsquad.account.service.AccountService;
import com.example.learningsquad.document.domain.DocumentEntity;
import com.example.learningsquad.document.model.GetDocumentResponseDto;
import com.example.learningsquad.document.repository.DocumentRepository;
import com.example.learningsquad.global.exception.AppException;
import com.example.learningsquad.global.exception.ExceptionCode;
import com.example.learningsquad.question.domain.QuestionEntity;
import com.example.learningsquad.question.service.QuestionService;
import com.example.learningsquad.upload.model.UploadRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class DocumentService {

    private final AccountService accountService;
    private final QuestionService questionService;

    private final DocumentRepository documentRepository;

    @Transactional
    public DocumentEntity saveDocumentFromUpload(UploadRequestDto requestDto) {
        final String signInId = SecurityContextHolder.getContext().getAuthentication().getName();

        final AccountEntity accountEntity = accountService.findAccountBySignInId(signInId);

        final DocumentEntity documentEntity = DocumentEntity.builder()
                .url(requestDto.getDocumentUrl())
                .title(requestDto.getTitle())
                .accountEntity(accountEntity)
                .build();

        return documentRepository.save(documentEntity);
    }

    public void updateDocumentQuestionSize(DocumentEntity documentEntity, Integer questionSize) {
        documentEntity.setQuestionSize(questionSize);
        documentRepository.save(documentEntity);
    }

    public GetDocumentResponseDto getDocument(Long id) {
        final String signInId = SecurityContextHolder.getContext().getAuthentication().getName();
        final Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        final AccountEntity accountEntity = accountService.findAccountBySignInId(signInId);

        final DocumentEntity documentEntity = documentRepository.findById(id)
                .orElseThrow(() -> new AppException(ExceptionCode.NON_EXISTENT_DOCUMENT));

        if (!documentEntity.getAccountEntity().getObjectId().equals(accountEntity.getObjectId())
                && authorities.stream().noneMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AppException(ExceptionCode.UNAUTHORIZED_DOCUMENT);
        }

        final List<QuestionEntity> questions = questionService.getQuestionsByDocumentId(id);

        return GetDocumentResponseDto.of(documentEntity, questions);
    }

    public List<GetDocumentResponseDto> getDocumentList(Boolean includeQuestions) {
        final String signInId = SecurityContextHolder.getContext().getAuthentication().getName();

        final AccountEntity accountEntity = accountService.findAccountBySignInId(signInId);

        final List<DocumentEntity> documentEntities = documentRepository.findByAccountEntity_ObjectId(accountEntity.getObjectId())
                .orElseThrow(() -> new AppException(ExceptionCode.NON_EXISTENT_DOCUMENT));

        if (includeQuestions) {
            return documentEntities.stream()
                    .map(documentEntity -> {
                        List<QuestionEntity> questions = questionService.getQuestionsByDocumentId(documentEntity.getObjectId());
                        return GetDocumentResponseDto.of(documentEntity, questions);
                    })
                    .toList();
        }

        return documentEntities.stream()
                .map(GetDocumentResponseDto::of)
                .toList();
    }

    public void deleteDocument(Long id) {
        final String signInId = SecurityContextHolder.getContext().getAuthentication().getName();
        final Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();

        final AccountEntity accountEntity = accountService.findAccountBySignInId(signInId);

        final DocumentEntity documentEntity = documentRepository.findById(id)
                .orElseThrow(() -> new AppException(ExceptionCode.NON_EXISTENT_DOCUMENT));

        if (!documentEntity.getAccountEntity().getObjectId().equals(accountEntity.getObjectId())
                && authorities.stream().noneMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AppException(ExceptionCode.UNAUTHORIZED_DOCUMENT);
        }

        documentRepository.deleteById(id);
    }
}
