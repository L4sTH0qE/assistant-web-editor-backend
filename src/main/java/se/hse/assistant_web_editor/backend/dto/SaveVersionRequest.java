package se.hse.assistant_web_editor.backend.dto;

import lombok.Data;
import se.hse.assistant_web_editor.backend.model.BlockData;

import java.util.List;
import java.util.Map;

@Data
public class SaveVersionRequest {
    private String title;
    private Map<String, Object> metadata;
    private List<BlockData> blocks;
    private String slug;
}
