package com.mycompany.mytestframework.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
public class HomeController {

    @GetMapping("/home")
    public RedirectView home() {
        return new RedirectView("/index.html");
    }

    @GetMapping("/hello")
    public String hello() {
        return "Test Automation Framework is running on port 8080";
    }
}
