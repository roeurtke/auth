package com.auth.controller;

import com.auth.model.Permission;
import com.auth.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/permissions")
@Tag(name = "Permissions", description = "Permission management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class PermissionController {
    
    private final PermissionService permissionService;
    
    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }
    
    @GetMapping
    @Operation(summary = "Get all permissions", 
        description = "Retrieve all permissions (requires PERMISSION_READ permission and ADMIN role)")
    public Flux<Permission> getAllPermissions() {
        return permissionService.findAllPermissions();
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get permission by ID", 
        description = "Retrieve permission by ID (requires PERMISSION_READ permission and ADMIN role)")
    public Mono<Permission> getPermissionById(@PathVariable Long id) {
        return permissionService.findPermissionById(id);
    }
    
    @PostMapping
    @Operation(summary = "Create permission", 
        description = "Create a new permission (requires PERMISSION_WRITE permission and ADMIN role)")
    public Mono<Permission> createPermission(@RequestBody Permission permission) {
        return permissionService.createPermission(permission);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete permission", 
        description = "Delete permission (requires PERMISSION_DELETE permission and ADMIN role)")
    public Mono<Void> deletePermission(@PathVariable Long id) {
        return permissionService.deletePermission(id);
    }
}