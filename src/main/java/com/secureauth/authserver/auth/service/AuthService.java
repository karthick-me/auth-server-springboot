package com.secureauth.authserver.auth.service;

import com.secureauth.authserver.auth.dto.LoginRequest;
import com.secureauth.authserver.auth.dto.SignupRequest;
import com.secureauth.authserver.user.dto.UserDto;
import com.secureauth.authserver.user.model.User;
import com.secureauth.authserver.user.service.UserService;

import org.apache.coyote.BadRequestException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;
    private final JwtService tokenService;

    private final AuthenticationManager authenticationManager;

    public AuthService(UserService userService,
                       JwtService tokenService,
                       AuthenticationManager authenticationManager){
        this.userService = userService;
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
    }

    public UserDto signup(SignupRequest signupRequest) throws BadRequestException {

        User user = new User();

        user.setEmail(signupRequest.getEmail());
        user.setPassword(signupRequest.getPassword());
        user.setUsername(signupRequest.getUsername());

        return userService.registerUser(user);
    }

    public String login(LoginRequest loginRequest) {

        if (loginRequest.getEmail() == null || loginRequest.getPassword() == null) {
            throw new BadCredentialsException("Email and password must not be null");
        }

        String email = loginRequest.getEmail().trim();
        String password = loginRequest.getPassword().trim();

        if (email.isEmpty() || password.isEmpty()) {
            throw new BadCredentialsException("Email and password must not be empty");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        userService.getUserByEmail(email);

        return tokenService.generateToken(email);
    }
}
