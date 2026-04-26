package se.hse.assistant_web_editor.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import se.hse.assistant_web_editor.backend.dto.PageType;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "pages", schema = "public")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(unique = true)
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private UserEntity owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PageType type;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata; // rubric (String), tags (List<String>), keywords (List<String>), externalUrl (String)

    @Column(name = "sync_status")
    private String syncStatus; // "SYNCED", "DESYNCED", "DRAFT"

    @Column(name = "last_sync_check")
    private LocalDateTime lastSyncCheck;

    @Column(name = "last_synced_version")
    private Integer lastSyncedVersion;
}