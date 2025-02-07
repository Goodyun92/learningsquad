package com.example.learningsquad.security.jwt.util;

import com.example.learningsquad.auth.service.SignOutAccessTokenService;
import com.example.learningsquad.global.exception.AppException;
import com.example.learningsquad.security.service.CustomUserDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;

import static com.example.learningsquad.global.exception.ExceptionCode.*;

@Component
@RequiredArgsConstructor
public class TokenManager {
    private final CustomUserDetailService customUserDetailService;
    private final SignOutAccessTokenService signOutAccessTokenService;

    private final JwtTokenUtil jwtTokenUtil;

    public Authentication getAuthentication(String jwt) {
        String tokenSignInId = this.jwtTokenUtil.parseToken(jwt);

        if (tokenSignInId == null) {
            throw new AppException(MISMATCH_SIGNINID_TOKEN);
        }

        UserDetails userDetails = customUserDetailService.loadUserByUsername(tokenSignInId);

        if (!userDetails.getUsername().equals(tokenSignInId)) {
            throw new AppException(MISMATCH_SIGNINID_TOKEN);
        }
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public boolean validateToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new AppException(INVALID_TOKEN);  // 토큰 검증 실패
        } else if (this.jwtTokenUtil.parseClaims(token).getExpiration().before(new Date())) {
            throw new AppException(EXPIRED_TOKEN);  // 토큰 만료
        } else if (checkLogout(token)) {
            throw new AppException(ALREADY_LOGOUT_USER);
        }
        return true;
    }

    private boolean checkLogout(String token) {
        return signOutAccessTokenService.existsSignOutAccessTokenById(token);
    }

}