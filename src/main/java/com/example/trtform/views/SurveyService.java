package com.example.trtform.views;

import java.util.ArrayList;
import java.util.List;

public class SurveyService {
    private static final List<MainView.SurveyDto> surveys = new ArrayList<>();
    private static long idCounter = 1;

    static {
        // Test amaçlı örnek bir anket ekleyelim
        surveys.add(new MainView.SurveyDto(idCounter++, "Ürün Memnuniyeti Anketi", "Ürünümüz hakkındaki görüşleriniz bizim için önemlidir."));
    }

    public static List<MainView.SurveyDto> getSurveys() {
        return surveys;
    }

    public static void addSurvey(String name, String description) {
        surveys.add(new MainView.SurveyDto(idCounter++, name, description));
    }

   public static void updateSurvey(Long id, String newName, String newDesc) {
    for (MainView.SurveyDto s : surveys) {
        if (s.getId().equals(id)) {
            s.setName(newName);
            s.setDescription(newDesc);
            break;
        }
    }
}


public static void deleteSurvey(Long id) {
    surveys.removeIf(s -> s.getId().equals(id));
}
}