package com.example.learningsquad.auth.controller;

import com.example.learningsquad.auth.model.AccountAuth;
import com.example.learningsquad.auth.model.SignInDto;
import com.example.learningsquad.auth.model.SignUpDto;
import com.example.learningsquad.auth.service.AuthService;
import com.example.learningsquad.global.common.controller.Controller;
import com.example.learningsquad.global.common.model.ApiResponse;
import com.example.learningsquad.oauth.common.OAuthLoginParams;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController extends Controller {

    private final AuthService authService;

    @PostMapping("/signup")
    public ApiResponse signUp(@RequestBody SignUpDto signUpDto) {
        final SignUpDto response = authService.signUp(signUpDto);
        return success(response);
    }

    @PostMapping("/signin")
    public ApiResponse signIn(@RequestBody SignInDto signInDto) {
        final AccountAuth response = authService.signInWithSignInDto(signInDto);
        return success(response);
    }

    @PostMapping("/signin/oauth")
    public ApiResponse signInOAuth(@RequestBody OAuthLoginParams params) {
        final AccountAuth response = authService.signInWithOAuth(params);
        return success(response);
    }

    @PostMapping("/signout")
    public ApiResponse signOut(@RequestHeader("Authorization") String accessToken) {
        authService.signOut(accessToken);
        return success();
    }

    @PostMapping("/reissue")
    public ApiResponse reissue(@CookieValue("RefreshToken") String refreshToken, @RequestHeader("Authorization") String accessToken) {
        final AccountAuth response = authService.reissue(refreshToken, accessToken);
        return success(response);
    }
}
