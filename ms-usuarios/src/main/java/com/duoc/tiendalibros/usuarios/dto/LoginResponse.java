package com.duoc.tiendalibros.usuarios.dto;

public record LoginResponse(String token, String tipo, Long usuarioId, String email, String rol) {}
