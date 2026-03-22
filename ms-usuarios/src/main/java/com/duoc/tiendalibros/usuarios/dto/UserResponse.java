package com.duoc.tiendalibros.usuarios.dto;

public record UserResponse(
    Long id, String email, String nombre, String apellido, boolean activo, String rol) {}
