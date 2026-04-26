package se.hse.assistant_web_editor.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequest(@NotBlank String email) {}
