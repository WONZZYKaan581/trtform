package com.example.trtform.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;

@Route("add-question")
@PageTitle("Soru Ekle | Dinamik Anket")
public class AddQuestionView extends VerticalLayout {

    private final VerticalLayout optionsLayout = new VerticalLayout();
    private final List<TextField> optionFields = new ArrayList<>();

    public AddQuestionView() {
        H2 title = new H2("Ankete Soru Ekle");

        // Anket Seçimi
        ComboBox<MainView.SurveyDto> surveyComboBox = new ComboBox<>("Anket Seç");
        surveyComboBox.setItems(SurveyService.getSurveys());
        surveyComboBox.setItemLabelGenerator(MainView.SurveyDto::getName);
        surveyComboBox.setWidthFull();

        // Soru Metni
        TextField questionTextField = new TextField("Soru Metni");
        questionTextField.setPlaceholder("Örn: Bu ürünü tavsiye eder misiniz?");
        questionTextField.setWidthFull();

        // Soru Türü Seçimi
        ComboBox<String> questionTypeBox = new ComboBox<>("Soru Türü");
        questionTypeBox.setItems("Çoktan Seçmeli", "Metin Alanı");
        questionTypeBox.setValue("Çoktan Seçmeli");
        questionTypeBox.setWidthFull();

        // Seçenekler Alanı
        optionsLayout.setPadding(false);
        
        Button addOptionButton = new Button("Seçenek Ekle", event -> {
            TextField optionField = new TextField("Seçenek Metni");
            optionField.setWidthFull();
            optionFields.add(optionField);
            optionsLayout.add(optionField);
        });
        addOptionButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        // Soru türü değiştiğinde seçenek kutularını göster/gizle
        questionTypeBox.addValueChangeListener(event -> {
            boolean isMultiple = "Çoktan Seçmeli".equals(event.getValue());
            optionsLayout.setVisible(isMultiple);
            addOptionButton.setVisible(isMultiple);
        });

        // Başlangıçta 2 tane varsayılan seçenek kutusu ekle
        for (int i = 0; i < 2; i++) {
            TextField opt = new TextField("Seçenek " + (i + 1));
            opt.setWidthFull();
            optionFields.add(opt);
            optionsLayout.add(opt);
        }

        // Soruyu Kaydet Butonu
        Button saveQuestionButton = new Button("Soruyu Kaydet", event -> {
            if (surveyComboBox.isEmpty() || questionTextField.isEmpty()) {
                Notification.show("Lütfen anket seçin ve soru metnini yazın!", 3000, Notification.Position.MIDDLE);
                return;
            }

            Long selectedSurveyId = surveyComboBox.getValue().getId();
            String qText = questionTextField.getValue();
            String qType = questionTypeBox.getValue();
            
            List<String> optionsList = new ArrayList<>();
            if ("Çoktan Seçmeli".equals(qType)) {
                for (TextField tf : optionFields) {
                    if (tf != null && !tf.isEmpty()) {
                        optionsList.add(tf.getValue());
                    }
                }
            }

            // Soruyu ve seçenekleri servise kaydet
            QuestionService.addQuestion(selectedSurveyId, qText, qType, optionsList);

            Notification.show("Soru başarıyla eklendi!", 3000, Notification.Position.MIDDLE);
            getUI().ifPresent(ui -> ui.navigate(""));
        });
        saveQuestionButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveQuestionButton.setWidthFull();

        Button backButton = new Button("Geri Dön", event -> {
            getUI().ifPresent(ui -> ui.navigate(""));
        });
        backButton.setWidthFull();

        VerticalLayout formLayout = new VerticalLayout(
            title, surveyComboBox, questionTextField, questionTypeBox, 
            new Hr(), optionsLayout, addOptionButton, new Hr(), 
            saveQuestionButton, backButton
        );
        formLayout.setWidth("500px");
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