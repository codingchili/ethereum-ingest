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
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.codingchili.core.files.Configurations.launcher;
import static com.codingchili.ethereumingest.views.Settings.SETTINGS_FXML;
import static java.lang.String.format;

/**
 * View that is visible when importing.
 * <p>
 * - Shows import progress bar while importing
 * - Shows a spinner while waiting for new blocks
 * - Shows a spinner while waiting for the storage to load/connect
 * - Shows an error message when an import operation fails. The error mesage
 * is displayed in an Alert, which when closed returns the view to Import.
 */
public class Importing implements ApplicationScene {
    public static final String IMPORTING_FXML = "/importing.fxml";
    private static final int BLOCK_WAIT_SHOW = 3800;
    private static CoreContext core;

    static {
        StartupListener.subscibe(core -> Importing.core = core);
    }

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
    Hyperlink version;
    @FXML
    Label statusLabel;
    private double progress = 0f;
    private AtomicInteger totalBlocksImported = new AtomicInteger(0);
    private AtomicInteger totalTxImported = new AtomicInteger(0);
    private AtomicLong blocksLeftToImport = new AtomicLong(0L);
    private AtomicInteger importedThisSec = new AtomicInteger(0);
    private AtomicBoolean synced = new AtomicBoolean(false);
    private AtomicLong lastBlock = new AtomicLong(0);
    private Future blockImport = Future.future();
    private Future txImport = Future.future();
    private List<String> deployments = new ArrayList<>();
    private ApplicationConfig config = ApplicationConfig.get();
    private ImportListener blockListener = new ImportListener() {

        @Override
        public void onSourceDepleted() {
            synced.set(true);
            core.timer(BLOCK_WAIT_SHOW, done -> {
                Platform.runLater(() -> {
                    Form.fadeIn(loadingPane);
                    loadingPane.setVisible(true);
                    importingPane.setVisible(false);
                    title.setText("Pending");
                    statusLabel.setText("Waiting for block number " + (lastBlock.get() + 1) + " ..");
                });
            });
        }

        @Override
        public void onImportStarted(String hash, Long number) {
            if (synced.get()) {
                Platform.runLater(() -> {
                    loadingPane.setVisible(false);
                    importingPane.setVisible(true);
                });
            }
        }

        @Override
        public void onImported(String hash, Long number) {
            lastBlock.set(number);
            totalBlocksImported.incrementAndGet();
            importedThisSec.incrementAndGet();
            blocksLeftToImport.decrementAndGet();
            Platform.runLater(() -> {
                blockProgress.setProgress(getProgress(number));
                blocksLeft.setText(blocksLeftToImport.get() + " blocks left");
            });
        }

        private double getProgress(Long blockNumber) {
            long startBlock = Long.parseLong(config.getStartBlock());
            long blockEnd = Long.parseLong(config.getBlockEnd());
            long total = blockEnd - startBlock;
            long current = blockNumber - startBlock;

            double progress = (current * 1.0 / total * 1.0);
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
        public void onError(Throwable e, String hash) {
            blockImport.tryFail(e);
        }
    };
    private ImportListener txListener = new ImportListener() {
        @Override
        public void onImported(String hash, Long number) {
            totalTxImported.incrementAndGet();
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
        public void onError(Throwable e, String hash) {
            txImport.tryFail(e);
        }
    };

    @FXML
    public void cancelImport(Event event) {
        Async.setScene(SETTINGS_FXML);
        deployments.forEach(core::stop);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<Future> deployed = new ArrayList<>();
        version.setText(launcher().getVersion());

        Form.centerLabelText(statusLabel);
        Form.centerLabelText(title);

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
        Form.showInfoAlert("Success!",
                format("Imported %d blocks into '%s'.\nImported %d transactions into '%s'.",
                        totalBlocksImported.get(), config.getBlockIndex(),
                        totalTxImported.get(), config.getTxIndex()));
        cancelImport(null);
    }

    @FXML
    private void openGithubRepo(Event event) {
        try {
            Desktop.getDesktop().browse(URI.create("https://github.com/codingchili/ethereum-ingest"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
