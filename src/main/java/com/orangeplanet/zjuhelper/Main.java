package com.orangeplanet.zjuhelper;

import com.orangeplanet.zjuhelper.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("ZJU Helper Application Started");
        
        AuthService authService = new AuthService();
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
        
        scanner.close();
    }
}
