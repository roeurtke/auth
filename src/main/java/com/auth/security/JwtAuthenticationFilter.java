package com.auth.security;

import com.auth.service.UserService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author Roeurt Kesei
 * JWT Authentication Filter for extracting and validating JWT tokens from HTTP requests.
 */
@Component
public class JwtAuthenticationFilter implements ServerAuthenticationConverter {
    
    private final JwtService jwtService;
    private final UserService userService;
    
    public JwtAuthenticationFilter(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }
    
    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return extractTokenFromRequest(exchange)
                .filter(token -> jwtService.validateToken(token))
                .flatMap(token -> {
                    String username = jwtService.extractUsername(token);
                    return userService.findByUsername(username)
                            .map(userDetails -> new UsernamePasswordAuthenticationToken(
                                    userDetails, 
                                    token, 
                                    userDetails.getAuthorities()
                            ));
                });
    }
    
    private Mono<String> extractTokenFromRequest(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return Mono.just(authHeader.substring(7));
        }
        
        // Also check query parameter for WebSocket connections
        String tokenParam = exchange.getRequest().getQueryParams().getFirst("token");
        if (tokenParam != null) {
            return Mono.just(tokenParam);
        }
        
        return Mono.empty();
    }
}