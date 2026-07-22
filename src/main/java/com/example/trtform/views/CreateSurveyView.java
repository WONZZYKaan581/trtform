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

    public CreateSurveyView() {
        H2 title = new H2("Yeni Anket Oluştur");

        TextField nameField = new TextField("Anket Adı");
        nameField.setRequired(true);
        nameField.setWidthFull();

        TextArea descArea = new TextArea("Anket Açıklaması (İsteğe bağlı)");
        descArea.setWidthFull();

        Button saveButton = new Button("Anketi Kaydet", event -> {
            if (nameField.isEmpty()) {
                Notification.show("Anket adı boş bırakılamaz!", 3000, Notification.Position.MIDDLE);
                return;
            }

            SurveyService.addSurvey(nameField.getValue(), descArea.getValue());
            Notification.show("Anket başarıyla oluşturuldu!", 3000, Notification.Position.MIDDLE);
            getUI().ifPresent(ui -> ui.navigate(""));
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

        // Sayfayı tam ortala
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        add(formLayout);
    }
}