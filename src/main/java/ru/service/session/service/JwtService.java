package ru.service.session.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.service.session.config.common.ServerProperties;
import ru.service.session.config.security.JwtProperties;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class JwtService {

    private final JwtProperties jwtProperties;
    private final ServerProperties serverProperties;
    private SecretKey accessKey;

    @Autowired
    public JwtService(JwtProperties jwtProperties,
                      ServerProperties serverProperties) {
        this.jwtProperties = jwtProperties;
        this.serverProperties = serverProperties;
        this.accessKey = Keys.hmacShaKeyFor(jwtProperties.getAccessKey().getBytes());
    }

    public void validateToken(String token) {
        log.info("Validating token");
        Claims claims = extractAllClaims(token);
        if (!claims.getIssuedAt().before(new Date())) {
            throw new JwtException("Token expired");
        }
    }

    public Claims extractAllClaims(String token) {
        log.info("Verifying with key and extracting claims from token");
        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(accessKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw new JwtException(e.getMessage());
        }
        return claims;
    }

    public String generateOwnToken() {
        var jwtBuilder = Jwts.builder()
                .subject(serverProperties.getName())
                .claim("userId", Long.parseLong(serverProperties.getPort()))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60))
                .signWith(Keys.hmacShaKeyFor(jwtProperties.getAccessKey().getBytes()))
                .claim("authorities", List.of("ROLE_SERVICE"));
        return jwtBuilder.compact();
    }
}
