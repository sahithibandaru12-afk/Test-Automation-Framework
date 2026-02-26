package com.mycompany.mytestframework.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "🎉 SUCCESS! Your Spring Boot Application is running!<br>" +
                "✅ MySQL Connected<br>" +
                "✅ Server Time: " + LocalDateTime.now();
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello World! Test endpoint working!";
    }
}