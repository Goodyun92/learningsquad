package com.example.learningsquad.answer.repository;

import com.example.learningsquad.answer.domain.AnswerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<AnswerEntity, Long> {
    List<AnswerEntity> findByQuestionEntity_ObjectIdOrderByCreatedOnDesc(Long questionId); // 최신순

    List<AnswerEntity> findByQuestionEntity_ObjectIdOrderByCreatedOnAsc(Long questionId);  // 오래된순

    List<AnswerEntity> findByQuestionEntity_ObjectIdOrderByScoreAsc(Long questionId);        // 낮은 점수순

    List<AnswerEntity> findByQuestionEntity_ObjectIdOrderByScoreDesc(Long questionId);       // 높은 점수순
}
