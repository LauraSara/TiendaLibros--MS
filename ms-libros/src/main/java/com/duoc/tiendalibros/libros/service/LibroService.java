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
import org.springframework.util.StringUtils;
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
  public List<LibroResponse> listar(String q) {
    List<Libro> libros;
    if (!StringUtils.hasText(q)) {
      libros = libroRepository.findByActivoTrueOrderByTituloAsc();
    } else {
      libros = libroRepository.buscarPorTexto(q.trim());
    }
    return libros.stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public LibroResponse obtener(Long id) {
    return libroRepository
        .findById(id)
        .map(this::toResponse)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Libro no encontrado"));
  }

  @Transactional
  public LibroResponse crear(LibroRequest req) {
    Libro l = new Libro();
    aplicar(l, req);
    return toResponse(libroRepository.save(l));
  }

  @Transactional
  public LibroResponse actualizar(Long id, LibroRequest req) {
    Libro l =
        libroRepository
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Libro no encontrado"));
    aplicar(l, req);
    return toResponse(libroRepository.save(l));
  }

  @Transactional
  public void eliminar(Long id) {
    if (!libroRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Libro no encontrado");
    }
    if (detallePedidoRepository.existsByLibro_Id(id)) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "No se puede eliminar: el libro tiene pedidos asociados");
    }
    libroRepository.deleteById(id);
  }

  private void aplicar(Libro l, LibroRequest req) {
    l.setTitulo(req.titulo().trim());
    l.setAutor(req.autor().trim());
    l.setIsbn(req.isbn() != null ? req.isbn().trim() : null);
    l.setEditorial(req.editorial() != null ? req.editorial().trim() : null);
    l.setPrecio(req.precio());
    l.setStock(req.stock());
    l.setActivo(req.activo());
    l.setDescripcion(req.descripcion() != null ? req.descripcion().trim() : null);
  }

  private LibroResponse toResponse(Libro l) {
    return new LibroResponse(
        l.getId(),
        l.getTitulo(),
        l.getAutor(),
        l.getIsbn(),
        l.getEditorial(),
        l.getPrecio(),
        l.getStock(),
        Boolean.TRUE.equals(l.getActivo()),
        l.getDescripcion());
  }
}
