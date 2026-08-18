package com.example.trtform.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class SurveyOwnedEntity extends BaseEntity {

    @Column(name = "survey_id", nullable = false)
    protected Long surveyId;

    public Long getSurveyId() {
        return surveyId;
    }

    public void setSurveyId(Long surveyId) {
        this.surveyId = surveyId;
    }
}
