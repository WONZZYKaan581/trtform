package com.example.trtform.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;

@Route("manage-questions")
@PageTitle("Soruları Yönet | Dinamik Anket")
public class ManageQuestionsView extends VerticalLayout implements BeforeEnterObserver, HasUrlParameter<Long> {

    private final UserService userService;
    private final QuestionService questionService;
    private Long surveyId;
    private Grid<QuestionService.QuestionDto> questionGrid;

    public ManageQuestionsView(UserService userService, QuestionService questionService) {
        this.userService = userService;
        this.questionService = questionService;

        H2 title = new H2("Anket Sorularını Düzenle / Sil");

        questionGrid = new Grid<>(QuestionService.QuestionDto.class);
        questionGrid.removeAllColumns();

        questionGrid.addColumn(question -> question.getText()).setHeader("Soru Metni");
        questionGrid.addColumn(question -> question.getType()).setHeader("Soru Türü");

        questionGrid.addComponentColumn(question -> {
            Button editBtn = new Button("Düzenle");
            editBtn.addClickListener(e -> {
                Dialog editDialog = new Dialog();
                editDialog.setHeaderTitle("Soruyu ve Seçenekleri Düzenle");

                VerticalLayout dialogLayout = new VerticalLayout();

                TextField editField = new TextField("Soru Metni");
                editField.setValue(question.getText() != null ? question.getText() : "");
                editField.setWidthFull();
                dialogLayout.add(editField);

                List<TextField> optionTextFields = new ArrayList<>();
                VerticalLayout optionsLayout = new VerticalLayout();

                boolean hasOptions = "Çoktan Seçmeli".equals(question.getType())
                        || "Onay Kutuları".equals(question.getType())
                        || "Açılır Menü".equals(question.getType());

                boolean isScale = "Doğrusal Ölçek".equals(question.getType());

                if (hasOptions) {
                    optionsLayout.add(new H4("Seçenekler:"));

                    if (question.getOptions() != null) {
                        for (String optValue : question.getOptions()) {
                            TextField optField = new TextField();
                            optField.setValue(optValue != null ? optValue : "");
                            optField.setWidthFull();
                            optionTextFields.add(optField);
                            optionsLayout.add(optField);
                        }
                    }
                } else if (isScale) {
                    optionsLayout.add(new H4("Ölçek Aralıkları:"));

                    TextField minField = new TextField("Başlangıç Değeri");
                    TextField maxField = new TextField("Bitiş Değeri");

                    if (question.getOptions() != null && question.getOptions().size() >= 2) {
                        minField.setValue(question.getOptions().get(0));
                        maxField.setValue(question.getOptions().get(1));
                    } else {
                        minField.setValue("1");
                        maxField.setValue("5");
                    }

                    minField.setWidthFull();
                    maxField.setWidthFull();

                    optionTextFields.add(minField);
                    optionTextFields.add(maxField);
                    optionsLayout.add(minField, maxField);
                }

                dialogLayout.add(optionsLayout);

                Button saveBtn = new Button("Güncelle");
                saveBtn.addClickListener(saveEvent -> {
                    if (editField.isEmpty()) {
                        Notification.show("Soru metni boş olamaz!", 2000, Notification.Position.MIDDLE);
                        return;
                    }

                    List<String> updatedOptions = new ArrayList<>();
                    for (TextField tf : optionTextFields) {
                        if (tf != null && tf.getValue() != null && !tf.getValue().trim().isEmpty()) {
                            updatedOptions.add(tf.getValue().trim());
                        }
                    }

                    // Eğer seçenek türündeyse ve boş kalmasın isteniyorsa, en azından eski listeyi koru veya güncelleneni al
                    if (hasOptions && updatedOptions.isEmpty() && question.getOptions() != null) {
                        updatedOptions = question.getOptions();
                    }

                    try {
                        questionService.updateQuestion(question.getId(), editField.getValue(), updatedOptions);
                        refreshGrid();
                        Notification.show("Soru ve seçenekler başarıyla güncellendi.", 2000, Notification.Position.MIDDLE);
                        editDialog.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Notification.show("Güncelleme sırasında hata oluştu: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
                    }
                });
                saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

                Button cancelBtn = new Button("İptal");
                cancelBtn.addClickListener(cancelEvent -> editDialog.close());

                editDialog.add(dialogLayout);
                editDialog.getFooter().add(cancelBtn, saveBtn);
                editDialog.open();
            });
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);

            Button deleteBtn = new Button("Sil");
            deleteBtn.addClickListener(e -> {
                questionService.deleteQuestion(question.getId());
                refreshGrid();
                Notification.show("Soru silindi.", 2000, Notification.Position.MIDDLE);
            });
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);

            return new HorizontalLayout(editBtn, deleteBtn);
        }).setHeader("İşlemler");

        questionGrid.setWidthFull();

        Button backButton = new Button("Geri Dön");
        backButton.addClickListener(event -> {
            getUI().ifPresent(ui -> ui.navigate(""));
        });
        backButton.setWidthFull();

        VerticalLayout layout = new VerticalLayout(title, questionGrid, backButton);
        layout.setWidth("700px");
        layout.setAlignItems(Alignment.STRETCH);
        layout.setPadding(true);
        layout.getStyle().set("background", "var(--lumo-base-color)");
        layout.getStyle().set("border-radius", "8px");
        layout.getStyle().set("box-shadow", "var(--lumo-box-shadow-m)");

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        add(layout);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (userService.getLoggedInUser() == null) {
            Notification.show("Bu sayfaya erişmek için giriş yapmalısınız!", 3000, Notification.Position.MIDDLE);
            event.forwardTo("login");
        }
    }

    @Override
    public void setParameter(BeforeEvent event, Long parameter) {
        this.surveyId = parameter;
        refreshGrid();
    }

    private void refreshGrid() {
        if (surveyId != null) {
            questionGrid.setItems(questionService.getQuestionsBySurveyId(surveyId));
        }
    }
}