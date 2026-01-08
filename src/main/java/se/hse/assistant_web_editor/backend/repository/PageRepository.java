package se.hse.assistant_web_editor.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.hse.assistant_web_editor.backend.entity.PageEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<PageEntity, Long> {

    List<PageEntity> findAllByOwnerUsername(String username);

    Optional<PageEntity> findBySlug(String slug);
}
