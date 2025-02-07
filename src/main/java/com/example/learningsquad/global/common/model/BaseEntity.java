package com.example.learningsquad.global.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

@Setter
@Getter
@ToString
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public abstract class BaseEntity implements Serializable {
    @Column(name = "CREATEDON", nullable = true, updatable = true)
    @CreatedDate
    protected LocalDateTime createdOn;

    @Column(name = "UPDATEDON", nullable = true)
    @LastModifiedDate
    protected LocalDateTime updatedOn;
}