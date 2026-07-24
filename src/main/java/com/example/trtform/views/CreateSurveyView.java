package com.example.trtform.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("create-survey")
@PageTitle("Yeni Anket Oluştur | Dinamik Anket")
public class CreateSurveyView extends VerticalLayout {

    private final SurveyService surveyService;

    public CreateSurveyView(SurveyService surveyService) {
        this.surveyService = surveyService;

        H2 title = new H2("Yeni Anket Oluştur");

        TextField nameField = new TextField("Anket Adı");
        nameField.setRequired(true);
        nameField.setWidthFull();

        TextArea descArea = new TextArea("Anket Açıklaması (İsteğe bağlı)");
        descArea.setWidthFull();

        Button saveButton = new Button("Anketi Kaydet", event -> {
    String titleValue = nameField.getValue();
    String descValue = descArea.getValue();

    if (titleValue == null || titleValue.trim().isEmpty()) {
        Notification.show("Anket adı boş bırakılamaz!", 3000, Notification.Position.MIDDLE);
        return;
    }

    try {
        surveyService.addSurvey(titleValue, descValue);
        Notification.show("Anket başarıyla oluşturuldu!", 3000, Notification.Position.MIDDLE);
        getUI().ifPresent(ui -> ui.navigate(""));
    } catch (Exception e) {
        e.printStackTrace(); // Hata olursa terminale yazdırır
        Notification.show("Kayıt sırasında hata oluştu: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
    }
});
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.setWidthFull();

        Button backButton = new Button("Geri Dön", event -> {
            getUI().ifPresent(ui -> ui.navigate(""));
        });
        backButton.setWidthFull();

        VerticalLayout formLayout = new VerticalLayout(title, nameField, descArea, saveButton, backButton);
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
}