package com.kardexccpll.kardex.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class CodigoServiceTest {

    private CodigoService codigoService;
    private JdbcTemplate jdbcMock;

    @BeforeAll
    public static void setUpClass() {
        System.out.println("== Iniciando pruebas CodigoService == ");
    }

    @AfterAll
    public static void tearDownClass() {
        System.out.println("== Pruebas finalizadas ==");
    }

    @BeforeEach
    public void setUp() throws Exception {
        jdbcMock = mock(JdbcTemplate.class);
        codigoService = new CodigoService();

       
        var field = CodigoService.class.getDeclaredField("jdbcTemplate");
        field.setAccessible(true);
        field.set(codigoService, jdbcMock);
    }

    @AfterEach
    public void tearDown() {
    }

  // TEST 1: Generar código correcto ART0001
    @Test
    public void testGenerarCodigoArticulo_correcto() {

        
        when(jdbcMock.queryForObject(anyString(), eq(Integer.class))).thenReturn(1);

       
        String codigo = codigoService.generarCodigoArticulo();

        
        assertEquals("ART0001", codigo);
    }


    // TEST 2: Cuando la BD devuelve un número negativo, debe lanzar una excpepción

    @Test
    public void testGenerarCodigoArticulo_numeroNegativo() {

       
        when(jdbcMock.queryForObject(anyString(), eq(Integer.class))).thenReturn(-5);

       
        assertThrows(IllegalArgumentException.class, () -> {
            codigoService.generarCodigoArticulo();
        });
    }
}