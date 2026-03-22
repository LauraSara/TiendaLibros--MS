package com.duoc.tiendalibros.libros.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record LibroRequest(
    @NotBlank @Size(max = 200) String titulo,
    @NotBlank @Size(max = 150) String autor,
    @Size(max = 20) String isbn,
    @Size(max = 100) String editorial,
    @NotNull @DecimalMin("0.0") BigDecimal precio,
    @NotNull @Min(0) Integer stock,
    @NotNull Boolean activo,
    @Size(max = 500) String descripcion) {}
