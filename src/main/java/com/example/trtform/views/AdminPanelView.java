package com.example.trtform.views;

import com.example.trtform.service.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
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

        H2 title = new H2("Kullanıcı Yönetimi / Admin Yetkisi");
        title.getStyle().set("margin-top", "0");

        Span currentAdmin = new Span("Aktif admin: " + userService.getLoggedInUserFullName());
        currentAdmin.getStyle().set("font-weight", "600");

        Paragraph infoText = new Paragraph("Bu panelden kullanıcıları görüntüleyebilir, admin rolü verebilir ya da alabilirsiniz.");
        infoText.getStyle().set("color", "var(--lumo-secondary-text-color)");

        userGrid = new Grid<>(UserService.UserSummaryDto.class);
        userGrid.removeAllColumns();
        userGrid.addColumn(user -> user.getId()).setHeader("ID").setAutoWidth(true);
        userGrid.addColumn(user -> user.getUsername()).setHeader("Kullanıcı Adı");
        userGrid.addColumn(user -> user.getFullName()).setHeader("Ad Soyad");
        userGrid.addColumn(user -> user.getRole()).setHeader("Rol");
        userGrid.addComponentColumn(user -> {
            Button roleButton = new Button("ADMIN".equalsIgnoreCase(user.getRole()) ? "Admin Yetkisini Al" : "Admin Yap");
            roleButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            roleButton.addClickListener(event -> {
                String newRole = "ADMIN".equalsIgnoreCase(user.getRole()) ? "USER" : "ADMIN";
                boolean updated = userService.updateUserRole(user.getUsername(), newRole);
                if (updated) {
                    refreshGrid();
                    Notification.show(user.getUsername() + " kullanıcısı için rol güncellendi.", 2500, Notification.Position.MIDDLE);
                } else {
                    Notification.show("Kullanıcı rolü güncellenemedi.", 2500, Notification.Position.MIDDLE);
                }
            });
            return roleButton;
        }).setHeader("İşlem");
        userGrid.setWidthFull();
        userGrid.setAllRowsVisible(true);

        Button backButton = new Button("Ana Sayfaya Dön", event -> getUI().ifPresent(ui -> ui.navigate("")));
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout controls = new HorizontalLayout(backButton);
        controls.setWidthFull();
        controls.setJustifyContentMode(JustifyContentMode.END);

        add(title, currentAdmin, infoText, userGrid, controls);
        setPadding(true);
        setWidthFull();
        setAlignItems(Alignment.STRETCH);
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
