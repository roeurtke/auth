package com.auth.controller;

import com.auth.model.Role;
import com.auth.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/roles")
@Tag(name = "Roles", description = "Role management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class RoleController {
    
    private final RoleService roleService;
    
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }
    
    @GetMapping
    @Operation(summary = "Get all roles", 
        description = "Retrieve all roles (requires ROLE_READ permission and ADMIN role)")
    public Flux<Role> getAllRoles() {
        return roleService.findAllRoles();
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get role by ID", 
        description = "Retrieve role by ID (requires ROLE_READ permission and ADMIN role)")
    public Mono<Role> getRoleById(@PathVariable Long id) {
        return roleService.findRoleById(id);
    }
    
    @PostMapping
    @Operation(summary = "Create role", 
        description = "Create a new role (requires ROLE_WRITE permission and ADMIN role)")
    public Mono<Role> createRole(@RequestBody Role role) {
        return roleService.createRole(role);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update role", 
        description = "Update role information (requires ROLE_WRITE permission and ADMIN role)")
    public Mono<Role> updateRole(@PathVariable Long id, @RequestBody Role role) {
        return roleService.updateRole(id, role);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete role", 
        description = "Delete role (requires ROLE_DELETE permission and ADMIN role)")
    public Mono<Void> deleteRole(@PathVariable Long id) {
        return roleService.deleteRole(id);
    }
}