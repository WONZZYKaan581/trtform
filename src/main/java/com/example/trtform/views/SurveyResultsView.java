package com.example.trtform.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;

@Route("survey-results")
@PageTitle("Anket Sonuçları | Dinamik Anket")
public class SurveyResultsView extends VerticalLayout implements HasUrlParameter<Long> {

    private Long surveyId;
    private Grid<DetailedAnswerDto> resultsGrid;

    @Override
    public void setParameter(BeforeEvent event, Long parameter) {
        this.surveyId = parameter;
        refreshGrid();
    }

    public SurveyResultsView() {
        H2 title = new H2("Detaylı Anket Sonuçları ve Yanıtlar");

        resultsGrid = new Grid<>(DetailedAnswerDto.class);
        resultsGrid.removeAllColumns();

        resultsGrid.addColumn(DetailedAnswerDto::getQuestionText).setHeader("Soru Metni").setAutoWidth(true);
        resultsGrid.addColumn(DetailedAnswerDto::getQuestionType).setHeader("Soru Türü");
        resultsGrid.addColumn(DetailedAnswerDto::getAnswerText).setHeader("Verilen Yanıt");
        resultsGrid.setWidthFull();

        Button backButton = new Button("Geri Dön", event -> {
            getUI().ifPresent(ui -> ui.navigate(""));
        });
        backButton.setWidthFull();

        VerticalLayout layout = new VerticalLayout(title, resultsGrid, backButton);
        layout.setWidth("800px");
        layout.setAlignItems(Alignment.STRETCH);
        layout.setPadding(true);
        layout.getStyle().set("background", "var(--lumo-base-color)");
        layout.getStyle().set("border-radius", "8px");
        layout.getStyle().set("box-shadow", "var(--lumo-box-shadow-m)");

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        add(layout);
    }

    private void refreshGrid() {
        if (surveyId != null) {
            // O ankete ait soruları ve verilen yanıtları birleştirerek detaylı bir liste oluşturalım
            List<QuestionService.QuestionDto> questions = QuestionService.getQuestionsBySurveyId(surveyId);
            List<ParticipationService.AnswerDto> answers = ParticipationService.getAnswersBySurveyId(surveyId);
            
            List<DetailedAnswerDto> detailedList = new ArrayList<>();

            for (ParticipationService.AnswerDto ans : answers) {
                // İlgili sorunun metnini ve türünü bulalım
                String qText = "Bilinmeyen Soru";
                String qType = "-";
                
                for (QuestionService.QuestionDto q : questions) {
                    if (q.getId().equals(ans.getQuestionId())) {
                        qText = q.getText();
                        qType = q.getType();
                        break;
                    }
                }
                
                detailedList.add(new DetailedAnswerDto(qText, qType, ans.getAnswerText()));
            }

            resultsGrid.setItems(detailedList);
        }
    }

    // Sonuç tablosu için detaylı DTO sınıfı
    public static class DetailedAnswerDto {
        private String questionText;
        private String questionType;
        private String answerText;

        public DetailedAnswerDto(String questionText, String questionType, String answerText) {
            this.questionText = questionText;
            this.questionType = questionType;
            this.answerText = answerText;
        }

        public String getQuestionText() { return questionText; }
        public String getQuestionType() { return questionType; }
        public String getAnswerText() { return answerText; }
    }
}