package se.hse.assistant_web_editor.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import se.hse.assistant_web_editor.backend.model.BlockData;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "page_versions", schema = "public")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageVersionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", nullable = false)
    private PageEntity page;

    private Integer versionNumber;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "structure_json", columnDefinition = "jsonb")
    private List<BlockData> structure;

    @CreationTimestamp
    private LocalDateTime createdAt;
}