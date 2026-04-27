package se.hse.assistant_web_editor.backend.service;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.hse.assistant_web_editor.backend.dto.PageDetailDto;
import se.hse.assistant_web_editor.backend.dto.SaveVersionRequest;
import se.hse.assistant_web_editor.backend.entity.GlossaryTermEntity;
import se.hse.assistant_web_editor.backend.model.BlockData;
import se.hse.assistant_web_editor.backend.repository.GlossaryRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/// Service for auto-linking algorithm.
@Service
@RequiredArgsConstructor
public class AutoLinkService {

    private final GlossaryRepository glossaryRepository;
    private final PageService pageService;

    @Transactional
    public PageDetailDto processAndSaveAutoLinks(Long pageId) {
        PageDetailDto page = pageService.findLatestEntity(pageId);
        List<GlossaryTermEntity> terms = glossaryRepository.findAll();

        if (terms.isEmpty()) {
            return page;
        }

        terms.sort(Comparator.comparingInt((GlossaryTermEntity t) -> t.getTerm().length()).reversed());

        boolean isModified = false;

        for (BlockData block : page.getBlocks()) {
            if ("text".equals(block.getType())) {
                String content = (String) block.getProps().get("content");
                if (content != null && !content.isEmpty()) {

                    String updatedContent = replaceTermsInHtml(content, terms);

                    if (!content.equals(updatedContent)) {
                        block.getProps().put("content", updatedContent);
                        isModified = true;
                    }
                }
            }
        }

        if (isModified) {
            SaveVersionRequest request = new SaveVersionRequest();
            request.setBlocks(page.getBlocks());
            request.setTitle(page.getTitle());
            request.setMetadata(page.getMetadata());

            pageService.savePageVersion(pageId, request);
        }

        return pageService.findLatestEntity(pageId);
    }

    private String replaceTermsInHtml(String html, List<GlossaryTermEntity> terms) {
        Document doc = Jsoup.parseBodyFragment(html);
        boolean documentChanged = false;

        for (GlossaryTermEntity term : terms) {

            List<TextNode> targetNodes = new ArrayList<>();

            doc.body().traverse(new NodeVisitor() {
                @Override
                public void head(org.jsoup.nodes.Node node, int depth) {
                    if (node.parent() != null && node.parent().nodeName().equalsIgnoreCase("a")) {
                        return;
                    }
                    if (node instanceof TextNode textNode) {
                        targetNodes.add(textNode);
                    }
                }

                @Override
                public void tail(org.jsoup.nodes.Node node, int depth) {}
            });

            for (TextNode textNode : targetNodes) {
                String text = textNode.getWholeText();

                String regex = "(?iU)\\b" + Pattern.quote(term.getTerm()) + "\\b";
                String replacement = String.format("<a href=\"%s\" style=\"color: var(--hse-blue-accent);\">%s</a>",
                        term.getUrl(), "$0");

                String newText = text.replaceAll(regex, replacement);

                if (!newText.equals(text)) {
                    textNode.before(newText);
                    textNode.remove();
                    documentChanged = true;
                }
            }
        }

        return documentChanged ? doc.body().html() : html;
    }
}
