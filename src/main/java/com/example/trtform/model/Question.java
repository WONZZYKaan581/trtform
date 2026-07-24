package com.example.trtform.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "survey_id", nullable = false)
    private Long surveyId;

    // Sütun adını doğrudan "question_text" olarak sabitliyoruz
    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(name = "question_type", nullable = false)
    private String type;

    @ElementCollection
    @CollectionTable(name = "question_options", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "option_text")
    private List<String> options;

    // Getter ve Setter metotları
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSurveyId() { return surveyId; }
    public void setSurveyId(Long surveyId) { this.surveyId = surveyId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
}