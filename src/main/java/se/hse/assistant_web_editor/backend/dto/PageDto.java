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
    private String ownerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
