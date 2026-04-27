package se.hse.assistant_web_editor.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class TaxonomyResponseDto {
    private List<TaxonomyItemDto> rubrics;
    private List<TaxonomyItemDto> tags;
    private List<TaxonomyItemDto> keywords;
}
