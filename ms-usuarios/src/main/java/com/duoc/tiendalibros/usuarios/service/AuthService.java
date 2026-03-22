package com.duoc.tiendalibros.usuarios.service;

import com.duoc.tiendalibros.usuarios.dto.LoginRequest;
import com.duoc.tiendalibros.usuarios.dto.LoginResponse;
import com.duoc.tiendalibros.usuarios.entity.Usuario;
import com.duoc.tiendalibros.usuarios.repository.UsuarioRepository;
import com.duoc.tiendalibros.usuarios.security.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
public class AuthService {

  private final UsuarioRepository usuarioRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthService(
      UsuarioRepository usuarioRepository,
      PasswordEncoder passwordEncoder,
      JwtService jwtService) {
    this.usuarioRepository = usuarioRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  public LoginResponse login(LoginRequest req) {
    Usuario u =
        usuarioRepository
            .findByEmail(req.email().trim().toLowerCase())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas"));
    if (!Boolean.TRUE.equals(u.getActivo())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario desactivado");
    }
    if (!passwordEncoder.matches(req.password(), u.getPasswordHash())) {
      log.debug("Password mismatch for {}", req.email());
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas");
    }
    String rol = u.getRol().getNombre();
    String token = jwtService.createToken(u.getId(), u.getEmail(), rol);
    return new LoginResponse(token, "Bearer", u.getId(), u.getEmail(), rol);
  }
}
