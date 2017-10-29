package com.codingchili.ethereumingest.model;

import com.codingchili.core.logging.Logger;

import java.util.HashMap;
import java.util.Map;

import static com.codingchili.core.configuration.CoreStrings.throwableToString;
import static com.codingchili.core.logging.Level.ERROR;

public class BlockLogListener implements ImportListener {
    private Map<String, Long> timers = new HashMap<>();
    private Logger logger;

    public BlockLogListener(Logger logger) {
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
    public boolean onError(Throwable e, String hash) {
        logger.event("blockImportFailed", ERROR)
                .put("hash", hash)
                .send(throwableToString(e));
        return true;
    }
}
