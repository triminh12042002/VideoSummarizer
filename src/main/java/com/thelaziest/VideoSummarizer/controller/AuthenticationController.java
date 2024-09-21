package com.thelaziest.VideoSummarizer.controller;

import com.thelaziest.VideoSummarizer.dto.LoginResponse;
import com.thelaziest.VideoSummarizer.dto.LoginUserDTO;
import com.thelaziest.VideoSummarizer.dto.RegisterUserDTO;
import com.thelaziest.VideoSummarizer.model.User;
import com.thelaziest.VideoSummarizer.service.AuthenticationService;
import com.thelaziest.VideoSummarizer.service.JWTService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/auth")
@RestController
public class AuthenticationController {
    private final JWTService jwtService;

    private final AuthenticationService authenticationService;

    public AuthenticationController(JWTService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody RegisterUserDTO registerUserDTO) {
        System.out.println(registerUserDTO.toString());
        System.out.println("Sign up");
        User registeredUser = authenticationService.signUp(registerUserDTO);
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserDTO loginUserDTO) {
        User authenticatedUser = authenticationService.authenticate(loginUserDTO);

        String jwtToken = jwtService.generateToken(authenticatedUser);

        LoginResponse loginResponse = new LoginResponse().setToken(jwtToken).setExpiresIn(jwtService.getExpirationTime());

        return ResponseEntity.ok(loginResponse);
    }
}

