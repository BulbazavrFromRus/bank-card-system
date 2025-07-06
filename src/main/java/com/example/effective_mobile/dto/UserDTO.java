package com.example.effective_mobile.dto;

import lombok.Data;
import com.example.effective_mobile.entity.User.Role;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private Role role;
}
