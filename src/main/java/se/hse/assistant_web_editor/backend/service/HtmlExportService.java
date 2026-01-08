package se.hse.assistant_web_editor.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.hse.assistant_web_editor.backend.dto.PageDetailDto;
import se.hse.assistant_web_editor.backend.model.BlockData;

@Service
@RequiredArgsConstructor
public class HtmlExportService {

    private final PageService pageService;

    public String exportHtml(Long pageId) {
        PageDetailDto page = pageService.findLatestEntity(pageId);
        StringBuilder html = new StringBuilder();

        html.append("<div class=\"hse-page-content\">\n");

        for (BlockData block : page.getBlocks()) {
            html.append(renderBlock(block));
        }

        html.append("</div>");
        return html.toString();
    }

    private String renderBlock(BlockData block) {
        String type = block.getType();
        var props = block.getProps();

        return switch (type) {
            case "header" -> "<h1>" + props.getOrDefault("text", "") + "</h1>\n";
            case "paragraph" -> "<p class=\"hse-text\">" + props.getOrDefault("content", "") + "</p>\n";
            // TODO: добавить сложные блоки (Hero, News)
            default -> "<!-- Unknown block " + type + " -->\n";
        };
    }
}
