package com.licr.graduation.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtService {
    private final SecretKey key;
    private final long expireHours;
    public JwtService(@Value("${jwt.secret}") String secret, @Value("${jwt.expire-hours}") long expireHours) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); this.expireHours = expireHours;
    }
    public String create(CurrentUser user) {
        Instant now = Instant.now();
        return Jwts.builder().subject(user.username()).claim("uid", user.id()).claim("role", user.role())
                .issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(expireHours * 3600)))
                .signWith(key).compact();
    }
    public CurrentUser parse(String token) {
        var claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        return new CurrentUser(claims.get("uid", Long.class), claims.getSubject(), claims.get("role", String.class));
    }
}
