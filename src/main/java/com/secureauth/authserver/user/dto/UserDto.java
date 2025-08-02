package com.secureauth.authserver.user.dto;

import com.secureauth.authserver.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    public long id;
    public String email;
    public String username;

    public static UserDto fromEntity(User user){
        return new UserDto(user.getId(), user.getEmail(), user.getUsername());
    }
}
