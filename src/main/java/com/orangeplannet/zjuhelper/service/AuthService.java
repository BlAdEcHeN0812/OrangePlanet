package com.orangeplannet.zjuhelper.service;

import com.orangeplannet.zjuhelper.api.ZjuPassportApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final ZjuPassportApi passportApi;
    private boolean isLoggedIn = false;

    public AuthService() {
        this.passportApi = new ZjuPassportApi();
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
