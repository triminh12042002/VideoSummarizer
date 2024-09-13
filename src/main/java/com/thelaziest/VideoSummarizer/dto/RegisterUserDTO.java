package com.thelaziest.VideoSummarizer.dto;

import lombok.Data;

@Data
public class RegisterUserDTO {
    private String email;

    private String password;

    private String fullName;

    @Override
    public String toString() {
        return "RegisterUserDTO{" +
                "email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", fullName='" + fullName + '\'' +
                '}';
    }
}
