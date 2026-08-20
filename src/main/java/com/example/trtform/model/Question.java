package com.example.trtform.model;

import jakarta.persistence.*;
import java.util.List;

// Question now extends BaseEntity directly
@Entity
@Table(name = "questions")
public class Question extends BaseEntity {

    // Sütun adını doğrudan "question_text" olarak sabitliyoruz
    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(name = "question_type", nullable = false)
    private String type;

    @ElementCollection
    @CollectionTable(name = "question_options", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "option_text")
    private List<String> options;

    // surveyId moved from SurveyOwnedEntity
    @Column(name = "survey_id", nullable = false)
    protected Long surveyId;

    // Getter and Setter for surveyId
    public Long getSurveyId() { return surveyId; }
    public void setSurveyId(Long surveyId) { this.surveyId = surveyId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
}