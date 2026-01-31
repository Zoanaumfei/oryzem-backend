package com.oryzem.backend.modules.system.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api")  // ⬅️ TODOS endpoints desta classe começam com /api
public class HelloController {

    @GetMapping("/hello")  // ⬅️ Agora é /api/hello (combinado com @RequestMapping)
    public String hello() {
        return "Hello, Oryzem API is running!";
    }

    // Outro endpoint automaticamente seria /api/outro
    @GetMapping("/status")
    public String status() {
        return "API está operacional!";
    }
}

