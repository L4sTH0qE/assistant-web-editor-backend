package se.hse.assistant_web_editor.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import se.hse.assistant_web_editor.backend.entity.FileEntity;
import se.hse.assistant_web_editor.backend.exception.ResourceNotFoundException;
import se.hse.assistant_web_editor.backend.repository.FileRepository;

import java.io.IOException;

/// Service for files handling.
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;

    public FileEntity store(MultipartFile file) throws IOException {
        FileEntity fileEntity = FileEntity.builder()
                .filename(file.getOriginalFilename())
                .contentType(file.getContentType())
                .data(file.getBytes())
                .build();
        return fileRepository.save(fileEntity);
    }

    public FileEntity getFile(String id) {
        return fileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Файл не найден: " + id));
    }
}
