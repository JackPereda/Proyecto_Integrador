package com.kardexccpll.kardex.repository;

import com.kardexccpll.kardex.model.Articulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticuloRepository extends JpaRepository<Articulo, Integer> {

    // Buscar artículos activos
    List<Articulo> findByActivoTrue();

    // Buscar por código (solo activos)
    Optional<Articulo> findByCodigoAndActivoTrue(String codigo);

    // Verificar si existe un código
    boolean existsByCodigo(String codigo);
}