package se.hse.assistant_web_editor.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import se.hse.assistant_web_editor.backend.dto.CreatePageRequest;
import se.hse.assistant_web_editor.backend.dto.PageDetailDto;
import se.hse.assistant_web_editor.backend.dto.PageDto;
import se.hse.assistant_web_editor.backend.dto.SaveVersionRequest;
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
    public ResponseEntity<List<PageDto>> getAll(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(pageService.getAllPages(user.getUsername()));
    }

    @GetMapping("/my")
    public ResponseEntity<List<PageDto>> getAllByUser(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(pageService.getAllUserPages(user.getUsername()));
    }

    @PostMapping
    public ResponseEntity<PageDto> create(@RequestBody CreatePageRequest request,
                                          @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(pageService.createPage(request, user.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PageDetailDto> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(pageService.getPageDetails(id));
    }

    @PostMapping("/{id}/save")
    public ResponseEntity<String> saveVersion(@PathVariable Long id,
                                              @RequestBody SaveVersionRequest request) {
        pageService.savePageVersion(id, request);
        return ResponseEntity.ok("Saved");
    }

    @GetMapping("/{id}/export")
    public ResponseEntity<String> export(@PathVariable Long id) {
        return ResponseEntity.ok(htmlExportService.exportHtml(id));
    }
}
