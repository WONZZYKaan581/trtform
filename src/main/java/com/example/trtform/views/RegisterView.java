package com.example.trtform.views;

import com.example.trtform.service.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("register")
@PageTitle("Kayıt Ol | Dinamik Anket")
public class RegisterView extends VerticalLayout {

    private final UserService userService;

    public RegisterView(UserService userService) {
        this.userService = userService;

        H2 title = new H2("Kayıt Ol");

        TextField firstNameField = new TextField("Ad");
        firstNameField.setWidthFull();

        TextField lastNameField = new TextField("Soyad");
        lastNameField.setWidthFull();

        TextField usernameField = new TextField("Kullanıcı Adı");
        usernameField.setWidthFull();

        PasswordField passwordField = new PasswordField("Şifre");
        passwordField.setWidthFull();

        Button registerButton = new Button("Kayıt Ol", event -> {
            if (firstNameField.isEmpty() || lastNameField.isEmpty() || usernameField.isEmpty() || passwordField.isEmpty()) {
                Notification.show("Tüm alanlar doldurulmalıdır!", 3000, Notification.Position.MIDDLE);
                return;
            }

            boolean created = userService.register(
                usernameField.getValue(),
                passwordField.getValue(),
                firstNameField.getValue(),
                lastNameField.getValue()
            );

            if (created) {
                Notification.show("Kayıt başarılı! Giriş yapabilirsiniz.", 3000, Notification.Position.MIDDLE);
                getUI().ifPresent(ui -> ui.navigate("login"));
            } else {
                Notification.show("Bu kullanıcı adı zaten alınmış!", 3000, Notification.Position.MIDDLE);
            }
        });
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.setWidthFull();

        Button loginRedirectButton = new Button("Zaten hesabın var mı? Giriş Yap", event -> {
            getUI().ifPresent(ui -> ui.navigate("login"));
        });
        loginRedirectButton.setWidthFull();

        VerticalLayout formLayout = new VerticalLayout(title, firstNameField, lastNameField, usernameField, passwordField, registerButton, loginRedirectButton);
        formLayout.setWidth("400px");
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