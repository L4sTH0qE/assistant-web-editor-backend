package se.hse.assistant_web_editor.backend.service;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import se.hse.assistant_web_editor.backend.dto.PageDetailDto;
import se.hse.assistant_web_editor.backend.model.BlockData;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SyncService {

    private final PageService pageService;

    public String checkSync(Long pageId) {
        PageDetailDto page = pageService.findLatestEntity(pageId);
        String externalUrl = (String) page.getMetadata().get("externalUrl");

        if (externalUrl == null || externalUrl.isEmpty()) {
            return "DRAFT";
        }

        try {
            Document doc = Jsoup.connect(externalUrl).get();
            String externalText = doc.body().text();

            StringBuilder internalBuilder = new StringBuilder();
            if (page.getBlocks() != null) {
                for (BlockData block : page.getBlocks()) {
                    String html = (String) block.getProps().get("content");
                    internalBuilder.append(Jsoup.parse(html).text()).append(" ");
                }
            }
            String internalText = internalBuilder.toString();

            String status = calculateSimilarity(internalText, externalText) > 0.8 ? "SYNCED" : "DESYNCED";

            pageService.updateSyncStatus(pageId, status, LocalDateTime.now());

            return status;

        } catch (IOException e) {
            return "DRAFT";
        }
    }

    private double calculateSimilarity(String s1, String s2) {
        String clean1 = s1.replaceAll("\\s+", "").toLowerCase();
        String clean2 = s2.replaceAll("\\s+", "").toLowerCase();
        return clean2.contains(clean1) ? 1.0 : 0.0;
    }
}
