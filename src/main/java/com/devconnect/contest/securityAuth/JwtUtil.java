package com.devconnect.contest.securityAuth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@RequiredArgsConstructor
@Slf4j
@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.expiration}")
    private int expiration;

    @Value("${app.jwt.temp-expiration}")
    private int tempExpiration;


    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }





    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSignKey()).build().parseSignedClaims(token);
            return true;
        } catch (RuntimeException e) {
            log.warn(e.getMessage());
            return false;
        }
    }

    public Long getId(String token) {
        Claims claims = Jwts.parser().verifyWith(getSignKey()).build().parseSignedClaims(token).getPayload();
        return Long.parseLong(claims.getSubject());
    }

    public boolean isPending2FA(String token) {
        Claims claims = Jwts.parser().verifyWith(getSignKey()).build().parseSignedClaims(token).getPayload();
        return claims.get("isPending2FA", Boolean.class);
    }
    public String getRole(String token) {
        Claims claims = Jwts.parser().verifyWith(getSignKey()).build().parseSignedClaims(token).getPayload();
        return claims.get("role", String.class);
    }

}