package com.onePilates.agendamento.auth.security;

import com.onePilates.agendamento.model.Funcionario;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms:86400000}")
    private long expirationMs;

    private Key getSigningKey() {
        byte[] keyBytes;

        // Tenta decodificar Base64; se falhar, usa bytes UTF-8 da string diretamente
        try {
            keyBytes = Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException ex) {
            keyBytes = secret.getBytes();
        }

        // Validação mínima: HS256 requer 256 bits (32 bytes)
        if (keyBytes.length < 32) {
            throw new IllegalStateException("Chave JWT muito curta. Use pelo menos 256 bits (32 bytes) ou um Base64 com >=32 bytes.");
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Funcionario funcionario) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(funcionario.getEmail())
                .claim("role", funcionario.getRole().name())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
    }

    public String extractEmail(String token) {
        return parseToken(token).getBody().getSubject();
    }

    public String extractRole(String token) {
        Object r = parseToken(token).getBody().get("role");
        return r == null ? null : r.toString();
    }

    public boolean isTokenExpired(String token) {
        Date exp = parseToken(token).getBody().getExpiration();
        return exp.before(new Date());
    }
}