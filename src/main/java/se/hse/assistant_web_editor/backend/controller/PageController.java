package se.hse.assistant_web_editor.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import se.hse.assistant_web_editor.backend.dto.*;
import se.hse.assistant_web_editor.backend.service.HtmlExportService;
import se.hse.assistant_web_editor.backend.service.PageService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pages")
@RequiredArgsConstructor
public class PageController {

    private final PageService pageService;
    private final HtmlExportService htmlExportService;

    @GetMapping
    public ResponseEntity<List<PageDto>> getPages(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(pageService.getAllPages(user.getUsername()));
    }

    @GetMapping("/my")
    public ResponseEntity<List<PageDto>> getUserPages(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(pageService.getAllUserPages(user.getUsername()));
    }

    @PostMapping
    public ResponseEntity<PageDto> createPage(@RequestBody CreatePageRequest request,
                                          @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(pageService.createPage(request, user.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PageDetailDto> getPage(@PathVariable Long id) {
        return ResponseEntity.ok(pageService.getPageDetails(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePage(@PathVariable Long id) {
        pageService.deletePage(id);
        return ResponseEntity.ok("Deleted");
    }

    @PutMapping("/{id}")
    public ResponseEntity<PageDto> updatePageMeta(@PathVariable Long id, @RequestBody CreatePageRequest request) {
        return ResponseEntity.ok(pageService.updatePageMeta(id, request));
    }

    @PostMapping("/{id}/duplicate")
    public ResponseEntity<PageDto> duplicatePage(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(pageService.duplicatePage(id, user.getUsername()));
    }

    @PostMapping("/{id}/save")
    public ResponseEntity<String> savePageVersion(@PathVariable Long id,
                                              @RequestBody SaveVersionRequest request) {
        pageService.savePageVersion(id, request);
        return ResponseEntity.ok("Saved");
    }

    @GetMapping("/{id}/export")
    public ResponseEntity<List<ExportBlockDto>> exportFragments(@PathVariable Long id) {
        return ResponseEntity.ok(htmlExportService.exportBlocks(id));
    }
}
