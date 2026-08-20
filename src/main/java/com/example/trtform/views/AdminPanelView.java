package com.example.trtform.views;

import com.example.trtform.service.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route("admin-panel")
@PageTitle("Admin Panel | Dinamik Anket")
public class AdminPanelView extends VerticalLayout implements BeforeEnterObserver {

    private final UserService userService;
    private Grid<UserService.UserSummaryDto> userGrid;

    public AdminPanelView(UserService userService) {
        this.userService = userService;

        // --- SAYFA ARKA PLANI VE GENEL AYARLAR ---
        setSizeFull();
        getStyle().set("background", "linear-gradient(135deg, #f0f4f8 0%, #d9e2ec 100%)"); // Ana sayfadaki gradyan
        setAlignItems(Alignment.CENTER);
        setPadding(true);

        // --- ANA KART (İçerikleri saran şık beyaz kutu) ---
        VerticalLayout mainCard = new VerticalLayout();
        mainCard.setWidth("1000px");
        mainCard.setMaxWidth("100%");
        mainCard.getStyle().set("background", "white");
        mainCard.getStyle().set("border-radius", "16px");
        mainCard.getStyle().set("box-shadow", "0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)");
        mainCard.setPadding(true);
        mainCard.setSpacing(true);

        // Başlık
        H1 title = new H1("⚙️ Kullanıcı Yönetimi");
        title.getStyle().set("color", "var(--lumo-primary-text-color)");
        title.getStyle().set("font-size", "28px");
        title.getStyle().set("margin", "0");

        // Geri Dön Butonu
        Button backButton = new Button("Ana Sayfaya Dön", new Icon(VaadinIcon.ARROW_LEFT), event -> getUI().ifPresent(ui -> ui.navigate("")));
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout headerLayout = new HorizontalLayout(title, backButton);
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        headerLayout.setAlignItems(Alignment.CENTER);
        headerLayout.getStyle().set("border-bottom", "1px solid var(--lumo-contrast-10pct)");
        headerLayout.getStyle().set("padding-bottom", "15px");

        // Bilgi Metinleri
        Span currentAdmin = new Span("🛡️ Aktif admin: " + userService.getLoggedInUserFullName());
        currentAdmin.getStyle().set("font-weight", "600").set("color", "var(--lumo-primary-text-color)");

        Paragraph infoText = new Paragraph("Bu panelden kullanıcıları görüntüleyebilir, admin rolü verebilir ya da alabilirsiniz.");
        infoText.getStyle().set("color", "var(--lumo-secondary-text-color)").set("margin-top", "5px");

        VerticalLayout infoLayout = new VerticalLayout(currentAdmin, infoText);
        infoLayout.setPadding(false);
        infoLayout.setSpacing(false);

        // Tablo Tasarımı
        userGrid = new Grid<>(UserService.UserSummaryDto.class);
        userGrid.removeAllColumns();
        userGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);

        userGrid.addColumn(user -> user.getId()).setHeader("ID").setAutoWidth(true).setFlexGrow(0);
        userGrid.addColumn(user -> user.getUsername()).setHeader("Kullanıcı Adı").setFlexGrow(1);
        userGrid.addColumn(user -> user.getFullName()).setHeader("Ad Soyad").setFlexGrow(1);
        
        // Şık Rol Etiketleri (Badge)
        userGrid.addComponentColumn(user -> {
            Span roleBadge = new Span(user.getRole());
            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                roleBadge.getStyle().set("background", "var(--lumo-error-color-10pct)");
                roleBadge.getStyle().set("color", "var(--lumo-error-text-color)");
            } else {
                roleBadge.getStyle().set("background", "var(--lumo-success-color-10pct)");
                roleBadge.getStyle().set("color", "var(--lumo-success-text-color)");
            }
            roleBadge.getStyle().set("padding", "4px 10px");
            roleBadge.getStyle().set("border-radius", "12px");
            roleBadge.getStyle().set("font-weight", "bold");
            roleBadge.getStyle().set("font-size", "12px");
            return roleBadge;
        }).setHeader("Rol").setAutoWidth(true);

        // İkonlu İşlem Butonları
        userGrid.addComponentColumn(user -> {
            boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
            Button roleButton = new Button(
                isAdmin ? "Yetkiyi Al" : "Admin Yap",
                new Icon(isAdmin ? VaadinIcon.MINUS_CIRCLE : VaadinIcon.SHIELD)
            );
            
            if (isAdmin) {
                roleButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            } else {
                roleButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            }

            roleButton.addClickListener(event -> {
                String newRole = isAdmin ? "USER" : "ADMIN";
                boolean updated = userService.updateUserRole(user.getUsername(), newRole);
                if (updated) {
                    refreshGrid();
                    Notification.show(user.getUsername() + " kullanıcısı için rol güncellendi.", 2500, Notification.Position.MIDDLE);
                } else {
                    Notification.show("Kullanıcı rolü güncellenemedi.", 2500, Notification.Position.MIDDLE);
                }
            });
            return roleButton;
        }).setHeader("İşlem").setAutoWidth(true);
        
        userGrid.setWidthFull();
        userGrid.setAllRowsVisible(true);

        mainCard.add(headerLayout, infoLayout, userGrid);
        add(mainCard);
        refreshGrid();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!userService.isAdmin()) {
            Notification.show("Bu sayfaya erişmek için admin yetkisi gerekli.", 2500, Notification.Position.MIDDLE);
            event.forwardTo("");
        }
    }

    private void refreshGrid() {
        List<UserService.UserSummaryDto> users = userService.getAllUsers();
        userGrid.setItems(users);
    }
}