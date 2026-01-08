package se.hse.assistant_web_editor.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import se.hse.assistant_web_editor.backend.dto.AuthRequest;
import se.hse.assistant_web_editor.backend.dto.AuthResponse;
import se.hse.assistant_web_editor.backend.dto.UserDto;
import se.hse.assistant_web_editor.backend.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /// Endpoint for user registration.
    ///
    /// @param request DTO object containing registration data.
    /// @return ResponseEntity containing the registration response.
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid AuthRequest request, BindingResult result) {

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(AuthResponse.error("Invalid username or password"));
        }

        AuthResponse authResponse = authService.register(request);
        if (authResponse.success()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
        } else {
            return ResponseEntity.badRequest().body(authResponse);
        }
    }

    /// Endpoint for user authentication.
    ///
    /// @param request DTO object containing login data.
    /// @return ResponseEntity containing the login response.
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticate(@RequestBody @Valid AuthRequest request, BindingResult result) {

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(AuthResponse.error("Invalid username or password"));
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
}
