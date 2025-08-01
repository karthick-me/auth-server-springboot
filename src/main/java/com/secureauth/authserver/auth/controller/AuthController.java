package com.secureauth.authserver.auth.controller;

import com.secureauth.authserver.auth.dto.SignupRequest;
import com.secureauth.authserver.auth.service.AuthService;
import com.secureauth.authserver.common.response.ApiSuccessResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService){
        this.authService = authService;
    }

    @GetMapping("/greet")
    public ResponseEntity<String> greeting(){
       return new ResponseEntity<>("hi from server", HttpStatus.ACCEPTED);
    }

    @GetMapping("/signup")
    public ResponseEntity<ApiSuccessResponse> signup(@RequestBody SignupRequest signupRequest){
        authService.signup(signupRequest);
        ApiSuccessResponse apiResponse = new ApiSuccessResponse(
                "User created successfully",
                signupRequest, HttpStatus.CREATED);
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }
}
