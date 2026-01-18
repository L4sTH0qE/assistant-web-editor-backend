package se.hse.assistant_web_editor.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.hse.assistant_web_editor.backend.dto.PageDetailDto;
import se.hse.assistant_web_editor.backend.dto.ExportBlockDto;
import se.hse.assistant_web_editor.backend.model.BlockData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HtmlExportService {

    private final PageService pageService;

    public List<ExportBlockDto> exportBlocks(Long pageId) {
        PageDetailDto page = pageService.findLatestEntity(pageId);
        List<ExportBlockDto> result = new ArrayList<>();

        for (BlockData block : page.getBlocks()) {
            String html = renderBlock(block);
            String humanName = getReadableName(block.getType());

            result.add(ExportBlockDto.builder()
                    .id(block.getId())
                    .blockName(humanName)
                    .htmlContent(html)
                    .build());
        }

        return result;
    }

    private String getReadableName(String type) {
        return switch (type) {
            case "header" -> "Заголовок";
            case "text" -> "Текстовый блок";
            case "hero" -> "Баннер";
            default -> "Блок " + type;
        };
    }

    private String renderBlock(BlockData block) {
        String type = block.getType();
        Map<String, Object> props = block.getProps();

        return switch (type) {
            case "header" -> {
                int level = (int) props.getOrDefault("level", 2);
                String text = (String) props.getOrDefault("text", "");
                yield "<h" + level + ">" + text + "</h" + level + ">";
            }
            case "text" -> {
                String content = (String) props.getOrDefault("content", "");
                yield "<div class=\"hse-text-block\">" + content + "</div>";
            }
            case "hero" -> {
                String title = (String) props.getOrDefault("title", "");
                String img = (String) props.getOrDefault("imageUrl", "");
                yield String.format(
                        "<div class=\"hse-hero\" style=\"background-image: url('%s')\"><h1>%s</h1></div>",
                        img, title
                );
            }
            default -> "<!-- Неподдерживаемый блок -->";
        };
    }
}