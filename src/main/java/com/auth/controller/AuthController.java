package com.auth.controller;

import com.auth.dto.AuthRequest;
import com.auth.dto.AuthResponse;
import com.auth.dto.RefreshTokenRequest;
import com.auth.dto.RegisterRequest;
import com.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {
    
    private final AuthService authService;
    
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user", 
        description = "Creates a new user account with USER role")
    @ApiResponse(responseCode = "201", description = "User registered successfully", 
        content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input or user already exists")
    public Mono<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }
    
    @PostMapping("/login")
    @Operation(summary = "Authenticate user", 
        description = "Authenticates user and returns JWT tokens")
    @ApiResponse(responseCode = "200", description = "Authentication successful", 
        content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    public Mono<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return authService.authenticate(request);
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", 
        description = "Generates new access token using refresh token")
    @ApiResponse(responseCode = "200", description = "Token refreshed successfully", 
        content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    @ApiResponse(responseCode = "401", description = "Invalid refresh token")
    public Mono<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refreshToken(request.getRefreshToken());
    }
}