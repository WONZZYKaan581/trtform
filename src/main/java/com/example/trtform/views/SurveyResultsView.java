package com.example.trtform.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.component.notification.Notification;

import java.util.ArrayList;
import java.util.List;

@Route("survey-results/1")
@PageTitle("Anket Sonuçları | Dinamik Anket")
public class SurveyResultsView extends VerticalLayout implements BeforeEnterObserver {

    private Long surveyId;
    private Grid<DetailedAnswerDto> resultsGrid;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (UserService.getLoggedInUser() == null) {
            Notification.show("Bu sayfaya erişmek için giriş yapmalısınız!", 3000, Notification.Position.MIDDLE);
            event.forwardTo("login"); // Giriş sayfasına veya anasayfaya yönlendirir
        }
    }

    public SurveyResultsView() {
        H2 title = new H2("Katılımcı Yanıtları ve Sonuçlar");

        resultsGrid = new Grid<>(DetailedAnswerDto.class);
        resultsGrid.removeAllColumns();

        resultsGrid.addColumn(DetailedAnswerDto::getParticipantName).setHeader("Katılımcı (Ad Soyad)").setAutoWidth(true);
        resultsGrid.addColumn(DetailedAnswerDto::getQuestionText).setHeader("Soru Metni").setAutoWidth(true);
        resultsGrid.addColumn(DetailedAnswerDto::getQuestionType).setHeader("Soru Türü");
        resultsGrid.addColumn(DetailedAnswerDto::getAnswerText).setHeader("Verilen Yanıt");
        resultsGrid.setWidthFull();

        Button backButton = new Button("Geri Dön", event -> {
            getUI().ifPresent(ui -> ui.navigate(""));
        });
        backButton.setWidthFull();

        VerticalLayout layout = new VerticalLayout(title, resultsGrid, backButton);
        layout.setWidth("900px");
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
            List<QuestionService.QuestionDto> questions = QuestionService.getQuestionsBySurveyId(surveyId);
            List<ParticipationService.AnswerDto> answers = ParticipationService.getAnswersBySurveyId(surveyId);
            
            List<DetailedAnswerDto> detailedList = new ArrayList<>();

            for (ParticipationService.AnswerDto ans : answers) {
                String qText = "Bilinmeyen Soru";
                String qType = "-";
                
                for (QuestionService.QuestionDto q : questions) {
                    if (q.getId().equals(ans.getQuestionId())) {
                        qText = q.getText();
                        qType = q.getType();
                        break;
                    }
                }
                
                // Metinlerin veya diğer yanıtların düzgün görünmesini sağlıyoruz
                String answerText = ans.getAnswerText();
                if (answerText == null) {
                    answerText = "";
                }

                detailedList.add(new DetailedAnswerDto(ans.getParticipantName(), qText, qType, answerText));
            }

            resultsGrid.setItems(detailedList);
        }
    }

    public static class DetailedAnswerDto {
        private String participantName;
        private String questionText;
        private String questionType;
        private String answerText;

        public DetailedAnswerDto(String participantName, String questionText, String questionType, String answerText) {
            this.participantName = participantName;
            this.questionText = questionText;
            this.questionType = questionType;
            this.answerText = answerText;
        }

        public String getParticipantName() { return participantName; }
        public String getQuestionText() { return questionText; }
        public String getQuestionType() { return questionType; }
        public String getAnswerText() { return answerText; }
    }
}