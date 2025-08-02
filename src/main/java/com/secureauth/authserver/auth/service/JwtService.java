package com.secureauth.authserver.auth.service;

import com.secureauth.authserver.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private final JwtConfig jwtConfig;

    public JwtService(JwtConfig jwtConfig){
        this.jwtConfig = jwtConfig;

    }

    public String generateToken(String email){
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtConfig.getExpirationMs()))
                .and()
                .signWith(jwtConfig.getSecretKey())
                .compact();
    }

    public String extractUserEmail(String token){
        return extractClaim(token, Claims::getSubject);
    }

    private  <T> T extractClaim(String token, Function<Claims, T> claimResolver){
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token){
        return Jwts.parser()
                .verifyWith(jwtConfig.getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValidToken(String token, UserDetails userDetails) {
        final String userName = extractUserEmail(token);
        return (userName.equals(userDetails.getUsername()))  && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }


}
