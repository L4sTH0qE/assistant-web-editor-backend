package se.hse.assistant_web_editor.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreatePageRequest {
    @NotBlank
    private String title;
    private String slug;
}
