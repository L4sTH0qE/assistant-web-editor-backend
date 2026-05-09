package se.hse.assistant_web_editor.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.hse.assistant_web_editor.backend.entity.GlossaryTermEntity;

@Repository
public interface GlossaryRepository extends JpaRepository<GlossaryTermEntity, Long> {
    boolean existsByTerm(String term);
}
