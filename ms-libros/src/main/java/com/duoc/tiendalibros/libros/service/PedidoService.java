package com.duoc.tiendalibros.libros.service;

import com.duoc.tiendalibros.libros.dto.DetallePedidoResponse;
import com.duoc.tiendalibros.libros.dto.LineaPedidoRequest;
import com.duoc.tiendalibros.libros.dto.PedidoCreateRequest;
import com.duoc.tiendalibros.libros.dto.PedidoResponse;
import com.duoc.tiendalibros.libros.entity.DetallePedido;
import com.duoc.tiendalibros.libros.entity.Libro;
import com.duoc.tiendalibros.libros.entity.Pedido;
import com.duoc.tiendalibros.libros.repository.LibroRepository;
import com.duoc.tiendalibros.libros.repository.PedidoRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PedidoService {

  public static final String PAGADO_SIMULADO = "PAGADO_SIMULADO";
  public static final String CANCELADO = "CANCELADO";

  private final PedidoRepository pedidoRepository;
  private final LibroRepository libroRepository;

  public PedidoService(PedidoRepository pedidoRepository, LibroRepository libroRepository) {
    this.pedidoRepository = pedidoRepository;
    this.libroRepository = libroRepository;
  }

  @Transactional(readOnly = true)
  public List<PedidoResponse> listar(Authentication auth) {
    Long uid = currentUserId(auth);
    boolean admin = isAdmin(auth);
    List<Pedido> lista =
        admin ? pedidoRepository.findAllConDetalles() : pedidoRepository.findByUsuarioIdConDetalles(uid);
    return lista.stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public PedidoResponse obtener(Long id, Authentication auth) {
    Pedido p =
        pedidoRepository
            .findByIdConDetalles(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));
    authorizePedido(p, auth);
    return toResponse(p);
  }

  @Transactional
  public PedidoResponse crear(PedidoCreateRequest req, Authentication auth) {
    Long usuarioId = currentUserId(auth);
    if (req.lineas().isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El pedido debe tener lineas");
    }
    Pedido pedido = new Pedido();
    pedido.setUsuarioId(usuarioId);
    pedido.setFechaCreacion(java.time.LocalDateTime.now());
    pedido.setEstadoPago(PAGADO_SIMULADO);
    BigDecimal total = BigDecimal.ZERO;
    List<DetallePedido> detalles = new ArrayList<>();
    for (LineaPedidoRequest linea : req.lineas()) {
      Libro libro =
          libroRepository
              .findById(linea.libroId())
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Libro no encontrado"));
      if (!Boolean.TRUE.equals(libro.getActivo())) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Libro inactivo: " + libro.getId());
      }
      if (libro.getStock() < linea.cantidad()) {
        throw new ResponseStatusException(
            HttpStatus.CONFLICT, "Stock insuficiente para libro id " + libro.getId());
      }
      BigDecimal subtotal = libro.getPrecio().multiply(BigDecimal.valueOf(linea.cantidad()));
      total = total.add(subtotal);
      DetallePedido dp = new DetallePedido();
      dp.setPedido(pedido);
      dp.setLibro(libro);
      dp.setCantidad(linea.cantidad());
      dp.setSubtotal(subtotal);
      detalles.add(dp);
      libro.setStock(libro.getStock() - linea.cantidad());
    }
    pedido.setTotal(total);
    pedido.getDetalles().addAll(detalles);
    Pedido guardado = pedidoRepository.save(pedido);
    return toResponse(
        pedidoRepository
            .findByIdConDetalles(guardado.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)));
  }

  @Transactional
  public PedidoResponse cancelar(Long id, Authentication auth) {
    Pedido p =
        pedidoRepository
            .findByIdConDetalles(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));
    authorizePedido(p, auth);
    if (!PAGADO_SIMULADO.equals(p.getEstadoPago())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se pueden cancelar pedidos pagados simulados");
    }
    p.setEstadoPago(CANCELADO);
    for (DetallePedido d : p.getDetalles()) {
      Libro libro = d.getLibro();
      libro.setStock(libro.getStock() + d.getCantidad());
    }
    return toResponse(pedidoRepository.save(p));
  }

  @Transactional
  public void eliminar(Long id, Authentication auth) {
    Pedido p =
        pedidoRepository
            .findByIdConDetalles(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));
    if (!isAdmin(auth)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo administrador");
    }
    if (PAGADO_SIMULADO.equals(p.getEstadoPago())) {
      for (DetallePedido d : p.getDetalles()) {
        Libro libro = d.getLibro();
        libro.setStock(libro.getStock() + d.getCantidad());
      }
    }
    pedidoRepository.delete(p);
  }

  private void authorizePedido(Pedido p, Authentication auth) {
    if (isAdmin(auth)) {
      return;
    }
    Long uid = currentUserId(auth);
    if (!p.getUsuarioId().equals(uid)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
    }
  }

  private static Long currentUserId(Authentication auth) {
    if (auth == null || auth.getName() == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
    }
    return Long.parseLong(auth.getName());
  }

  private static boolean isAdmin(Authentication auth) {
    return auth != null
        && auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
  }

  private PedidoResponse toResponse(Pedido p) {
    List<DetallePedidoResponse> lineas =
        p.getDetalles().stream()
            .map(
                d ->
                    new DetallePedidoResponse(
                        d.getLibro().getId(),
                        d.getLibro().getTitulo(),
                        d.getCantidad(),
                        d.getSubtotal()))
            .toList();
    String mensaje =
        PAGADO_SIMULADO.equals(p.getEstadoPago())
            ? "Pago simulado exitoso. No se utilizo pasarela real."
            : CANCELADO.equals(p.getEstadoPago())
                ? "Pedido cancelado. Stock restaurado."
                : "";
    return new PedidoResponse(
        p.getId(),
        p.getUsuarioId(),
        p.getFechaCreacion(),
        p.getTotal(),
        p.getEstadoPago(),
        mensaje,
        lineas);
  }
}
