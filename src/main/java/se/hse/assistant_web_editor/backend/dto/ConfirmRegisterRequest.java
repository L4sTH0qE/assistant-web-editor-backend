package se.hse.assistant_web_editor.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ConfirmRegisterRequest(
        @NotBlank String username,
        @NotBlank
        @Pattern(regexp = "(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[~!?@#$%^&*\\-_+(){}\\[\\]<>/\\\\|\"'.:,]).{8,64}",
                message = "Пароль не соответствует требованиям безопасности")
        String password,
        @NotBlank String code) {
}
