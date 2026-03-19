package se.hse.assistant_web_editor.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.hse.assistant_web_editor.backend.dto.TaxonomyCreateRequest;
import se.hse.assistant_web_editor.backend.dto.TaxonomyItemDto;
import se.hse.assistant_web_editor.backend.dto.TaxonomyResponseDto;
import se.hse.assistant_web_editor.backend.service.TaxonomyService;

@RestController
@RequestMapping("/api/v1/taxonomy")
@RequiredArgsConstructor
public class TaxonomyController {

    private final TaxonomyService taxonomyService;

    @GetMapping
    public ResponseEntity<TaxonomyResponseDto> getTaxonomies() {
        return ResponseEntity.ok(taxonomyService.getAllTaxonomiesGrouped());
    }

    @PostMapping
    public ResponseEntity<TaxonomyItemDto> createTaxonomy(@RequestBody @Valid TaxonomyCreateRequest request) {
        TaxonomyItemDto created = taxonomyService.addTaxonomy(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTaxonomy(@PathVariable Long id) {
        taxonomyService.deleteTaxonomy(id);
        return ResponseEntity.ok().build();
    }
}
