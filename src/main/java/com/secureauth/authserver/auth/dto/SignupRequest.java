package com.secureauth.authserver.auth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SignupRequest {

    private String email;
    private String username;
    private String password;
}
