package com.example.trtform.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("edit-survey")
@PageTitle("Anketi Düzenle | Dinamik Anket")
public class EditSurveyView extends VerticalLayout implements HasUrlParameter<Long> {

    private final SurveyService surveyService;
    private Long surveyId;
    private final TextField nameField = new TextField("Anket Adı");
    private final TextArea descArea = new TextArea("Anket Açıklaması");

    public EditSurveyView(SurveyService surveyService) {
        this.surveyService = surveyService;

        H2 title = new H2("Anketi Düzenle");

        nameField.setRequired(true);
        nameField.setWidthFull();

        descArea.setWidthFull();

        Button updateButton = new Button("Değişiklikleri Kaydet", event -> {
            if (nameField.isEmpty()) {
                Notification.show("Anket adı boş bırakılamaz!", 3000, Notification.Position.MIDDLE);
                return;
            }

            surveyService.updateSurvey(surveyId, nameField.getValue(), descArea.getValue());

            Notification.show("Anket başarıyla güncellendi!", 3000, Notification.Position.MIDDLE);
            getUI().ifPresent(ui -> ui.navigate(""));
        });
        updateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        updateButton.setWidthFull();

        Button backButton = new Button("Geri Dön", event -> {
            getUI().ifPresent(ui -> ui.navigate(""));
        });
        backButton.setWidthFull();

        VerticalLayout formLayout = new VerticalLayout(title, nameField, descArea, updateButton, backButton);
        formLayout.setWidth("450px");
        formLayout.setAlignItems(Alignment.STRETCH);
        formLayout.setPadding(true);
        formLayout.getStyle().set("background", "var(--lumo-base-color)");
        formLayout.getStyle().set("border-radius", "8px");
        formLayout.getStyle().set("box-shadow", "var(--lumo-box-shadow-m)");

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        add(formLayout);
    }

    @Override
    public void setParameter(BeforeEvent event, Long parameter) {
        this.surveyId = parameter;

        MainView.SurveyDto survey = surveyService.getSurveys().stream()
                .filter(s -> s.getId().equals(surveyId))
                .findFirst()
                .orElse(null);

        if (survey != null) {
            nameField.setValue(survey.getName() != null ? survey.getName() : "");
            descArea.setValue(survey.getDescription() != null ? survey.getDescription() : "");
        }
    }
}