package com.secureauth.authserver.user.service;

import com.secureauth.authserver.common.exception.BadRequestException;
import com.secureauth.authserver.common.exception.UserNotFoundException;
import com.secureauth.authserver.user.dto.UserDto;
import com.secureauth.authserver.user.model.User;
import com.secureauth.authserver.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = new BCryptPasswordEncoder(12);
    }

    public UserDto registerUser(User user) throws BadRequestException {
        if(user.getEmail() == null
                || user.getPassword() == null
                || user.getUsername() == null){
            throw new BadRequestException("All fields must be provided.");
        }

        String email = user.getEmail().trim().toLowerCase();
        String username = user.getUsername().trim();

        if(userRepository.existsByEmail(email)){
            throw new BadRequestException("Email already registered.");
        }
        if(userRepository.existsByUsername(username)){
            throw new BadRequestException("Username is already taken.");
        }

        String password = user.getPassword().trim();
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
        if (passwordBytes.length > 72) {
            throw new BadRequestException("Password must not exceed 72 bytes.");
        }

        String encryptedPassword = bCryptPasswordEncoder.encode(password);

        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(encryptedPassword);

        User savedUser = userRepository.save(user);
        return UserDto.fromEntity(savedUser);
    }

    public void setRefreshTokenDetails(String email,
                                       String refreshToken){
        User user = getUserByEmail(email);
        String encryptedRefreshToken = hashRefreshToken(refreshToken);
        user.setRefreshToken(encryptedRefreshToken);
        userRepository.save(user);
    }

    public boolean isRefreshTokenValid(String refreshToken, String email) {
        User user = getUserByEmail(email);
        String hashed = hashRefreshToken(refreshToken);
        return  MessageDigest.isEqual(
                hashed.getBytes(StandardCharsets.UTF_8),
                user.getRefreshToken().getBytes(StandardCharsets.UTF_8)
        );
    }

    private User getUserByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with this email id."));
    }

    public String hashRefreshToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing refresh token", e);
        }
    }

}
