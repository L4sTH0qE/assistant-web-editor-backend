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

/// Service for handling pages CRUD operations.
@Service
@RequiredArgsConstructor
public class PageService {

    private final PageRepository pageRepository;
    private final PageVersionRepository versionRepository;
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
    /// @param request Page meta information.
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
    /// @param id Page id.
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

    /// Clone page.
    ///
    /// @param username Creator username.
    /// @param sourceId Source page id.
    /// @return DTO object containing page data.
    @Transactional
    public PageDto duplicatePage(Long sourceId, String username) {
        PageEntity source = pageRepository.findById(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found with id: " + sourceId));

        PageVersionEntity sourceVersion = versionRepository.findFirstByPageIdOrderByVersionNumberDesc(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Source page is empty"));

        UserEntity currentUser = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        PageEntity newPage = PageEntity.builder()
                .title("Копия " + source.getTitle())
                .type(source.getType())
                .slug(null)
                .owner(currentUser)
                .build();
        newPage = pageRepository.save(newPage);

        PageVersionEntity newVersion = PageVersionEntity.builder()
                .page(newPage)
                .versionNumber(1)
                .structure(sourceVersion.getStructure())
                .build();
        versionRepository.save(newVersion);

        return mapToDto(newPage);
    }

    /// Retrieve full page data.
    ///
    /// @param pageId Page id.
    /// @return DTO object containing full page data.
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

    /// Retrieve page latest version.
    ///
    /// @param pageId Page id.
    /// @return DTO object containing full page data.
    public PageDetailDto findLatestEntity(Long pageId) {
        return getPageDetails(pageId);
    }

    /// Save page new version.
    ///
    /// @param pageId Page id.
    /// @param request Page blocks data.
    @Transactional
    public void savePageVersion(Long pageId, SaveVersionRequest request) {
        PageEntity page = pageRepository.findById(pageId)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found with id: " + pageId));

        Integer lastVer = versionRepository.findMaxVersionByPageId(pageId).orElse(0);

        createVersion(page, request.getBlocks(), lastVer + 1);

        page.setUpdatedAt(java.time.LocalDateTime.now());
        pageRepository.save(page);
    }

    /// Create page new version.
    ///
    /// @param page Page entity.
    /// @param blocks Page blocks data.
    /// @param version Page version id.
    private void createVersion(PageEntity page, List<BlockData> blocks, int version) {
        PageVersionEntity newVersion = PageVersionEntity.builder()
                .page(page)
                .versionNumber(version)
                .structure(blocks)
                .build();
        versionRepository.save(newVersion);
    }

    /// Convert page entity to page dto.
    ///
    /// @param entity Page entity.
    /// @return DTO object containing page data.
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
