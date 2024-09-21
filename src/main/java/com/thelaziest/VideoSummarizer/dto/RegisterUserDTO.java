package com.thelaziest.VideoSummarizer.dto;

import lombok.Data;

@Data
public class RegisterUserDTO {
    private String email;

    private String password;

    private String name;

    private String fullName;
}
