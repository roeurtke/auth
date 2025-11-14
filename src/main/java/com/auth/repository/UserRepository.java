package com.auth.repository;

import com.auth.model.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

/**
 * @author Roeurt Kesei
 * Repository for managing User entities.
 */
public interface UserRepository extends ReactiveCrudRepository<User, Long> {
    
    Mono<User> findByUsername(String username);
    
    Mono<User> findByEmail(String email);
    
    Mono<Boolean> existsByUsername(String username);
    
    Mono<Boolean> existsByEmail(String email);
    
    @Query("SELECT u.*, r.id as role_id, r.name as role_name, r.description as role_description " +
        "FROM tbl_user u " +
        "LEFT JOIN tbl_user_role ur ON u.id = ur.user_id " +
        "LEFT JOIN tbl_role r ON ur.role_id = r.id " +
        "WHERE u.username = :username")
    Mono<User> findByUsernameWithRoles(String username);
}