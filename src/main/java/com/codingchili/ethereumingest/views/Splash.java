package com.codingchili.ethereumingest.views;

import com.codingchili.core.context.StartupListener;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;

import static com.codingchili.core.files.Configurations.launcher;
import static com.codingchili.ethereumingest.views.Settings.SETTINGS_FXML;

/**
 * This view is shown when the application is loading.
 */
public class Splash implements ApplicationScene {
    public static final String SPLASH_XML = "/splash.fxml";
    @FXML
    Label version;
    @FXML
    Label title;
    @FXML
    Hyperlink author;
    @FXML
    ProgressIndicator loading;
    @FXML
    ImageView logo;

    @FXML
    private void openGithubLink(Event event) {
        try {
            Desktop.getDesktop().browse(URI.create("https://github.com/codingchili"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Form.centerLabelText(title);
        Form.centerLabelText(version);

        version.setText(launcher().getVersion());
        title.setText(launcher().getApplication());
        author.setText("An app by " + launcher().getAuthor());

        logo.setOpacity(0.24);
        StartupListener.subscribe(core -> {
            Async.onExecutor(() -> {
                Platform.runLater(() -> {
                    loading.setVisible(false);
                });
            });
            // fade in the logo when loading is complete.
            Async.periodic(16, () -> {
                logo.setOpacity(logo.getOpacity() + 0.016);
                if (logo.getOpacity() > 1.0) {
                    logo.setOpacity(1.0);
                }
            });
            // wait some additional time to make sure that the splash is shown for a bit.
            Async.timer(1250, () -> {
                Async.setScene(SETTINGS_FXML);
            });
        });
    }
}
