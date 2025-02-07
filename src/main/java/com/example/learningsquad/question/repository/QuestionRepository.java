package com.example.learningsquad.question.repository;

import com.example.learningsquad.question.domain.QuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<QuestionEntity,Long>{
    List<QuestionEntity> findByDocumentEntity_ObjectId(Long documentId);
}
