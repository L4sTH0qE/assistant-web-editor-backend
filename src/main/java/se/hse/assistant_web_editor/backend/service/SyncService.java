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

            List<String> externalParagraphs = extractGranularParagraphs(extBodyEl);

            String internalTitle = page.getTitle() != null ? page.getTitle() : "";
            List<String> internalParagraphs = new ArrayList<>();

            String annotation = (String) page.getMetadata().get("annotation");
            if (annotation != null && !annotation.isBlank()) internalParagraphs.add(annotation.trim());

            if (page.getBlocks() != null) {
                for (BlockData block : page.getBlocks()) {
                    if ("text".equals(block.getType())) {
                        String html = (String) block.getProps().get("content");
                        if (html != null && !html.isBlank()) {
                            internalParagraphs.addAll(extractGranularParagraphs(Jsoup.parseBodyFragment(html).body()));
                        }
                    } else if ("person".equals(block.getType())) {
                        internalParagraphs.add(((String) block.getProps().get("name")).trim());
                    }
                }
            }

            boolean titleMatches = normalizeStrict(internalTitle).contains(normalizeStrict(externalTitle)) ||
                    normalizeStrict(externalTitle).contains(normalizeStrict(internalTitle));

            List<String> missingOnWebsite = findDiffParagraphs(internalParagraphs, externalParagraphs);
            List<String> extraOnWebsite = findDiffParagraphs(externalParagraphs, internalParagraphs);

            int totalParagraphs = Math.max(internalParagraphs.size(), externalParagraphs.size());
            int similarityPercent = totalParagraphs == 0 ? 100 : (int) Math.round((1.0 - (double) missingOnWebsite.size() / totalParagraphs) * 100);

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

    private List<String> extractGranularParagraphs(Element element) {
        List<String> result = new ArrayList<>();
        if (element == null) return result;

        element.select("figcaption").remove();

        for (Element img : element.select("img")) {
            String alt = img.attr("alt").trim();
            if (!alt.isEmpty()) result.add(alt);
            img.remove();
        }

        for (Element el : element.select("p, h1, h2, h3, h4, h5, h6, li")) {
            String text = el.text().trim();
            if (text.length() > 5 && !text.contains("Подписаться на рассылку") && !text.equals("НИУ ВШЭ") && !text.contains("ОПТИМИЗИРОВАТЬ")) {
                result.add(text);
            }
            el.remove();
        }

        String leftoverText = element.text().trim();
        if (leftoverText.length() > 5 && !leftoverText.contains("Подписаться на рассылку") && !leftoverText.equals("НИУ ВШЭ") && !leftoverText.contains("ОПТИМИЗИРОВАТЬ")) {
            String[] leftovers = leftoverText.split("(?<=[.!?])\\s+");
            for (String piece : leftovers) {
                if (piece.trim().length() > 5) result.add(piece.trim());
            }
        }

        return result.stream()
                .filter(s -> !s.isBlank())
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> findDiffParagraphs(List<String> sourceParagraphs, List<String> targetParagraphs) {
        String targetMerged = targetParagraphs.stream()
                .map(this::normalizeStrict)
                .collect(Collectors.joining(" "));

        List<String> diff = new ArrayList<>();
        for (String source : sourceParagraphs) {
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