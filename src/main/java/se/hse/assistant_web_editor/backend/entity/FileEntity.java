package se.hse.assistant_web_editor.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "files", schema = "public")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileEntity {
    @Id
    @UuidGenerator
    private String id;

    private String filename;

    private String contentType;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "data", columnDefinition = "bytea")
    private byte[] data;
}
