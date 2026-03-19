package se.hse.assistant_web_editor.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.hse.assistant_web_editor.backend.dto.AnalyticsDto;
import se.hse.assistant_web_editor.backend.entity.PageEntity;
import se.hse.assistant_web_editor.backend.repository.PageRepository;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final PageRepository pageRepository;

    public AnalyticsDto getStats() {
        List<PageEntity> pages = pageRepository.findAll();

        // 1. Группировка основных полей через Java Streams
        Map<String, Integer> byType = pages.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getType().name(),
                        Collectors.summingInt(e -> 1)
                ));

        Map<String, Integer> syncStatuses = pages.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getSyncStatus() == null || p.getSyncStatus().isEmpty() ? "DRAFT" : p.getSyncStatus(),
                        Collectors.summingInt(e -> 1)
                ));

        Map<String, Integer> authorsActive = pages.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getOwner().getUsername(),
                        Collectors.summingInt(e -> 1)
                ));

        // 2. Парсинг динамических метаданных (JSONB)
        Map<String, Integer> rawTagsCount = new HashMap<>();
        Map<String, Integer> rawRubricCount = new HashMap<>();

        for (PageEntity page : pages) {
            Map<String, Object> meta = page.getMetadata();
            if (meta == null) continue;

            // Безопасное извлечение рубрики
            Object rubricObj = meta.get("rubric");
            if (rubricObj instanceof String rubric && !rubric.trim().isEmpty()) {
                rawRubricCount.merge(rubric, 1, Integer::sum);
            }

            // Безопасное извлечение списка тегов
            Object tagsObj = meta.get("tags");
            if (tagsObj instanceof List<?> tags) {
                for (Object tagObj : tags) {
                    if (tagObj instanceof String tag && !tag.trim().isEmpty()) {
                        rawTagsCount.merge(tag, 1, Integer::sum);
                    }
                }
            }
        }

        // 3. Формирование ответа с сортировкой рубрик и тегов по убыванию (для красивых графиков)
        return AnalyticsDto.builder()
                .totalPages(pages.size())
                .byType(sortByValueDesc(byType))
                .syncStatuses(sortByValueDesc(syncStatuses))
                .authorsActive(sortByValueDesc(authorsActive))
                .rubrics(sortByValueDesc(rawRubricCount))
                .tags(sortByValueDesc(rawTagsCount))
                .build();
    }

    /// Вспомогательный метод для сортировки Map по значению (по убыванию)
    private Map<String, Integer> sortByValueDesc(Map<String, Integer> unsortedMap) {
        return unsortedMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new // LinkedHashMap сохраняет порядок вставки
                ));
    }
}
