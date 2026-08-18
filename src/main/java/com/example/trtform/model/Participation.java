package com.example.trtform.model;

import jakarta.persistence.*;

@Entity
@Table(name = "participations")
public class Participation extends SurveyOwnedEntity {

    @Column(name = "question_id", nullable = false)
    protected Long questionId;

    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;

    @Column(name = "participant_name", nullable = false)
    private String participantName;

    // Getter ve Setter metotları
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public String getAnswerText() { return answerText; }
    public void setAnswerText(String answerText) { this.answerText = answerText; }

    public String getParticipantName() { return participantName; }
    public void setParticipantName(String participantName) { this.participantName = participantName; }
}