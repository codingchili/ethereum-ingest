package com.codingchili.ethereumingest.views;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.StartupListener;
import com.codingchili.ethereumingest.importer.BlockService;
import com.codingchili.ethereumingest.importer.TransactionService;
import com.codingchili.ethereumingest.model.ApplicationConfig;
import com.codingchili.ethereumingest.model.ImportListener;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

import static com.codingchili.core.files.Configurations.launcher;
import static com.codingchili.ethereumingest.views.Settings.SETTINGS_FXML;

public class Importing implements ApplicationScene {
    public static final String IMPORTING_FXML = "/importing.fxml";
    private static CoreContext core;
    private AtomicInteger progress = new AtomicInteger(0);
    private List<String> deployments = new ArrayList<>();
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

        deployed.add(core.service(() -> new BlockService().setListener(blockListener)));
        deployed.add(core.service(() -> new TransactionService().setListener(txListener)));

        CompositeFuture.<String>all(deployed)
                .setHandler(done -> Async.invoke(() -> {
                    if (done.succeeded()) {
                        importingPane.setVisible(true);
                        loadingPane.setVisible(false);
                        title.setText("Importing");
                        deployments = done.result().list();
                    } else {
                        Form.showAlertFromError(done.cause());
                        Async.setScene(SETTINGS_FXML);
                    }
                }));
    }

    private ImportListener blockListener = new ImportListener() {
        @Override
        public void onImported(String hash, Long number) {

        }

        @Override
        public void onQueueChanged(int queued) {
            Platform.runLater(() -> {
                blockQueued.setText("Blocks queued: " + queued);
            });
        }

        @Override
        public void onFinished() {
            // show success alert - back to settings.
        }

        @Override
        public boolean onError(Throwable e) {
            cancelImport(null);
            Form.showAlertFromError(e);
            Async.setScene(SETTINGS_FXML);
            return true;
        }
    };

    private ImportListener txListener = new ImportListener() {
        @Override
        public void onImported(String hash, Long number) {

        }

        @Override
        public void onQueueChanged(int queued) {
            Platform.runLater(() -> {
                txQueued.setText("Tx queued: " + queued);
            });
        }

        @Override
        public void onFinished() {
            if (!config.isBlockImport()) {
                // show success alert - back to settings.
            }
        }

        @Override
        public boolean onError(Throwable e) {
            cancelImport(null);
            Form.showAlertFromError(e);
            Async.setScene(SETTINGS_FXML);
            return true;
        }
    };
}
