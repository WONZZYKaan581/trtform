package com.example.trtform.views;

import com.example.trtform.service.QuestionService;
import com.example.trtform.service.SurveyService;
import com.example.trtform.service.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.UI;

import java.util.ArrayList;
import java.util.List;

@Route("")
@PageTitle("Ana Sayfa | Dinamik Anket")
public class MainView extends VerticalLayout {

    private final UserService userService;
    private final SurveyService surveyService;
    private final QuestionService questionService;
    private Grid<SurveyDto> surveyGrid;

    public MainView(UserService userService, SurveyService surveyService, QuestionService questionService) {
        this.userService = userService;
        this.surveyService = surveyService;
        this.questionService = questionService;

        if (userService.getLoggedInUser() == null) {
            add(new Span("Giriş ekranına yönlendiriliyorsunuz..."));
            UI ui = UI.getCurrent();
            if (ui != null) {
                ui.navigate("login");
            }
            return;
        }

        // --- SAYFA ARKA PLANI VE GENEL AYARLAR ---
        setSizeFull();
        getStyle().set("background", "linear-gradient(135deg, #f0f4f8 0%, #d9e2ec 100%)"); // Soft mavi/gri gradyan
        setAlignItems(Alignment.CENTER);
        setPadding(true);

        // --- ANA KART (İçerikleri saran şık beyaz kutu) ---
        VerticalLayout mainCard = new VerticalLayout();
        mainCard.setWidth("1200px");
        mainCard.setMaxWidth("100%");
        mainCard.getStyle().set("background", "white");
        mainCard.getStyle().set("border-radius", "16px");
        mainCard.getStyle().set("box-shadow", "0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)");
        mainCard.setPadding(true);
        mainCard.setSpacing(true);

        // Başlık
        H1 title = new H1("Dinamik Anket Uygulaması");
        title.getStyle().set("color", "var(--lumo-primary-text-color)");
        title.getStyle().set("font-size", "28px");
        title.getStyle().set("margin", "0");

        HorizontalLayout actionButtons = new HorizontalLayout();
        actionButtons.setAlignItems(Alignment.CENTER);

        Button createSurveyButton = new Button("Yeni Anket", new Icon(VaadinIcon.PLUS_CIRCLE));
        createSurveyButton.addClickListener(event -> {
            if (userService.getLoggedInUser() == null) {
                Notification.show("Anket oluşturmak için önce giriş yapmalısınız!", 3000, Notification.Position.MIDDLE);
                getUI().ifPresent(ui -> ui.navigate("login"));
            } else {
                openCreateSurveyDialog();
            }
        });
        createSurveyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        actionButtons.add(createSurveyButton);

        if (userService.getLoggedInUser() == null) {
            Button loginButton = new Button("Giriş Yap", new Icon(VaadinIcon.SIGN_IN));
            loginButton.addClickListener(event -> {
                getUI().ifPresent(ui -> ui.navigate("login"));
            });
            actionButtons.add(loginButton);
        } else {
            Span welcomeText = new Span("👋 Hoş geldin, " + userService.getLoggedInUser());
            welcomeText.getStyle().set("font-weight", "600").set("color", "var(--lumo-secondary-text-color)");
            
            Button adminPanelButton = new Button("Admin", new Icon(VaadinIcon.COG));
            adminPanelButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("admin-panel")));
            adminPanelButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
            
            Button logoutButton = new Button("Çıkış", new Icon(VaadinIcon.SIGN_OUT));
            logoutButton.addClickListener(event -> {
                userService.logout();
                Notification.show("Çıkış yapıldı.", 2000, Notification.Position.MIDDLE);
                getUI().ifPresent(ui -> ui.getPage().reload());
            });
            logoutButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            
            actionButtons.add(welcomeText, adminPanelButton, logoutButton);
        }

        surveyGrid = new Grid<>(SurveyDto.class);
        surveyGrid.removeAllColumns();
        // Tabloyu daha şık hale getir: Satır çizgileri ve kenarlıksız görünüm
        surveyGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);

        surveyGrid.addColumn(survey -> survey.getId()).setHeader("ID").setAutoWidth(true).setFlexGrow(0);
        surveyGrid.addColumn(survey -> survey.getName()).setHeader("Anket Adı").setSortable(true).setFlexGrow(1);
        surveyGrid.addColumn(survey -> survey.getDescription()).setHeader("Açıklama").setFlexGrow(2);

        surveyGrid.addComponentColumn(survey -> {
            HorizontalLayout rowButtons = new HorizontalLayout();
            rowButtons.setSpacing(true);

            String currentUsername = userService.getLoggedInUser();
            boolean isOwner = currentUsername != null && currentUsername.equals(survey.getCreator());
            boolean canManageSurvey = isOwner || userService.isAdmin();

            if (canManageSurvey) {
                Button addQuestionBtn = new Button(new Icon(VaadinIcon.PLUS));
                addQuestionBtn.setTooltipText("Soru Ekle");
                addQuestionBtn.addClickListener(e -> openAddQuestionDialog(survey));
                addQuestionBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_ICON);
                
                Button editSurveyBtn = new Button(new Icon(VaadinIcon.EDIT));
                editSurveyBtn.setTooltipText("Düzenle");
                editSurveyBtn.addClickListener(e -> openEditSurveyDialog(survey));
                editSurveyBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ICON);

                Button shareButton = new Button(new Icon(VaadinIcon.SHARE));
                shareButton.setTooltipText("Paylaş");
                shareButton.addClickListener(event -> {
                    String baseUrl = "http://localhost:8080";
                    String surveyUrl = baseUrl + "/participate/" + survey.getId();

                    Dialog dialog = new Dialog();
                    dialog.setHeaderTitle("Anket Paylaşım Bağlantısı");

                    TextField linkField = new TextField();
                    linkField.setValue(surveyUrl);
                    linkField.setReadOnly(true);
                    linkField.setWidthFull();
                    linkField.getElement().addEventListener("click", clickEvent -> {
                        getUI().ifPresent(ui -> ui.getPage().executeJs(
                                "navigator.clipboard.writeText($0).then(() => {}).catch(() => {});",
                                surveyUrl
                        ));
                        Notification.show("Bağlantı panoya kopyalandı.", 2000, Notification.Position.MIDDLE);
                    });

                    VerticalLayout dialogLayout = new VerticalLayout(new Span("Bağlantıya tıklayarak kopyalayabilirsiniz:"), linkField);
                    dialogLayout.setPadding(false);

                    Button closeButton = new Button("Kapat", e -> dialog.close());
                    dialog.add(dialogLayout);
                    dialog.getFooter().add(closeButton);
                    dialog.open();
                });
                shareButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);

                Button manageQuestionsBtn = new Button("Sorular", new Icon(VaadinIcon.LIST));
                manageQuestionsBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("manage-questions/" + survey.getId())));
                manageQuestionsBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_CONTRAST);

                Button resultsBtn = new Button("Sonuçlar", new Icon(VaadinIcon.PIE_CHART));
                resultsBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("survey-results/" + survey.getId())));
                resultsBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);

                Button deleteBtn = new Button(new Icon(VaadinIcon.TRASH));
                deleteBtn.setTooltipText("Sil");
                deleteBtn.addClickListener(e -> {
                    surveyService.deleteSurvey(survey.getId());
                    refreshGrid();
                    Notification.show("Anket silindi.", 2000, Notification.Position.MIDDLE);
                });
                deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_ICON);

                rowButtons.add(addQuestionBtn, editSurveyBtn, shareButton, manageQuestionsBtn, resultsBtn, deleteBtn);
            } else {
                Span infoSpan = new Span("Katılımcı Görünümü");
                infoSpan.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "var(--lumo-font-size-s)");
                rowButtons.add(infoSpan);
            }

            return rowButtons;

        }).setHeader("İşlemler").setAutoWidth(true);

        surveyGrid.addItemClickListener(event -> {
            Long selectedSurveyId = event.getItem().getId();
            getUI().ifPresent(ui -> ui.navigate(ParticipateSurveyView.class, selectedSurveyId));
        });

        surveyGrid.setWidthFull();
        refreshGrid();

        HorizontalLayout headerLayout = new HorizontalLayout(title, actionButtons);
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        headerLayout.setAlignItems(Alignment.CENTER);
        headerLayout.getStyle().set("border-bottom", "1px solid var(--lumo-contrast-10pct)");
        headerLayout.getStyle().set("padding-bottom", "15px");

        mainCard.add(headerLayout, surveyGrid);
        add(mainCard);
    }

    @Override
    protected void onAttach(com.vaadin.flow.component.AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        if (userService.getLoggedInUser() == null) {
            Notification.show("Bu sayfaya giriş yapmadan erişemezsiniz.", 3000, Notification.Position.MIDDLE);
            getUI().ifPresent(ui -> ui.navigate("login"));
            return;
        }

        refreshGrid();
    }

    private void refreshGrid() {
        surveyGrid.setItems(surveyService.getSurveys());
    }

    private void openAddQuestionDialog(SurveyDto survey) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Soru Ekle: " + survey.getName());
        dialog.setWidth("500px");
        dialog.setMaxWidth("100%");

        TextField questionTextField = new TextField("Soru Metni");
        questionTextField.setPlaceholder("Örn: Bu etkinliği nasıl buldunuz?");
        questionTextField.setWidthFull();

        ComboBox<String> questionTypeBox = new ComboBox<>("Soru Türü");
        questionTypeBox.setItems("Çoktan Seçmeli", "Onay Kutuları", "Açılır Menü", "Doğrusal Ölçek", "Kısa Yanıt", "Paragraf");
        questionTypeBox.setValue("Çoktan Seçmeli");
        questionTypeBox.setWidthFull();

        VerticalLayout optionsLayout = new VerticalLayout();
        optionsLayout.setPadding(false);

        List<TextField> optionFields = new ArrayList<>();

        Button addOptionButton = new Button("Seçenek Ekle", new Icon(VaadinIcon.PLUS), event -> {
            TextField optionField = new TextField("Seçenek Metni");
            optionField.setWidthFull();
            optionFields.add(optionField);
            optionsLayout.add(optionField);
        });
        addOptionButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

        questionTypeBox.addValueChangeListener(event -> {
            String selectedType = event.getValue();
            optionsLayout.removeAll();
            optionFields.clear();

            boolean needsOptions = "Çoktan Seçmeli".equals(selectedType) || "Onay Kutuları".equals(selectedType) || "Açılır Menü".equals(selectedType);
            boolean isScale = "Doğrusal Ölçek".equals(selectedType);

            optionsLayout.setVisible(needsOptions || isScale);
            addOptionButton.setVisible(needsOptions);

            if (needsOptions) {
                for (int i = 0; i < 2; i++) {
                    TextField opt = new TextField("Seçenek " + (i + 1));
                    opt.setWidthFull();
                    optionFields.add(opt);
                    optionsLayout.add(opt);
                }
            } else if (isScale) {
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

        for (int i = 0; i < 2; i++) {
            TextField opt = new TextField("Seçenek " + (i + 1));
            opt.setWidthFull();
            optionFields.add(opt);
            optionsLayout.add(opt);
        }

        VerticalLayout dialogLayout = new VerticalLayout(questionTextField, questionTypeBox, optionsLayout, addOptionButton);
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);
        
        dialog.add(dialogLayout);

        Button saveButton = new Button("Kaydet", new Icon(VaadinIcon.CHECK), e -> {
            if (questionTextField.isEmpty()) {
                Notification.show("Soru metni boş bırakılamaz!", 3000, Notification.Position.MIDDLE);
                return;
            }

            try {
                String qText = questionTextField.getValue();
                String qType = questionTypeBox.getValue();

                List<String> optionsList = new ArrayList<>();
                for (TextField tf : optionFields) {
                    if (tf != null && !tf.isEmpty()) {
                        optionsList.add(tf.getValue());
                    }
                }

                questionService.addQuestion(survey.getId(), qText, qType, optionsList);

                Notification.show("Soru başarıyla eklendi!", 3000, Notification.Position.MIDDLE);
                dialog.close();
                
            } catch (Exception ex) {
                Notification.show("Hata oluştu: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("İptal", e -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();
    }

    private void openCreateSurveyDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Yeni Anket Oluştur");

        TextField nameField = new TextField("Anket Adı");
        nameField.setWidthFull();
        nameField.setRequired(true);

        TextArea descArea = new TextArea("Anket Açıklaması");
        descArea.setWidthFull();

        VerticalLayout dialogLayout = new VerticalLayout(nameField, descArea);
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);
        dialogLayout.setAlignItems(Alignment.STRETCH);
        dialogLayout.getStyle().set("width", "400px").set("max-width", "100%");
        
        dialog.add(dialogLayout);

        Button createButton = new Button("Oluştur", new Icon(VaadinIcon.PLUS), e -> {
            if (nameField.isEmpty()) {
                Notification.show("Anket adı boş bırakılamaz!", 3000, Notification.Position.MIDDLE);
                return;
            }

            surveyService.addSurvey(nameField.getValue(), descArea.getValue());
            
            Notification.show("Anket başarıyla oluşturuldu!", 3000, Notification.Position.MIDDLE);
            dialog.close();
            refreshGrid();
        });
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("İptal", e -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.getFooter().add(cancelButton, createButton);
        dialog.open();
    }

    private void openEditSurveyDialog(SurveyDto survey) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Anketi Düzenle");

        TextField nameField = new TextField("Anket Adı");
        nameField.setWidthFull();
        nameField.setRequired(true);
        nameField.setValue(survey.getName() != null ? survey.getName() : "");

        TextArea descArea = new TextArea("Anket Açıklaması");
        descArea.setWidthFull();
        descArea.setValue(survey.getDescription() != null ? survey.getDescription() : "");

        VerticalLayout dialogLayout = new VerticalLayout(nameField, descArea);
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);
        dialogLayout.setAlignItems(Alignment.STRETCH);
        dialogLayout.getStyle().set("width", "400px").set("max-width", "100%");
        
        dialog.add(dialogLayout);

        Button saveButton = new Button("Değişiklikleri Kaydet", new Icon(VaadinIcon.CHECK), e -> {
            if (nameField.isEmpty()) {
                Notification.show("Anket adı boş bırakılamaz!", 3000, Notification.Position.MIDDLE);
                return;
            }

            surveyService.updateSurvey(survey.getId(), nameField.getValue(), descArea.getValue());
            
            Notification.show("Anket başarıyla güncellendi!", 3000, Notification.Position.MIDDLE);
            dialog.close();
            refreshGrid();
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("İptal", e -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();
    }

    public static class SurveyDto {
        private Long id;
        private String name;
        private String description;
        private String creator;

        public SurveyDto(Long id, String name, String description, String creator) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.creator = creator;
        }

        public Long getId() { return id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getCreator() { return creator; }
        public void setCreator(String creator) { this.creator = creator; }
    }
}