package com.auth.service;

import com.auth.model.Role;
import com.auth.repository.RoleRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Roeurt Kesei
 * Service for managing roles.
 */
@Service
public class RoleService {
    
    private final RoleRepository roleRepository;
    
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }
    
    @PreAuthorize("hasAuthority('ROLE_READ') and hasRole('ADMIN')")
    public Flux<Role> findAllRoles() {
        return roleRepository.findAll();
    }
    
    @PreAuthorize("hasAuthority('ROLE_READ') and hasRole('ADMIN')")
    public Mono<Role> findRoleById(Long id) {
        return roleRepository.findById(id);
    }
    
    @PreAuthorize("hasAuthority('ROLE_WRITE') and hasRole('ADMIN')")
    public Mono<Role> createRole(Role role) {
        return roleRepository.existsByName(role.getName())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new RuntimeException("Role already exists"));
                    }
                    return roleRepository.save(role);
                });
    }
    
    @PreAuthorize("hasAuthority('ROLE_WRITE') and hasRole('ADMIN')")
    public Mono<Role> updateRole(Long id, Role role) {
        return roleRepository.findById(id)
                .flatMap(existingRole -> {
                    existingRole.setName(role.getName());
                    existingRole.setDescription(role.getDescription());
                    return roleRepository.save(existingRole);
                });
    }
    
    @PreAuthorize("hasAuthority('ROLE_DELETE') and hasRole('ADMIN')")
    public Mono<Void> deleteRole(Long id) {
        return roleRepository.deleteById(id);
    }
}