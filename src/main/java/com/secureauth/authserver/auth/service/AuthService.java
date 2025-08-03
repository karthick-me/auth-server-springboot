package com.secureauth.authserver.auth.service;

import com.secureauth.authserver.auth.dto.LoginRequest;
import com.secureauth.authserver.auth.dto.SignupRequest;
import com.secureauth.authserver.auth.dto.Token;
import com.secureauth.authserver.auth.dto.VerifyUserRequest;
import com.secureauth.authserver.auth.model.EmailVerificationToken;
import com.secureauth.authserver.auth.repository.EmailVerificationTokenRepository;
import com.secureauth.authserver.common.exception.InvalidTokenException;
import com.secureauth.authserver.user.dto.UserDto;
import com.secureauth.authserver.user.model.User;
import com.secureauth.authserver.user.service.UserService;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final EmailService emailService;

    private final EmailVerificationTokenRepository emailVerificationTokenRepository;

    private AuthenticationManager authenticationManager;

    private final UserDetailsService userDetailsService;

    public AuthService(UserService userService,
                       JwtService jwtService,
                       UserDetailsService userDetailsService,
                       EmailService emailService,
                       AuthenticationManager authenticationManager,
                       EmailVerificationTokenRepository emailVerificationTokenRepository){
        this.userService = userService;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
    }

    public UserDto signup(SignupRequest signupRequest){

        User user = new User();

        user.setEmail(signupRequest.getEmail());
        user.setPassword(signupRequest.getPassword());
        user.setUsername(signupRequest.getUsername());
        user.setEnabled(false);
        User registeredUser = userService.registerUser(user);;


        emailVerificationTokenRepository.deleteByUser(registeredUser);
        EmailVerificationToken emailVerificationToken = new EmailVerificationToken();

        emailVerificationToken.setCode(generateVerificationCode());
        emailVerificationToken.setUsed(false);
        emailVerificationToken.setExpiresAt(LocalDateTime.now().plusHours(1));

        emailVerificationToken.setUser(registeredUser);

        emailVerificationTokenRepository.save(emailVerificationToken);
        sendVerificationEmail(registeredUser, emailVerificationToken);

        return UserDto.fromEntity(registeredUser);
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

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

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

    private void sendVerificationEmail(User user,
                                       EmailVerificationToken emailVerificationToken) {
        String subject = "Account Verification";
        String verificationCode = "VERIFICATION CODE " + emailVerificationToken.getCode();

        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Welcome to our app!</h2>"
                + "<p style=\"font-size: 16px;\">Please enter the verification code below to continue:</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Verification Code:</h3>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + verificationCode + "</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";

        try {
            emailService.sendVerificationEmail(user.getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            System.out.println(e.getMessage());
        }
    }

    public void resendVerificationCode(String email) {

        User user = userService.getUserByEmail(email);

        emailVerificationTokenRepository.deleteByUser(user);
        if (user.isEnabled()) {
            throw new RuntimeException("Account is already verified");
        }
        EmailVerificationToken emailVerificationToken = emailVerificationTokenRepository.findByUser(user);
        emailVerificationToken.setCode(generateVerificationCode());
        emailVerificationToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        sendVerificationEmail(user, emailVerificationToken);
        emailVerificationTokenRepository.save(emailVerificationToken);
    }

    public void verifyUser(VerifyUserRequest verifyUserRequest) {

        User user = userService.getUserByEmail(verifyUserRequest.getEmail());

        EmailVerificationToken emailVerificationToken =
                emailVerificationTokenRepository.findByUser(user);

        if (emailVerificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification code has expired");
        }
        if (emailVerificationToken.getCode().equals(verifyUserRequest.getVerificationCode())) {
            user.setEnabled(true);
            userService.saveUser(user);
            emailVerificationTokenRepository.delete(emailVerificationToken);
        } else {
            throw new RuntimeException("Invalid verification code");
        }

    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}
