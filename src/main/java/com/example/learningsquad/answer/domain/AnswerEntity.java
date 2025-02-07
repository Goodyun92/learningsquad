package com.example.learningsquad.answer.domain;

import com.example.learningsquad.document.domain.DocumentEntity;
import com.example.learningsquad.global.common.model.BaseEntity;
import com.example.learningsquad.question.domain.QuestionEntity;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "answer")
@Table(
        indexes = {
                @Index(name = "IDX_ANSWER_QUESTION_ID", columnList = "QUESTION_ID")
        }
)
public class AnswerEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "OID", nullable = false, unique = true)
    private Long objectId;

    @ManyToOne
    @JoinColumn(name = "QUESTION_ID", referencedColumnName = "OID")
    private QuestionEntity questionEntity;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Column(nullable = false)
    private Integer score;
}
