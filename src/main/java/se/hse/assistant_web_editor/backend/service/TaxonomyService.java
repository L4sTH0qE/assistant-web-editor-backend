package se.hse.assistant_web_editor.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.hse.assistant_web_editor.backend.dto.TaxonomyCreateRequest;
import se.hse.assistant_web_editor.backend.dto.TaxonomyItemDto;
import se.hse.assistant_web_editor.backend.dto.TaxonomyResponseDto;
import se.hse.assistant_web_editor.backend.entity.TaxonomyEntity;
import se.hse.assistant_web_editor.backend.entity.TaxonomyType;
import se.hse.assistant_web_editor.backend.exception.ResourceNotFoundException;
import se.hse.assistant_web_editor.backend.repository.TaxonomyRepository;

import java.util.List;
import java.util.stream.Collectors;

/// Service for taxonomy handling.
@Service
@RequiredArgsConstructor
public class TaxonomyService {

    private final TaxonomyRepository taxonomyRepository;

    public TaxonomyResponseDto getAllTaxonomiesGrouped() {
        List<TaxonomyEntity> all = taxonomyRepository.findAll();

        return TaxonomyResponseDto.builder()
                .rubrics(filterAndMap(all, TaxonomyType.RUBRIC))
                .tags(filterAndMap(all, TaxonomyType.TAG))
                .keywords(filterAndMap(all, TaxonomyType.KEYWORD))
                .build();
    }

    @Transactional
    public TaxonomyItemDto addTaxonomy(TaxonomyCreateRequest request) {
        TaxonomyType type = TaxonomyType.fromString(request.getType());
        String name = request.getName().trim();

        if (taxonomyRepository.existsByTypeAndNameIgnoreCase(type, name)) {
            throw new IllegalArgumentException("Элемент '" + name + "' уже существует в данной категории");
        }

        TaxonomyEntity entity = TaxonomyEntity.builder()
                .type(type)
                .name(name)
                .build();

        entity = taxonomyRepository.save(entity);
        return new TaxonomyItemDto(entity.getId(), entity.getName());
    }

    @Transactional
    public void deleteTaxonomy(Long id) {
        if (!taxonomyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Элемент справочника не найден");
        }
        taxonomyRepository.deleteById(id);
    }

    private List<TaxonomyItemDto> filterAndMap(List<TaxonomyEntity> list, TaxonomyType targetType) {
        return list.stream()
                .filter(entity -> entity.getType() == targetType)
                .map(entity -> new TaxonomyItemDto(entity.getId(), entity.getName()))
                .collect(Collectors.toList());
    }
}
