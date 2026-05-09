package se.hse.assistant_web_editor.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.hse.assistant_web_editor.backend.dto.TaxonomyCreateRequest;
import se.hse.assistant_web_editor.backend.dto.TaxonomyItemDto;
import se.hse.assistant_web_editor.backend.dto.TaxonomyResponseDto;
import se.hse.assistant_web_editor.backend.entity.GlossaryTermEntity;
import se.hse.assistant_web_editor.backend.entity.TaxonomyEntity;
import se.hse.assistant_web_editor.backend.entity.TaxonomyType;
import se.hse.assistant_web_editor.backend.exception.ResourceNotFoundException;
import se.hse.assistant_web_editor.backend.repository.GlossaryRepository;
import se.hse.assistant_web_editor.backend.repository.TaxonomyRepository;

import java.util.List;
import java.util.stream.Collectors;

/// Service for glossary handling.
@Service
@RequiredArgsConstructor
public class GlossaryService {

    private final GlossaryRepository glossaryRepository;

    public List<GlossaryTermEntity> getAll() {
        return glossaryRepository.findAll();
    }

    @Transactional
    public GlossaryTermEntity addTerm(GlossaryTermEntity termEntity) {
        String term = termEntity.getTerm();

        if (glossaryRepository.existsByTerm(term)) {
            throw new IllegalArgumentException("Термин '" + term + "' уже существует в глоссарии");
        }

        return glossaryRepository.save(termEntity);
    }

    @Transactional
    public void deleteTerm(Long id) {
        if (!glossaryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Термин отсутствует в глоссарии");
        }
        glossaryRepository.deleteById(id);
    }
}
