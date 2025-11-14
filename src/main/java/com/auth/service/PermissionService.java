package com.auth.service;

import com.auth.model.Permission;
import com.auth.repository.PermissionRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Roeurt Kesei
 * Service for managing permissions.
 */
@Service
public class PermissionService {
    
    private final PermissionRepository permissionRepository;
    
    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }
    
    @PreAuthorize("hasAuthority('PERMISSION_READ') and hasRole('ADMIN')")
    public Flux<Permission> findAllPermissions() {
        return permissionRepository.findAll();
    }
    
    @PreAuthorize("hasAuthority('PERMISSION_READ') and hasRole('ADMIN')")
    public Mono<Permission> findPermissionById(Long id) {
        return permissionRepository.findById(id);
    }
    
    @PreAuthorize("hasAuthority('PERMISSION_WRITE') and hasRole('ADMIN')")
    public Mono<Permission> createPermission(Permission permission) {
        return permissionRepository.existsByName(permission.getName())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new RuntimeException("Permission already exists"));
                    }
                    return permissionRepository.save(permission);
                });
    }
    
    @PreAuthorize("hasAuthority('PERMISSION_DELETE') and hasRole('ADMIN')")
    public Mono<Void> deletePermission(Long id) {
        return permissionRepository.deleteById(id);
    }
}