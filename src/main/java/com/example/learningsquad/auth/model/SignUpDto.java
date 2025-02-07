package com.example.learningsquad.auth.model;

import com.example.learningsquad.account.domain.AccountEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpDto {
    private String signInId;
    private String password;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String username;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String email;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String profileImage;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean emailNotification;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long AccountId;

    public static SignUpDto of(AccountEntity accountEntity) {
        SignUpDto signUpDto = new SignUpDto();
        signUpDto.signInId = accountEntity.getSignInId();
        signUpDto.password = accountEntity.getPassword();
        signUpDto.username = accountEntity.getUserName();
        signUpDto.email = accountEntity.getEmail();
        signUpDto.profileImage = accountEntity.getProfileImage();
        signUpDto.emailNotification = accountEntity.getEmailNotification();
        signUpDto.AccountId = accountEntity.getObjectId();
        return signUpDto;
    }
}