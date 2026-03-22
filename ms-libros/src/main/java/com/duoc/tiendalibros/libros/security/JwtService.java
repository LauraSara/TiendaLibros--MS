package com.duoc.tiendalibros.libros.security;

import com.duoc.tiendalibros.libros.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final JwtProperties properties;

  public JwtService(JwtProperties properties) {
    this.properties = properties;
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
