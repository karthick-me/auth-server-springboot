package com.secureauth.authserver.user.service;

import com.secureauth.authserver.common.exception.BadRequestException;
import com.secureauth.authserver.user.model.User;
import com.secureauth.authserver.user.repository.UserRepository;
import org.springframework.stereotype.Service;


@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }
    public User registerUser(User user) throws BadRequestException {
        if(user.getEmail() == null
                || user.getPassword() == null
                || user.getUsername() == null){
            throw new BadRequestException("All fields must be provided.");
        }

        String email = user.getEmail().trim().toLowerCase();
        String username = user.getUsername().trim();
        String password = user.getPassword().trim();

        if(userRepository.existsByEmail(email)){
            throw new BadRequestException("Email already registered.");
        }
        if(userRepository.existsByUsername(username)){
            throw new BadRequestException("Username is already taken.");
        }

        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(password);
        return userRepository.save(user);
    }
}
