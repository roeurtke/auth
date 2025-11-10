package com.auth.service;

import com.auth.dto.UserDto;
import com.auth.model.User;
import com.auth.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserService implements ReactiveUserDetailsService {
    
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUsernameWithRoles(username)
                .cast(UserDetails.class)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found: " + username)));
    }
    
    public Mono<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    @PreAuthorize("hasAuthority('USER_READ') or hasRole('ADMIN')")
    public Flux<UserDto> findAllUsers() {
        return userRepository.findAll()
                .flatMap(this::mapUserToDto);
    }
    
    @PreAuthorize("hasAuthority('USER_READ') or hasRole('ADMIN')")
    public Mono<UserDto> findUserById(Long id) {
        return userRepository.findById(id)
                .flatMap(this::mapUserToDto);
    }
    
    @PreAuthorize("hasAuthority('USER_WRITE') and hasRole('ADMIN')")
    public Mono<UserDto> updateUser(Long id, UserDto userDto) {
        return userRepository.findById(id)
                .flatMap(user -> {
                    user.setFirstName(userDto.getFirstName());
                    user.setLastName(userDto.getLastName());
                    user.setEmail(userDto.getEmail());
                    user.setPhoneNumber(userDto.getPhoneNumber());
                    user.setEnabled(userDto.getEnabled());
                    return userRepository.save(user);
                })
                .flatMap(this::mapUserToDto);
    }
    
    @PreAuthorize("hasAuthority('USER_DELETE') and hasRole('ADMIN')")
    public Mono<Void> deleteUser(Long id) {
        return userRepository.deleteById(id);
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
        dto.setEnabled(user.getEnabled());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setRolesFromEntities(user.getRoles());
        
        return Mono.just(dto);
    }
}