package com.duoc.tiendalibros.libros.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record PedidoCreateRequest(@NotEmpty @Valid List<PedidoLineRequest> lineas) {}
