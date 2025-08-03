package com.secureauth.authserver.auth.service;

import com.secureauth.authserver.auth.dto.Token;
import com.secureauth.authserver.auth.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private JwtUtils jwtUtils;

    @Value("${jwt.access.token.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh.token.expiration}")
    private long refreshTokenExpiration;

    public JwtService(JwtUtils jwtUtils){
        this.jwtUtils = jwtUtils;
    }

    public boolean isValidToken(String token, UserDetails userDetails){
        return jwtUtils.validateToken(token, userDetails);
    }

    public Token generateToken(String email) {
        String accessToken = jwtUtils.createToken(email, accessTokenExpiration);
        String refreshToken = jwtUtils.createToken(email, refreshTokenExpiration);

        return new Token(
                accessToken,
                refreshToken,
                accessTokenExpiration,
                refreshTokenExpiration
        );
    }

    public String extractUserEmail(String token){
        return jwtUtils.extractUserEmail(token);
    }

}
