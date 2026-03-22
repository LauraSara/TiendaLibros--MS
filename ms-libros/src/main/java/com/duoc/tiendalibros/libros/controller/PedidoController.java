package com.duoc.tiendalibros.libros.controller;

import com.duoc.tiendalibros.libros.dto.PedidoCreateRequest;
import com.duoc.tiendalibros.libros.dto.PedidoResponse;
import com.duoc.tiendalibros.libros.service.PedidoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

  private final PedidoService pedidoService;

  public PedidoController(PedidoService pedidoService) {
    this.pedidoService = pedidoService;
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENTE')")
  public List<PedidoResponse> list() {
    return pedidoService.list();
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENTE')")
  public PedidoResponse get(@PathVariable Long id) {
    return pedidoService.getById(id);
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENTE')")
  public ResponseEntity<PedidoResponse> create(@Valid @RequestBody PedidoCreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.create(request));
  }

  @PutMapping("/{id}/cancelar")
  @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENTE')")
  public PedidoResponse cancelar(@PathVariable Long id) {
    return pedidoService.cancelar(id);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    pedidoService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
