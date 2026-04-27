package se.hse.assistant_web_editor.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import se.hse.assistant_web_editor.backend.entity.PageVersionEntity;

import java.util.Optional;

@Repository
public interface PageVersionRepository extends JpaRepository<PageVersionEntity, Long> {

    @Query("SELECT MAX(v.versionNumber) FROM PageVersionEntity v WHERE v.page.id = :pageId")
    Optional<Integer> findMaxVersionByPageId(Long pageId);

    Optional<PageVersionEntity> findFirstByPageIdOrderByVersionNumberDesc(Long pageId);

    void deleteAllByPageId(Long pageId);
}
