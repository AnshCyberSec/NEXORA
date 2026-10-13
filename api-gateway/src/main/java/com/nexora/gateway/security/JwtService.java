package com.nexora.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class JwtService {
    private static final String LOG_PREFIX = "[GATEWAY-JWT]";

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey getSigningKey(){
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims validateToken(String token){
        try{
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        }catch (Exception e){
            log.warn("{} Invalid Jwt: {}", LOG_PREFIX, e.getMessage());
            return null;
        }
    }

    public String extractUsername(String token){
        Claims claims = validateToken(token);
        return claims != null ? claims.getSubject() : null;
    }

    public String extractUserId(String token){
        Claims claims =  validateToken(token);
        return claims != null ? claims.get("userId",String.class) : null;
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token){
        Claims claims = validateToken(token);
        return claims != null ? claims.get("roles",List.class) : List.of();
    }

    public boolean isTokenExpired(String token){
        Claims claims = validateToken(token);
        if (claims == null) return true;
        return claims.getExpiration().before(new Date());
    }
}
