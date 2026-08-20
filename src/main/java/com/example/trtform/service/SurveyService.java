package com.example.trtform.service;

import com.example.trtform.model.Survey;
import com.example.trtform.repository.SurveyRepository;
import com.example.trtform.views.MainView;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final UserService userService; // Kullanıcı adını almak için ekledik

    public SurveyService(SurveyRepository surveyRepository, UserService userService) {
        this.surveyRepository = surveyRepository;
        this.userService = userService;
    }

    public List<MainView.SurveyDto> getSurveys() {
        List<Survey> surveysFromDb = surveyRepository.findAll();
        return surveysFromDb.stream()
                .map(s -> new MainView.SurveyDto(
                    s.getId(), 
                    s.getTitle(), 
                    s.getDescription(), 
                    s.getCreatorUsername() // Doğru metot adı
                ))
                .collect(Collectors.toList());
    }

    public void addSurvey(String name, String description) {
        Survey survey = new Survey();
        survey.setTitle(name);
        survey.setDescription(description);
        survey.setCreatorUsername(userService.getLoggedInUser()); // Anketi oluşturan kullanıcıyı kaydediyoruz
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