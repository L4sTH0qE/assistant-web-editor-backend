package se.hse.assistant_web_editor.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.hse.assistant_web_editor.backend.dto.CreatePageRequest;
import se.hse.assistant_web_editor.backend.dto.PageDetailDto;
import se.hse.assistant_web_editor.backend.dto.PageDto;
import se.hse.assistant_web_editor.backend.dto.SaveVersionRequest;
import se.hse.assistant_web_editor.backend.entity.PageEntity;
import se.hse.assistant_web_editor.backend.entity.PageVersionEntity;
import se.hse.assistant_web_editor.backend.entity.UserEntity;
import se.hse.assistant_web_editor.backend.exception.ResourceNotFoundException;
import se.hse.assistant_web_editor.backend.model.BlockData;
import se.hse.assistant_web_editor.backend.repository.PageRepository;
import se.hse.assistant_web_editor.backend.repository.PageVersionRepository;
import se.hse.assistant_web_editor.backend.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PageService {

    private final PageRepository pageRepository;
    private final PageVersionRepository versionRepository;
    private final UserRepository userRepository;

    public List<PageDto> getAllPages(String username) {
        return pageRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<PageDto> getAllUserPages(String username) {
        return pageRepository.findAll().stream()
                .filter(p -> p.getOwner().getUsername().equals(username))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PageDto createPage(CreatePageRequest request, String username) {
        UserEntity owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (request.getSlug() != null && pageRepository.findBySlug(request.getSlug()).isPresent()) {
            throw new IllegalArgumentException("Page with slug '" + request.getSlug() + "' already exists");
        }

        PageEntity page = PageEntity.builder()
                .title(request.getTitle())
                .slug(request.getSlug())
                .type(request.getType())
                .owner(owner)
                .build();

        page = pageRepository.save(page);

        createVersion(page, List.of(), 1);

        return mapToDto(page);
    }

    @Transactional
    public void deletePage(Long pageId) {
        if (!pageRepository.existsById(pageId)) {
            throw new ResourceNotFoundException("Page not found with id: " + pageId);
        }
        pageRepository.deleteById(pageId);
    }

    public PageDetailDto getPageDetails(Long pageId) {
        PageEntity page = pageRepository.findById(pageId)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found with id: " + pageId));

        PageVersionEntity latestVersion = versionRepository.findFirstByPageIdOrderByVersionNumberDesc(pageId)
                .orElseThrow(() -> new ResourceNotFoundException("No versions found for page: " + pageId));

        return PageDetailDto.builder()
                .id(page.getId())
                .title(page.getTitle())
                .currentVersion(latestVersion.getVersionNumber())
                .blocks(latestVersion.getStructure())
                .build();
    }

    public PageDetailDto findLatestEntity(Long pageId) {
        return getPageDetails(pageId);
    }

    @Transactional
    public void savePageVersion(Long pageId, SaveVersionRequest request) {
        PageEntity page = pageRepository.findById(pageId)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found with id: " + pageId));

        Integer lastVer = versionRepository.findMaxVersionByPageId(pageId).orElse(0);

        createVersion(page, request.getBlocks(), lastVer + 1);

        page.setUpdatedAt(java.time.LocalDateTime.now());
        pageRepository.save(page);
    }

    private void createVersion(PageEntity page, List<BlockData> blocks, int version) {
        PageVersionEntity newVersion = PageVersionEntity.builder()
                .page(page)
                .versionNumber(version)
                .structure(blocks)
                .build();
        versionRepository.save(newVersion);
    }

    private PageDto mapToDto(PageEntity entity) {
        return PageDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .slug(entity.getSlug())
                .type(entity.getType())
                .ownerName(entity.getOwner().getUsername())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
