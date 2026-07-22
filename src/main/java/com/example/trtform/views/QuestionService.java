package com.example.trtform.views;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class QuestionService {
    private static final List<QuestionDto> questions = new ArrayList<>();
    private static long idCounter = 1;

    static {
        questions.add(new QuestionDto(idCounter++, 1L, "Bu ürünü diğer arkadaşlarınıza tavsiye eder misiniz?", "Çoktan Seçmeli", List.of("Kalite", "Fiyat", "Hızlı Teslimat", "Paketleme")));
        questions.add(new QuestionDto(idCounter++, 1L, "Ürün hakkında başka bir yorum var mı?", "Metin Alanı", new ArrayList<>()));
    }

    public static void addQuestion(Long surveyId, String text, String type, List<String> options) {
        questions.add(new QuestionDto(idCounter++, surveyId, text, type, options));
    }

    public static List<QuestionDto> getQuestionsBySurveyId(Long surveyId) {
        return questions.stream()
                .filter(q -> q.getSurveyId().equals(surveyId))
                .collect(Collectors.toList());
    }

    public static void deleteQuestion(Long questionId) {
        questions.removeIf(q -> q.getId().equals(questionId));
    }

    public static void updateQuestion(Long questionId, String newText) {
        for (QuestionDto q : questions) {
            if (q.getId().equals(questionId)) {
                q.setText(newText);
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
        public List<String> getOptions() { return options; }
    }
}