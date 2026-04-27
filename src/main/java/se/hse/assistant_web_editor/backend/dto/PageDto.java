package se.hse.assistant_web_editor.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PageDto {
    private Long id;
    private String title;
    private String slug;
    private PageType type;
    private String ownerName;
    private String syncStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
