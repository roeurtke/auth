package com.auth.service;

import com.auth.dto.AuthRequest;
import com.auth.dto.AuthResponse;
import com.auth.dto.RegisterRequest;
import com.auth.model.User;
import com.auth.repository.RoleRepository;
import com.auth.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.HashSet;

@Service
public class AuthService {
    
    private final UserService userService;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    
    public AuthService(UserService userService, RoleRepository roleRepository, 
    JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.roleRepository = roleRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Transactional
    public Mono<AuthResponse> register(RegisterRequest request) {
        return userService.existsByUsername(request.getUsername())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new RuntimeException("Username already exists"));
                    }
                    return userService.existsByEmail(request.getEmail());
                })
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new RuntimeException("Email already exists"));
                    }
                    
                    User user = new User(
                            request.getFirstName(),
                            request.getLastName(),
                            request.getUsername(),
                            passwordEncoder.encode(request.getPassword()),
                            request.getEmail()
                    );
                    user.setPhoneNumber(request.getPhoneNumber());
                    
                    // Assign USER role by default
                    return roleRepository.findByName("USER")
                            .switchIfEmpty(Mono.error(new RuntimeException("USER role not found")))
                            .flatMap(role -> {
                                user.setRoles(new HashSet<>());
                                user.getRoles().add(role);
                                return userService.save(user);
                            });
                })
                .flatMap(user -> {
                    String accessToken = jwtService.generateAccessToken(user);
                    String refreshToken = jwtService.generateRefreshToken(user);
                    Long expiresIn = jwtService.extractExpiration(accessToken).getTime() - System.currentTimeMillis();
                    
                    return Mono.just(new AuthResponse(accessToken, refreshToken, expiresIn));
                });
    }
    
    public Mono<AuthResponse> authenticate(AuthRequest request) {
        return userService.findByUsername(request.getUsername())
                .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid credentials")))
                .flatMap(user -> {
                    String accessToken = jwtService.generateAccessToken(user);
                    String refreshToken = jwtService.generateRefreshToken(user);
                    Long expiresIn = jwtService.extractExpiration(accessToken).getTime() - System.currentTimeMillis();
                    
                    return Mono.just(new AuthResponse(accessToken, refreshToken, expiresIn));
                });
    }
    
    public Mono<AuthResponse> refreshToken(String refreshToken) {
        if (!jwtService.validateToken(refreshToken)) {
            return Mono.error(new RuntimeException("Invalid refresh token"));
        }
        
        String username = jwtService.extractUsername(refreshToken);
        
        return userService.findByUsername(username)
                .flatMap(user -> {
                    String newAccessToken = jwtService.generateAccessToken(user);
                    String newRefreshToken = jwtService.generateRefreshToken(user);
                    Long expiresIn = jwtService.extractExpiration(newAccessToken).getTime() - System.currentTimeMillis();
                    
                    return Mono.just(new AuthResponse(newAccessToken, newRefreshToken, expiresIn));
                });
    }
}