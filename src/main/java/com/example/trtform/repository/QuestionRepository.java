package com.example.trtform.repository;

import com.example.trtform.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    
    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.options WHERE q.surveyId = :surveyId")
    List<Question> findBySurveyId(@Param("surveyId") Long surveyId);
}