package com.orangeplanet.zjuhelper.controller;

import com.orangeplanet.zjuhelper.model.Email;
import com.orangeplanet.zjuhelper.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/emails")
    public List<Email> getEmails(@RequestParam String username, @RequestParam String password) throws Exception {
        return emailService.fetchEmails(username, password);
    }
}
