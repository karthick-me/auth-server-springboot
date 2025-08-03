package com.secureauth.authserver.auth.controller;

import com.secureauth.authserver.auth.dto.LoginRequest;
import com.secureauth.authserver.auth.dto.SignupRequest;
import com.secureauth.authserver.auth.dto.Token;
import com.secureauth.authserver.auth.service.AuthService;
import com.secureauth.authserver.common.response.ApiSuccessResponse;
import com.secureauth.authserver.user.dto.UserDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

        ApiSuccessResponse apiSuccessResponse = new ApiSuccessResponse(
                "User created successfully",
                savedUser, HttpStatus.CREATED.value());

        return new ResponseEntity<>(apiSuccessResponse, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiSuccessResponse> login(@RequestBody LoginRequest loginRequest,
                                                    HttpServletResponse httpServletResponse){

        Token token = authService.login(loginRequest);

        setTokenInCookieResponse(httpServletResponse, token);

        ApiSuccessResponse apiSuccessResponse = new ApiSuccessResponse(
                "User logged in successfully", HttpStatus.OK.value());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(apiSuccessResponse);
    }

    @PostMapping("token/refresh")
    public ResponseEntity<ApiSuccessResponse> refreshToken(HttpServletRequest httpServletRequest,
                                               HttpServletResponse httpServletResponse){

        Token token = authService.authenticateAndGenerateToken(httpServletRequest);

        setTokenInCookieResponse(httpServletResponse, token);

        ApiSuccessResponse apiSuccessResponse = new ApiSuccessResponse(
                "Token refreshed successfully", HttpStatus.OK.value());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(apiSuccessResponse);
    }

    private void setTokenInCookieResponse(HttpServletResponse httpServletResponse,
                                                             Token token) {
        int accessTokenExpiry = (int) token.getAccessTokenExpiration() / 1000;
        int refreshTokenExpiry = (int) token.getRefreshTokenExpiration() / 1000;


        Cookie accessTokenCookie = new Cookie("accessToken", token.getAccessToken());
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setMaxAge(accessTokenExpiry);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setSecure(true);

        Cookie refreshTokenCookie = new Cookie("refreshToken", token.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/api/auth/token/refresh");
        refreshTokenCookie.setMaxAge(refreshTokenExpiry);

        httpServletResponse.addCookie(accessTokenCookie);
        httpServletResponse.addCookie(refreshTokenCookie);
    }
}
