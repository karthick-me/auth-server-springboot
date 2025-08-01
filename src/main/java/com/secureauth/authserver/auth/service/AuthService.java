package com.secureauth.authserver.auth.service;

import com.secureauth.authserver.auth.dto.SignupRequest;
import com.secureauth.authserver.user.service.UserService;

import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;

    public AuthService(UserService userService){
        this.userService = userService;
    }

    public void signup(SignupRequest signupRequest) {
        userService.registerUser();
    }
}
