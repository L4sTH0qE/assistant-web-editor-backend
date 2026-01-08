package se.hse.assistant_web_editor.backend.dto;

import lombok.Data;
import se.hse.assistant_web_editor.backend.model.BlockData;

import java.util.List;

@Data
public class SaveVersionRequest {
    private List<BlockData> blocks;
}
