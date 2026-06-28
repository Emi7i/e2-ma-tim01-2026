package com.example.slagalica.domain.model.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationDTO {
    private String email;
    private String username;
    private String region;
    private String password;
    private String repeatedPassword;
}