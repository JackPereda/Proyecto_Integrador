package com.kardexccpll.kardex.service;

import com.kardexccpll.kardex.dao.LoginDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    @Autowired
    private LoginDAO loginDAO;

    public boolean validarCredenciales(String usuario, String contraseña) {
        return loginDAO.autenticar(usuario, contraseña);
    }
}
