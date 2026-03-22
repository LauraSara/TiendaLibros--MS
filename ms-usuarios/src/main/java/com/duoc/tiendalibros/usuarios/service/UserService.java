package com.duoc.tiendalibros.usuarios.service;

import com.duoc.tiendalibros.usuarios.dto.UserRequest;
import com.duoc.tiendalibros.usuarios.dto.UserResponse;
import com.duoc.tiendalibros.usuarios.dto.UserUpdateRequest;
import com.duoc.tiendalibros.usuarios.entity.Rol;
import com.duoc.tiendalibros.usuarios.entity.Usuario;
import com.duoc.tiendalibros.usuarios.repository.RolRepository;
import com.duoc.tiendalibros.usuarios.repository.UsuarioRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

  private final UsuarioRepository usuarioRepository;
  private final RolRepository rolRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService(
      UsuarioRepository usuarioRepository,
      RolRepository rolRepository,
      PasswordEncoder passwordEncoder) {
    this.usuarioRepository = usuarioRepository;
    this.rolRepository = rolRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Transactional(readOnly = true)
  public List<UserResponse> findAll() {
    return usuarioRepository.findAll().stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public UserResponse findById(Long id) {
    return usuarioRepository
        .findById(id)
        .map(this::toResponse)
        .orElseThrow(
            () -> new org.springframework.web.server.ResponseStatusException(
                HttpStatus.NOT_FOUND, "Usuario no encontrado"));
  }

  @Transactional
  public UserResponse create(UserRequest req) {
    String email = req.email().trim().toLowerCase();
    if (usuarioRepository.existsByEmail(email)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email ya registrado");
    }
    Rol rol = findRol(req.rolNombre());
    Usuario u = new Usuario();
    u.setEmail(email);
    u.setPasswordHash(passwordEncoder.encode(req.password()));
    u.setNombre(req.nombre().trim());
    u.setApellido(req.apellido().trim());
    u.setActivo(req.activo());
    u.setRol(rol);
    return toResponse(usuarioRepository.save(u));
  }

  @Transactional
  public UserResponse update(Long id, UserUpdateRequest req) {
    Usuario u =
        usuarioRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new org.springframework.web.server.ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    String email = req.email().trim().toLowerCase();
    if (!email.equals(u.getEmail()) && usuarioRepository.existsByEmail(email)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email ya registrado");
    }
    u.setEmail(email);
    if (req.password() != null && !req.password().isBlank()) {
      if (req.password().length() < 6) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password minimo 6 caracteres");
      }
      u.setPasswordHash(passwordEncoder.encode(req.password()));
    }
    u.setNombre(req.nombre().trim());
    u.setApellido(req.apellido().trim());
    u.setActivo(req.activo());
    u.setRol(findRol(req.rolNombre()));
    return toResponse(usuarioRepository.save(u));
  }

  @Transactional
  public void delete(Long id) {
    if (!usuarioRepository.existsById(id)) {
      throw new org.springframework.web.server.ResponseStatusException(
          HttpStatus.NOT_FOUND, "Usuario no encontrado");
    }
    usuarioRepository.deleteById(id);
  }

  private Rol findRol(String nombre) {
    return rolRepository
        .findByNombre(nombre.trim().toUpperCase())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol no existe"));
  }

  private UserResponse toResponse(Usuario u) {
    return new UserResponse(
        u.getId(),
        u.getEmail(),
        u.getNombre(),
        u.getApellido(),
        Boolean.TRUE.equals(u.getActivo()),
        u.getRol().getNombre());
  }

}
