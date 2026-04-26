package se.hse.assistant_web_editor.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import se.hse.assistant_web_editor.backend.dto.*;
import se.hse.assistant_web_editor.backend.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Log4j2
public class AuthController {

    private final AuthService authService;

    /// Endpoint for sending code to email for user registration.
    ///
    /// @param request DTO object containing registration data.
    /// @return ResponseEntity containing the registration response.
    @PostMapping("/register/send-code")
    public ResponseEntity<AuthResponse> sendCode(@RequestBody @Valid SendCodeRequest request) {
        AuthResponse resp = authService.sendVerificationCode(request.username());
        if (!resp.success()) return ResponseEntity.badRequest().body(resp);
        return ResponseEntity.ok(resp);
    }

    /// Endpoint for user registration.
    ///
    /// @param request DTO object containing registration data.
    /// @return ResponseEntity containing the registration response.
    @PostMapping("/register/confirm")
    public ResponseEntity<AuthResponse> confirmRegister(@RequestBody ConfirmRegisterRequest request) {
        AuthResponse resp = authService.confirmRegistration(request);
        if (!resp.success()) return ResponseEntity.badRequest().body(resp);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    /// Endpoint for user authentication.
    ///
    /// @param request DTO object containing login data.
    /// @return ResponseEntity containing the login response.
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticate(@RequestBody @Valid AuthRequest request, BindingResult result) {

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(AuthResponse.error("Неверная почта или пароль"));
        }

        AuthResponse authResponse = authService.authenticate(request);
        if (authResponse.success()) {
            return ResponseEntity.ok(authResponse);
        } else {
            return ResponseEntity.badRequest().body(authResponse);
        }
    }

    /// Endpoint for getting user info by token.
    ///
    /// @param userDetails Return value of userDetailsService.
    /// @return ResponseEntity containing user info.
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {

        UserDto userDto = authService.getUserInfo(userDetails.getUsername());

        return ResponseEntity.ok(userDto);
    }

    /// Endpoint for sending code to email for user authorization.
    ///
    /// @param request DTO object containing password reset request.
    /// @return ResponseEntity containing the login response.
    @PostMapping("/password-reset/send-code")
    public ResponseEntity<AuthResponse> requestPasswordReset(@RequestBody @Valid PasswordResetRequest request) {
        AuthResponse resp = authService.sendPasswordResetCode(request.email());
        if (!resp.success()) return ResponseEntity.badRequest().body(resp);
        return ResponseEntity.ok(resp);
    }

    /// Endpoint for user authorization after changing password.
    ///
    /// @param request DTO object containing new password.
    /// @return ResponseEntity containing the login response.
    @PostMapping("/password-reset/confirm")
    public ResponseEntity<AuthResponse> confirmPasswordReset(@RequestBody @Valid PasswordResetConfirmRequest request, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(AuthResponse.error("Пароль не соответствует требованиям безопасности (8-64 символа, заглавная, строчная, цифра, спецсимвол)"));
        }
        AuthResponse resp = authService.confirmPasswordReset(request);
        if (!resp.success()) return ResponseEntity.badRequest().body(resp);
        return ResponseEntity.ok(resp);
    }
}
