package com.duoc.tiendalibros.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
    @NotBlank @Email String email,
    String password,
    @NotBlank @Size(max = 80) String nombre,
    @NotBlank @Size(max = 80) String apellido,
    @NotNull Boolean activo,
    @NotBlank String rolNombre) {}
