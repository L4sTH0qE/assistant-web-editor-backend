package se.hse.assistant_web_editor.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class PageVersionDto {
    private Long id;
    private Integer versionNumber;
    private LocalDateTime createdAt;
    private boolean isPublished;
}
