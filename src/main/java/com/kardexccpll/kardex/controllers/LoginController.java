package com.kardexccpll.kardex.controllers;

import com.kardexccpll.kardex.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

@Controller
public class LoginController {

    @Autowired
    private LoginService loginService;

    @GetMapping("/loginCCPLL")
    public String mostrarLogin() {
        return "loginCCPLL";
    }

    @PostMapping("/loginCCPLL")
    public String procesarLogin(@RequestParam String usuario,
                                @RequestParam String contraseña,
                                Model model) {

        if (loginService.validarCredenciales(usuario, contraseña)) {
            return "redirect:/menu";
        } else {
            model.addAttribute("error", "Usuario o contraseña incorrectos");
            return "loginCCPLL";
        }
    }
}
