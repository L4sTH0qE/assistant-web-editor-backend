package se.hse.assistant_web_editor.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import se.hse.assistant_web_editor.backend.dto.AuthRequest;
import se.hse.assistant_web_editor.backend.dto.AuthResponse;
import se.hse.assistant_web_editor.backend.dto.ConfirmRegisterRequest;
import se.hse.assistant_web_editor.backend.dto.UserDto;
import se.hse.assistant_web_editor.backend.entity.UserEntity;
import se.hse.assistant_web_editor.backend.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/// Service for handling users authentication and registration logic.
@Service
@RequiredArgsConstructor
@Log4j2
public class AuthService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final MailService mailService;

    private record VerificationSession(String code, LocalDateTime expiresAt) {}

    private final Map<String, VerificationSession> verificationCodes = new ConcurrentHashMap<>();

    /// Отправка кода и запуск таймера
    public AuthResponse sendVerificationCode(String email) {
        if (repository.findByUsername(email).isPresent()) {
            return AuthResponse.error("Пользователь с таким email уже существует");
        }

        String code = String.format("%06d", new Random().nextInt(999999));

        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(10);
        verificationCodes.put(email, new VerificationSession(code, expiryTime));

        mailService.sendVerificationEmail(email, code);

        return AuthResponse.success("Код подтверждения отправлен на вашу почту");
    }

    /// Проверка кода и создание аккаунта
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

    /// Очистка памяти: Каждые 30 минут удаляем просроченные сессии из ConcurrentHashMap
    @Scheduled(fixedRate = 1800000)
    public void cleanupExpiredCodes() {
        log.info("Running cleanup task for expired registration codes...");
        LocalDateTime now = LocalDateTime.now();
        verificationCodes.entrySet().removeIf(entry -> now.isAfter(entry.getValue().expiresAt()));
    }

    public AuthResponse authenticate(AuthRequest request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (AuthenticationException e) {
            return AuthResponse.error("Неправильный почта или пароль");
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
}
