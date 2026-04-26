package se.hse.assistant_web_editor.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import se.hse.assistant_web_editor.backend.dto.*;
import se.hse.assistant_web_editor.backend.entity.UserEntity;
import se.hse.assistant_web_editor.backend.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/// Service for handling users authentication and registration.
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final MailService mailService;

    private record VerificationSession(String code, LocalDateTime expiresAt) {}

    private final Map<String, VerificationSession> verificationCodes = new ConcurrentHashMap<>();

    public AuthResponse sendVerificationCode(String email) {
        if (repository.findByUsername(email).isPresent()) {
            return AuthResponse.error("Пользователь с такой почтой уже существует");
        }

        String code = String.format("%06d", new Random().nextInt(999999));

        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(10);
        verificationCodes.put(email, new VerificationSession(code, expiryTime));

        mailService.sendVerificationEmail(email, code);

        return AuthResponse.success("Код подтверждения отправлен на вашу почту");
    }

    public AuthResponse confirmRegistration(ConfirmRegisterRequest request) {
        VerificationSession session = verificationCodes.get(request.username());

        if (session == null) {
            return AuthResponse.error("Код не найден или сессия истекла. Запросите код заново.");
        }

        if (LocalDateTime.now().isAfter(session.expiresAt())) {
            verificationCodes.remove(request.username());
            return AuthResponse.error("Время действия кода истекло. Запросите новый код.");
        }

        if (!session.code().equals(request.code())) {
            return AuthResponse.error("Неверный код подтверждения");
        }

        var user = UserEntity.builder()
                .username(request.username())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.username().split("@")[0])
                .build();
        repository.save(user);

        verificationCodes.remove(request.username());

        return authenticate(new AuthRequest(request.username(), request.password()));
    }

    @Scheduled(fixedRate = 1800000)
    public void cleanupExpiredCodes() {
        LocalDateTime now = LocalDateTime.now();
        verificationCodes.entrySet().removeIf(entry -> now.isAfter(entry.getValue().expiresAt()));
    }

    public AuthResponse authenticate(AuthRequest request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (AuthenticationException e) {
            return AuthResponse.error("Неправильная почта или пароль");
        }
        var userDetails = userDetailsService.loadUserByUsername(request.username());
        var jwtToken = jwtService.generateToken(userDetails);
        return AuthResponse.success(jwtToken);
    }

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

    public AuthResponse sendPasswordResetCode(String email) {
        if (repository.findByUsername(email).isEmpty()) {
            return AuthResponse.error("Пользователь с такой почтой не найден");
        }

        String code = String.format("%06d", new Random().nextInt(999999));
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(10);
        verificationCodes.put("RESET_" + email, new VerificationSession(code, expiryTime));

        mailService.sendVerificationEmail(email, code);
        return AuthResponse.success("Код для сброса пароля отправлен на почту");
    }

    public AuthResponse confirmPasswordReset(PasswordResetConfirmRequest request) {
        String sessionKey = "RESET_" + request.email();
        VerificationSession session = verificationCodes.get(sessionKey);

        if (session == null || LocalDateTime.now().isAfter(session.expiresAt())) {
            return AuthResponse.error("Код не найден или истек. Запросите новый код.");
        }

        if (!session.code().equals(request.code())) {
            return AuthResponse.error("Неверный код подтверждения");
        }

        UserEntity user = repository.findByUsername(request.email()).orElseThrow();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        repository.save(user);

        verificationCodes.remove(sessionKey);

        return authenticate(new AuthRequest(request.email(), request.newPassword()));
    }
}
