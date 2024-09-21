package com.thelaziest.VideoSummarizer.service;

import com.thelaziest.VideoSummarizer.dto.LoginUserDTO;
import com.thelaziest.VideoSummarizer.dto.RegisterUserDTO;
import com.thelaziest.VideoSummarizer.model.User;
import com.thelaziest.VideoSummarizer.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public User signUp(RegisterUserDTO userDTO) {
        User user = new User()
                .setFullName(userDTO.getName())
                .setEmail(userDTO.getEmail())
                .setPassword(passwordEncoder.encode(userDTO.getPassword()));

        return userRepository.save(user);
    }

    public User authenticate(LoginUserDTO loginUserDTO) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginUserDTO.getEmail(),
                        loginUserDTO.getPassword()
                )
        );

        return userRepository.findByEmail(loginUserDTO.getEmail()).orElseThrow();
    }
}
