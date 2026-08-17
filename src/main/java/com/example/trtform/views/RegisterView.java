package com.example.trtform.views;

import com.example.trtform.service.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
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

        // --- SAYFA ARKA PLANI VE HİZALAMA ---
        setSizeFull();
        getStyle().set("background", "linear-gradient(135deg, #f0f4f8 0%, #d9e2ec 100%)");
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // --- ANA KART TASARIMI ---
        VerticalLayout formCard = new VerticalLayout();
        formCard.setWidth("450px");
        formCard.setMaxWidth("100%");
        formCard.getStyle().set("background", "white");
        formCard.getStyle().set("border-radius", "16px");
        formCard.getStyle().set("box-shadow", "0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)");
        formCard.setPadding(true);
        formCard.setSpacing(true);
        formCard.setAlignItems(Alignment.STRETCH);

        // Başlık
        H2 title = new H2("📝 Yeni Hesap Oluştur");
        title.getStyle().set("text-align", "center");
        title.getStyle().set("margin-top", "0");
        title.getStyle().set("margin-bottom", "15px");
        title.getStyle().set("color", "var(--lumo-primary-text-color)");

        // Form Alanları (İkonlu)
        TextField firstNameField = new TextField("Ad");
        firstNameField.setPrefixComponent(new Icon(VaadinIcon.USER_CARD));
        firstNameField.setWidthFull();

        TextField lastNameField = new TextField("Soyad");
        lastNameField.setPrefixComponent(new Icon(VaadinIcon.USER_CARD));
        lastNameField.setWidthFull();

        TextField usernameField = new TextField("Kullanıcı Adı");
        usernameField.setPrefixComponent(new Icon(VaadinIcon.USER));
        usernameField.setWidthFull();

        PasswordField passwordField = new PasswordField("Şifre");
        passwordField.setPrefixComponent(new Icon(VaadinIcon.LOCK));
        passwordField.setWidthFull();

        // Butonlar
        Button registerButton = new Button("Kayıt Ol", new Icon(VaadinIcon.CHECK), event -> {
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
        registerButton.getStyle().set("margin-top", "10px");

        Button loginRedirectButton = new Button("Zaten hesabın var mı? Giriş Yap", new Icon(VaadinIcon.SIGN_IN), event -> {
            getUI().ifPresent(ui -> ui.navigate("login"));
        });
        loginRedirectButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        loginRedirectButton.setWidthFull();

        // Elemanları Karta Ekle
        formCard.add(title, firstNameField, lastNameField, usernameField, passwordField, registerButton, loginRedirectButton);
        add(formCard);
    }
}