package com.duoc.tiendalibros.libros.dto;

import java.math.BigDecimal;

public record LibroResponse(
    Long id,
    String titulo,
    String autor,
    String isbn,
    String editorial,
    BigDecimal precio,
    Integer stock,
    boolean activo,
    String descripcion) {}
