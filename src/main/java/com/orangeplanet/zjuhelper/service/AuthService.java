package com.orangeplanet.zjuhelper.service;

import com.orangeplanet.zjuhelper.api.ZjuPassportApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final ZjuPassportApi passportApi;
    private boolean isLoggedIn = false;

    @Autowired
    public AuthService(ZjuPassportApi passportApi) {
        this.passportApi = passportApi;
    }

    public boolean login(String username, String password) {
        if (isLoggedIn) {
            logger.info("User already logged in.");
            return true;
        }
        
        boolean success = passportApi.login(username, password);
        if (success) {
            isLoggedIn = true;
        }
        return success;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }
}
