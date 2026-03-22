package com.duoc.tiendalibros.libros.dto;

import java.math.BigDecimal;

public record DetallePedidoResponse(
    Long libroId, String tituloLibro, Integer cantidad, BigDecimal subtotal) {}
