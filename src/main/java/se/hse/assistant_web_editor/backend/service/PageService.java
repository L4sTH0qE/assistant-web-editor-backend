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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/// Service for handling pages CRUD operations.
@Service
@RequiredArgsConstructor
public class PageService {

    private final PageRepository pageRepository;
    private final UserRepository userRepository;
    private final PageVersionRepository pageVersionRepository;

    /// Get all pages.
    ///
    /// @return List of DTO objects containing pages data.
    public List<PageDto> getAllPages() {
        return pageRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /// Get all pages created by user.
    ///
    /// @param username Owner username.
    /// @return List of DTO objects containing pages data created by user.
    public List<PageDto> getAllUserPages(String username) {
        return pageRepository.findAll().stream()
                .filter(p -> p.getOwner().getUsername().equals(username))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /// Create new page.
    ///
    /// @param request  Page meta information.
    /// @param username Creator username.
    /// @return DTO object containing page data.
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

    /// Delete page.
    ///
    /// @param pageId Page id.
    @Transactional
    public void deletePage(Long pageId) {
        if (!pageRepository.existsById(pageId)) {
            throw new ResourceNotFoundException("Page not found with id: " + pageId);
        }
        pageVersionRepository.deleteAllByPageId(pageId);
        pageRepository.deleteById(pageId);
    }

    /// Update page meta information.
    ///
    /// @param request Page meta information.
    /// @param id      Page id.
    /// @return DTO object containing page data.
    @Transactional
    public PageDto updatePageMeta(Long id, CreatePageRequest request) {
        PageEntity page = pageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found with id: " + id));

        page.setTitle(request.getTitle());

        if (request.getSlug() != null && !request.getSlug().equals(page.getSlug())) {
            if (pageRepository.findBySlug(request.getSlug()).isPresent()) {
                throw new IllegalArgumentException("Page with slug '" + request.getSlug() + "' already exists");
            } else {
                page.setSlug(request.getSlug());
            }
        }

        return mapToDto(pageRepository.save(page));
    }

    /// Update page sync status
    ///
    /// @param id               Page id.
    /// @param syncStatus       Page sync status.
    /// @param lastSyncCheck    Page last sync check.
    @Transactional
    public void updateSyncStatus(Long id, String syncStatus, LocalDateTime lastSyncCheck) {
        PageEntity page = pageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found with id: " + id));
        page.setSyncStatus(syncStatus);
        page.setLastSyncCheck(lastSyncCheck);
        pageRepository.save(page);
    }

    /// Clone page.
    ///
    /// @param username Creator username.
    /// @param sourceId Source page id.
    /// @return DTO object containing page data.
    @Transactional
    public PageDto duplicatePage(Long sourceId, String username, String slug) {
        PageEntity source = pageRepository.findById(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found with id: " + sourceId));

        PageVersionEntity sourceVersion = pageVersionRepository.findFirstByPageIdOrderByVersionNumberDesc(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Source page is empty"));

        UserEntity currentUser = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (pageRepository.findBySlug(slug).isPresent()) {
            throw new IllegalArgumentException("Page with slug '" + slug + "' already exists");
        }

        PageEntity newPage = PageEntity.builder()
                .title("Копия " + source.getTitle())
                .type(source.getType())
                .slug(slug)
                .owner(currentUser)
                .build();
        newPage = pageRepository.save(newPage);

        PageVersionEntity newVersion = PageVersionEntity.builder()
                .page(newPage)
                .versionNumber(1)
                .structure(sourceVersion.getStructure())
                .build();
        pageVersionRepository.save(newVersion);

        return mapToDto(newPage);
    }

    /// Retrieve full page data.
    ///
    /// @param pageId Page id.
    /// @return DTO object containing full page data.
    public PageDetailDto getPageDetails(Long pageId) {
        PageEntity page = pageRepository.findById(pageId)
                .orElseThrow(() -> new ResourceNotFoundException("Страница не найдена: " + pageId));

        PageVersionEntity latestVersion = pageVersionRepository.findFirstByPageIdOrderByVersionNumberDesc(pageId)
                .orElseThrow(() -> new ResourceNotFoundException("Не найдено ни одной версии для страницы: " + pageId));

        List<BlockData> safeBlocks = latestVersion.getStructure() != null ? latestVersion.getStructure() : java.util.List.of();
        java.util.Map<String, Object> safeMetadata = page.getMetadata() != null ? page.getMetadata() : java.util.Map.of();
        String safeSyncStatus = (page.getSyncStatus() == null || page.getSyncStatus().isEmpty()) ? "DRAFT" : page.getSyncStatus();

        return PageDetailDto.builder()
                .id(page.getId())
                .title(page.getTitle())
                .slug(page.getSlug())
                .type(page.getType())
                .syncStatus(safeSyncStatus)
                .currentVersion(latestVersion.getVersionNumber())
                .blocks(safeBlocks)
                .metadata(safeMetadata)
                .build();
    }

    /// Retrieve page latest version. (Псевдоним для getPageDetails)
    ///
    /// @param pageId Page id.
    /// @return DTO object containing full page data.
    public PageDetailDto findLatestEntity(Long pageId) {
        return getPageDetails(pageId);
    }

    /// Save page new version.
    ///
    /// @param pageId  Page id.
    /// @param request Page blocks data.
    @Transactional
    public void savePageVersion(Long pageId, SaveVersionRequest request) {
        PageEntity page = pageRepository.findById(pageId)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found with id: " + pageId));

        if (request.getTitle() != null) {
            page.setTitle(request.getTitle());
        }
        if (request.getMetadata() != null) {
            page.setMetadata(request.getMetadata());
        }

        if (Objects.equals(page.getSyncStatus(), "SYNCED")) {
            page.setSyncStatus("DESYNCED");
        }

        if (request.getSlug() != null && !request.getSlug().isBlank() && !request.getSlug().equals(page.getSlug())) {
            if (pageRepository.findBySlug(request.getSlug()).isPresent()) {
                throw new IllegalArgumentException("Путь '" + request.getSlug() + "' уже занят другой страницей");
            }
            page.setSlug(request.getSlug());
        }

        page.setUpdatedAt(java.time.LocalDateTime.now());

        Integer lastVer = pageVersionRepository.findMaxVersionByPageId(pageId).orElse(0);
        createVersion(page, request.getBlocks(), lastVer + 1);

        pageRepository.save(page);
    }

    /// Create page new version.
    ///
    /// @param page    Page entity.
    /// @param blocks  Page blocks data.
    /// @param version Page version id.
    private void createVersion(PageEntity page, List<BlockData> blocks, int version) {
        PageVersionEntity newVersion = PageVersionEntity.builder()
                .page(page)
                .versionNumber(version)
                .structure(blocks)
                .build();
        pageVersionRepository.save(newVersion);
    }

    /// Convert page entity to page dto.
    ///
    /// @param entity Page entity.
    /// @return DTO object containing page data.
    private PageDto mapToDto(PageEntity entity) {
        String safeSyncStatus = (entity.getSyncStatus() == null || entity.getSyncStatus().isBlank())
                ? "DRAFT"
                : entity.getSyncStatus();

        return PageDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .slug(entity.getSlug())
                .type(entity.getType())
                .ownerName(entity.getOwner().getUsername())
                .syncStatus(safeSyncStatus)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
