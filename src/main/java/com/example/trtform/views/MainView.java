package com.example.trtform.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.UI;

@Route("")
@PageTitle("Ana Sayfa | Dinamik Anket")
public class MainView extends VerticalLayout {

    private final UserService userService;
    private final SurveyService surveyService;
    private Grid<SurveyDto> surveyGrid;

    public MainView(UserService userService, SurveyService surveyService) {
        this.userService = userService;
        this.surveyService = surveyService;

        if (userService.getLoggedInUser() == null) {
            add(new Span("Giriş ekranına yönlendiriliyorsunuz..."));
            UI ui = UI.getCurrent();
            if (ui != null) {
                ui.navigate("login");
            }
            return;
        }

        H1 title = new H1("Dinamik Anket Uygulaması");

        HorizontalLayout actionButtons = new HorizontalLayout();
        actionButtons.setAlignItems(Alignment.CENTER);

        Button addQuestionButton = new Button("Soru Ekle");
        addQuestionButton.addClickListener(event -> {
            if (userService.getLoggedInUser() == null) {
                Notification.show("Soru eklemek için giriş yapmalısınız!", 3000, Notification.Position.MIDDLE);
                getUI().ifPresent(ui -> ui.navigate("login"));
            } else {
                getUI().ifPresent(ui -> ui.navigate("add-question"));
            }
        });
        actionButtons.add(addQuestionButton);

        Button createSurveyButton = new Button("Yeni Anket Oluştur");
        createSurveyButton.addClickListener(event -> {
            if (userService.getLoggedInUser() == null) {
                Notification.show("Anket oluşturmak için önce giriş yapmalısınız!", 3000, Notification.Position.MIDDLE);
                getUI().ifPresent(ui -> ui.navigate("login"));
            } else {
                getUI().ifPresent(ui -> ui.navigate("create-survey"));
            }
        });
        createSurveyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        actionButtons.add(createSurveyButton);

        if (userService.getLoggedInUser() == null) {
            Button loginButton = new Button("Giriş Yap");
            loginButton.addClickListener(event -> {
                getUI().ifPresent(ui -> ui.navigate("login"));
            });
            actionButtons.add(loginButton);
        } else {
            Span welcomeText = new Span("Hoş geldin, " + userService.getLoggedInUser());
            Button logoutButton = new Button("Çıkış Yap");
            logoutButton.addClickListener(event -> {
                userService.logout();
                Notification.show("Çıkış yapıldı.", 2000, Notification.Position.MIDDLE);
                getUI().ifPresent(ui -> ui.getPage().reload());
            });
            logoutButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            actionButtons.add(welcomeText, logoutButton);
        }

        surveyGrid = new Grid<>(SurveyDto.class);
        surveyGrid.removeAllColumns();

        surveyGrid.addColumn(survey -> survey.getId()).setHeader("ID").setAutoWidth(true);
        surveyGrid.addColumn(survey -> survey.getName()).setHeader("Anket Adı");
        surveyGrid.addColumn(survey -> survey.getDescription()).setHeader("Açıklama");

        surveyGrid.addComponentColumn(survey -> {
            HorizontalLayout rowButtons = new HorizontalLayout();

            Button editSurveyBtn = new Button("Düzenle");
            editSurveyBtn.addClickListener(e -> {
                getUI().ifPresent(ui -> ui.navigate("edit-survey/" + survey.getId()));
            });
            editSurveyBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);

            Button shareButton = new Button("Anketi Paylaş", event -> {
    // Sabit veya dinamik olarak temel URL'yi alıyoruz
    String baseUrl = "http://localhost:8080"; // Gerekirse kendi adresinle değiştirebilirsin
    String surveyUrl = baseUrl + "/participate/" + survey.getId(); // Not: survey değişken adını kendi modeline göre ayarlayabilirsin

    Dialog dialog = new Dialog();
    dialog.setHeaderTitle("Anket Paylaşım Bağlantısı");

    TextField linkField = new TextField();
    linkField.setValue(surveyUrl);
    linkField.setReadOnly(true);
    linkField.setWidthFull();
    linkField.setHelperText("Bağlantıya tıklayın, otomatik kopyalanır.");
    linkField.getElement().addEventListener("click", clickEvent -> {
        getUI().ifPresent(ui -> ui.getPage().executeJs(
                "navigator.clipboard.writeText($0).then(() => {}).catch(() => {});",
                surveyUrl
        ));
        Notification.show("Bağlantı panoya kopyalandı.", 2000, Notification.Position.MIDDLE);
    });

    VerticalLayout dialogLayout = new VerticalLayout(
        new com.vaadin.flow.component.html.Paragraph("Bu bağlantıyı kopyalayarak başkalarıyla paylaşabilirsiniz:"),
        linkField
    );
    dialogLayout.setPadding(false);

    Button closeButton = new Button("Kapat", e -> dialog.close());
    closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

    dialog.add(dialogLayout);
    dialog.getFooter().add(closeButton);
    dialog.open();
});
shareButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            Button manageQuestionsBtn = new Button("Sorular");
            manageQuestionsBtn.addClickListener(e -> {
                getUI().ifPresent(ui -> ui.navigate("manage-questions/" + survey.getId()));
            });
            manageQuestionsBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);

            rowButtons.add(editSurveyBtn, shareButton, manageQuestionsBtn);

            if (userService.getLoggedInUser() != null) {
                Button resultsBtn = new Button("Sonuçlar");
                resultsBtn.addClickListener(e -> {
                    getUI().ifPresent(ui -> ui.navigate("survey-results/" + survey.getId()));
                });
                resultsBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
                rowButtons.add(resultsBtn);
            }

            Button deleteBtn = new Button("Sil");
            deleteBtn.addClickListener(e -> {
                surveyService.deleteSurvey(survey.getId());
                refreshGrid();
                Notification.show("Anket silindi.", 2000, Notification.Position.MIDDLE);
            });
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);

            rowButtons.add(deleteBtn);

            return rowButtons;

            
        }).setHeader("İşlemler");

        surveyGrid.addItemClickListener(event -> {
            Long selectedSurveyId = event.getItem().getId();
            getUI().ifPresent(ui -> ui.navigate(ParticipateSurveyView.class, selectedSurveyId));
        });

        surveyGrid.setWidthFull();
        refreshGrid();

        setAlignItems(Alignment.CENTER);
        setPadding(true);

        HorizontalLayout headerLayout = new HorizontalLayout(title, actionButtons);
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        headerLayout.setAlignItems(Alignment.CENTER);

        add(headerLayout, surveyGrid);
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

    public static class SurveyDto {
        private Long id;
        private String name;
        private String description;
        private String creator;

        public SurveyDto(Long id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.creator = "admin";
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