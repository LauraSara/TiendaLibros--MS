package com.duoc.tiendalibros.usuarios.controller;

import com.duoc.tiendalibros.usuarios.dto.UserRequest;
import com.duoc.tiendalibros.usuarios.dto.UserResponse;
import com.duoc.tiendalibros.usuarios.dto.UserUpdateRequest;
import com.duoc.tiendalibros.usuarios.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public List<UserResponse> list() {
    return userService.findAll();
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or authentication.name == #id.toString()")
  public UserResponse get(@PathVariable Long id) {
    return userService.findById(id);
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public UserResponse update(
      @PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
    return userService.update(id, request);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
    if (authentication != null
        && authentication.getName() != null
        && authentication.getName().equals(String.valueOf(id))) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puede eliminar su propio usuario");
    }
    userService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
