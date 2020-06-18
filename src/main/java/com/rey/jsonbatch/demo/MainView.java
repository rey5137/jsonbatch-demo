package com.rey.jsonbatch.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rey.jsonbatch.BatchEngine;
import com.rey.jsonbatch.model.BatchTemplate;
import com.rey.jsonbatch.model.Request;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutorService;

@Route
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
public class MainView extends VerticalLayout {

    private TextArea templateArea;
    private TextArea requestArea;
    private TextArea responseArea;
    private Button goButton;
    private ProgressBar progressBar;

    private UI ui;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BatchEngine batchEngine;

    @Autowired
    private ExecutorService executorService;

    public MainView() {
        setSizeFull();

        progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);

        H3 title = new H3("JsonBatch Demo");

        HorizontalLayout mainLayout = new HorizontalLayout();
        add(progressBar, title, mainLayout);
        mainLayout.setSizeFull();

        VerticalLayout templateLayout = new VerticalLayout();
        templateLayout.setHeightFull();
        templateLayout.setPadding(false);

        H4 templateLabel = new H4("Template");
        templateArea = new TextArea();
        templateArea.setSizeFull();
        templateArea.setMaxHeight("240px");
        H4 requestLabel = new H4("Request");
        requestArea = new TextArea();
        requestArea.setSizeFull();
        requestArea.setMaxHeight("240px");

        templateLayout.add(templateLabel, templateArea, requestLabel, requestArea);
        VerticalLayout responseLayout = new VerticalLayout();
        responseLayout.setHeightFull();
        responseLayout.setPadding(false);

        H4 responseLabel = new H4("Response");
        responseArea = new TextArea();
        responseArea.setSizeFull();
        responseArea.setReadOnly(true);

        responseLayout.add(responseLabel, responseArea);

        goButton = new Button(null, new Icon(VaadinIcon.ARROW_RIGHT), (ComponentEventListener<ClickEvent<Button>>) this::onGoClicked);

        mainLayout.add(templateLayout, goButton, responseLayout);
        mainLayout.setAlignItems(Alignment.CENTER);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        ui = attachEvent.getUI();
    }

    private void onGoClicked(ClickEvent<Button> event) {
        BatchTemplate batchTemplate;
        Request originalRequest;
        if(templateArea.getValue() == null || templateArea.getValue().isEmpty()){
            templateArea.setInvalid(true);
            templateArea.setErrorMessage("Template is empty");
            return;
        }

        try {
            batchTemplate = objectMapper.readValue(templateArea.getValue(), BatchTemplate.class);
        } catch (Exception e) {
            templateArea.setInvalid(true);
            templateArea.setErrorMessage("Invalid format");
            return;
        }

        if(requestArea.getValue() == null || requestArea.getValue().isEmpty())
            originalRequest = new Request();
        else {
            try {
                originalRequest = objectMapper.readValue(requestArea.getValue(), Request.class);
            } catch (Exception e) {
                requestArea.setInvalid(true);
                requestArea.setErrorMessage("Invalid format");
                return;
            }
        }

        templateArea.setInvalid(false);
        templateArea.setErrorMessage(null);
        requestArea.setInvalid(false);
        requestArea.setErrorMessage(null);
        responseArea.setValue("Executing ...");
        progressBar.setVisible(true);
        goButton.setEnabled(false);
        executorService.submit(() -> {
            Object response;
            try {
                response = batchEngine.execute(originalRequest, batchTemplate);
            } catch (Exception e) {
                ui.access((Command) () -> {
                    progressBar.setVisible(false);
                    goButton.setEnabled(true);
                    responseArea.setValue("Error when executing request");
                });
                return;
            }

            try {
                final String value = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
                ui.access((Command) () -> {
                    progressBar.setVisible(false);
                    goButton.setEnabled(true);
                    responseArea.setValue(value);
                });
            } catch (JsonProcessingException e) {
                ui.access((Command) () -> {
                    progressBar.setVisible(false);
                    goButton.setEnabled(true);
                    responseArea.setValue("Error when parsing response");
                });
            }
        });
    }

}
