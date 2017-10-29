package com.codingchili.ethereumingest.model;

import com.codingchili.core.logging.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static com.codingchili.core.configuration.CoreStrings.throwableToString;
import static com.codingchili.core.logging.Level.ERROR;

/**
 * Provides logging mesages from the Block import service.
 * Used by default when importing from the command line.
 */
public class BlockLogListener implements ImportListener {
    private Map<String, Long> timers = new HashMap<>();
    private AtomicLong lastBlock = new AtomicLong(0);
    private Logger logger;

    public BlockLogListener(Logger logger) {
        ApplicationConfig config = ApplicationConfig.get();
        lastBlock.set(Long.parseLong(config.getStartBlock()));
        this.logger = logger;
    }

    @Override
    public void onImported(String hash, Long number) {
        logger.event("blockPersisted")
                .put("time", System.currentTimeMillis() - timers.get(hash))
                .put("block", number)
                .send("block persisted");
    }

    @Override
    public void onSourceDepleted() {
        logger.event("onBlockSynced")
                .put("next", lastBlock.get() + 1)
                .send("fully synchroized: waiting for next block.");
    }

    @Override
    public void onImportStarted(String hash, Long number) {
        timers.put(hash, System.currentTimeMillis());
        logger.event("processingBlock")
                .put("number", number)
                .send("persisting block");
    }

    @Override
    public void onFinished() {
        logger.event("blockImportComplete").send();
    }

    @Override
    public void onError(Throwable e, String hash) {
        logger.event("blockImportFailed", ERROR)
                .put("hash", hash)
                .send(throwableToString(e));
    }
}
