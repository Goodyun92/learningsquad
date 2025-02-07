package com.example.learningsquad.security.config;

import com.example.learningsquad.security.filter.JwtAuthenticationFilter;
import com.example.learningsquad.security.filter.JwtExceptionFilter;
import com.example.learningsquad.security.service.CustomAuthenticationProvider;
import com.example.learningsquad.security.service.JwtAccessDeniedHandler;
import com.example.learningsquad.security.service.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final JwtExceptionFilter jwtExceptionFilter;

    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private final CustomAuthenticationProvider customAuthenticationProvider;

    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.cors(cors -> cors.configurationSource(corsConfigurationSource)); // CORS 설정

        http.csrf(AbstractHttpConfigurer::disable); // CSRF 비활성화

        /**
         * HTTP Strict Transport Security (HSTS) 설정
         * HTTPS 강제
         */
        http.headers((headers) -> headers
                .httpStrictTransportSecurity((hsts) -> hsts
                        .maxAgeInSeconds(31536000) // HSTS 정책을 1년(31536000초) 동안 유지
                        .includeSubDomains(true)   // 서브도메인에도 HSTS 정책 적용
                        .preload(true)             // 브라우저의 HSTS Preload List에 등록 요청
                )
        );

        http.sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        http.exceptionHandling((exceptions) -> exceptions
                .accessDeniedHandler(jwtAccessDeniedHandler)
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
        );

        http.authenticationProvider(customAuthenticationProvider);

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(jwtExceptionFilter, JwtAuthenticationFilter.class);

        http.authorizeHttpRequests((auth) -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/admin").hasRole("ADMIN") // ROLE_ADMIN
                .anyRequest().authenticated()
        );

        return http.build();
    }

}