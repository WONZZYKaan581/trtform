package com.example.trtform.views;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ParticipationService {
    private static final List<AnswerDto> answers = new ArrayList<>();

    public static void saveAnswer(Long surveyId, Long questionId, String answerText) {
        answers.add(new AnswerDto(surveyId, questionId, answerText));
    }

    public static List<AnswerDto> getAnswersBySurveyId(Long surveyId) {
        return answers.stream()
                .filter(a -> a.getSurveyId().equals(surveyId))
                .collect(Collectors.toList());
    }

    public static class AnswerDto {
        private Long surveyId;
        private Long questionId;
        private String answerText;

        public AnswerDto(Long surveyId, Long questionId, String answerText) {
            this.surveyId = surveyId;
            this.questionId = questionId;
            this.answerText = answerText;
        }

        public Long getSurveyId() { return surveyId; }
        public Long getQuestionId() { return questionId; }
        public String getAnswerText() { return answerText; }
    }
}