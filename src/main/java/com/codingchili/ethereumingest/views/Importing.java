package com.codingchili.ethereumingest.views;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.StartupListener;
import com.codingchili.ethereumingest.importer.BlockService;
import com.codingchili.ethereumingest.importer.TransactionService;
import com.codingchili.ethereumingest.model.ApplicationConfig;
import com.sun.javafx.util.Utils;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

import static com.codingchili.core.configuration.CoreStrings.throwableToString;
import static com.codingchili.core.files.Configurations.launcher;
import static com.codingchili.ethereumingest.views.Settings.SETTINGS_FXML;

public class Importing implements ApplicationScene {
    public static final String IMPORTING_FXML = "/importing.fxml";
    private static CoreContext core;
    private AtomicInteger progress = new AtomicInteger(0);
    private List<String> deployments;
    private ApplicationConfig config = ApplicationConfig.get();
    @FXML
    ProgressBar blockProgress;
    @FXML
    ProgressIndicator txProgress;
    @FXML
    Label blockQueued;
    @FXML
    Label txQueued;
    @FXML
    Label importedPerSec;
    @FXML
    Label blocksLeft;
    @FXML
    Label title;
    @FXML
    Pane importingPane;
    @FXML
    Pane loadingPane;
    @FXML
    Label version;

    static {
        StartupListener.subscibe(core -> {
            Importing.core = core;
        });
    }

    @FXML
    public void cancelImport(Event event) {
        Async.setScene(SETTINGS_FXML);
        deployments.forEach(core::stop);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<Future> deployed = new ArrayList<>();
        version.setText(launcher().getVersion());

        if (config.isBlockImport()) {
            deployed.add(core.service(BlockService::new));
        }

        if (config.isTxImport()) {
            deployed.add(core.service(TransactionService::new));
        }

        CompositeFuture.<String>all(deployed)
                .setHandler(done -> Async.invoke(() -> {
                    if (done.succeeded()) {
                        importingPane.setVisible(true);
                        loadingPane.setVisible(false);
                        title.setText("Importing");
                        deployments = done.result().list();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.initStyle(StageStyle.UTILITY);
                        alert.setTitle("Error importing");
                        alert.setContentText("An error has occured, submit an issue with the stack " +
                                "trace if you need help.");
                        alert.setHeaderText(null);

                        TextArea textArea = new TextArea(throwableToString(done.cause()));
                        textArea.setEditable(false);

                        textArea.setMaxWidth(Double.MAX_VALUE);
                        textArea.setMaxHeight(Double.MAX_VALUE);
                        GridPane.setVgrow(textArea, Priority.ALWAYS);
                        GridPane.setHgrow(textArea, Priority.ALWAYS);

                        GridPane expContent = new GridPane();
                        expContent.setMaxWidth(Double.MAX_VALUE);
                        expContent.add(textArea, 0, 1);
                        alert.getDialogPane().setExpandableContent(expContent);

                        alert.showAndWait();
                        Async.setScene(SETTINGS_FXML);
                    }
                }));
    }
}
