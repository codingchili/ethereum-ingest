package com.codingchili.ethereumingest.views;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.StartupListener;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Async {
    private static ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);
    private static Stage holder;
    private static CoreContext core;

    static {
        StartupListener.subscibe(core -> {
            Async.core = core;
        });
    }

    public static void setScene(String xml) {
        Parent parent = Form.parent(xml);

        FadeTransition transition = new FadeTransition(Duration.millis(1250), parent);
        transition.setFromValue(0.0);
        transition.setToValue(1.0);
        transition.play();

        StyledScene scene = new StyledScene(parent);
        invoke(() -> {
            holder.setScene(scene);
            holder.show();
        });

        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                shutdown();
            }
        });
    }

    public static void shutdown() {
        invoke(() -> {
            holder.close();
            if (core != null) {
                core.close();
            }
            executor.shutdown();
            Platform.exit();
        });
    }

    public static void timer(int ms, Runnable runnable) {
        executor.schedule(() -> invoke(runnable), ms, TimeUnit.MILLISECONDS);
    }

    public static void periodic(int ms, Runnable runnable) {
        executor.scheduleAtFixedRate(() -> invoke(runnable), 0, ms, TimeUnit.MILLISECONDS);
    }

    public static void onExecutor(Runnable runnable) {
        executor.submit(runnable);
    }

    public static void invoke(Runnable runnable) {
        Platform.runLater(runnable);
    }

    public static void invoke(Consumer<Stage> callable) {
        Platform.runLater(() -> callable.accept(holder));
    }

    public static void setStage(Stage stage) {
        Async.holder = stage;
    }
}
