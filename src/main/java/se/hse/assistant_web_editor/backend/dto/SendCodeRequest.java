package se.hse.assistant_web_editor.backend.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SendCodeRequest(
        @NotBlank
        @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@(hse\\.ru|edu\\.hse\\.ru)$", message = "Разрешены только домены @hse.ru и @edu.hse.ru")
        String username
) {}
