package se.hse.assistant_web_editor.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import se.hse.assistant_web_editor.backend.dto.PageDetailDto;
import se.hse.assistant_web_editor.backend.dto.SyncReportDto;
import se.hse.assistant_web_editor.backend.model.BlockData;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(7000)
                    .get();

            doc.select("script, style, noscript, .ya-share2, .articleMeta, .header-board, .footer, .pk-menu, .header-top").remove();

            String externalTitle = doc.select("h1").text();

            String externalBody = doc.select(".post__text").text();
            if (externalBody.isEmpty()) {
                externalBody = doc.select(".post__content, .builder-section, .tab-content").text();
            }

            String internalTitle = page.getTitle() != null ? page.getTitle() : "";
            StringBuilder internalBuilder = new StringBuilder();

            String annotation = (String) page.getMetadata().get("annotation");
            if (annotation != null && !annotation.isBlank()) {
                internalBuilder.append(annotation).append(". ");
            }

            if (page.getBlocks() != null) {
                for (BlockData block : page.getBlocks()) {
                    if ("text".equals(block.getType())) {
                        String html = (String) block.getProps().get("content");
                        if (html != null && !html.isBlank()) {
                            internalBuilder.append(Jsoup.parse(html).text()).append(" ");
                        }
                    }
                }
            }
            String internalBody = internalBuilder.toString();

            boolean titleMatches = normalizeStrict(internalTitle).contains(normalizeStrict(externalTitle)) ||
                    normalizeStrict(externalTitle).contains(normalizeStrict(internalTitle));

            double similarity = calculateJaccardSimilarity(internalBody, externalBody, 3);
            int similarityPercent = (int) Math.round(similarity * 100);

            List<String> missingOnWebsite = findDiffSentences(internalBody, externalBody);
            List<String> extraOnWebsite = findDiffSentences(externalBody, internalBody);

            String status = (titleMatches && similarityPercent >= 90) ? "SYNCED" : "DESYNCED";

            pageService.updateSyncStatus(pageId, status, LocalDateTime.now());

            return SyncReportDto.builder()
                    .status(status)
                    .titleMatch(titleMatches)
                    .similarityPercent(similarityPercent)
                    .missingOnWebsite(missingOnWebsite)
                    .extraOnWebsite(extraOnWebsite)
                    .build();

        } catch (Exception e) {
            log.error("Failed to check sync for URL: {}", externalUrl, e);
            pageService.updateSyncStatus(pageId, "DESYNCED", LocalDateTime.now());
            return SyncReportDto.builder().status("DESYNCED").build();
        }
    }

    /**
     * Сравнивает предложения. Возвращает те, что есть в source, но отсутствуют в target.
     */
    private List<String> findDiffSentences(String source, String target) {
        String normalizedTarget = normalizeStrict(target);
        String[] sentences = source.split("(?<=[.!?])\\s+");

        List<String> diff = new ArrayList<>();
        for (String s : sentences) {
            if (s.trim().length() < 15) continue;
            String normS = normalizeStrict(s);
            if (!normalizedTarget.contains(normS)) {
                diff.add(s.trim());
            }
        }
        return diff;
    }

    private String normalizeStrict(String text) {
        return text.replaceAll("[^a-zA-Zа-яА-ЯёЁ0-9]", "").toLowerCase();
    }

    private double calculateJaccardSimilarity(String text1, String text2, int nGramSize) {
        Set<String> nGrams1 = getNgrams(text1, nGramSize);
        Set<String> nGrams2 = getNgrams(text2, nGramSize);

        if (nGrams1.isEmpty() && nGrams2.isEmpty()) return 1.0;
        if (nGrams1.isEmpty() || nGrams2.isEmpty()) return 0.0;

        Set<String> intersection = new HashSet<>(nGrams1);
        intersection.retainAll(nGrams2);

        Set<String> union = new HashSet<>(nGrams1);
        union.addAll(nGrams2);

        return (double) intersection.size() / union.size();
    }

    private Set<String> getNgrams(String text, int n) {
        String[] words = text.replaceAll("[^a-zA-Zа-яА-ЯёЁ0-9\\s]", "").toLowerCase().split("\\s+");
        Set<String> nGrams = new HashSet<>();
        if (words.length < n) return nGrams;
        for (int i = 0; i <= words.length - n; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < n; j++) sb.append(words[i + j]).append(" ");
            nGrams.add(sb.toString().trim());
        }
        return nGrams;
    }
}
