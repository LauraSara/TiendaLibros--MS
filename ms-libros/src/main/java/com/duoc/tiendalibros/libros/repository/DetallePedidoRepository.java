package com.duoc.tiendalibros.libros.repository;

import com.duoc.tiendalibros.libros.entity.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

  boolean existsByLibro_Id(Long libroId);
}
