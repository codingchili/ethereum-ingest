package com.codingchili.ethereumingest.importer;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.ethereumingest.model.EthereumTransaction;
import com.codingchili.ethereumingest.model.ImportListener;
import com.codingchili.ethereumingest.model.Importer;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import static com.codingchili.core.configuration.CoreStrings.*;
import static com.codingchili.core.logging.Level.ERROR;
import static com.codingchili.ethereumingest.importer.ApplicationContext.*;

public class TransactionService implements Importer {
    private ImportListener listener = new ImportListener() {};
    private ApplicationContext context;
    private AtomicInteger queue = new AtomicInteger(0);
    private Logger logger;

    @Override
    public void init(CoreContext context) {
        this.context = new ApplicationContext(context);
        this.logger = this.context.logger(getClass());
    }

    @Override
    public void start(Future<Void> future) {
        context.txStorage().setHandler(storage -> {

            if (storage.succeeded()) {
                context.bus().consumer(TX_ADDR, request -> {
                    Collection<EthereumTransaction> txs = getTransactionList(request.body());
                    final long start = System.currentTimeMillis();

                    queue.getAndAdd(txs.size());
                    for (EthereumTransaction tx : txs) {
                        String txHash = tx.getHash();
                        logger.event("processingTx")
                                .put("hash", shorten(txHash))
                                .send("processing transaction");

                        storage.result().put(tx, done -> {
                            queue.decrementAndGet();
                            if (done.succeeded()) {
                                listener.onImported(tx.getHash(), tx.getBlockNumber().longValue());
                                logger.event("persistedTx")
                                        .put("hash", shorten(txHash))
                                        .put("block", tx.getBlockNumberRaw())
                                        .put("time", System.currentTimeMillis() - start)
                                        .send("transaction persisted");
                                request.reply(true);
                                listener.onFinished(); // todo: does not know when the last tx is imported
                                                        // finish when the current batch is done? mark the last batch?
                            } else {
                                logger.event("failedTx", ERROR)
                                        .put("hash", txHash)
                                        .send(throwableToString(done.cause()));
                                listener.onError(done.cause());
                                request.reply(false);
                            }
                        });
                    }
                });
                future.complete();
            } else {
                future.fail(storage.cause());
            }
        });
    }

    private Collection<EthereumTransaction> getTransactionList(Object body) {
        Collection<EthereumTransaction> txs = new ArrayList<>();
        JsonObject data = (JsonObject) body;
        data.getJsonArray(ID_COLLECTION).forEach(json -> {
            EthereumTransaction tx = Serializer.unpack((JsonObject) json, EthereumTransaction.class);
            tx.setTimestamp(timestampFrom(data.getLong(ID_TIME)));
            txs.add(tx);
        });
        return txs;
    }

    @Override
    public Importer setListener(ImportListener listener) {
        this.listener = listener;
        return this;
    }
}
