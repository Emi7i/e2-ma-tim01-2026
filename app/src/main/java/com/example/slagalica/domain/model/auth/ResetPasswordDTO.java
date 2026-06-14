package com.example.slagalica.domain.model.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordDTO {
    private String oldPassword;
    private String newPassword;
    private String repeatedNewPassword;
}