package com.codingchili.ethereumingest.model;

import com.codingchili.core.logging.Logger;

import java.util.HashMap;
import java.util.Map;

import static com.codingchili.core.configuration.CoreStrings.throwableToString;
import static com.codingchili.core.logging.Level.ERROR;
import static com.codingchili.ethereumingest.importer.ApplicationContext.shorten;

public class TransactionLogListener implements ImportListener {
    private Map<String, Long> timers = new HashMap<>();
    private Logger logger;

    public TransactionLogListener(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void onImported(String hash, Long number) {
        logger.event("persistedTx")
                .put("hash", shorten(hash))
                .put("block", number)
                .put("time", System.currentTimeMillis() - timers.get(hash))
                .send("transaction persisted");
    }

    @Override
    public void onImportStarted(String hash, Long number) {
        timers.put(hash, System.currentTimeMillis());
        /*logger.event("processingTx")
                .put("hash", shorten(hash))
                .send("processing transaction");*/
    }

    @Override
    public boolean onError(Throwable e, String hash) {
        logger.event("failedTx", ERROR)
                .put("hash", hash)
                .send(throwableToString(e));
        return true;
    }
}
