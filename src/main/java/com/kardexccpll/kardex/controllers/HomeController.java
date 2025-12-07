package com.kardexccpll.kardex.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("mensaje", "hola");
        return "home";
    }

    @GetMapping("/reportes")
    public String reportes() {
        return "reportes";
    }
}