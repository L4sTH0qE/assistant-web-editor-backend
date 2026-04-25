package se.hse.assistant_web_editor.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class SyncReportDto {
    private String status;
    private boolean titleMatch;
    private int similarityPercent;
    private List<String> missingOnWebsite;
    private List<String> extraOnWebsite;
}
