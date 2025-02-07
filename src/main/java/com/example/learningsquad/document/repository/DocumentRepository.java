package com.example.learningsquad.document.repository;

import com.example.learningsquad.document.domain.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity,Long> {
    Optional<List<DocumentEntity>> findByAccountEntity_ObjectId(Long accountId);
}
