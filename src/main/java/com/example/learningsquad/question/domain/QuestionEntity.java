package com.example.learningsquad.question.domain;

import com.example.learningsquad.document.domain.DocumentEntity;
import com.example.learningsquad.global.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "question")
@Table(
        indexes = {
                @Index(name = "IDX_QUESTION_DOCUMENT_ID", columnList = "DOCUMENT_ID")
        }
)
public class QuestionEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "OID", nullable = false, unique = true)
    private Long objectId;

    @ManyToOne
    @JoinColumn(name = "DOCUMENT_ID", referencedColumnName = "OID")
    private DocumentEntity documentEntity;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Integer questionIndex;

    @Column(nullable = false)
    private String modelAnswer;

    @Column
    private String bestAnswer;

    @Column(nullable = false)
    private Integer bestScore;
}
