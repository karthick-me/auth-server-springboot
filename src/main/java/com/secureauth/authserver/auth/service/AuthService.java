package com.secureauth.authserver.auth.service;

import com.secureauth.authserver.auth.dto.LoginRequest;
import com.secureauth.authserver.auth.dto.SignupRequest;
import com.secureauth.authserver.auth.dto.Token;
import com.secureauth.authserver.common.exception.InvalidTokenException;
import com.secureauth.authserver.user.dto.UserDto;
import com.secureauth.authserver.user.model.User;
import com.secureauth.authserver.user.service.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;

    private final UserDetailsService userDetailsService;

    public AuthService(UserService userService,
                       JwtService jwtService,
                       UserDetailsService userDetailsService){
        this.userService = userService;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    public UserDto signup(SignupRequest signupRequest){

        User user = new User();

        user.setEmail(signupRequest.getEmail());
        user.setPassword(signupRequest.getPassword());
        user.setUsername(signupRequest.getUsername());

        return userService.registerUser(user);
    }

    public Token login(LoginRequest loginRequest) {

        if (loginRequest.getEmail() == null || loginRequest.getPassword() == null) {
            throw new BadCredentialsException("Email and password must not be null");
        }

        String email = loginRequest.getEmail().trim();
        String password = loginRequest.getPassword().trim();

        if (email.isEmpty() || password.isEmpty()) {
            throw new BadCredentialsException("Email and password must not be empty");
        }

        Token token = jwtService.generateToken(email);

        userService.setRefreshTokenDetails(email, token.getRefreshToken());

        return token;
    }

    public Token authenticateAndGenerateToken(HttpServletRequest request) {

        String jwtToken = null;
        if(request.getCookies() != null){
            for(Cookie cookie : request.getCookies()){
                if("refreshToken".equals(cookie.getName())){
                    jwtToken = cookie.getValue();
                }
            }
        }

        if(jwtToken == null){
            throw new InvalidTokenException("Invalid token");
        }

        String email = jwtService.extractUserEmail(jwtToken);

        if(email == null ){
            throw new InvalidTokenException("Invalid token");
        }

        UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);

        if(!jwtService.isValidToken(jwtToken, userDetails)){
            throw new InvalidTokenException("Invalid token");
        }

        if(!userService.isRefreshTokenValid(jwtToken, userDetails.getUsername())){
            throw new InvalidTokenException("Invalid token");
        }

        Token token = jwtService.generateToken(email);

        userService.setRefreshTokenDetails(email, token.getRefreshToken());

        return jwtService.generateToken(email);
    }
}
