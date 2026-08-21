package com.example.trtform.views;

import com.example.trtform.service.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image; // LOGO İÇİN EKLENDİ
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("login")
@PageTitle("Giriş Yap | Dinamik Anket")
public class LoginView extends VerticalLayout {

    private final UserService userService;

    public LoginView(UserService userService) {
        this.userService = userService;

        // --- SAYFA ARKA PLANI VE HİZALAMA ---
        setSizeFull();
        getStyle().set("background", "linear-gradient(135deg, #f0f4f8 0%, #d9e2ec 100%)");
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // --- ANA KART TASARIMI ---
        VerticalLayout formCard = new VerticalLayout();
        formCard.setWidth("420px");
        formCard.setMaxWidth("100%");
        formCard.getStyle().set("background", "white");
        formCard.getStyle().set("border-radius", "16px");
        formCard.getStyle().set("box-shadow", "0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)");
        formCard.setPadding(true);
        formCard.setSpacing(true);
        formCard.setAlignItems(Alignment.STRETCH);

        // --- TRT LOGOSU ---
        Image trtLogo = new Image("images/trt-logo.png", "TRT Kurumsal Logo");
        trtLogo.setWidth("150px");
        formCard.setAlignSelf(Alignment.CENTER, trtLogo); // Logoyu kartın ortasına hizala

        // Başlık
        H2 title = new H2("Giriş Yap"); // İkonu kaldırdık çünkü üstünde kurumsal logo var
        title.getStyle().set("text-align", "center");
        title.getStyle().set("margin-top", "0");
        title.getStyle().set("margin-bottom", "20px");
        title.getStyle().set("color", "var(--lumo-primary-text-color)");

        // Form Alanları (İkonlu)
        TextField usernameField = new TextField("Kullanıcı Adı");
        usernameField.setPrefixComponent(new Icon(VaadinIcon.USER));
        usernameField.setWidthFull();

        PasswordField passwordField = new PasswordField("Şifre");
        passwordField.setPrefixComponent(new Icon(VaadinIcon.LOCK));
        passwordField.setWidthFull();

        // Butonlar
        Button loginButton = new Button("Giriş Yap", new Icon(VaadinIcon.SIGN_IN), event -> {
            String username = usernameField.getValue();
            String password = passwordField.getValue();

            if (username.isBlank() || password.isBlank()) {
                Notification.show("Kullanıcı adı ve şifre zorunludur.", 3000, Notification.Position.MIDDLE);
                return;
            }

            boolean loggedIn = userService.login(username, password);
            if (loggedIn) {
                Notification.show("Giriş başarılı.", 2000, Notification.Position.MIDDLE);
                getUI().ifPresent(ui -> {
                    if (userService.isAdmin()) {
                        ui.navigate("admin-panel");
                    } else {
                        ui.navigate("");
                    }
                });
            } else {
                Notification.show("Kullanıcı adı veya şifre yanlış.", 3000, Notification.Position.MIDDLE);
            }
        });
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.setWidthFull();
        loginButton.getStyle().set("margin-top", "10px");

        Button registerButton = new Button("Hesabın yok mu? Kayıt Ol", new Icon(VaadinIcon.USER_CHECK), event -> {
            getUI().ifPresent(ui -> ui.navigate("register"));
        });
        registerButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        registerButton.setWidthFull();

        // Elemanları Karta Ekle (trtLogo en başa eklendi)
        formCard.add(trtLogo, title, usernameField, passwordField, loginButton, registerButton);
        add(formCard);
    }
}