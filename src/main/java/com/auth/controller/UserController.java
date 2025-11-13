package com.auth.controller;

import com.auth.dto.UserDto;
import com.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Roeurt Kesei
 * User management REST controller.
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping
    @Operation(summary = "Get all users", 
        description = "Retrieve all users (requires USER_READ permission or ADMIN role)")
    public Flux<UserDto> getAllUsers() {
        return userService.findAllUsers();
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", 
        description = "Retrieve user by ID (requires USER_READ permission or ADMIN role)")
    public Mono<UserDto> getUserById(@PathVariable Long id) {
        return userService.findUserById(id);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update user", 
        description = "Update user information (requires USER_WRITE permission and ADMIN role)")
    public Mono<UserDto> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        return userService.updateUser(id, userDto);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", 
        description = "Delete user (requires USER_DELETE permission and ADMIN role)")
    public Mono<Void> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id);
    }
}