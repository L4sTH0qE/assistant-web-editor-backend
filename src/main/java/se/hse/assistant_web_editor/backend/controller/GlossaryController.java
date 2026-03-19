package se.hse.assistant_web_editor.backend.controller;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.hse.assistant_web_editor.backend.entity.GlossaryTermEntity;
import se.hse.assistant_web_editor.backend.repository.GlossaryRepository;
import java.util.List;

@RestController
@RequestMapping("/api/v1/glossary")
@RequiredArgsConstructor
public class GlossaryController {
    private final GlossaryRepository glossaryRepository;

    @GetMapping
    public ResponseEntity<List<GlossaryTermEntity>> getAll() {
        return ResponseEntity.ok(glossaryRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<GlossaryTermEntity> add(@RequestBody GlossaryTermEntity term) {
        return ResponseEntity.ok(glossaryRepository.save(term));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        glossaryRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
