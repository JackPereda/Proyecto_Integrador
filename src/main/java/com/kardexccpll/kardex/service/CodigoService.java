package com.kardexccpll.kardex.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings; 

@Service
public class CodigoService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional
    public String generarCodigoArticulo() {
        
        Preconditions.checkNotNull(jdbcTemplate, "JdbcTemplate no puede ser nulo");

        String sql = "SELECT siguiente_numero FROM secuencia_codigos WHERE prefijo = 'ART' FOR UPDATE";
        Integer siguienteNumero = jdbcTemplate.queryForObject(sql, Integer.class);

        if (siguienteNumero == null) {
            siguienteNumero = 1;
            jdbcTemplate.update("INSERT INTO secuencia_codigos (prefijo, siguiente_numero, descripcion) VALUES ('ART', 2, 'Secuencia para códigos de artículos')");
        }

       
        Preconditions.checkArgument(siguienteNumero > 0, "El número de secuencia debe ser positivo");

        String codigo = String.format("ART%04d", siguienteNumero);

      
        if (Strings.isNullOrEmpty(codigo)) {
            throw new IllegalStateException("No se pudo generar el código");
        }

        jdbcTemplate.update("UPDATE secuencia_codigos SET siguiente_numero = siguiente_numero + 1 WHERE prefijo = 'ART'");

        return codigo;
    }
}