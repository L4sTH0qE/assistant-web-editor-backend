package se.hse.assistant_web_editor.backend.controller;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.hse.assistant_web_editor.backend.dto.AnalyticsDto;
import se.hse.assistant_web_editor.backend.service.AnalyticsService;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    @GetMapping
    public ResponseEntity<AnalyticsDto> getAnalytics() {
        return ResponseEntity.ok(analyticsService.getStats());
    }
}
