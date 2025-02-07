package com.example.learningsquad.auth.model;

import com.example.learningsquad.security.jwt.model.Authority;
import io.jsonwebtoken.Claims;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@ToString(callSuper = true)
public class AccountAuth extends User {
    private Long accountId;
    private String userName;
    private String email;
    private String profileImage;
    private String accessToken;
    private String refreshToken;

    public AccountAuth(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
    }

    public AccountAuth(String userName, Collection<? extends GrantedAuthority> authorities) {
        super(userName, "password", true, true, true, true, authorities);
    }

    public static AccountAuth of(Claims body, List<Authority> authorities) {
        String userName = body.getSubject();
        AccountAuth accountAuth = new AccountAuth(userName, authorities);
        return accountAuth;
    }

}
