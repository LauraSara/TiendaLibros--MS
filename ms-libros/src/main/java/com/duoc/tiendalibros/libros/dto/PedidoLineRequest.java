package com.duoc.tiendalibros.libros.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PedidoLineRequest(@NotNull Long libroId, @NotNull @Min(1) Integer cantidad) {}
