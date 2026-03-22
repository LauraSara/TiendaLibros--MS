package com.duoc.tiendalibros.libros.repository;

import com.duoc.tiendalibros.libros.entity.Libro;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LibroRepository extends JpaRepository<Libro, Long> {

  List<Libro> findByActivoTrueOrderByTituloAsc();

  @Query(
      "SELECT l FROM Libro l WHERE l.activo = true AND (LOWER(l.titulo) LIKE LOWER(CONCAT('%', :q, '%'))"
          + " OR LOWER(l.autor) LIKE LOWER(CONCAT('%', :q, '%'))) ORDER BY l.titulo")
  List<Libro> buscarActivos(@Param("q") String q);
}
