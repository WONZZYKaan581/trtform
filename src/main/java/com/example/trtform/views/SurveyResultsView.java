package com.example.trtform.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.component.html.Anchor;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Route("survey-results")
@PageTitle("Anket Detaylı Sonuçları | Dinamik Anket")
public class SurveyResultsView extends VerticalLayout implements BeforeEnterObserver, HasUrlParameter<Long> {

    private final UserService userService;
    private final QuestionService questionService;
    private final ParticipationService participationService;
    private final SurveyService surveyService;
    
    private Long surveyId;
    private VerticalLayout contentLayout;
    private Paragraph summaryParagraph;
    private HorizontalLayout topActionLayout;

    public SurveyResultsView(UserService userService, QuestionService questionService, ParticipationService participationService, SurveyService surveyService) {
        this.userService = userService;
        this.questionService = questionService;
        this.participationService = participationService;
        this.surveyService = surveyService;

        H2 title = new H2("Anket Detaylı Analiz ve Sonuçlar");
        
        summaryParagraph = new Paragraph("Yükleniyor...");
        summaryParagraph.getStyle().set("font-weight", "bold");

        topActionLayout = new HorizontalLayout();
        topActionLayout.setWidthFull();
        topActionLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        contentLayout = new VerticalLayout();
        contentLayout.setWidthFull();
        contentLayout.setPadding(false);

        Button backButton = new Button("Geri Dön", event -> {
            getUI().ifPresent(ui -> ui.navigate(""));
        });
        backButton.setWidthFull();

        VerticalLayout mainWrapper = new VerticalLayout(title, summaryParagraph, topActionLayout, contentLayout, backButton);
        mainWrapper.setWidth("950px");
        mainWrapper.setAlignItems(Alignment.STRETCH);
        mainWrapper.setPadding(true);
        mainWrapper.getStyle().set("background", "var(--lumo-base-color)");
        mainWrapper.getStyle().set("border-radius", "8px");
        mainWrapper.getStyle().set("box-shadow", "var(--lumo-box-shadow-m)");

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        add(mainWrapper);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (userService.getLoggedInUser() == null) {
            Notification.show("Bu sayfaya erişmek için giriş yapmalısınız!", 3000, Notification.Position.MIDDLE);
            event.forwardTo("login");
        }
    }

    @Override
    public void setParameter(BeforeEvent event, Long parameter) {
        this.surveyId = parameter;
        loadResults();
    }

    private void loadResults() {
        if (surveyId == null) return;

        List<MainView.SurveyDto> surveys = surveyService.getSurveys();
        String surveyName = "Anket";
        String surveyDesc = "";
        for (MainView.SurveyDto s : surveys) {
            if (s.getId().equals(surveyId)) {
                surveyName = s.getName();
                surveyDesc = s.getDescription();
                break;
            }
        }

        List<QuestionService.QuestionDto> questions = questionService.getQuestionsBySurveyId(surveyId);
        List<ParticipationService.AnswerDto> answers = participationService.getAnswersBySurveyId(surveyId);

        Set<String> uniqueParticipants = new HashSet<>();
        for (ParticipationService.AnswerDto ans : answers) {
            if (ans.getParticipantName() != null) {
                uniqueParticipants.add(ans.getParticipantName());
            }
        }

        summaryParagraph.setText("Anket: " + surveyName + " | Açıklama: " + surveyDesc + " | Toplam Katılımcı: " + uniqueParticipants.size());
        
        // Üst kısma Excel/CSV İndir butonunu ekliyoruz
        topActionLayout.removeAll();
        if (!answers.isEmpty()) {
            StreamResource csvResource = new StreamResource("anket_sonuclari_" + surveyId + ".csv", () -> {
                StringBuilder sb = new StringBuilder();
                // Türkçe karakterlerin Excel'de düzgün görünmesi için BOM ekliyoruz
                sb.append("\ufeff");
                sb.append("Katılımcı Adı;Soru Metni;Soru Türü;Verilen Yanıt\n");
                
                for (ParticipationService.AnswerDto ans : answers) {
                    String qText = "Bilinmeyen Soru";
                    String qType = "-";
                    for (QuestionService.QuestionDto q : questions) {
                        if (q.getId().equals(ans.getQuestionId())) {
                            qText = q.getText().replace(";", ",");
                            qType = q.getType();
                            break;
                        }
                    }
                    String pName = ans.getParticipantName() != null ? ans.getParticipantName() : "Anonim";
                    String aText = ans.getAnswerText() != null ? ans.getAnswerText().replace(";", ",") : "";
                    
                    sb.append(pName).append(";")
                      .append(qText).append(";")
                      .append(qType).append(";")
                      .append(aText).append("\n");
                }
                return new java.io.ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));
            });

            Anchor downloadLink = new Anchor(csvResource, "CSV / Excel Olarak İndir");
            downloadLink.getElement().setAttribute("download", true);
            Button downloadBtn = new Button("CSV / Excel İndir");
            downloadBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
            downloadLink.add(downloadBtn);
            topActionLayout.add(downloadLink);
        }

        contentLayout.removeAll();

        for (int i = 0; i < questions.size(); i++) {
            QuestionService.QuestionDto q = questions.get(i);
            
            VerticalLayout questionBox = new VerticalLayout();
            questionBox.setWidthFull();
            questionBox.getStyle().set("border", "1px solid var(--lumo-contrast-10pct)");
            questionBox.getStyle().set("border-radius", "6px");
            questionBox.getStyle().set("padding", "15px");
            questionBox.getStyle().set("margin-bottom", "15px");

            H4 qTitle = new H4((i + 1) + ". " + q.getText() + " (" + q.getType() + ")");
            questionBox.add(qTitle);

            List<ParticipationService.AnswerDto> qAnswers = new ArrayList<>();
            for (ParticipationService.AnswerDto ans : answers) {
                if (ans.getQuestionId().equals(q.getId())) {
                    qAnswers.add(ans);
                }
            }

            if (qAnswers.isEmpty()) {
                questionBox.add(new Paragraph("Bu soruya henüz yanıt verilmemiş."));
            } else {
                boolean isChoiceBased = "Çoktan Seçmeli".equals(q.getType()) 
                        || "Onay Kutuları".equals(q.getType()) 
                        || "Açılır Menü".equals(q.getType());

                if (isChoiceBased) {
                    Map<String, Integer> optionCounts = new HashMap<>();
                    int totalSelectionCount = qAnswers.size();

                    for (ParticipationService.AnswerDto ans : qAnswers) {
                        String choice = ans.getAnswerText();
                        if (choice != null && !choice.isBlank()) {
                            optionCounts.put(choice, optionCounts.getOrDefault(choice, 0) + 1);
                        }
                    }

                    List<OptionStatDto> statsList = new ArrayList<>();
                    for (Map.Entry<String, Integer> entry : optionCounts.entrySet()) {
                        double percentage = totalSelectionCount > 0 ? (entry.getValue() * 100.0) / totalSelectionCount : 0.0;
                        statsList.add(new OptionStatDto(entry.getKey(), entry.getValue(), String.format(Locale.US, "%.1f", percentage) + "%"));
                    }

                    Grid<OptionStatDto> statGrid = new Grid<>(OptionStatDto.class);
                    statGrid.removeAllColumns();
                    statGrid.addColumn(OptionStatDto::getOptionText).setHeader("Seçenek").setAutoWidth(true);
                    statGrid.addColumn(OptionStatDto::getCount).setHeader("Seçilme Sayısı").setAutoWidth(true);
                    statGrid.addColumn(OptionStatDto::getPercentage).setHeader("Yüzdelik Dilim").setAutoWidth(true);
                    statGrid.setItems(statsList);
                    statGrid.setHeight("150px");
                    statGrid.setWidthFull();

                    questionBox.add(statGrid);

                } else {
                    VerticalLayout answersListLayout = new VerticalLayout();
                    answersListLayout.setPadding(false);
                    answersListLayout.setSpacing(false);

                    for (ParticipationService.AnswerDto ans : qAnswers) {
                        String pName = ans.getParticipantName() != null ? ans.getParticipantName() : "Anonim";
                        String text = ans.getAnswerText() != null ? ans.getAnswerText() : "";
                        Paragraph p = new Paragraph("• " + pName + ": " + text);
                        p.getStyle().set("margin", "4px 0");
                        answersListLayout.add(p);
                    }
                    questionBox.add(answersListLayout);
                }
            }

            contentLayout.add(questionBox);
        }
    }

    public static class OptionStatDto {
        private String optionText;
        private int count;
        private String percentage;

        public OptionStatDto(String optionText, int count, String percentage) {
            this.optionText = optionText;
            this.count = count;
            this.percentage = percentage;
        }

        public String getOptionText() { return optionText; }
        public int getCount() { return count; }
        public String getPercentage() { return percentage; }
    }
}