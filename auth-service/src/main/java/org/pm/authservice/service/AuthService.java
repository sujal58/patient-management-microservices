package org.pm.authservice.service;

import io.jsonwebtoken.JwtException;
import org.pm.authservice.dto.LoginRequestDto;
import org.pm.authservice.model.User;
import org.pm.authservice.utils.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public Optional<String> authenticate(LoginRequestDto loginRequestDto){
        Optional<String> token = userService
                .findByEmail(loginRequestDto.getEmail())
                .filter(u -> passwordEncoder.matches(loginRequestDto.getPassword(), u.getPassword()))
                .map(u -> jwtUtil.generateToken(u.getEmail(), u.getRole()));

        return token;
    }

    public Boolean validateToken(String token){
        try {
            jwtUtil.verifyToken(token);
            return true;
        }catch (JwtException e){
            return false;
        }
    }
}
