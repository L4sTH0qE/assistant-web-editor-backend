package se.hse.assistant_web_editor.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "glossary_terms")
@Data
public class GlossaryTermEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String term;

    @Column(nullable = false)
    private String url;
}
