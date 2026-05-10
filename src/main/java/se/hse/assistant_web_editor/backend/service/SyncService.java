package se.hse.assistant_web_editor.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import se.hse.assistant_web_editor.backend.dto.PageDetailDto;
import se.hse.assistant_web_editor.backend.dto.SyncReportDto;
import se.hse.assistant_web_editor.backend.model.BlockData;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/// Service for synchronization.
@Service
@RequiredArgsConstructor
@Slf4j
public class SyncService {

    private final PageService pageService;

    public SyncReportDto checkSync(Long pageId) {
        PageDetailDto page = pageService.findLatestEntity(pageId);
        String externalUrl = (String) page.getMetadata().get("externalUrl");

        if (externalUrl == null || externalUrl.isBlank()) {
            pageService.updateSyncStatus(pageId, "DRAFT", LocalDateTime.now());
            return SyncReportDto.builder().status("DRAFT").build();
        }

        if (!externalUrl.startsWith("http://") && !externalUrl.startsWith("https://")) {
            externalUrl = "https://" + externalUrl;
        }

        try {
            Document doc = Jsoup.connect(externalUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .timeout(7000)
                    .get();

            doc.select("script, style, noscript, .ya-share2, .articleMeta, .header-board, .footer, .pk-menu, .header-top").remove();
            doc.select(".fotorama--hidden, .fotorama__nav-wrap, .fotorama__caption, .fotorama__copyright, .fotorama-bottom_caption").remove();

            String externalTitle = doc.select("h1").text();
            Element extBodyEl = doc.selectFirst(".post__text");
            if (extBodyEl == null) extBodyEl = doc.selectFirst(".post__content, .builder-section, .tab-content");

            List<String> externalBlocks = extractBlocksSafely(extBodyEl);

            String internalTitle = page.getTitle() != null ? page.getTitle() : "";
            List<String> internalBlocks = new ArrayList<>();

            String annotation = (String) page.getMetadata().get("annotation");
            if (annotation != null && !annotation.isBlank()) internalBlocks.add(annotation.trim());

            if (page.getBlocks() != null) {
                for (BlockData block : page.getBlocks()) {
                    if ("text".equals(block.getType())) {
                        String html = (String) block.getProps().get("content");
                        if (html != null && !html.isBlank()) {
                            internalBlocks.addAll(extractBlocksSafely(Jsoup.parseBodyFragment(html).body()));
                        }
                    } else if ("person".equals(block.getType())) {
                        internalBlocks.add(((String) block.getProps().get("name")).trim());
                    }
                }
            }

            boolean titleMatches = normalizeStrict(internalTitle).contains(normalizeStrict(externalTitle)) ||
                    normalizeStrict(externalTitle).contains(normalizeStrict(internalTitle));

            List<String> missingOnWebsite = findDiffBlocks(internalBlocks, externalBlocks);
            List<String> extraOnWebsite = findDiffBlocks(externalBlocks, internalBlocks);

            int totalBlocks = Math.max(internalBlocks.size(), externalBlocks.size());
            int similarityPercent = totalBlocks == 0 ? 100 : (int) Math.round((1.0 - (double) missingOnWebsite.size() / totalBlocks) * 100);

            String status = (titleMatches && similarityPercent >= 90 && missingOnWebsite.isEmpty()) ? "SYNCED" : "DESYNCED";
            pageService.updateSyncStatus(pageId, status, LocalDateTime.now());

            if ("SYNCED".equals(status)) pageService.markCurrentVersionAsSynced(pageId);

            return SyncReportDto.builder()
                    .status(status)
                    .titleMatch(titleMatches)
                    .similarityPercent(Math.max(0, Math.min(100, similarityPercent)))
                    .missingOnWebsite(missingOnWebsite)
                    .extraOnWebsite(extraOnWebsite)
                    .build();

        } catch (Exception e) {
            pageService.updateSyncStatus(pageId, "DESYNCED", LocalDateTime.now());
            return SyncReportDto.builder().status("DESYNCED").build();
        }
    }

    private List<String> extractBlocksSafely(Element element) {
        List<String> result = new ArrayList<>();
        if (element == null) return result;

        for (Element img : element.select("img")) {
            String alt = img.attr("alt").trim();
            if (!alt.isEmpty()) result.add(alt);
            img.remove();
        }
        for (Element el : element.select("p, h1, h2, h3, h4, h5, h6, li")) {
            String text = el.text().trim();
            if (text.length() > 5 && !text.contains("Подписаться на рассылку") && !text.equals("НИУ ВШЭ")) {
                result.add(text);
            }
            el.remove();
        }

        String leftoverText = element.text().trim();
        if (leftoverText.length() > 5 && !leftoverText.contains("Подписаться на рассылку")) {
            result.add(leftoverText);
        }

        return result.stream()
                .filter(s -> !s.isBlank())
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> findDiffBlocks(List<String> sourceBlocks, List<String> targetBlocks) {
        String targetMerged = targetBlocks.stream()
                .map(this::normalizeStrict)
                .collect(Collectors.joining(" "));

        List<String> diff = new ArrayList<>();
        for (String source : sourceBlocks) {
            String normSource = normalizeStrict(source);
            if (!targetMerged.contains(normSource)) {
                diff.add(source);
            }
        }
        return diff;
    }

    private String normalizeStrict(String text) {
        return text.replaceAll("[^a-zA-Zа-яА-ЯёЁ0-9]", "").toLowerCase();
    }
}