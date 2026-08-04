package com.example.trtform.service;

import com.example.trtform.model.Participation;
import com.example.trtform.repository.ParticipationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ParticipationService {

    private final ParticipationRepository participationRepository;

    public ParticipationService(ParticipationRepository participationRepository) {
        this.participationRepository = participationRepository;
    }

    public void saveAnswer(Long surveyId, Long questionId, String answerText, String participantName) {
        Participation participation = new Participation();
        participation.setSurveyId(surveyId);
        participation.setQuestionId(questionId);
        participation.setAnswerText(answerText);
        participation.setParticipantName(participantName);
        participationRepository.save(participation);
    }

    public List<AnswerDto> getAnswersBySurveyId(Long surveyId) {
        List<Participation> list = participationRepository.findBySurveyId(surveyId);
        return list.stream()
                .map(p -> new AnswerDto(p.getId(), p.getSurveyId(), p.getQuestionId(), p.getAnswerText(), p.getParticipantName()))
                .collect(Collectors.toList());
    }

    public static class AnswerDto {
        private Long id;
        private Long surveyId;
        private Long questionId;
        private String answerText;
        private String participantName;

        public AnswerDto(Long id, Long surveyId, Long questionId, String answerText, String participantName) {
            this.id = id;
            this.surveyId = surveyId;
            this.questionId = questionId;
            this.answerText = answerText;
            this.participantName = participantName;
        }

        public Long getId() { return id; }
        public Long getSurveyId() { return surveyId; }
        public Long getQuestionId() { return questionId; }
        public String getAnswerText() { return answerText; }
        public String getParticipantName() { return participantName; }
    }
}