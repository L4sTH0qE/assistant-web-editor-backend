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

        // Сортируем от самых длинных фраз к самым коротким (чтобы длинные заменялись первыми и не было коллизий)
        terms.sort(Comparator.comparingInt((GlossaryTermEntity t) -> t.getTerm().length()).reversed());

        boolean isModified = false;

        for (BlockData block : page.getBlocks()) {
            if ("text".equals(block.getType())) {
                String content = (String) block.getProps().get("content");
                if (content != null && !content.isEmpty()) {

                    String updatedContent = replaceTermsInHtml(content, terms);

                    // Если HTML изменился
                    if (!content.equals(updatedContent)) {
                        block.getProps().put("content", updatedContent);
                        isModified = true;
                    }
                }
            }
        }

        // Сохраняем новую версию только если были реальные изменения
        if (isModified) {
            SaveVersionRequest request = new SaveVersionRequest();
            request.setBlocks(page.getBlocks());
            request.setTitle(page.getTitle());
            request.setMetadata(page.getMetadata());

            pageService.savePageVersion(pageId, request);
        }

        return pageService.findLatestEntity(pageId);
    }

    /**
     * Безопасная манипуляция DOM-деревом.
     */
    private String replaceTermsInHtml(String html, List<GlossaryTermEntity> terms) {
        Document doc = Jsoup.parseBodyFragment(html);
        boolean documentChanged = false;

        // Применяем термины ПО ОЧЕРЕДИ
        for (GlossaryTermEntity term : terms) {

            // 1. Сначала собираем все текстовые ноды в отдельный список,
            // чтобы не модифицировать DOM-дерево прямо во время обхода Jsoup (Избегаем IndexOutOfBoundsException)
            List<TextNode> targetNodes = new ArrayList<>();

            doc.body().traverse(new NodeVisitor() {
                @Override
                public void head(org.jsoup.nodes.Node node, int depth) {
                    // Игнорируем текст, который уже обернут в ссылку <a> (защита от ссылок в ссылке)
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

            // 2. Теперь безопасно модифицируем собранные ноды
            for (TextNode textNode : targetNodes) {
                String text = textNode.getWholeText();

                // Флаги: CASE_INSENSITIVE (?i) + UNICODE_CHARACTER_CLASS (?U) критически важны для работы с кириллицей!
                String regex = "(?iU)\\b" + Pattern.quote(term.getTerm()) + "\\b";
                String replacement = String.format("<a href=\"%s\" style=\"color: var(--hse-blue-accent);\">%s</a>",
                        term.getUrl(), "$0"); // $0 сохраняет оригинальный регистр найденного слова (ФКН, фкн)

                String newText = text.replaceAll(regex, replacement);

                // Если слово найдено и текст изменился
                if (!newText.equals(text)) {
                    textNode.before(newText); // Парсим и добавляем новый HTML ДО старого текстового узла
                    textNode.remove();        // Полностью удаляем старый текстовый узел
                    documentChanged = true;
                }
            }
        }

        return documentChanged ? doc.body().html() : html;
    }
}
