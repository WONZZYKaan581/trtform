package com.example.trtform.service;

import com.example.trtform.model.Question;
import com.example.trtform.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 1. Burayı ekledik

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class QuestionService {

    private final QuestionRepository questionRepository;

    public QuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public void addQuestion(Long surveyId, String text, String type, List<String> options) {
        Question question = new Question();
        question.setSurveyId(surveyId);
        question.setText(text);
        question.setType(type);
        question.setOptions(options != null ? options : new ArrayList<>());
        questionRepository.save(question);
    }

    // 2. Buraya @Transactional(readOnly = true) ekledik
    @Transactional(readOnly = true)
    public List<QuestionDto> getQuestionsBySurveyId(Long surveyId) {
        // Not: QuestionRepository içine eklediğimiz özel sorguyu çağırıyoruz
        List<Question> questionsFromDb = questionRepository.findBySurveyId(surveyId);
        return questionsFromDb.stream()
                .map(q -> new QuestionDto(q.getId(), q.getSurveyId(), q.getText(), q.getType(), q.getOptions()))
                .collect(Collectors.toList());
    }

    public void deleteQuestion(Long questionId) {
        questionRepository.deleteById(questionId);
    }

    public void updateQuestion(Long questionId, String newText, List<String> newOptions) {
        questionRepository.findById(questionId).ifPresent(q -> {
            q.setText(newText);
            if (newOptions != null) {
                q.setOptions(newOptions);
            }
            questionRepository.save(q);
        });
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