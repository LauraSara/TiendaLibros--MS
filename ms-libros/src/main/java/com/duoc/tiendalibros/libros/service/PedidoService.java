package com.duoc.tiendalibros.libros.service;

import com.duoc.tiendalibros.libros.dto.DetallePedidoResponse;
import com.duoc.tiendalibros.libros.dto.PedidoCreateRequest;
import com.duoc.tiendalibros.libros.dto.PedidoLineRequest;
import com.duoc.tiendalibros.libros.dto.PedidoResponse;
import com.duoc.tiendalibros.libros.entity.DetallePedido;
import com.duoc.tiendalibros.libros.entity.Libro;
import com.duoc.tiendalibros.libros.entity.Pedido;
import com.duoc.tiendalibros.libros.repository.LibroRepository;
import com.duoc.tiendalibros.libros.repository.PedidoRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
  public List<PedidoResponse> list() {
    if (isAdmin()) {
      return pedidoRepository.findAllWithDetalles().stream().map(PedidoService::toResponse).toList();
    }
    Long uid = currentUserId();
    return pedidoRepository.findByUsuarioIdWithDetalles(uid).stream()
        .map(PedidoService::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public PedidoResponse getById(Long id) {
    Pedido p =
        pedidoRepository
            .findByIdWithDetalles(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));
    if (!canAccessPedido(p)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
    }
    return toResponse(p);
  }

  @Transactional
  public PedidoResponse create(PedidoCreateRequest req) {
    Long uid = currentUserId();
    if (req.lineas() == null || req.lineas().isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe incluir al menos una linea");
    }
    Pedido pedido = new Pedido();
    pedido.setUsuarioId(uid);
    pedido.setEstadoPago(PAGADO_SIMULADO);

    BigDecimal total = BigDecimal.ZERO;
    List<DetallePedido> detalles = new ArrayList<>();

    for (PedidoLineRequest linea : req.lineas()) {
      Libro libro =
          libroRepository
              .findById(linea.libroId())
              .orElseThrow(
                  () ->
                      new ResponseStatusException(
                          HttpStatus.BAD_REQUEST, "Libro no encontrado: " + linea.libroId()));
      if (!Boolean.TRUE.equals(libro.getActivo())) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Libro inactivo: " + libro.getId());
      }
      if (libro.getStock() < linea.cantidad()) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Stock insuficiente para libro id " + libro.getId());
      }
      BigDecimal subtotal =
          libro.getPrecio().multiply(BigDecimal.valueOf(linea.cantidad())).setScale(2, RoundingMode.HALF_UP);
      total = total.add(subtotal);

      DetallePedido d = new DetallePedido();
      d.setPedido(pedido);
      d.setLibro(libro);
      d.setCantidad(linea.cantidad());
      d.setSubtotal(subtotal);
      detalles.add(d);

      libro.setStock(libro.getStock() - linea.cantidad());
      libroRepository.save(libro);
    }

    pedido.setTotal(total.setScale(2, RoundingMode.HALF_UP));
    pedido.getDetalles().addAll(detalles);
    Pedido guardado = pedidoRepository.save(pedido);
    return toResponse(guardado);
  }

  @Transactional
  public PedidoResponse cancelar(Long id) {
    Pedido p =
        pedidoRepository
            .findByIdWithDetalles(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));
    if (!canAccessPedido(p)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
    }
    if (CANCELADO.equals(p.getEstadoPago())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El pedido ya esta cancelado");
    }
    if (!PAGADO_SIMULADO.equals(p.getEstadoPago())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado no permite cancelacion");
    }
    restaurarStock(p);
    p.setEstadoPago(CANCELADO);
    return toResponse(pedidoRepository.save(p));
  }

  @Transactional
  public void delete(Long id) {
    Pedido p =
        pedidoRepository
            .findByIdWithDetalles(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));
    if (PAGADO_SIMULADO.equals(p.getEstadoPago())) {
      restaurarStock(p);
    }
    pedidoRepository.delete(p);
  }

  private void restaurarStock(Pedido p) {
    for (DetallePedido d : p.getDetalles()) {
      Libro libro = d.getLibro();
      libro.setStock(libro.getStock() + d.getCantidad());
      libroRepository.save(libro);
    }
  }

  private boolean canAccessPedido(Pedido p) {
    if (isAdmin()) {
      return true;
    }
    return p.getUsuarioId().equals(currentUserId());
  }

  private static boolean isAdmin() {
    Authentication a = SecurityContextHolder.getContext().getAuthentication();
    if (a == null) {
      return false;
    }
    for (GrantedAuthority ga : a.getAuthorities()) {
      if ("ROLE_ADMIN".equals(ga.getAuthority())) {
        return true;
      }
    }
    return false;
  }

  private static Long currentUserId() {
    Authentication a = SecurityContextHolder.getContext().getAuthentication();
    if (a == null || a.getName() == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
    }
    try {
      return Long.parseLong(a.getName());
    } catch (NumberFormatException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token invalido");
    }
  }

  private static PedidoResponse toResponse(Pedido p) {
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
    return new PedidoResponse(
        p.getId(), p.getUsuarioId(), p.getFechaCreacion(), p.getTotal(), p.getEstadoPago(), lineas);
  }
}
