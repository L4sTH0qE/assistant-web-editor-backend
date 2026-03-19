package se.hse.assistant_web_editor.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CopyPageRequest {
    @NotBlank
    private String slug;
}
