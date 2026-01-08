package se.hse.assistant_web_editor.backend.dto;

import lombok.Builder;
import lombok.Data;
import se.hse.assistant_web_editor.backend.model.BlockData;

import java.util.List;

@Data
@Builder
public class PageDetailDto {
    private Long id;
    private String title;
    private Integer currentVersion;
    private List<BlockData> blocks;
}
