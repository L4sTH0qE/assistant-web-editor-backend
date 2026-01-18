package se.hse.assistant_web_editor.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportBlockDto {
    private String id;
    private String blockName;
    private String htmlContent;
}
