package com.example.trtform.views;

import com.example.trtform.service.UserService;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class MainLayout extends AppLayout {

    private final UserService userService;

    public MainLayout(UserService userService) {
        this.userService = userService;
        createHeader();
    }

    private void createHeader() {
        // 1. Sol Taraf: Logo ve TRT Anket Sistemi Yazısı
        Image logo = new Image("images/trt-logo.png", "TRT Logo");
        logo.setHeight("40px");

        H1 title = new H1("TRT Anket Sistemi");
        title.getStyle().set("font-size", "1.3rem");
        title.getStyle().set("font-weight", "600");
        title.getStyle().set("color", "#333333");
        title.getStyle().set("margin", "0");

        HorizontalLayout leftLayout = new HorizontalLayout(logo, title);
        leftLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        leftLayout.setSpacing(true);

        // 2. Sağ Taraf: Üç Çizgili Pop-up Menü
        MenuBar menuBar = new MenuBar();
        menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY); // Butonu şeffaf yapar
        MenuItem hamburgerMenu = menuBar.addItem(new Icon(VaadinIcon.MENU));

        // Menü alt seçeneklerini açılır menüye (pop-up) ekliyoruz
        hamburgerMenu.getSubMenu().addItem("➕ Yeni Anket", e -> {
            getUI().ifPresent(ui -> ui.navigate("new-survey")); // Yönlendirme yollarını kendi projene göre düzenleyebilirsin
        });

        if (userService.isAdmin()) {
            hamburgerMenu.getSubMenu().addItem("⚙️ Admin Paneli", e -> {
                getUI().ifPresent(ui -> ui.navigate("admin-panel"));
            });
        }

        hamburgerMenu.getSubMenu().addItem("🚪 Çıkış Yap", e -> {
            // Çıkış işlemleri
            getUI().ifPresent(ui -> ui.navigate("login"));
        });

        // 3. Üst Şeridi (Navbar) Toplama ve Şekillendirme
        HorizontalLayout header = new HorizontalLayout(leftLayout, menuBar);
        header.setWidthFull();
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN); // Sola ve sağa yaslar
        
        // TRT Kurumsal Görünümü (Beyaz arka plan, kırmızı alt çizgi)
        header.getStyle().set("background-color", "#ffffff");
        header.getStyle().set("border-bottom", "3px solid var(--lumo-primary-color)");
        header.getStyle().set("padding", "10px 24px");
        header.getStyle().set("box-shadow", "var(--lumo-box-shadow-s)");

        addToNavbar(header); // Vaadin'in ana Navbar'ına ekler
    }
}