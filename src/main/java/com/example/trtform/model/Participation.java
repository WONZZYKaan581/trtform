package com.example.trtform.model;

import jakarta.persistence.*;

// Participation now extends BaseEntity directly
@Entity
@Table(name = "participations")
public class Participation extends BaseEntity {

    @Column(name = "question_id", nullable = false)
    protected Long questionId;

    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;

    @Column(name = "participant_name", nullable = false)
    private String participantName;

    // surveyId moved from SurveyOwnedEntity
    @Column(name = "survey_id", nullable = false)
    protected Long surveyId;

    // Getter and Setter for surveyId
    public Long getSurveyId() { return surveyId; }
    public void setSurveyId(Long surveyId) { this.surveyId = surveyId; }

    // Getter ve Setter metotları
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public String getAnswerText() { return answerText; }
    public void setAnswerText(String answerText) { this.answerText = answerText; }

    public String getParticipantName() { return participantName; }
    public void setParticipantName(String participantName) { this.participantName = participantName; }
}