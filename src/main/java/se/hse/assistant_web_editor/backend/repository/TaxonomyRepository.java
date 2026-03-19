package se.hse.assistant_web_editor.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.hse.assistant_web_editor.backend.entity.TaxonomyEntity;
import se.hse.assistant_web_editor.backend.entity.TaxonomyType;

@Repository
public interface TaxonomyRepository extends JpaRepository<TaxonomyEntity, Long> {

    // Поиск дубликатов без учета регистра
    boolean existsByTypeAndNameIgnoreCase(TaxonomyType type, String name);
}
