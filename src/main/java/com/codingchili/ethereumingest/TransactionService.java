package com.codingchili.ethereumingest;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.listener.CoreService;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.protocol.Serializer;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.web3j.protocol.core.methods.response.Transaction;

import java.util.ArrayList;
import java.util.Collection;

import static com.codingchili.core.configuration.CoreStrings.ID_COLLECTION;
import static com.codingchili.core.configuration.CoreStrings.ID_TIME;
import static com.codingchili.core.configuration.CoreStrings.throwableToString;
import static com.codingchili.core.logging.Level.ERROR;
import static com.codingchili.ethereumingest.ApplicationContext.TX_ADDR;
import static com.codingchili.ethereumingest.ApplicationContext.shorten;
import static com.codingchili.ethereumingest.ApplicationContext.timestampFrom;

public class TransactionService implements CoreService {
    private ApplicationContext context;
    private Logger logger;

    @Override
    public void init(CoreContext context) {
        this.context = new ApplicationContext(context);
        this.logger = this.context.logger(getClass());
    }

    @Override
    public void start(Future<Void> future) {
        context.txStorage().setHandler(storage -> {

            context.bus().consumer(TX_ADDR, request -> {
                Collection<EthereumTransaction> txs = getTransactionList(request.body());
                final long start = System.currentTimeMillis();

                for (EthereumTransaction tx : txs) {
                    String txHash = tx.getHash();
                    logger.event("processingTx")
                            .put("hash", shorten(txHash))
                            .send("processing transaction");

                    storage.result().put(tx, done -> {
                        if (done.succeeded()) {
                            logger.event("persistedTx")
                                    .put("hash", shorten(txHash))
                                    .put("block", tx.getBlockNumberRaw())
                                    .put("time", System.currentTimeMillis() - start)
                                    .send("transaction persisted");
                            request.reply(true);
                        } else {
                            logger.event("failedTx", ERROR)
                                    .put("hash", txHash)
                                    .send(throwableToString(done.cause()));
                            request.reply(false);
                        }
                    });
                }
            });
            future.complete();
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
}
