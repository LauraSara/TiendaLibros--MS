package com.duoc.tiendalibros.usuarios;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class PasswordEncodingTest {

  private static final String SEED_HASH =
      "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi";

  @Test
  void seedPasswordMatchesDemo() {
    BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
    assertTrue(enc.matches("password", SEED_HASH));
  }
}
