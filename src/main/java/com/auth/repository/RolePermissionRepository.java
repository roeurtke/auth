package com.auth.repository;

import com.auth.model.RolePermission;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface RolePermissionRepository extends ReactiveCrudRepository<RolePermission, Long> {
    
    Flux<RolePermission> findByRoleId(Long roleId);
}