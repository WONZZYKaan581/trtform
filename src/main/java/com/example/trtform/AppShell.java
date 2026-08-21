package com.example.trtform;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.theme.Theme;

@Theme("trt-tema")
public class AppShell implements AppShellConfigurator {
    @Override
    public void configurePage(AppShellSettings settings) {
        AppShellConfigurator.super.configurePage(settings);
    }
}