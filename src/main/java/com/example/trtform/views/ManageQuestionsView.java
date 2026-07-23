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
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.component.notification.Notification;

import java.util.ArrayList;
import java.util.List;

@Route("manage-questions/1")
@PageTitle("Soruları Yönet | Dinamik Anket")
public class ManageQuestionsView extends VerticalLayout implements BeforeEnterObserver {
    

    private Long surveyId;
    private Grid<QuestionService.QuestionDto> questionGrid;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (UserService.getLoggedInUser() == null) {
            Notification.show("Bu sayfaya erişmek için giriş yapmalısınız!", 3000, Notification.Position.MIDDLE);
            event.forwardTo("login"); // Giriş sayfasına veya anasayfaya yönlendirir
        }
    }

    public ManageQuestionsView() {
        H2 title = new H2("Anket Sorularını Düzenle / Sil");

        questionGrid = new Grid<>(QuestionService.QuestionDto.class);
        questionGrid.removeAllColumns();

        questionGrid.addColumn(QuestionService.QuestionDto::getText).setHeader("Soru Metni");
        questionGrid.addColumn(QuestionService.QuestionDto::getType).setHeader("Soru Türü");

        // Düzenle ve Sil Butonları
        questionGrid.addComponentColumn(question -> {
            Button editBtn = new Button("Düzenle");
            editBtn.addClickListener(e -> {
                Dialog editDialog = new Dialog();
                editDialog.setHeaderTitle("Soruyu ve Seçenekleri Düzenle");

                VerticalLayout dialogLayout = new VerticalLayout();
                
                TextField editField = new TextField("Soru Metni");
                editField.setValue(question.getText());
                editField.setWidthFull();
                dialogLayout.add(editField);

                List<TextField> optionTextFields = new ArrayList<>();
                VerticalLayout optionsLayout = new VerticalLayout();

                boolean hasOptions = "Çoktan Seçmeli".equals(question.getType()) || 
                                     "Onay Kutuları".equals(question.getType()) || 
                                     "Açılır Menü".equals(question.getType());

                boolean isScale = "Doğrusal Ölçek".equals(question.getType());

                if (hasOptions) {
                    optionsLayout.add(new H4("Seçenekler:"));
                    
                    if (question.getOptions() != null) {
                        for (int i = 0; i < question.getOptions().size(); i++) {
                            final int index = i;
                            TextField optField = new TextField();
                            optField.setValue(question.getOptions().get(i));
                            optionTextFields.add(optField);
                            
                            Button removeOptBtn = new Button("Sil");
                            removeOptBtn.addClickListener(remEvent -> {
                                question.getOptions().remove(index);
                                editDialog.close();
                                editBtn.click(); // Pencereyi yenile
                            });
                            removeOptBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);

                            HorizontalLayout optRow = new HorizontalLayout(optField, removeOptBtn);
                            optRow.setAlignItems(Alignment.CENTER);
                            optionsLayout.add(optRow);
                        }
                    }

                    TextField newOptField = new TextField();
                    newOptField.setPlaceholder("Yeni seçenek ekle...");
                    
                    Button addOptBtn = new Button("Seçenek Ekle");
                    addOptBtn.addClickListener(addEvent -> {
                        if (!newOptField.isEmpty()) {
                            if (question.getOptions() == null) {
                                question.setOptions(new ArrayList<>());
                            }
                            question.getOptions().add(newOptField.getValue());
                            editDialog.close();
                            editBtn.click();
                        }
                    });
                    addOptBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
                    
                    HorizontalLayout addOptRow = new HorizontalLayout(newOptField, addOptBtn);
                    optionsLayout.add(addOptRow);
                } 
                else if (isScale) {
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
                        if (tf != null && !tf.isEmpty()) {
                            updatedOptions.add(tf.getValue());
                        }
                    }
                    
                    if (hasOptions && question.getOptions() != null && !question.getOptions().isEmpty() && updatedOptions.isEmpty()) {
                        updatedOptions = question.getOptions(); 
                    }

                    QuestionService.updateQuestion(question.getId(), editField.getValue(), updatedOptions);
                    refreshGrid();
                    Notification.show("Soru ve seçenekler güncellendi.", 2000, Notification.Position.MIDDLE);
                    editDialog.close();
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
                QuestionService.deleteQuestion(question.getId());
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

    private void refreshGrid() {
        if (surveyId != null) {
            questionGrid.setItems(QuestionService.getQuestionsBySurveyId(surveyId));
        }
    }
}