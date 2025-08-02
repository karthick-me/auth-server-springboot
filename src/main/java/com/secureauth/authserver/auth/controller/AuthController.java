package com.secureauth.authserver.auth.controller;

import com.secureauth.authserver.auth.dto.SignupRequest;
import com.secureauth.authserver.auth.service.AuthService;
import com.secureauth.authserver.common.response.ApiSuccessResponse;
import com.secureauth.authserver.user.dto.UserDto;
import com.secureauth.authserver.user.model.User;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService){
        this.authService = authService;
    }

    @GetMapping("/greet")
    public ResponseEntity<String> greet(){
        return new ResponseEntity<>("Hello from server", HttpStatus.ACCEPTED);
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiSuccessResponse> signup(@RequestBody SignupRequest signupRequest) throws BadRequestException {
        UserDto savedUser = authService.signup(signupRequest);
        ApiSuccessResponse apiResponse = new ApiSuccessResponse(
                "User created successfully",
                savedUser, HttpStatus.CREATED.value());
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }
}
