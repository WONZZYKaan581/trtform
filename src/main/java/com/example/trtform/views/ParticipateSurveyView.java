package com.example.trtform.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;

@Route("participate")
@PageTitle("Ankete Katıl | Dinamik Anket")
public class ParticipateSurveyView extends VerticalLayout implements HasUrlParameter<Long> {

    private final UserService userService;
    private final QuestionService questionService;
    private final ParticipationService participationService;
    private final VerticalLayout contentLayout = new VerticalLayout();
    private Long surveyId;
    private final List<QuestionAnswerComponent> questionComponents = new ArrayList<>();

    public ParticipateSurveyView(UserService userService, QuestionService questionService, ParticipationService participationService) {
        this.userService = userService;
        this.questionService = questionService;
        this.participationService = participationService;
    }

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

        List<QuestionService.QuestionDto> questions = questionService.getQuestionsBySurveyId(surveyId);

        if (questions.isEmpty()) {
            contentLayout.add(new Paragraph("Bu ankete henüz soru eklenmemiş."));
        } else {
            int index = 1;
            for (QuestionService.QuestionDto q : questions) {
                Paragraph qText = new Paragraph(index + ". " + q.getText());
                contentLayout.add(qText);

                if ("Çoktan Seçmeli".equals(q.getType())) {
                    RadioButtonGroup<String> radioGroup = new RadioButtonGroup<>();
                    radioGroup.setItems(q.getOptions());
                    contentLayout.add(radioGroup);
                    questionComponents.add(new QuestionAnswerComponent(q.getId(), radioGroup));
                } else if ("Onay Kutuları".equals(q.getType())) {
                    CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
                    checkboxGroup.setItems(q.getOptions());
                    contentLayout.add(checkboxGroup);
                    questionComponents.add(new QuestionAnswerComponent(q.getId(), checkboxGroup));
                } else if ("Açılır Menü".equals(q.getType())) {
                    ComboBox<String> comboBox = new ComboBox<>();
                    comboBox.setItems(q.getOptions());
                    comboBox.setPlaceholder("Seçiniz...");
                    comboBox.setWidthFull();
                    contentLayout.add(comboBox);
                    questionComponents.add(new QuestionAnswerComponent(q.getId(), comboBox));
                } else if ("Doğrusal Ölçek".equals(q.getType())) {
                    List<String> scaleOpts = new ArrayList<>();
                    if (q.getOptions() != null && q.getOptions().size() >= 2) {
                        try {
                            int min = Integer.parseInt(q.getOptions().get(0));
                            int max = Integer.parseInt(q.getOptions().get(1));
                            for (int i = min; i <= max; i++) {
                                scaleOpts.add(String.valueOf(i));
                            }
                        } catch (Exception e) {
                            scaleOpts = q.getOptions();
                        }
                    } else {
                        scaleOpts = List.of("1", "2", "3", "4", "5");
                    }

                    RadioButtonGroup<String> scaleGroup = new RadioButtonGroup<>();
                    scaleGroup.setItems(scaleOpts);
                    contentLayout.add(scaleGroup);
                    questionComponents.add(new QuestionAnswerComponent(q.getId(), scaleGroup));
                } else if ("Kısa Yanıt".equals(q.getType())) {
                    TextField textField = new TextField();
                    textField.setPlaceholder("Cevabınızı buraya yazın...");
                    textField.setWidthFull();
                    contentLayout.add(textField);
                    questionComponents.add(new QuestionAnswerComponent(q.getId(), textField));
                } else if ("Paragraf".equals(q.getType())) {
                    TextArea textArea = new TextArea();
                    textArea.setPlaceholder("Detaylı cevabınızı buraya yazın...");
                    textArea.setWidthFull();
                    contentLayout.add(textArea);
                    questionComponents.add(new QuestionAnswerComponent(q.getId(), textArea));
                }
                index++;
            }
        }

        Button submitButton = new Button("Anketi Gönder");
        submitButton.addClickListener(event -> {
            try {
                String respondentName = userService.getLoggedInUserFullName();

                for (QuestionAnswerComponent qa : questionComponents) {
                    String answerValue = "";

                    if (qa.component instanceof RadioButtonGroup) {
                        RadioButtonGroup<?> rb = (RadioButtonGroup<?>) qa.component;
                        if (rb.getValue() != null) {
                            answerValue = rb.getValue().toString();
                        }
                    } else if (qa.component instanceof CheckboxGroup) {
                        CheckboxGroup<?> cb = (CheckboxGroup<?>) qa.component;
                        if (cb.getValue() != null) {
                            answerValue = cb.getValue().toString();
                        }
                    } else if (qa.component instanceof ComboBox) {
                        ComboBox<?> cmb = (ComboBox<?>) qa.component;
                        if (cmb.getValue() != null) {
                            answerValue = cmb.getValue().toString();
                        }
                    } else if (qa.component instanceof TextField) {
                        TextField tf = (TextField) qa.component;
                        answerValue = tf.getValue();
                    } else if (qa.component instanceof TextArea) {
                        TextArea ta = (TextArea) qa.component;
                        answerValue = ta.getValue();
                    }

                    if (answerValue != null && !answerValue.trim().isEmpty() && !answerValue.equals("[]")) {
                        participationService.saveAnswer(surveyId, qa.questionId, answerValue, respondentName);
                    }
                }

                Notification.show("Cevaplarınız başarıyla kaydedildi! Teşekkür ederiz.", 4000, Notification.Position.MIDDLE);
                submitButton.setEnabled(false);
                contentLayout.add(new Paragraph("Anket başarıyla gönderildi. Teşekkür ederiz."));

            } catch (Exception e) {
                e.printStackTrace();
                Notification.show("Kayıt sırasında hata oluştu: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitButton.setWidthFull();

        contentLayout.add(submitButton);
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

    private static class QuestionAnswerComponent {
        private final Long questionId;
        private final Component component;

        public QuestionAnswerComponent(Long questionId, Component component) {
            this.questionId = questionId;
            this.component = component;
        }
    }
}