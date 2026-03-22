package com.duoc.tiendalibros.libros.service;

import com.duoc.tiendalibros.libros.dto.LibroRequest;
import com.duoc.tiendalibros.libros.dto.LibroResponse;
import com.duoc.tiendalibros.libros.entity.Libro;
import com.duoc.tiendalibros.libros.repository.DetallePedidoRepository;
import com.duoc.tiendalibros.libros.repository.LibroRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LibroService {

  private final LibroRepository libroRepository;
  private final DetallePedidoRepository detallePedidoRepository;

  public LibroService(LibroRepository libroRepository, DetallePedidoRepository detallePedidoRepository) {
    this.libroRepository = libroRepository;
    this.detallePedidoRepository = detallePedidoRepository;
  }

  @Transactional(readOnly = true)
  public List<LibroResponse> listCatalogo(String q) {
    List<Libro> rows;
    if (q == null || q.isBlank()) {
      rows = libroRepository.findByActivoTrueOrderByTituloAsc();
    } else {
      rows = libroRepository.buscarActivos(q.trim());
    }
    return rows.stream().map(LibroService::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public LibroResponse findById(Long id) {
    Libro l = libroRepository.findById(id).orElseThrow(() -> notFound(id));
    if (!Boolean.TRUE.equals(l.getActivo())) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Libro no disponible");
    }
    return toResponse(l);
  }

  @Transactional
  public LibroResponse create(LibroRequest req) {
    Libro l = new Libro();
    apply(l, req);
    return toResponse(libroRepository.save(l));
  }

  @Transactional
  public LibroResponse update(Long id, LibroRequest req) {
    Libro l = libroRepository.findById(id).orElseThrow(() -> notFound(id));
    apply(l, req);
    return toResponse(libroRepository.save(l));
  }

  @Transactional
  public void delete(Long id) {
    Libro l = libroRepository.findById(id).orElseThrow(() -> notFound(id));
    if (detallePedidoRepository.existsByLibro_Id(id)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "No se puede eliminar: el libro figura en pedidos");
    }
    libroRepository.delete(l);
  }

  private static ResponseStatusException notFound(Long id) {
    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Libro no encontrado: " + id);
  }

  private static void apply(Libro l, LibroRequest req) {
    l.setTitulo(req.titulo().trim());
    l.setAutor(req.autor().trim());
    l.setIsbn(trimToNull(req.isbn()));
    l.setEditorial(trimToNull(req.editorial()));
    l.setPrecio(req.precio());
    l.setStock(req.stock());
    l.setActivo(req.activo());
    l.setDescripcion(trimToNull(req.descripcion()));
  }

  private static String trimToNull(String s) {
    if (s == null || s.isBlank()) {
      return null;
    }
    return s.trim();
  }

  private static LibroResponse toResponse(Libro l) {
    return new LibroResponse(
        l.getId(),
        l.getTitulo(),
        l.getAutor(),
        l.getIsbn(),
        l.getEditorial(),
        l.getPrecio(),
        l.getStock(),
        l.getActivo(),
        l.getDescripcion());
  }
}
