package com.example.trtform.views;

import com.example.trtform.service.ParticipationService;
import com.example.trtform.service.QuestionService;
import com.example.trtform.service.UserService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;

@Route("participate")
@PageTitle("Ankete Katıl | Dinamik Anket")
public class ParticipateSurveyView extends VerticalLayout implements HasUrlParameter<Long>, BeforeEnterObserver {

    private final UserService userService;
    private final QuestionService questionService;
    private final ParticipationService participationService;
    
    // Tüm kartları (başlık, sorular, buton) tutacak ana taşıyıcı
    private final VerticalLayout formContainer = new VerticalLayout(); 
    
    private Long surveyId;
    private final List<QuestionAnswerComponent> questionComponents = new ArrayList<>();

    public ParticipateSurveyView(UserService userService, QuestionService questionService, ParticipationService participationService) {
        this.userService = userService;
        this.questionService = questionService;
        this.participationService = participationService;

        // --- SAYFA ARKA PLANI VE HİZALAMA ---
        setSizeFull();
        getStyle().set("background", "linear-gradient(135deg, #f0f4f8 0%, #d9e2ec 100%)");
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.START);
        getStyle().set("overflow-y", "auto");
        setPadding(true);

        // Ana form taşıyıcısının (konteynerin) ayarları
        formContainer.setWidth("750px");
        formContainer.setMaxWidth("100%");
        formContainer.setPadding(false);
        formContainer.setSpacing(true);
        
        add(formContainer);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!userService.isLoggedIn()) {
            Notification.show("Anketi doldurmak için giriş yapmanız gerekiyor.", 3000, Notification.Position.MIDDLE);
            event.forwardTo("login");
        }
    }

    @Override
    public void setParameter(BeforeEvent event, Long parameter) {
        this.surveyId = parameter;
        loadSurveyContent();
    }

    private void loadSurveyContent() {
        formContainer.removeAll();
        questionComponents.clear();

        // 1. BAŞLIK KARTI
        VerticalLayout headerCard = createCard();
        headerCard.getStyle().set("border-top", "10px solid var(--lumo-primary-color)");
        
        H2 title = new H2("📝 Anket Yanıtlama Ekranı");
        title.getStyle().set("margin-top", "0").set("margin-bottom", "5px");
        
        Paragraph desc = new Paragraph("Lütfen aşağıdaki soruları inceleyip, size en uygun olanı işaretleyiniz. Yanıtlarınız başarıyla kaydedilecektir.");
        desc.getStyle().set("color", "var(--lumo-secondary-text-color)").set("margin", "0");
        
        headerCard.add(title, desc);
        formContainer.add(headerCard);

        // Soruları Çek
        List<QuestionService.QuestionDto> questions = questionService.getQuestionsBySurveyId(surveyId);

        if (questions.isEmpty()) {
            VerticalLayout emptyCard = createCard();
            emptyCard.add(new Paragraph("Bu ankete henüz soru eklenmemiş."));
            formContainer.add(emptyCard);
        } else {
            // 2. SORU KARTLARI
            int index = 1;
            for (QuestionService.QuestionDto q : questions) {
                
                VerticalLayout questionCard = createCard();
                
                H4 qText = new H4(index + ". " + q.getText());
                qText.getStyle().set("margin-top", "0");
                questionCard.add(qText);

                if ("Çoktan Seçmeli".equals(q.getType())) {
                    RadioButtonGroup<String> radioGroup = new RadioButtonGroup<>();
                    radioGroup.setItems(q.getOptions());
                    questionCard.add(radioGroup);
                    questionComponents.add(new QuestionAnswerComponent(q.getId(), radioGroup));
                
                } else if ("Onay Kutuları".equals(q.getType())) {
                    CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
                    checkboxGroup.setItems(q.getOptions());
                    questionCard.add(checkboxGroup);
                    questionComponents.add(new QuestionAnswerComponent(q.getId(), checkboxGroup));
                
                } else if ("Açılır Menü".equals(q.getType())) {
                    ComboBox<String> comboBox = new ComboBox<>();
                    comboBox.setItems(q.getOptions());
                    comboBox.setPlaceholder("Seçiniz...");
                    comboBox.setWidthFull();
                    questionCard.add(comboBox);
                    questionComponents.add(new QuestionAnswerComponent(q.getId(), comboBox));
                
                } else if ("Doğrusal Ölçek".equals(q.getType())) {
                    String minVal = "1";
                    String maxVal = "5";
                    if (q.getOptions() != null && q.getOptions().size() >= 2) {
                        minVal = q.getOptions().get(0);
                        maxVal = q.getOptions().get(1);
                    }
                    
                    // --- SÜRÜKLEMELİ ÇUBUK (SLIDER) TASARIMI ---
                    
                    // Seçilen değeri anlık gösteren rozet (Görünmez olmaması için kesin renkler verildi)
                    Span valueBadge = new Span(minVal);
                    valueBadge.getStyle()
                        .set("background", "#2563eb") // Kesin Mavi Arka Plan
                        .set("color", "#ffffff")      // Kesin Beyaz Yazı
                        .set("padding", "4px 16px")
                        .set("border-radius", "16px")
                        .set("font-weight", "bold")
                        .set("font-size", "16px")
                        .set("box-shadow", "0 2px 4px rgba(0,0,0,0.2)");

                    // HTML5 Native Slider (Range Input)
                    Input slider = new Input();
                    slider.setType("range");
                    slider.getElement().setProperty("min", minVal);
                    slider.getElement().setProperty("max", maxVal);
                    slider.getElement().setProperty("step", "1"); // Sadece tam sayılarda atlaması için eklendi!
                    slider.setValue(minVal); 
                    slider.getStyle().set("width", "100%");
                    slider.getStyle().set("cursor", "pointer");

                    // Sürükleme anında rozetteki sayıyı canlı olarak güncelle
                    slider.setValueChangeMode(ValueChangeMode.EAGER);
                    slider.addValueChangeListener(e -> {
                        if(e.getValue() != null && !e.getValue().isEmpty()) {
                            valueBadge.setText(e.getValue());
                        }
                    });

                    // Sınır etiketleri (Min ve Max) ve Çubuk
                    HorizontalLayout sliderRow = new HorizontalLayout();
                    sliderRow.setWidthFull();
                    sliderRow.setAlignItems(Alignment.CENTER);

                    Span minLabel = new Span(minVal);
                    minLabel.getStyle().set("font-weight", "bold").set("color", "var(--lumo-secondary-text-color)");
                    
                    Span maxLabel = new Span(maxVal);
                    maxLabel.getStyle().set("font-weight", "bold").set("color", "var(--lumo-secondary-text-color)");

                    sliderRow.add(minLabel, slider, maxLabel);
                    sliderRow.expand(slider); 

                    // Tüm Slider alanını saran kutu
                    VerticalLayout scaleWrapper = new VerticalLayout(valueBadge, sliderRow);
                    scaleWrapper.setPadding(true);
                    scaleWrapper.setAlignItems(Alignment.CENTER);
                    scaleWrapper.getStyle().set("background", "var(--lumo-contrast-5pct)");
                    scaleWrapper.getStyle().set("border-radius", "8px");
                    
                    questionCard.add(scaleWrapper);
                    questionComponents.add(new QuestionAnswerComponent(q.getId(), slider));
                    // -------------------------------------------
                
                } else if ("Kısa Yanıt".equals(q.getType())) {
                    TextField textField = new TextField();
                    textField.setPlaceholder("Kısa yanıtınızı buraya yazın...");
                    textField.setWidthFull();
                    questionCard.add(textField);
                    questionComponents.add(new QuestionAnswerComponent(q.getId(), textField));
                
                } else if ("Paragraf".equals(q.getType())) {
                    TextArea textArea = new TextArea();
                    textArea.setPlaceholder("Detaylı yanıtınızı buraya yazın...");
                    textArea.setWidthFull();
                    questionCard.add(textArea);
                    questionComponents.add(new QuestionAnswerComponent(q.getId(), textArea));
                }
                
                formContainer.add(questionCard);
                index++;
            }
        }

        // 3. GÖNDER BUTONU ALANI
        Button submitButton = new Button("Anketi Gönder", new Icon(VaadinIcon.PAPERPLANE));
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        submitButton.setWidthFull();
        
        submitButton.addClickListener(event -> {
            try {
                if (!userService.isLoggedIn()) {
                    Notification.show("Anketi göndermek için giriş yapmanız gerekiyor.", 3000, Notification.Position.MIDDLE);
                    getUI().ifPresent(ui -> ui.navigate("login"));
                    return;
                }

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
                    else if (qa.component instanceof Input) {
                        Input sliderInput = (Input) qa.component;
                        if (sliderInput.getValue() != null) {
                            answerValue = sliderInput.getValue();
                        }
                    }

                    if (answerValue != null && !answerValue.trim().isEmpty() && !answerValue.equals("[]")) {
                        participationService.saveAnswer(surveyId, qa.questionId, answerValue, respondentName);
                    }
                }

                Notification.show("Cevaplarınız başarıyla kaydedildi! Teşekkür ederiz.", 4000, Notification.Position.MIDDLE);
                
                formContainer.removeAll();
                VerticalLayout successCard = createCard();
                successCard.setAlignItems(Alignment.CENTER);
                successCard.getStyle().set("border-top", "10px solid var(--lumo-success-color)");
                
                H2 successTitle = new H2("🎉 Anket Tamamlandı!");
                Paragraph successDesc = new Paragraph("Yanıtlarınız kaydedildi. Değerli vaktinizi ayırdığınız için teşekkür ederiz.");
                Button goHomeButton = new Button("Ana Sayfaya Dön", e -> getUI().ifPresent(ui -> ui.navigate("")));
                goHomeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                
                successCard.add(successTitle, successDesc, goHomeButton);
                formContainer.add(successCard);

            } catch (Exception e) {
                e.printStackTrace();
                Notification.show("Kayıt sırasında hata oluştu: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });

        VerticalLayout submitArea = new VerticalLayout(submitButton);
        submitArea.setPadding(false);
        submitArea.getStyle().set("margin-top", "10px");
        formContainer.add(submitArea);
    }

    private VerticalLayout createCard() {
        VerticalLayout card = new VerticalLayout();
        card.getStyle().set("background", "white");
        card.getStyle().set("border-radius", "12px");
        card.getStyle().set("box-shadow", "0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)");
        card.setPadding(true);
        card.setSpacing(true);
        card.setWidthFull();
        return card;
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