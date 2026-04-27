package se.hse.assistant_web_editor.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private Long id;
    private String username;
    private String fullName;
    private String role;
}