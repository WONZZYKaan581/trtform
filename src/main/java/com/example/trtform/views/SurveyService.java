package com.example.trtform.views;

import com.example.trtform.model.Survey;
import com.example.trtform.repository.SurveyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SurveyService {

    private final SurveyRepository surveyRepository;

    public SurveyService(SurveyRepository surveyRepository) {
        this.surveyRepository = surveyRepository;
    }

    public List<MainView.SurveyDto> getSurveys() {
        List<Survey> surveysFromDb = surveyRepository.findAll();
        return surveysFromDb.stream()
                .map(s -> new MainView.SurveyDto(s.getId(), s.getTitle(), s.getDescription()))
                .collect(Collectors.toList());
    }

    public void addSurvey(String name, String description) {
        Survey survey = new Survey();
        survey.setTitle(name);
        survey.setDescription(description);
        surveyRepository.save(survey);
    }

    public void updateSurvey(Long id, String newName, String newDesc) {
        surveyRepository.findById(id).ifPresent(survey -> {
            survey.setTitle(newName);
            survey.setDescription(newDesc);
            surveyRepository.save(survey);
        });
    }

    public void deleteSurvey(Long id) {
        surveyRepository.deleteById(id);
    }
}