package com.auth.controller;

import com.auth.dto.UserCreateRequest;
import com.auth.dto.UserResponse;
import com.auth.dto.ApiResponse;
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
    
    @PostMapping
    @Operation(summary = "Create new user",
            description = "Create a new user (requires USER_WRITE permission and ADMIN role)")
    public Mono<ApiResponse<UserResponse>> createUser(@RequestBody UserCreateRequest request) {
        return userService.createUser(request)
                .map(userResponse -> new ApiResponse<>("User is created successfully.", userResponse));
    }
    
    @GetMapping
    @Operation(summary = "Get all users", 
        description = "Retrieve all users (requires USER_READ permission or ADMIN role)")
    public Flux<UserResponse> getAllUsers() {
        return userService.findAllUsers();
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", 
        description = "Retrieve user by ID (requires USER_READ permission or ADMIN role)")
    public Mono<UserResponse> getUserById(@PathVariable Long id) {
        return userService.findUserById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", 
        description = "Update user information (requires USER_WRITE permission and ADMIN role)")
    public Mono<ApiResponse<UserResponse>> updateUser(@PathVariable Long id, @RequestBody UserResponse userResponse) {
        return userService.updateUser(id, userResponse)
                .map(dto -> new ApiResponse<>("User is updated successfully.", dto));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", 
        description = "Delete user (requires USER_DELETE permission and ADMIN role)")
    public Mono<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id)
                .thenReturn(new ApiResponse<Void>("User is deleted successfully.", null));
    }
}