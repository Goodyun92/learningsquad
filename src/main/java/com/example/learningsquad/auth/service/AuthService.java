package com.example.learningsquad.auth.service;

import com.example.learningsquad.account.domain.AccountEntity;
import com.example.learningsquad.account.service.AccountService;
import com.example.learningsquad.auth.domain.RefreshToken;
import com.example.learningsquad.auth.domain.SignOutAccessToken;
import com.example.learningsquad.auth.model.AccountAuth;
import com.example.learningsquad.auth.model.SignInDto;
import com.example.learningsquad.auth.model.SignUpDto;
import com.example.learningsquad.global.common.model.ResultCode;
import com.example.learningsquad.global.exception.AppException;
import com.example.learningsquad.oauth.common.OAuthInfoResponse;
import com.example.learningsquad.oauth.common.OAuthLoginParams;
import com.example.learningsquad.oauth.common.RequestOAuthInfoService;
import com.example.learningsquad.security.jwt.util.JwtTokenUtil;
import com.example.learningsquad.security.jwt.util.TokenManager;
import com.example.learningsquad.security.service.CustomAuthenticationProvider;
import com.example.learningsquad.security.service.CustomUserDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.example.learningsquad.global.exception.ExceptionCode.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AccountService accountService;
    private final CustomAuthenticationProvider authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final RefreshTokenService refreshTokenService;
    private final SignOutAccessTokenService signOutAccessTokenService;
    private final CustomUserDetailService customUserDetailService;
    private final TokenManager tokenManager;
    private final PasswordEncoder passwordEncoder;
    private final RequestOAuthInfoService requestOAuthInfoService;

    @Transactional
    public SignUpDto signUp(SignUpDto signUpDto) {

        // signupDto 필수값 null 체크 필요

        final String signInId = signUpDto.getSignInId();

        if (accountService.existsBySignInId(signInId)) {
            throw new AppException(ALREADY_EXIST_SIGNINID);
        }

        signUpDto.setPassword(passwordEncoder.encode(signUpDto.getPassword()));

        final AccountEntity savedAccountEntity = accountService.saveAccount(AccountEntity.of(signUpDto));

        return SignUpDto.of(savedAccountEntity);
    }

    public AccountAuth signInWithSignInDto(SignInDto signInDto) {

        Authentication authentication;

        try {
            final UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(signInDto.getSignInId(),
                    signInDto.getPassword());
            authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        } catch (AuthenticationException e) {
            throw new AppException(AUTHENTICATION_FAILED);
        }

        final AccountEntity accountEntity = accountService.findAccountBySignInId(signInDto.getSignInId());

        return signIn(accountEntity, authentication);
    }

    @Transactional
    public AccountAuth signInWithOAuth(OAuthLoginParams params){
        final OAuthInfoResponse oAuthInfoResponse = requestOAuthInfoService.request(params);

        AccountEntity accountEntity = accountService.findAccountBySignInIdOptional(oAuthInfoResponse.getEmail())
                .orElseGet(() -> {
                    String nickName = oAuthInfoResponse.getNickname() + "_" + oAuthInfoResponse.getOAuthProvider().toString();

                    SignUpDto signUpRequest = SignUpDto.builder()
                            .signInId(oAuthInfoResponse.getEmail())
                            .password(nickName)
                            .email(oAuthInfoResponse.getEmail())
                            .build();
                    SignUpDto signUpResponse = signUp(signUpRequest);
                    return accountService.findAccountBySignInId(signUpResponse.getSignInId());
                });

        // authority 추가
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        authorities.add(new SimpleGrantedAuthority(String.format("ROLE_OAUTH_%s", params.oAuthProvider().getValue())));

        // principal, authorities 채움
        final Authentication authentication = new UsernamePasswordAuthenticationToken(
                accountEntity.getSignInId(),
                null,
                authorities
        );

        return signIn(accountEntity, authentication);
    }

    private AccountAuth signIn(AccountEntity accountEntity, Authentication authentication) {
        final AccountAuth accountAuth = getAccountAuth(accountEntity, authentication);

        final String accessToken = jwtTokenUtil.generateToken(accountEntity.getSignInId(), accountEntity.getObjectId(), authentication.getAuthorities(), JwtTokenUtil.ACCESS_TOKEN_EXPIRE_TIME);
        final RefreshToken refreshToken = refreshTokenService.saveRefreshToken(accountEntity.getSignInId(), accountEntity.getObjectId(), authentication.getAuthorities(), JwtTokenUtil.REFRESH_TOKEN_EXPIRE_TIME);
        jwtTokenUtil.setRefreshTokenAtCookie(refreshToken);

        accountAuth.setAccessToken(accessToken);
        accountService.updateLastSignInAt(accountEntity);

        return accountAuth;
    }

    private AccountAuth getAccountAuth(AccountEntity accountEntity, Authentication authentication) {
        Object p = authentication.getPrincipal();
        if (p instanceof AccountAuth details) {
            details.setProfileImage(accountEntity.getProfileImage());
            return details;
        } else {
            throw new AppException(AUTHENTICATION_FAILED);
        }
    }

    //signOut
    @Transactional
    public void signOut(String rawAccessToken) {
        final String accessToken = jwtTokenUtil.resolveToken(rawAccessToken);
        if (accessToken == null) throw new AppException(INVALID_TOKEN);
        final String signInId = jwtTokenUtil.parseToken(accessToken);
        final long remainTime = jwtTokenUtil.getRemainTime(accessToken);
        if (remainTime <= 0) throw new AppException(EXPIRED_TOKEN);
        refreshTokenService.deleteRefreshTokenById(signInId);
        signOutAccessTokenService.saveSignOutAccessToken(SignOutAccessToken.from(signInId, accessToken, remainTime));
    }

    /**
     * RefreshToken을 이용하여 AccessToken 재발급
     * 만료된 accesstoken을 첨부해야함.
     * -> redis의 refreshtoken과 요청한 refreshtoken이 일치하는지 확인용
     * @param refreshToken
     * @param rawAccessToken
     * @return
     */
    @Transactional
    public AccountAuth reissue(String refreshToken, String rawAccessToken) {
        final String accessToken = jwtTokenUtil.resolveToken(rawAccessToken);
        if (accessToken == null) throw new AppException(ResultCode.FAILURE, "재발급을 위해 기존 토큰을 첨부해주세요.");

        final String curSignInId = jwtTokenUtil.parseToken(accessToken);

        final RefreshToken redisRefreshToken = refreshTokenService.findRefreshTokenById(curSignInId);

        if (refreshToken == null || !refreshToken.equals(redisRefreshToken.getRefreshToken())) {
            throw new AppException(ResultCode.FAILURE, "유효한 리프레시 토큰이 아닙니다.");
        }

        // 기존 accessToken signout 처리
        final long remainTime = jwtTokenUtil.getRemainTime(accessToken);
        signOutAccessTokenService.saveSignOutAccessToken(SignOutAccessToken.from(curSignInId, accessToken, remainTime));

        return createRefreshToken(refreshToken, curSignInId);
    }

    private AccountAuth createRefreshToken(String refreshToken, String signInId) {
        final UserDetails userDetails = customUserDetailService.loadUserByUsername(signInId);
        if (userDetails == null) throw new AppException(USERDETAILS_NOT_FOUND);

        final String accessToken = jwtTokenUtil.generateToken(signInId, ((AccountAuth) userDetails).getAccountId(), userDetails.getAuthorities(), JwtTokenUtil.ACCESS_TOKEN_EXPIRE_TIME);
        if (accessToken == null) throw new AppException(ResultCode.FAILURE, "토큰 생성 실패");

        if (lessThanReissueExpirationTimesLeft(refreshToken)) {
            // 기존 리프레시 토큰 삭제
            refreshTokenService.deleteRefreshTokenById(signInId);

            // 새로운 리프레시 토큰 생성
            final RefreshToken newRedisToken = refreshTokenService.saveRefreshToken(signInId, ((AccountAuth) userDetails).getAccountId(), userDetails.getAuthorities(), JwtTokenUtil.REFRESH_TOKEN_EXPIRE_TIME);
            if (newRedisToken == null) throw new AppException(ResultCode.FAILURE, "리프레시 토큰 생성 실패");
            jwtTokenUtil.setRefreshTokenAtCookie(newRedisToken);
        }

        SecurityContextHolder.clearContext();
        final Authentication authentication = tokenManager.getAuthentication(accessToken);
        // Authentication 객체를 SecurityContext에 저장, 인증 상태로 만듦
        if (tokenManager.validateToken(accessToken)) SecurityContextHolder.getContext().setAuthentication(authentication);

        final AccountEntity accountEntity = accountService.findAccountBySignInId(signInId);
        final AccountAuth accountAuth = getAccountAuth(accountEntity, authentication);
        accountAuth.setAccessToken(accessToken);
        return accountAuth;
    }

    private boolean lessThanReissueExpirationTimesLeft(String refreshToken) {
        return jwtTokenUtil.getRemainTime(refreshToken) < JwtTokenUtil.REISSUE_EXPIRE_TIME;
    }
}
