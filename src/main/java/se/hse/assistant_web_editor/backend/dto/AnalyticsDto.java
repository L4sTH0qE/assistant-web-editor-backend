package se.hse.assistant_web_editor.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class AnalyticsDto {
    private int totalPages;
    // Распределение по типам (NEWS, BASIC, ANNOUNCEMENT)
    private Map<String, Integer> byType;
    // Распределение по статусам (SYNCED, DESYNC, DRAFT)
    private Map<String, Integer> syncStatuses;
    // Активность редакторов (Логин -> Количество материалов)
    private Map<String, Integer> authorsActive;
    // Топ рубрик
    private Map<String, Integer> rubrics;
    // Топ тегов
    private Map<String, Integer> tags;
}
