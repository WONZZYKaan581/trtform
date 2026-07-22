package com.example.trtform.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;

@Route("participate")
@PageTitle("Ankete Katıl | Dinamik Anket")
public class ParticipateSurveyView extends VerticalLayout implements HasUrlParameter<Long> {

    private final VerticalLayout contentLayout = new VerticalLayout();
    private Long surveyId;
    
    // Soru ID'leri ile bileşenleri eşleştirmek için liste tutalım
    private final List<QuestionAnswerComponent> questionComponents = new ArrayList<>();

    @Override
    public void setParameter(BeforeEvent event, Long parameter) {
        this.surveyId = parameter;
        loadSurveyContent();
    }

    private void loadSurveyContent() {
        contentLayout.removeAll();
        questionComponents.clear();

        H2 title = new H2("Anket Yanıtlama Ekranı");
        Paragraph desc = new Paragraph("Lütfen aşağıdaki soruları yanıtlayıp gönder butonuna tıklayınız.");
        contentLayout.add(title, desc);

        List<QuestionService.QuestionDto> questions = QuestionService.getQuestionsBySurveyId(surveyId);

        if (questions.isEmpty()) {
            contentLayout.add(new Paragraph("Bu ankete henüz soru eklenmemiş."));
        } else {
            int index = 1;
            for (QuestionService.QuestionDto q : questions) {
                Paragraph qText = new Paragraph(index + ". " + q.getText());
                contentLayout.add(qText);

                if ("Çoktan Seçmeli".equals(q.getType())) {
                    CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
                    checkboxGroup.setItems(q.getOptions());
                    contentLayout.add(checkboxGroup);
                    questionComponents.add(new QuestionAnswerComponent(q.getId(), checkboxGroup));
                } else if ("Metin Alanı".equals(q.getType())) {
                    TextArea textArea = new TextArea();
                    textArea.setPlaceholder("Cevabınızı buraya yazın...");
                    textArea.setWidthFull();
                    contentLayout.add(textArea);
                    questionComponents.add(new QuestionAnswerComponent(q.getId(), textArea));
                }
                index++;
            }
        }

        Button submitButton = new Button("Anketi Gönder");
        submitButton.addClickListener(event -> {
            // Yanıtları kaydet
            for (QuestionAnswerComponent qa : questionComponents) {
                String answerValue = "";
                if (qa.component instanceof CheckboxGroup) {
                    CheckboxGroup<?> cb = (CheckboxGroup<?>) qa.component;
                    answerValue = cb.getValue().toString(); // Seçilen şıklar
                } else if (qa.component instanceof TextArea) {
                    TextArea ta = (TextArea) qa.component;
                    answerValue = ta.getValue(); // Girilen metin
                }
                
                if (answerValue != null && !answerValue.isEmpty() && !answerValue.equals("[]")) {
                    ParticipationService.saveAnswer(surveyId, qa.questionId, answerValue);
                }
            }

            Notification.show("Cevaplarınız başarıyla kaydedildi! Teşekkür ederiz.", 4000, Notification.Position.MIDDLE);
            getUI().ifPresent(ui -> ui.navigate(""));
        });
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitButton.setWidthFull();

        Button backButton = new Button("Geri Dön");
        backButton.addClickListener(event -> {
            getUI().ifPresent(ui -> ui.navigate(""));
        });
        backButton.setWidthFull();

        contentLayout.add(submitButton, backButton);
        contentLayout.setWidth("550px");
        contentLayout.setAlignItems(Alignment.STRETCH);
        contentLayout.getStyle().set("background", "var(--lumo-base-color)");
        contentLayout.getStyle().set("border-radius", "8px");
        contentLayout.getStyle().set("box-shadow", "var(--lumo-box-shadow-m)");

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        if (indexOf(contentLayout) == -1) {
            add(contentLayout);
        }
    }

    // Yardımcı sınıf: Soru ID ile arayüz bileşenini eşleştirmek için
    private static class QuestionAnswerComponent {
        private Long questionId;
        private Component component;

        public QuestionAnswerComponent(Long questionId, Component component) {
            this.questionId = questionId;
            this.component = component;
        }
    }
}