package se.hse.assistant_web_editor.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.hse.assistant_web_editor.backend.dto.ExportBlockDto;
import se.hse.assistant_web_editor.backend.dto.PageDetailDto;
import se.hse.assistant_web_editor.backend.model.BlockData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/// Service for exporting pages data as html code.
@Service
@RequiredArgsConstructor
public class HtmlExportService {

    private final PageService pageService;

    public List<ExportBlockDto> exportBlocks(Long pageId) {
        PageDetailDto page = pageService.findLatestEntity(pageId);
        List<ExportBlockDto> result = new ArrayList<>();

        for (BlockData block : page.getBlocks()) {
            String html = renderBlock(block);
            String humanName = getName(block.getType());

            result.add(ExportBlockDto.builder()
                    .id(block.getId())
                    .blockName(humanName)
                    .htmlContent(html)
                    .build());
        }

        return result;
    }

    /// Retrieve block readable name by its type.
    ///
    /// @param type Block type.
    /// @return String object representing block name.
    private String getName(String type) {
        return Objects.equals(type, "text") ? "Текстовый блок" : "Блок " + type;
    }

    /// Retrieve block content.
    ///
    /// @param block Block data.
    /// @return String object representing block content.
    private String renderBlock(BlockData block) {
        String type = block.getType();
        Map<String, Object> props = block.getProps();
        return Objects.equals(type, "text") ? (String) props.getOrDefault("content", "") : "<!-- Unknown Block -->";
    }
}