package com.kardexccpll.kardex.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LoginControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void cuandoLoginValido_debeRedirigirAMenu() throws Exception {
        mockMvc.perform(post("/loginCCPLL")
                .param("usuario", "logistica")
                .param("contraseña", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/menu"));
    }

    @Test
    void cuandoLoginInvalido_muestraError() throws Exception {
        mockMvc.perform(post("/loginCCPLL")
                .param("usuario", "x")
                .param("contraseña", "y"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"));
    }
}
