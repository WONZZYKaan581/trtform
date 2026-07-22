package com.example.trtform.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("login")
@PageTitle("Giriş Yap | Dinamik Anket")
public class LoginView extends VerticalLayout {

    public LoginView() {
        H2 title = new H2("Giriş Yap");

        TextField usernameField = new TextField("Kullanıcı Adı");
        usernameField.setWidthFull();

        PasswordField passwordField = new PasswordField("Şifre");
        passwordField.setWidthFull();

        Button loginButton = new Button("Giriş Yap", event -> {
            boolean success = UserService.login(usernameField.getValue(), passwordField.getValue());
            if (success) {
                Notification.show("Giriş başarılı!", 3000, Notification.Position.MIDDLE);
                getUI().ifPresent(ui -> ui.navigate(""));
            } else {
                Notification.show("Hatalı kullanıcı adı veya şifre!", 3000, Notification.Position.MIDDLE);
            }
        });
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.setWidthFull();

        Button registerRedirectButton = new Button("Hesabın yok mu? Kayıt Ol", event -> {
            getUI().ifPresent(ui -> ui.navigate("register"));
        });
        registerRedirectButton.setWidthFull();

        VerticalLayout formLayout = new VerticalLayout(title, usernameField, passwordField, loginButton, registerRedirectButton);
        formLayout.setWidth("400px");
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