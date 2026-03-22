package com.duoc.tiendalibros.libros.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
    Long id,
    Long usuarioId,
    LocalDateTime fechaCreacion,
    BigDecimal total,
    String estadoPago,
    List<DetallePedidoResponse> lineas) {}
