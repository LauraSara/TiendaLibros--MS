package com.duoc.tiendalibros.usuarios.security;

import com.duoc.tiendalibros.usuarios.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final JwtProperties properties;

  public JwtService(JwtProperties properties) {
    this.properties = properties;
  }

  public String createToken(Long userId, String email, String rolNombre) {
    Date now = new Date();
    Date exp = new Date(now.getTime() + properties.getExpirationMs());
    return Jwts.builder()
        .subject(String.valueOf(userId))
        .claim("email", email)
        .claim("role", rolNombre)
        .issuedAt(now)
        .expiration(exp)
        .signWith(getKey())
        .compact();
  }

  public Claims parse(String token) {
    return Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token).getPayload();
  }

  private SecretKey getKey() {
    byte[] bytes = properties.getSecret().getBytes(StandardCharsets.UTF_8);
    if (bytes.length < 32) {
      try {
        bytes = MessageDigest.getInstance("SHA-256").digest(bytes);
      } catch (NoSuchAlgorithmException e) {
        throw new IllegalStateException(e);
      }
    }
    return Keys.hmacShaKeyFor(bytes);
  }
}
