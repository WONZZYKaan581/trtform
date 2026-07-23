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
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEnterEvent;

import java.util.ArrayList;
import java.util.List;

@Route("add-question")
@PageTitle("Soru Ekle | Dinamik Anket")
public class AddQuestionView extends VerticalLayout implements BeforeEnterObserver {

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Eğer giriş yapılmamışsa veya admin değilse anasayfaya yönlendir
        if (UserService.getLoggedInUser() == null) {
            Notification.show("Bu sayfaya erişmek için giriş yapmalısınız!", 3000, Notification.Position.MIDDLE);
            event.forwardTo("login");
        }
    }

    private final List<TextField> optionFields = new ArrayList<>();
    private final VerticalLayout optionsLayout = new VerticalLayout();

    public AddQuestionView() {
        H2 title = new H2("Ankete Soru Ekle");

        // Anket Seçimi
        ComboBox<MainView.SurveyDto> surveyComboBox = new ComboBox<>("Anket Seç");
        surveyComboBox.setItems(SurveyService.getSurveys());
        surveyComboBox.setItemLabelGenerator(MainView.SurveyDto::getName);
        surveyComboBox.setWidthFull();

        // Soru Metni
        TextField questionTextField = new TextField("Soru Metni");
        questionTextField.setPlaceholder("Örn: Bu etkinliği nasıl buldunuz?");
        questionTextField.setWidthFull();

        // Soru Türü Seçimi (Tüm Google Formlar Tipleri Eklendi)
        ComboBox<String> questionTypeBox = new ComboBox<>("Soru Türü");
        questionTypeBox.setItems(
            "Çoktan Seçmeli", 
            "Onay Kutuları", 
            "Açılır Menü", 
            "Doğrusal Ölçek", 
            "Kısa Yanıt", 
            "Paragraf"
        );
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

        // Soru türü değiştiğinde dinamik alan ayarları
        questionTypeBox.addValueChangeListener(event -> {
            String selectedType = event.getValue();
            optionsLayout.removeAll();
            optionFields.clear();

            boolean needsOptions = "Çoktan Seçmeli".equals(selectedType) || 
                                   "Onay Kutuları".equals(selectedType) || 
                                   "Açılır Menü".equals(selectedType);

            boolean isScale = "Doğrusal Ölçek".equals(selectedType);

            optionsLayout.setVisible(needsOptions || isScale);
            addOptionButton.setVisible(needsOptions);

            if (needsOptions) {
                // Varsayılan 2 seçenek kutusu ekle
                for (int i = 0; i < 2; i++) {
                    TextField opt = new TextField("Seçenek " + (i + 1));
                    opt.setWidthFull();
                    optionFields.add(opt);
                    optionsLayout.add(opt);
                }
            } else if (isScale) {
                // Ölçek için varsayılan değerler (Örn: 1 ile 5 arası)
                TextField minField = new TextField("Başlangıç Değeri (Genelde 1)");
                minField.setValue("1");
                minField.setWidthFull();
                
                TextField maxField = new TextField("Bitiş Değeri (Örn: 5 veya 10)");
                maxField.setValue("5");
                maxField.setWidthFull();

                optionFields.add(minField);
                optionFields.add(maxField);
                optionsLayout.add(minField, maxField);
            }
        });

        // Başlangıçta (Çoktan Seçmeli için) 2 tane varsayılan seçenek kutusu ekle
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
            for (TextField tf : optionFields) {
                if (tf != null && !tf.isEmpty()) {
                    optionsList.add(tf.getValue());
                }
            }

            // Soruyu ve seçenekleri/ölçeği servise kaydet
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