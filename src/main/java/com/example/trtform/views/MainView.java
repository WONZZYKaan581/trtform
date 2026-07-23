package com.example.trtform.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("")
@PageTitle("Ana Sayfa | Dinamik Anket")
public class MainView extends VerticalLayout {

    private Grid<SurveyDto> surveyGrid;

    public MainView() {
        H1 title = new H1("Dinamik Anket Uygulaması");

        // Üst sağ kısım için butonlar (Soru Ekle, Anket Oluştur, Giriş/Çıkış)
        HorizontalLayout actionButtons = new HorizontalLayout();
        actionButtons.setAlignItems(Alignment.CENTER);

        Button addQuestionButton = new Button("Soru Ekle");
        addQuestionButton.addClickListener(event -> {
            if (UserService.getLoggedInUser() == null) {
                Notification.show("Soru eklemek için giriş yapmalısınız!", 3000, Notification.Position.MIDDLE);
                getUI().ifPresent(ui -> ui.navigate("login"));
            } else {
                getUI().ifPresent(ui -> ui.navigate("add-question"));
            }
        });
        actionButtons.add(addQuestionButton);

        Button createSurveyButton = new Button("Yeni Anket Oluştur");
        createSurveyButton.addClickListener(event -> {
            if (UserService.getLoggedInUser() == null) {
                Notification.show("Anket oluşturmak için önce giriş yapmalısınız!", 3000, Notification.Position.MIDDLE);
                getUI().ifPresent(ui -> ui.navigate("login"));
            } else {
                getUI().ifPresent(ui -> ui.navigate("create-survey"));
            }
        });
        createSurveyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        actionButtons.add(createSurveyButton);

        // Kullanıcı Giriş / Çıkış Durumu
        if (UserService.getLoggedInUser() == null) {
            Button loginButton = new Button("Giriş Yap");
            loginButton.addClickListener(event -> {
                getUI().ifPresent(ui -> ui.navigate("login"));
            });
            actionButtons.add(loginButton);
        } else {
            Span welcomeText = new Span("Hoş geldin, " + UserService.getLoggedInUser());
            Button logoutButton = new Button("Çıkış Yap");
            logoutButton.addClickListener(event -> {
                UserService.logout();
                Notification.show("Çıkış yapıldı.", 2000, Notification.Position.MIDDLE);
                getUI().ifPresent(ui -> ui.getPage().reload());
            });
            logoutButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            actionButtons.add(welcomeText, logoutButton);
        }

        // Grid Tanımlaması ve Sütunlar
        surveyGrid = new Grid<>(SurveyDto.class);
        surveyGrid.removeAllColumns();

        surveyGrid.addColumn(SurveyDto::getId).setHeader("ID").setAutoWidth(true);
        surveyGrid.addColumn(SurveyDto::getName).setHeader("Anket Adı");
        surveyGrid.addColumn(SurveyDto::getDescription).setHeader("Açıklama");

        // Düzenle, Sorular, Sonuçlar ve Sil butonlarını içeren İşlemler Kolonu
        surveyGrid.addComponentColumn(survey -> {
            HorizontalLayout rowButtons = new HorizontalLayout();

            Button editSurveyBtn = new Button("Düzenle");
            editSurveyBtn.addClickListener(e -> {
                getUI().ifPresent(ui -> ui.navigate("edit-survey/" + survey.getId()));
            });
            editSurveyBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);

            Button manageQuestionsBtn = new Button("Sorular");
            manageQuestionsBtn.addClickListener(e -> {
                getUI().ifPresent(ui -> ui.navigate("manage-questions/" + survey.getId()));
            });
            manageQuestionsBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);

            rowButtons.add(editSurveyBtn, manageQuestionsBtn);

            // KONTROL: Sadece giriş yapmış kullanıcılar sonuçları görebilsin 
            if (UserService.getLoggedInUser() != null) {
                Button resultsBtn = new Button("Sonuçlar");
                resultsBtn.addClickListener(e -> {
                    getUI().ifPresent(ui -> ui.navigate("survey-results/" + survey.getId()));
                });
                resultsBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
                rowButtons.add(resultsBtn);
            }

            Button deleteBtn = new Button("Sil");
            deleteBtn.addClickListener(e -> {
                SurveyService.deleteSurvey(survey.getId());
                refreshGrid();
                Notification.show("Anket silindi.", 2000, Notification.Position.MIDDLE);
            });
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);

            rowButtons.add(deleteBtn);

            return rowButtons;
        }).setHeader("İşlemler");

        // Tablodaki bir satıra tıklandığında ankete katılım sayfasına git
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
        refreshGrid();
    }

    private void refreshGrid() {
        surveyGrid.setItems(SurveyService.getSurveys());
    }

    public static class SurveyDto {
        private Long id;
        private String name;
        private String description;
        private String creator; // İleride anket sahibi kontrolü için

        public SurveyDto(Long id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.creator = "admin"; // Varsayılan oluşturan
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