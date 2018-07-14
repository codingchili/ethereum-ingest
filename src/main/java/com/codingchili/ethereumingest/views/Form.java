package com.codingchili.ethereumingest.views;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;

import static com.codingchili.core.configuration.CoreStrings.throwableToString;
import static com.codingchili.ethereumingest.views.Splash.SPLASH_XML;

/**
 * Helper class to simplify some form related tasks in JavaFX.
 */
public class Form extends Application {
    static final int WIDTH = 600;
    static final int HEIGHT = 300;
    static final String CSS_FILE = "/style.css";
    private static final String ICON_PNG = "/logo.png";
    private static final int FADE_IN_MS = 675;
    private static final String APP_TITLE = "Ethereum Ingest";
    private static double offsetX = 0;
    private static double offsetY = 0;

    public static void fadeIn(Node node) {
        FadeTransition transition = new FadeTransition(Duration.millis(FADE_IN_MS), node);
        transition.setFromValue(0.0);
        transition.setToValue(1.0);
        transition.play();
    }

    public static void centerLabelText(Label label) {
        label.setContentDisplay(ContentDisplay.TOP);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER);
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

    public static void showAlertFromError(Throwable e) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Error");
            alert.setContentText("An error has occured, submit an issue with the stack " +
                    "trace if you need help.");
            alert.setHeaderText(null);

            TextArea textArea = new TextArea(throwableToString(e));
            textArea.setEditable(false);

            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(textArea, 0, 1);
            alert.getDialogPane().setExpandableContent(expContent);
            alert.getDialogPane().getStylesheets().add(Form.class.getResource(CSS_FILE).toExternalForm());
            alert.showAndWait();
        });
    }

    public static void showInfoAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle(title);
            alert.setContentText(message);
            alert.setHeaderText(null);
            alert.getDialogPane().getStylesheets().add(Form.class.getResource(CSS_FILE).toExternalForm());
            alert.showAndWait();
        });
    }

    public static String css(String cssFile) {
        return Form.class.getResource(cssFile).toExternalForm();
    }

    public void start() {
        Async.onExecutor(Application::launch);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle(APP_TITLE);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setResizable(false);
        stage.centerOnScreen();
        Async.setStage(stage);
        Async.setScene(SPLASH_XML);
        stage.getIcons().add(new Image(Form.class.getResourceAsStream(ICON_PNG)));
    }
}
