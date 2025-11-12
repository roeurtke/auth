package com.auth.repository;

import com.auth.model.Permission;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface PermissionRepository extends ReactiveCrudRepository<Permission, Long> {
    
    Mono<Permission> findByName(String name);
    Mono<Boolean> existsByName(String name);
}