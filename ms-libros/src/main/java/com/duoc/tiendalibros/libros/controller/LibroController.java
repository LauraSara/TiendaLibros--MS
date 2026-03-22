package com.duoc.tiendalibros.libros.controller;

import com.duoc.tiendalibros.libros.dto.LibroRequest;
import com.duoc.tiendalibros.libros.dto.LibroResponse;
import com.duoc.tiendalibros.libros.service.LibroService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/libros")
public class LibroController {

  private final LibroService libroService;

  public LibroController(LibroService libroService) {
    this.libroService = libroService;
  }

  @GetMapping
  public List<LibroResponse> list(@RequestParam(required = false) String q) {
    return libroService.listCatalogo(q);
  }

  @GetMapping("/{id}")
  public LibroResponse get(@PathVariable Long id) {
    return libroService.findById(id);
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<LibroResponse> create(@Valid @RequestBody LibroRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(libroService.create(request));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public LibroResponse update(@PathVariable Long id, @Valid @RequestBody LibroRequest request) {
    return libroService.update(id, request);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    libroService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
