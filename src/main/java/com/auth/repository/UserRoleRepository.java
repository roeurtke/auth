package com.auth.repository;

import com.auth.model.UserRole;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Roeurt Kesei
 * Repository for managing UserRole entities.
 */
public interface UserRoleRepository extends ReactiveCrudRepository<UserRole, Long> {
    
    Flux<UserRole> findByUserId(Long userId);
    
    Mono<Void> deleteByUserIdAndRoleId(Long userId, Long roleId);
    
    @Query("DELETE FROM tbl_user_role WHERE user_id = :userId")
    Mono<Void> deleteByUserId(Long userId);
}