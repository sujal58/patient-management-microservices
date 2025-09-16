package org.pm.authservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.pm.authservice.dto.LoginRequestDto;
import org.pm.authservice.dto.LoginResponseDto;
import org.pm.authservice.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@Tag(name = "Auth Controller", description = "This controller is responsible for handling authentication related request")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Generate token on user login")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        Optional<String> tokenOptional = authService.authenticate(loginRequestDto);

        if(tokenOptional.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = tokenOptional.get();
        return ResponseEntity.ok(new LoginResponseDto(token));
    }

    @Operation(summary = "Endpoint to validate the token")
    @GetMapping("/validate")
    public ResponseEntity<String> validateToken(@RequestHeader("Authorization") String authHeader){
        //Authorization:Bearer <token>
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized user found!");
        }

        return authService.validateToken(authHeader.substring(7)) ?
         ResponseEntity.ok("Authorized token!") : ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized user found!");
    }
}
