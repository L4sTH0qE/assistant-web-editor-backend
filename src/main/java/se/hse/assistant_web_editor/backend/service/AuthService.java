package se.hse.assistant_web_editor.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import se.hse.assistant_web_editor.backend.dto.AuthRequest;
import se.hse.assistant_web_editor.backend.dto.AuthResponse;
import se.hse.assistant_web_editor.backend.dto.UserDto;
import se.hse.assistant_web_editor.backend.entity.UserEntity;
import se.hse.assistant_web_editor.backend.repository.UserRepository;

/// Service for handling users authentication and registration logic.
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;

    /// Register new client.
    ///
    /// @param request DTO object containing registration data.
    /// @return DTO object containing token to authorize with or error message.
    public AuthResponse register(AuthRequest request) {

        if (repository.findByUsername(request.username()).isPresent()) {
            return AuthResponse.error("Логин уже занят");
        }

        var user = UserEntity.builder()
                .username(request.username())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.username())
                .build();
        repository.save(user);

        return authenticate(request);
    }

    /// Login existing client.
    ///
    /// @param request DTO object containing authentication data.
    /// @return DTO object containing token to authorize with or error message.
    public AuthResponse authenticate(AuthRequest request) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
        } catch (AuthenticationException e) {
            return AuthResponse.error("Неправильный логин или пароль");
        }
        var userDetails = userDetailsService.loadUserByUsername(request.username());
        var jwtToken = jwtService.generateToken(userDetails);

        return AuthResponse.success(jwtToken);
    }

    /// Retrieve user info by username.
    ///
    /// @param username Username to get user info by.
    /// @return DTO object representing user info or Error message.
    public UserDto getUserInfo(String username) {

        var user = repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role("Editor")
                .build();
    }
}
