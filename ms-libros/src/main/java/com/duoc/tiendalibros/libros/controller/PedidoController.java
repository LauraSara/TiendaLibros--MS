package com.duoc.tiendalibros.libros.controller;

import com.duoc.tiendalibros.libros.dto.PedidoCreateRequest;
import com.duoc.tiendalibros.libros.dto.PedidoResponse;
import com.duoc.tiendalibros.libros.service.PedidoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
  public List<PedidoResponse> listar(Authentication authentication) {
    return pedidoService.listar(authentication);
  }

  @GetMapping("/{id}")
  public PedidoResponse obtener(@PathVariable Long id, Authentication authentication) {
    return pedidoService.obtener(id, authentication);
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN','CLIENTE')")
  public ResponseEntity<PedidoResponse> crear(
      @Valid @RequestBody PedidoCreateRequest request, Authentication authentication) {
    return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.crear(request, authentication));
  }

  @PutMapping("/{id}/cancelar")
  @PreAuthorize("hasAnyRole('ADMIN','CLIENTE')")
  public PedidoResponse cancelar(@PathVariable Long id, Authentication authentication) {
    return pedidoService.cancelar(id, authentication);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication authentication) {
    pedidoService.eliminar(id, authentication);
    return ResponseEntity.noContent().build();
  }
}
