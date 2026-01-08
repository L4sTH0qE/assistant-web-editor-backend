package se.hse.assistant_web_editor.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AuthRequest(@NotBlank(message = "username must be not blank string")
                          @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@hse\\.ru$",  // регулярное выражение
                              message = "username must be a valid email in format login@hse.ru")
                      String username,
                          @NotBlank(message = "password must be not blank string")
                          @Pattern(regexp = "(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[~!?@#$%^&*\\-_+(){}\\[\\]<>/\\\\|\"'.:,]).{8,64}",
                              message = "password must be between 8-64 characters and contain uppercase letter, lowercase letter, number and special character")
                      String password) {
}
