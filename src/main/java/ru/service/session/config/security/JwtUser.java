package ru.service.session.config.security;

import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

@Getter
public class JwtUser extends User {

    private final String userId;
    private final String login;
    private final String jwt;

    public JwtUser(String userId, String login, List<String> authorities, String jwt) {
        super(userId, jwt,
                authorities.stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList());
        this.userId = userId;
        this.login = login;
        this.jwt = jwt;
    }
}
