package se.hse.assistant_web_editor.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TaxonomyItemDto {
    private Long id;
    private String name;
}
