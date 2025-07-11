package com.auth_service.service;

import com.auth_service.dto.LoginRequestDTO;
import com.auth_service.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public Optional<String> authenticate(LoginRequestDTO loginRequestDTO) {
        System.out.println("Attempting login for email: " + loginRequestDTO.getEmail());

        return userService.findByEmail(loginRequestDTO.getEmail())
                .map(u -> {
                    boolean passwordMatch = passwordEncoder.matches(loginRequestDTO.getPassword(), u.getPassword());
                    System.out.println("User found. Password match: " + passwordMatch);
                    if (passwordMatch) {
                        String token = jwtUtil.generateToken(u.getEmail(), u.getRole());
                        System.out.println("Token generated: " + token);
                        return token;
                    } else {
                        return null;
                    }
                });
    }


    public boolean validateToken(String token) {
        try {
            jwtUtil.validateToken(token);
            return true;
        } catch (JwtException e){
            return false;
        }
    }
}