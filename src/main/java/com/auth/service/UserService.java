package com.auth.service;

import com.auth.dto.UserCreateRequest;
import com.auth.dto.UserDto;
import com.auth.model.User;
import com.auth.repository.UserRepository;
import com.auth.repository.UserRoleRepository;
import com.auth.repository.RoleRepository;
import com.auth.repository.RolePermissionRepository;
import com.auth.repository.PermissionRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Roeurt Kesei
 * Service for managing users.
 */
@Service
public class UserService implements ReactiveUserDetailsService {
    
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;
    
    public UserService(UserRepository userRepository, UserRoleRepository userRoleRepository, RoleRepository roleRepository,
                    RolePermissionRepository rolePermissionRepository, PermissionRepository permissionRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.permissionRepository = permissionRepository;
    }
    
        @Override
        public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUsernameAndIsDeletedFalse(username)
            .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found: " + username)))
            .flatMap(user -> loadUserRoles(user)
                .thenReturn(user)
                .cast(UserDetails.class)
            );
        }
    
    private Mono<Void> loadUserRoles(User user) {
        return userRoleRepository.findByUserId(user.getId())
            .collectList()
            .flatMap(userRoles -> {
                if (userRoles.isEmpty()) {
                    return Mono.empty();
                }
                return Flux.fromIterable(userRoles)
                    .flatMap(userRole -> roleRepository.findById(userRole.getRoleId()))
                    .collectList()
                    .doOnNext(roles -> user.setRoles(roles.stream().collect(java.util.stream.Collectors.toSet())))
                    .flatMap(roles -> loadPermissionsForRoles(user, roles))
                    .then();
            });
    }
    
    private Mono<Void> loadPermissionsForRoles(User user, java.util.List<com.auth.model.Role> roles) {
        return Flux.fromIterable(roles)
            .flatMap(role -> rolePermissionRepository.findByRoleId(role.getId()))
            .flatMap(rolePermission -> permissionRepository.findById(rolePermission.getPermissionId()))
            .collectList()
            .doOnNext(permissions -> user.setPermissions(permissions.stream().collect(java.util.stream.Collectors.toSet())))
            .then();
    }
    
    public Mono<User> findById(Long id) {
        return userRepository.findByIdAndIsDeletedFalse(id);
    }
    
    @PreAuthorize("hasAuthority('USER_READ') or hasRole('ADMIN')")
    public Flux<UserDto> findAllUsers() {
        return userRepository.findAllByIsDeletedFalse()
                .flatMap(user -> loadUserRoles(user).thenReturn(user))
                .flatMap(this::mapUserToDto);
    }
    
    @PreAuthorize("hasAuthority('USER_READ') or hasRole('ADMIN')")
    public Mono<UserDto> findUserById(Long id) {
        return userRepository.findByIdAndIsDeletedFalse(id)
                .flatMap(user -> loadUserRoles(user).thenReturn(user))
                .flatMap(this::mapUserToDto);
    }

    @PreAuthorize("hasAuthority('USER_WRITE') or hasRole('ADMIN')")
    public Mono<UserDto> createUser(UserCreateRequest request) {

        return existsByUsername(request.getUsername())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new RuntimeException("Username already exists"));
                    }
                    return existsByEmail(request.getEmail());
                })
                .flatMap(existsEmail -> {
                    if (existsEmail) {
                        return Mono.error(new RuntimeException("Email already exists"));
                    }

                    User user = new User();
                    user.setFirstName(request.getFirstName());
                    user.setLastName(request.getLastName());
                    user.setUsername(request.getUsername());
                    user.setPassword(request.getPassword());
                    user.setEmail(request.getEmail());
                    user.setPhoneNumber(request.getPhoneNumber());
                    user.setStatus(com.auth.model.UserStatus.ACTIVE.getValue());
                    user.setIsDeleted(false);
                    user.setCreatedAt(java.time.LocalDateTime.now());
                    user.setUpdatedAt(java.time.LocalDateTime.now());

                    return userRepository.save(user);
                })
                .flatMap(savedUser -> {

                    if (request.getRoles() == null || request.getRoles().isEmpty()) {
                        return Mono.just(savedUser);
                    }

                    return Flux.fromIterable(request.getRoles())
                            .flatMap(roleRepository::findByName)
                            .flatMap(role ->
                                    userRoleRepository.save(new com.auth.model.UserRole(
                                            savedUser.getId(),
                                            role.getId()
                                    ))
                            )
                            .then(Mono.just(savedUser));
                })
                .flatMap(user -> loadUserRoles(user).thenReturn(user))
                .flatMap(this::mapUserToDto);
    }

    @PreAuthorize("hasAuthority('USER_WRITE') and hasRole('ADMIN')")
    public Mono<UserDto> updateUser(Long id, UserDto userDto) {
        return userRepository.findById(id)
            .switchIfEmpty(Mono.error(new RuntimeException("User not found")))
            .flatMap(user -> {

                // Update normal fields
                if (userDto.getFirstName() != null)
                    user.setFirstName(userDto.getFirstName());

                if (userDto.getLastName() != null)
                    user.setLastName(userDto.getLastName());

                if (userDto.getEmail() != null)
                    user.setEmail(userDto.getEmail());

                if (userDto.getPhoneNumber() != null)
                    user.setPhoneNumber(userDto.getPhoneNumber());

                // Update status (enum name only)
                if (userDto.getStatus() != null) {
                    try {
                        com.auth.model.UserStatus statusEnum =
                                com.auth.model.UserStatus.valueOf(userDto.getStatus().toUpperCase());
                        user.setStatus(statusEnum.getValue());
                        // If status set to ACTIVE, ensure the user is not marked as deleted
                        if (statusEnum == com.auth.model.UserStatus.ACTIVE) {
                            user.setIsDeleted(false);
                        }
                    } catch (IllegalArgumentException ex) {
                        return Mono.error(new RuntimeException(
                                "Invalid status: " + userDto.getStatus()
                        ));
                    }
                }

                // Soft delete toggle
                if (userDto.getIsDeleted() != null)
                    user.setIsDeleted(userDto.getIsDeleted());

                user.setUpdatedAt(LocalDateTime.now());

                return userRepository.save(user);
            })
            .flatMap(savedUser -> {

                // Update roles if provided
                if (userDto.getRoles() == null || userDto.getRoles().isEmpty()) {
                    return Mono.just(savedUser);
                }

                return userRoleRepository.deleteByUserId(savedUser.getId())
                    .thenMany(Flux.fromIterable(userDto.getRoles())
                        .flatMap(roleName -> roleRepository.findByName(roleName)
                            .switchIfEmpty(Mono.error(new RuntimeException("Role not found: " + roleName)))
                        )
                        .flatMap(role -> 
                            userRoleRepository.save(
                                new com.auth.model.UserRole(savedUser.getId(), role.getId())
                            )
                        )
                    )
                    .then(Mono.just(savedUser));
            })
            .flatMap(user -> loadUserRoles(user).thenReturn(user))
            .flatMap(this::mapUserToDto);
    }
    
    @PreAuthorize("hasAuthority('USER_DELETE') and hasRole('ADMIN')")
    public Mono<Void> deleteUser(Long id) {
        return userRepository.findByIdAndIsDeletedFalse(id)
                .flatMap(user -> {
                    user.setIsDeleted(true);
                    user.setStatus(com.auth.model.UserStatus.DELETED.getValue());
                    user.setUpdatedAt(LocalDateTime.now());
                    return userRepository.save(user);
                })
                .then();
    }
    
    public Mono<Boolean> existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    public Mono<Boolean> existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    public Mono<User> save(User user) {
        return userRepository.save(user);
    }
    
    private Mono<UserDto> mapUserToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setStatusFromValue(user.getStatus());
        dto.setIsDeleted(user.getIsDeleted());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setRolesFromEntities(user.getRoles());
        
        return Mono.just(dto);
    }
}