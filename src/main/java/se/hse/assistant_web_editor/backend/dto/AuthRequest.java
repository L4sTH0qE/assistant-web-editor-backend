package se.hse.assistant_web_editor.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(@NotBlank(message = "username must be not blank string")
                          String username,
                          @NotBlank(message = "password must be not blank string")
                          String password) {
}
