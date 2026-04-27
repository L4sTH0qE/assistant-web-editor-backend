package se.hse.assistant_web_editor.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.hse.assistant_web_editor.backend.entity.FileEntity;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, String> {
}
