package com.example.learningsquad.document.domain;

import com.example.learningsquad.account.domain.AccountEntity;
import com.example.learningsquad.global.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "document")
@Table(
        indexes = {
                @Index(name = "IDX_DOCUMENT_ACCOUNT_ID", columnList = "ACCOUNT_ID")
        }
)
public class DocumentEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "OID", nullable = false, unique = true)
    private Long objectId;

    @ManyToOne
    @JoinColumn(name = "ACCOUNT_ID", referencedColumnName = "OID")
    private AccountEntity accountEntity;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String url;    //s3 주소

    @Column(nullable = false)
    private String title;

    @Column
    private Integer questionSize;    //문제 수
}
