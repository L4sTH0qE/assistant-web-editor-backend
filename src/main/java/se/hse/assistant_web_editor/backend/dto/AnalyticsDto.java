package se.hse.assistant_web_editor.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class AnalyticsDto {

    private int totalPages;

    private Map<String, Integer> byType;

    private Map<String, Integer> syncStatuses;

    private Map<String, Integer> authorsActive;

    private Map<String, Integer> rubrics;

    private Map<String, Integer> tags;
}
