package com.orangeplanet.zjuhelper;

import com.orangeplanet.zjuhelper.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Scanner;

@SpringBootApplication
public class Main implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    @Autowired
    private AuthService authService;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("ZJU Helper Application Started");
        
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter Username (Student ID): ");
        String username = scanner.nextLine();

        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        if (authService.login(username, password)) {
            logger.info("Login successful! You can now access other services.");
            // TODO: Initialize other services and start main loop
        } else {
            logger.error("Login failed. Please check your credentials.");
        }
        
        // scanner.close(); // Avoid closing System.in in Spring Boot if not necessary
    }
}
