package com.secureauth.authserver.auth.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.secureauth.authserver.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class EmailVerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code; // this is the email verification code

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime expiresAt;

    private boolean used = false;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}