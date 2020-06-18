package com.rey.jsonbatch.demo;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;

/**
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 */
@PWA(name = "JsonBatch Demo", shortName = "JsonBatch", enableInstallPrompt = false)
@Push
public class AppShell implements AppShellConfigurator {
}
