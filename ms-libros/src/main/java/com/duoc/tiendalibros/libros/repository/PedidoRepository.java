package com.duoc.tiendalibros.libros.repository;

import com.duoc.tiendalibros.libros.entity.Pedido;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

  @Query(
      "SELECT DISTINCT p FROM Pedido p LEFT JOIN FETCH p.detalles d LEFT JOIN FETCH d.libro WHERE p.usuarioId = :uid ORDER BY p.fechaCreacion DESC")
  List<Pedido> findByUsuarioIdConDetalles(@Param("uid") Long uid);

  @Query("SELECT p FROM Pedido p LEFT JOIN FETCH p.detalles d LEFT JOIN FETCH d.libro WHERE p.id = :id")
  Optional<Pedido> findByIdConDetalles(@Param("id") Long id);

  @Query(
      "SELECT DISTINCT p FROM Pedido p LEFT JOIN FETCH p.detalles d LEFT JOIN FETCH d.libro ORDER BY p.fechaCreacion DESC")
  List<Pedido> findAllConDetalles();
}
