package se.hse.assistant_web_editor.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import se.hse.assistant_web_editor.backend.dto.*;
import se.hse.assistant_web_editor.backend.model.BlockData;
import se.hse.assistant_web_editor.backend.service.AutoLinkService;
import se.hse.assistant_web_editor.backend.service.HtmlExportService;
import se.hse.assistant_web_editor.backend.service.PageService;
import se.hse.assistant_web_editor.backend.service.SyncService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pages")
@RequiredArgsConstructor
public class PageController {

    private final PageService pageService;
    private final HtmlExportService htmlExportService;
    private final SyncService syncService;
    private final AutoLinkService autoLinkService;

    /// Endpoint for getting all pages.
    ///
    /// @param userDetails Return value of userDetailsService.
    /// @return ResponseEntity containing List of DTO objects containing pages data.
    @GetMapping
    public ResponseEntity<List<PageDto>> getPages(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(pageService.getAllPages());
    }

    /// Endpoint for getting all pages created by user.
    ///
    /// @param userDetails Return value of userDetailsService.
    /// @return ResponseEntity containing List of DTO objects containing pages data.
    @GetMapping("/my")
    public ResponseEntity<List<PageDto>> getUserPages(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(pageService.getAllUserPages(userDetails.getUsername()));
    }

    /// Endpoint for creating new page.
    ///
    /// @param request     New page meta information.
    /// @param userDetails Return value of userDetailsService.
    /// @return ResponseEntity containing DTO object containing page data.
    @PostMapping
    public ResponseEntity<PageDto> createPage(@RequestBody CreatePageRequest request,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(pageService.createPage(request, userDetails.getUsername()));
    }

    /// Endpoint for getting a page.
    ///
    /// @param id Page id.
    /// @return ResponseEntity containing DTO object containing full page data.
    @GetMapping("/{id}")
    public ResponseEntity<PageDetailDto> getPage(@PathVariable Long id) {
        return ResponseEntity.ok(pageService.getPageDetails(id));
    }

    /// Endpoint for deleting a page.
    ///
    /// @param id Page id.
    /// @return ResponseEntity containing deleting status.
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePage(@PathVariable Long id) {
        pageService.deletePage(id);
        return ResponseEntity.ok("Deleted");
    }

    /// Endpoint for updating page meta information.
    ///
    /// @param id      Page id.
    /// @param request Page new meta information.
    /// @return ResponseEntity containing DTO object containing page data.
    @PutMapping("/{id}")
    public ResponseEntity<PageDto> updatePageMeta(@PathVariable Long id, @RequestBody CreatePageRequest request) {
        return ResponseEntity.ok(pageService.updatePageMeta(id, request));
    }

    /// Endpoint for cloning a page.
    ///
    /// @param id          Page id.
    /// @param request     Page slug.
    /// @param userDetails Return value of userDetailsService.
    /// @return ResponseEntity containing DTO object containing page data.
    @PostMapping("/{id}/duplicate")
    public ResponseEntity<PageDto> duplicatePage(@PathVariable Long id, @RequestBody CopyPageRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(pageService.duplicatePage(id, userDetails.getUsername(), request.getSlug()));
    }

    /// Endpoint for saving new page version.
    ///
    /// @param id      Page id.
    /// @param request Page new version data.
    /// @return ResponseEntity containing saving status.
    @PostMapping("/{id}/save")
    public ResponseEntity<String> savePageVersion(@PathVariable Long id,
                                                  @RequestBody SaveVersionRequest request) {
        pageService.savePageVersion(id, request);
        return ResponseEntity.ok("Saved");
    }

    /// Endpoint for exporting page content as html code.
    ///
    /// @param id Page id.
    /// @return ResponseEntity containing List of DTO objects containing page blocks data.
    @GetMapping("/{id}/export")
    public ResponseEntity<List<ExportBlockDto>> exportFragments(@PathVariable Long id) {
        return ResponseEntity.ok(htmlExportService.exportBlocks(id));
    }

    /// Endpoint for checking synchronization with external url.
    ///
    /// @param id Page id.
    /// @return ResponseEntity containing DTO object with synchronization statuses.
    @PostMapping("/{id}/check-sync")
    public ResponseEntity<SyncReportDto> checkSync(@PathVariable Long id) {
        SyncReportDto report = syncService.checkSync(id);
        return ResponseEntity.ok(report);
    }


    /// Endpoint for applying auto-linking.
    ///
    /// @param id Page id.
    /// @return ResponseEntity containing DTO object containing page detailed data.
    @PostMapping("/{id}/autolink")
    public ResponseEntity<PageDetailDto> applyAutoLinks(@PathVariable Long id) {
        PageDetailDto updatedPage = autoLinkService.processAndSaveAutoLinks(id);
        return ResponseEntity.ok(updatedPage);
    }

    /// Endpoint for getting page version history.
    ///
    /// @param id Page id.
    /// @return ResponseEntity containing List of DTO objects containing page version data.
    @GetMapping("/{id}/history")
    public ResponseEntity<List<PageVersionDto>> getPageHistory(@PathVariable Long id) {
        return ResponseEntity.ok(pageService.getPageHistory(id));
    }

    /// Endpoint for getting page version block data.
    ///
    /// @param versionId Page version id.
    /// @return ResponseEntity containing List of page version block data.
    @GetMapping("/versions/{versionId}")
    public ResponseEntity<List<BlockData>> getVersionBlocks(@PathVariable Long versionId) {
        return ResponseEntity.ok(pageService.getVersionBlocks(versionId));
    }
}
