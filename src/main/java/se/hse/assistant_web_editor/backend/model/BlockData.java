package se.hse.assistant_web_editor.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlockData implements Serializable {
    private String id;
    private String type;
    private Map<String, Object> props;
}