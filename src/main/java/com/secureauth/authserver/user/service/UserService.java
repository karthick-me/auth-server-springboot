package com.secureauth.authserver.user.service;

import com.secureauth.authserver.common.exception.BadRequestException;
import com.secureauth.authserver.common.exception.UserNotFoundException;
import com.secureauth.authserver.user.dto.UserDto;
import com.secureauth.authserver.user.model.User;
import com.secureauth.authserver.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


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
        String encryptedPassword = bCryptPasswordEncoder.encode(password);

        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(encryptedPassword);

        User savedUser = userRepository.save(user);
        return UserDto.fromEntity(savedUser);
    }

    public UserDto getUserByEmail(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with this email id."));

        return UserDto.fromEntity(user);
    }
}
