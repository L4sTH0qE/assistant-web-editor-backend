package se.hse.assistant_web_editor.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TaxonomyCreateRequest {
    @NotBlank(message = "Тип не может быть пустым (rubric, tag, keyword)")
    private String type;

    @NotBlank(message = "Название не может быть пустым")
    private String name;
}
