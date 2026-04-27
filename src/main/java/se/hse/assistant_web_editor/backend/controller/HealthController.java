package se.hse.assistant_web_editor.backend.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/health")
public class HealthController {


    @GetMapping
    public ResponseEntity<String> checkHealth() {
        return ResponseEntity.ok("OK");
    }
}
