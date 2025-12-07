package com.kardexccpll.kardex.repository;

import com.kardexccpll.kardex.model.MovimientoHistorial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimientoHistorialRepository extends JpaRepository<MovimientoHistorial, Integer> {

    // Buscar movimientos por artículo ordenados por fecha descendente
    List<MovimientoHistorial> findByArticuloIdOrderByFechaMovimientoDesc(Integer articuloId);

    // Buscar todos los movimientos ordenados por fecha descendente
    List<MovimientoHistorial> findAllByOrderByFechaMovimientoDesc();

    // Buscar movimientos entre fechas
    @Query("SELECT m FROM MovimientoHistorial m WHERE m.fechaMovimiento BETWEEN :fechaInicio AND :fechaFin ORDER BY m.fechaMovimiento DESC")
    List<MovimientoHistorial> findByFechaMovimientoBetween(@Param("fechaInicio") LocalDateTime fechaInicio,
                                                           @Param("fechaFin") LocalDateTime fechaFin);

    // Buscar movimientos por código de artículo
    @Query("SELECT m FROM MovimientoHistorial m WHERE m.articulo.codigo = :codigo ORDER BY m.fechaMovimiento DESC")
    List<MovimientoHistorial> findByArticuloCodigo(@Param("codigo") String codigo);
}