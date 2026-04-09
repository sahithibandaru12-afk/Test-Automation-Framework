package com.mycompany.mytestframework.controller;

import com.mycompany.mytestframework.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/send")
    public Map<String, String> sendTestEmail(@RequestParam String to) {
        Map<String, String> response = new HashMap<>();

        String subject = "Test Alert from Test Automation Framework";
        String content = "This is a test email to verify your email alerts are working correctly.\n\n" +
                "Your test automation framework is configured properly!";

        emailService.sendAlertEmail(to, subject, content);

        response.put("status", "success");
        response.put("message", "Test email sent to " + to);
        return response;
    }

    @PostMapping("/alert")
    public Map<String, String> sendAlert(@RequestBody Map<String, String> request) {
        String to = request.get("to");
        String subject = request.get("subject");
        String content = request.get("content");

        emailService.sendAlertEmail(to, subject, content);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Alert sent to " + to);
        return response;
    }
}