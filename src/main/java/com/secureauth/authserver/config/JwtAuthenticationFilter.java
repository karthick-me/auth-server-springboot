package com.secureauth.authserver.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.secureauth.authserver.auth.service.JwtService;
import com.secureauth.authserver.common.response.ApiErrorResponse;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   UserDetailsService userDetailsService,
                                   ObjectMapper objectMapper){
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }
        try{

            final String jwt = authHeader.substring(7);
            final String userEmail = jwtService.extractUserEmail(jwt);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if(userEmail != null && authentication == null){
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                if(jwtService.isValidToken(jwt, userDetails)){

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails,
                                    null,
                                    userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            filterChain.doFilter(request, response);

        } catch (JwtException exception) {
            ApiErrorResponse errorResponse = getApiErrorResponse(exception);

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            response.getWriter().write(convertToJson(errorResponse));
        }
    }

    private ApiErrorResponse getApiErrorResponse(JwtException exception) {
        String message;

        if (exception.getMessage().contains("expired")) {
            message = "Your session has expired. Please log in again.";
        } else if (exception.getMessage().contains("malformed")) {
            message = "Malformed JWT. Authentication failed.";
        } else if (exception.getMessage().contains("unsupported")) {
            message = "Unsupported JWT token.";
        } else {
            message = "Invalid authentication token.";
        }

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                message,
                HttpStatus.UNAUTHORIZED.value(),
                "UNAUTHORIZED"
        );
        return errorResponse;
    }

    private String convertToJson(ApiErrorResponse errorResponse) {
        try {
            return objectMapper.writeValueAsString(errorResponse);
        } catch (Exception e) {
            return "{\"message\":\"Failed to serialize error response\"}";
        }
    }
}
