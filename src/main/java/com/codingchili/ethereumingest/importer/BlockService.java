package com.codingchili.ethereumingest.importer;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.ethereumingest.model.ApplicationConfig;
import com.codingchili.ethereumingest.model.EthereumBlock;
import com.codingchili.ethereumingest.model.ImportListener;
import com.codingchili.ethereumingest.model.Importer;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.web3j.protocol.core.methods.response.EthBlock;
import rx.Subscriber;

import java.util.concurrent.atomic.AtomicInteger;

import static com.codingchili.core.configuration.CoreStrings.ID_TIME;
import static com.codingchili.core.configuration.CoreStrings.throwableToString;
import static com.codingchili.core.logging.Level.ERROR;
import static com.codingchili.ethereumingest.importer.ApplicationContext.*;

public class BlockService implements Importer {
    private ApplicationConfig config = ApplicationConfig.get();
    private ImportListener listener = new ImportListener() {};
    private AtomicInteger queue = new AtomicInteger(0);
    private ApplicationContext context;
    private Logger logger;

    @Override
    public void init(CoreContext core) {
        this.context = new ApplicationContext(core);
        this.logger = core.logger(getClass());
    }

    @Override
    public void start(Future<Void> future) {
        this.context.blockStorage().setHandler(storage -> {

            if (storage.succeeded()) {
                getIpcClient().catchUpToLatestAndSubscribeToNewBlocksObservable(getStartBlock(), true).subscribe(
                        new Subscriber<EthBlock>() {
                            private Long backpressure = config.getBackpressure();

                            @Override
                            public void onStart() {
                                request(backpressure);
                            }

                            @Override
                            public void onCompleted() {
                                logger.event("blockImportComplete").send();
                            }

                            @Override
                            public void onError(Throwable e) {
                                logger.event("blockImportFailed", ERROR).send(throwableToString(e));
                                listener.onError(e);
                            }

                            @Override
                            public void onNext(EthBlock ethBlock) {
                                String blockNumber = ethBlock.getBlock().getNumber().toString();
                                final long start = System.currentTimeMillis();

                                logger.event("processingBlock")
                                        .put("number", blockNumber)
                                        .send("persisting block");

                                if (config.isBlockImport()) {
                                    listener.onQueueChanged(queue.incrementAndGet());

                                    storage.result().put(new EthereumBlock(ethBlock.getBlock()), done -> {
                                        if (done.succeeded()) {
                                            logger.event("blockPersisted")
                                                    .put("time", System.currentTimeMillis() - start)
                                                    .put("block", blockNumber)
                                                    .send("block persisted");
                                        } else {
                                            logger.event("blockFailed", ERROR)
                                                    .put("block", blockNumber)
                                                    .send("failed to persist block");
                                            listener.onError(done.cause());
                                        }
                                        listener.onQueueChanged(queue.decrementAndGet());
                                    });
                                }
                                if (config.isTxImport()) {
                                    context.bus().send(TX_ADDR, getTransactionList(ethBlock));
                                }
                                request(1);
                            }
                        });
                future.complete();
            } else {
                future.fail(storage.cause());
            }
        });
    }

    private Object getTransactionList(EthBlock ethBlock) {
        JsonObject json = Serializer.json(ethBlock.getBlock().getTransactions());
        json.put(ID_TIME, ethBlock.getBlock().getTimestamp().longValue());
        return json;
    }

    @Override
    public Importer setListener(ImportListener listener) {
        this.listener = listener;
        return this;
    }
}