package com.example.trtform.views;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class QuestionService {
    private static final List<QuestionDto> questions = new ArrayList<>();

    public static void addQuestion(Long surveyId, String text, String type, List<String> options) {
        Long id = (long) (questions.size() + 1);
        questions.add(new QuestionDto(id, surveyId, text, type, options != null ? options : new ArrayList<>()));
    }

    public static List<QuestionDto> getQuestionsBySurveyId(Long surveyId) {
        return questions.stream()
                .filter(q -> q.getSurveyId().equals(surveyId))
                .collect(Collectors.toList());
    }

    public static void deleteQuestion(Long questionId) {
        questions.removeIf(q -> q.getId().equals(questionId));
    }

    // Soruyu ve seçenekleri güncelleyen metot
    public static void updateQuestion(Long questionId, String newText, List<String> newOptions) {
        for (QuestionDto q : questions) {
            if (q.getId().equals(questionId)) {
                q.setText(newText);
                if (newOptions != null) {
                    q.setOptions(newOptions);
                }
                break;
            }
        }
    }

    public static class QuestionDto {
        private Long id;
        private Long surveyId;
        private String text;
        private String type;
        private List<String> options;

        public QuestionDto(Long id, Long surveyId, String text, String type, List<String> options) {
            this.id = id;
            this.surveyId = surveyId;
            this.text = text;
            this.type = type;
            this.options = options;
        }

        public Long getId() { return id; }
        public Long getSurveyId() { return surveyId; }
        
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public List<String> getOptions() { return options; }
        public void setOptions(List<String> options) { this.options = options; }
    }
}