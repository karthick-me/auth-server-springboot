package com.secureauth.authserver.auth.controller;

import com.secureauth.authserver.auth.dto.LoginRequest;
import com.secureauth.authserver.auth.dto.SignupRequest;
import com.secureauth.authserver.auth.service.AuthService;
import com.secureauth.authserver.common.response.ApiSuccessResponse;
import com.secureauth.authserver.user.dto.UserDto;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthController(AuthService authService,
                          AuthenticationManager authenticationManager){
        this.authService = authService;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/greet")
    public ResponseEntity<String> greet(){
        return new ResponseEntity<>("Hello from server", HttpStatus.ACCEPTED);
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiSuccessResponse> signup(@RequestBody SignupRequest signupRequest) throws BadRequestException {

        UserDto savedUser = authService.signup(signupRequest);

        ApiSuccessResponse apiSuccessResponse = new ApiSuccessResponse(
                "User created successfully",
                savedUser, HttpStatus.CREATED.value());

        return new ResponseEntity<>(apiSuccessResponse, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiSuccessResponse> login(@RequestBody LoginRequest loginRequest){

        UserDto loggedInUser = authService.login(loginRequest);

        ApiSuccessResponse apiSuccessResponse = new ApiSuccessResponse(
                "User logged in successfully",
                loggedInUser, HttpStatus.OK.value());

        return new ResponseEntity<>(apiSuccessResponse, HttpStatus.OK);
    }
}
