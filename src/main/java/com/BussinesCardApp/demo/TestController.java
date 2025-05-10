package com.BussinesCardApp.demo;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*") // IP of the React machine!
public class TestController {
    @GetMapping("/api/test")
    public String testConnection() {
        return "✅ Backend is working!";
    }
}