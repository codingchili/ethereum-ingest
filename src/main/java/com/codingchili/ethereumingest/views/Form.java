package com.codingchili.ethereumingest.views;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

import static com.codingchili.ethereumingest.views.Splash.SPLASH_XML;

public class Form extends Application{
    static final int WIDTH = 600;
    static final int HEIGHT = 300;
    static final String CSS_FILE = "/style.css";
    private static final String APP_TITLE = "Ethereum Ingest";
    private static double offsetX = 0;
    private static double offsetY = 0;
    private Stage stage;

    public void start() {
        Async.onExecutor(Application::launch);
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        stage.setTitle(APP_TITLE);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setResizable(false);
        stage.centerOnScreen();
        Async.setStage(stage);
        Async.setScene(SPLASH_XML);
    }

    public static Parent parent(String resource) {
        try {
            Parent parent = FXMLLoader.load(Form.class.getResource(resource));
            parent.setOnMousePressed(event -> Async.invoke(stage -> {
                offsetX = stage.getX() - event.getScreenX();
                offsetY = stage.getY() - event.getScreenY();
            }));

            parent.setOnMouseDragged(event -> Async.invoke(stage -> {
                stage.setX(event.getScreenX() + offsetX);
                stage.setY(event.getScreenY() + offsetY);
            }));
            return parent;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String css(String cssFile) {
        return Form.class.getResource(cssFile).toExternalForm();
    }
}
