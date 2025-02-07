package com.example.learningsquad.security.jwt.util;

import com.example.learningsquad.auth.domain.RefreshToken;
import com.example.learningsquad.global.exception.AppException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.example.learningsquad.global.exception.ExceptionCode.EXPIRED_TOKEN;
import static com.example.learningsquad.global.exception.ExceptionCode.INVALID_TOKEN;

@Component
@Data
public class JwtTokenUtil {
    // Token
    public static final Long ACCESS_TOKEN_EXPIRE_TIME = 1000L * 60 * 30; // ACCESS 토근 만료 시간: 30분
    public static final Long REFRESH_TOKEN_EXPIRE_TIME = 1000L * 60 * 60 * 24 * 14; // Refresh 토큰 만료 시간 : 2주
    public static final Long REISSUE_EXPIRE_TIME = 1000L * 60 * 60 * 3; // Reissue 만료 시간 : 3시간

    // header
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    private static final String KEY_ROLES = "roles";

    @Value("${spring.jwt.secret}")
    private String secretKey;

    public String generateToken(String signInId, Long accountId, Collection<? extends GrantedAuthority> athorities, long expirationTime) {
        Claims claims = Jwts.claims().setSubject(signInId);
        claims.put("accountId", accountId);
        claims.put("auth", athorities.stream().map(s -> new SimpleGrantedAuthority(s.getAuthority())).filter(Objects::nonNull).collect(Collectors.toList()));
        // claims.put(KEY_ROLES, roles);

        var now = new Date();
        var expireDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS512, secretKey).compact();
    }

    public long getRemainTime(String token) {
        Date expiration = parseClaims(token).getExpiration();
        Date now = new Date();
        return expiration.getTime() - now.getTime();
    }

    public String parseToken(String token) {
        return parseClaims(token).getSubject();
    }

    public String resolveToken(String token) {
        if (!ObjectUtils.isEmpty(token) && token.startsWith(JwtTokenUtil.TOKEN_PREFIX)) {
            return token.substring(JwtTokenUtil.TOKEN_PREFIX.length());
        }
        return null;
    }

    public Claims parseClaims(String token) {
        try {
            return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            throw new AppException(EXPIRED_TOKEN);  // 토큰 만료
        } catch (Exception e) {
            throw new AppException(INVALID_TOKEN);  // 토큰 검증 실패
        }
    }

    public void setRefreshTokenAtCookie(RefreshToken refreshToken) {
        Cookie cookie = new Cookie("RefreshToken", refreshToken.getRefreshToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(refreshToken.getExpiration().intValue());
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes()).getResponse();
        response.addCookie(cookie);
    }
}