package com.autoflow.security;

import com.autoflow.security.dto.LoginRequest;
import com.autoflow.security.dto.LoginResponse;
import com.autoflow.security.dto.UserResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

/**
 * Handles login (verifying credentials and issuing a token) and looking up the
 * current user. Controllers never touch the repository or the security beans
 * directly.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (AuthenticationException e) {
            log.info("Failed login attempt for username '{}'", request.username());
            throw new BadCredentialsException("Feil brukernavn eller passord");
        }

        User user = userRepository.findByUsernameIgnoreCase(request.username()).orElseThrow();
        List<String> roles = roleNames(user);
        String token = jwtService.generateToken(user.getUsername(), roles);
        log.info("User '{}' logged in", user.getUsername());
        return new LoginResponse(token, jwtService.getExpirationMs(),
                user.getUsername(), user.getFullName(), roles);
    }

    public UserResponse currentUser(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username).orElseThrow();
        return new UserResponse(user.getId(), user.getUsername(), user.getFullName(), roleNames(user));
    }

    private List<String> roleNames(User user) {
        return user.getRoles().stream().map(Enum::name).sorted().toList();
    }
}
