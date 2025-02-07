package com.example.learningsquad.security.filter;

import com.example.learningsquad.security.jwt.util.JwtTokenUtil;
import com.example.learningsquad.security.jwt.util.TokenManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final TokenManager tokenManager;

    private final JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = this.resolveTokenFromRequest(request);
        String path = request.getRequestURI();

        if (!path.startsWith("/api/auth/") &&
                token != null && this.tokenManager.validateToken(token)) {
            Authentication auth = this.tokenManager.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
            log.debug(String.format("[%s] -> %s", jwtTokenUtil.parseToken(token), request.getRequestURI()));
        }
        filterChain.doFilter(request, response);
    }

    private String resolveTokenFromRequest(HttpServletRequest request) {
        String token = request.getHeader(JwtTokenUtil.TOKEN_HEADER);
        return jwtTokenUtil.resolveToken(token);
    }
}