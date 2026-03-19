package se.hse.assistant_web_editor.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "taxonomy_terms")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxonomyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaxonomyType type;

    @Column(nullable = false)
    private String name;
}
