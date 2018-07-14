package com.codingchili.ethereumingest.importer;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.storage.AsyncStorage;

import com.codingchili.ethereumingest.model.ApplicationConfig;
import com.codingchili.ethereumingest.model.ImportListener;
import com.codingchili.ethereumingest.model.Importer;
import com.codingchili.ethereumingest.model.StorableTransaction;
import com.codingchili.ethereumingest.model.TransactionLogListener;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import rx.Observable;
import rx.Subscriber;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.codingchili.core.configuration.CoreStrings.ID_COLLECTION;
import static com.codingchili.core.configuration.CoreStrings.ID_TIME;
import static com.codingchili.ethereumingest.importer.ApplicationContext.TX_ADDR;
import static com.codingchili.ethereumingest.importer.ApplicationContext.timestampFrom;

/**
 * A service that receives a list of transactions from each block retrieved from the
 * ipc connection in #{@link BlockService}. This service is only used if
 * #{@link ApplicationConfig#txImport} is set to true.
 */
public class TransactionService implements Importer {
    private AtomicInteger queue = new AtomicInteger(0);
    private AsyncStorage<StorableTransaction> storage;
    private ApplicationConfig config = ApplicationConfig.get();
    private ApplicationContext context;
    private ImportListener listener;

    @Override
    public void init(CoreContext context) {
        this.context = new ApplicationContext(context);

        if (listener == null) {
            listener = new TransactionLogListener(context.logger(getClass()));
        }
    }

    @Override
    public void start(Future<Void> future) {
        context.txStorage().setHandler(done -> {

            if (done.succeeded()) {
                storage = done.result();
                context.bus().localConsumer(TX_ADDR, request -> {
                    Collection<StorableTransaction> txs = getTransactionList(request.body());
                    queue.getAndAdd(txs.size());
                    listener.onQueueChanged(queue.get());
                    importTx(request, txs);
                });
                future.complete();
            } else {
                future.fail(done.cause());
            }
        });
    }

    private void importTx(Message<?> request, Collection<StorableTransaction> list) {
        final AtomicReference<String> hash = new AtomicReference<>();

        Observable.from(list).subscribe(new Subscriber<StorableTransaction>() {
            @Override
            public void onStart() {
                request(config.getBackPressureTx());
            }

            @Override
            public void onCompleted() {
                request.reply(null);
                listener.onFinished();
            }

            @Override
            public void onError(Throwable e) {
                listener.onError(e, hash.get());
                request.reply(e);
                unsubscribe();
            }

            @Override
            public void onNext(StorableTransaction tx) {
                listener.onImportStarted(tx.getHash(), tx.getBlockNumber().longValue());

                storage.put(tx, done -> {
                    listener.onQueueChanged(queue.decrementAndGet());

                    if (done.succeeded()) {
                        listener.onImported(tx.getHash(), tx.getBlockNumber().longValue());
                        request(1);
                    } else {
                        hash.set(tx.getHash());
                        onError(done.cause());
                    }
                });
            }
        });
    }

    /**
     * Retrieves a list of transactions from an eventbus message.
     *
     * @param body a json object that contains transactions.
     * @return a collection of transactions.
     */
    private Collection<StorableTransaction> getTransactionList(Object body) {
        Collection<StorableTransaction> txs = new ArrayList<>();
        JsonObject data = (JsonObject) body;
        data.getJsonArray(ID_COLLECTION).forEach(json -> {
            StorableTransaction tx = Serializer.unpack((JsonObject) json, StorableTransaction.class);
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
