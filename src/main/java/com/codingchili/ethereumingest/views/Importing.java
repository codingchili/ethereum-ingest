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
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.codingchili.core.files.Configurations.launcher;
import static com.codingchili.ethereumingest.views.Settings.SETTINGS_FXML;

public class Importing implements ApplicationScene {
    public static final String IMPORTING_FXML = "/importing.fxml";
    private static CoreContext core;
    private double progress = 0f;
    private AtomicLong blocksLeftToImport = new AtomicLong(0L);
    private AtomicInteger importedThisSec = new AtomicInteger(0);
    private Future blockImport = Future.future();
    private Future txImport = Future.future();
    private List<String> deployments = new ArrayList<>();
    private ApplicationConfig config = ApplicationConfig.get();
    @FXML
    ProgressBar blockProgress;
    @FXML
    Label blocksQueued;
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
        StartupListener.subscibe(core -> Importing.core = core);
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

        core.periodic(() -> 1000, "importPerSec", (i) -> {
            Platform.runLater(() -> {
                importedPerSec.setText(importedThisSec.get() + " Blocks/s");
                importedThisSec.set(0);
            });
        });

        blocksLeftToImport.set(Long.parseLong(config.getBlockEnd()) - Long.parseLong(config.getStartBlock()));
        setupCompletionListeners();
    }

    private void setupCompletionListeners() {
        List<Future> futures = new ArrayList<>();
        if (config.isTxImport()) {
            futures.add(txImport);
        }
        if (config.isBlockImport()) {
            futures.add(blockImport);
        }
        CompositeFuture.all(futures).setHandler(done -> {
            if (done.succeeded()) {
                finishWithSuccess();
            } else {
                cancelImport(null);
                Form.showAlertFromError(done.cause());
                Async.setScene(SETTINGS_FXML);
            }
        });
    }

    private void finishWithSuccess() {
        // todo show number of blocks and tx-s imported.
        Form.showInfoAlert("Success!", "The import has completed.");
        cancelImport(null);
    }

    private ImportListener blockListener = new ImportListener() {
        @Override
        public void onImported(String hash, Long number) {
            Platform.runLater(() -> {
                blockProgress.setProgress(getProgress(number));
                blocksLeftToImport.decrementAndGet();
                importedThisSec.incrementAndGet();
                blocksLeft.setText(blocksLeftToImport.get() + " blocks left");
            });
        }

        private double getProgress(Long blockNumber) {
            long startBlock = Long.parseLong(config.getStartBlock());
            long blockEnd = Long.parseLong(config.getBlockEnd());
            long total = blockEnd - startBlock;
            long imported = blockNumber - startBlock;

            double progress = (imported * 1.0 / total * 1.0);
            if (progress > Importing.this.progress) {
                // prevent the progress from jumping back when blocks are not imported in order.
                Importing.this.progress = progress;
                return progress;
            } else {
                return Importing.this.progress;
            }
        }

        @Override
        public void onQueueChanged(int queued) {
            Platform.runLater(() -> blocksQueued.setText("Blocks queued: " + queued));
        }

        @Override
        public void onFinished() {
            blockImport.complete();
        }

        @Override
        public boolean onError(Throwable e) {
            blockImport.fail(e);
            return true;
        }
    };

    private ImportListener txListener = new ImportListener() {
        @Override
        public void onImported(String hash, Long number) {
            Platform.runLater(() -> {

            });
        }

        @Override
        public void onQueueChanged(int queued) {
            Platform.runLater(() -> txQueued.setText("Tx queued: " + queued));
        }

        @Override
        public void onFinished() {
            txImport.tryComplete();
        }

        @Override
        public boolean onError(Throwable e) {
            txImport.tryFail(e);
            return true;
        }
    };
}
