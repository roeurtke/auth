package com.auth.repository;

import com.auth.model.Role;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface RoleRepository extends ReactiveCrudRepository<Role, Long> {
    
    Mono<Role> findByName(String name);
    Mono<Boolean> existsByName(String name);
}