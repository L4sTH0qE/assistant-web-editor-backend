package se.hse.assistant_web_editor.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(@NotBlank(message = "Логин не должен быть пустой строкой")
                          String username,
                          @NotBlank(message = "Пароль не должен быть пустой строкой")
                          String password) {
}
