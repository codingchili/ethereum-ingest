package com.codingchili.ethereumingest;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.listener.CoreService;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.protocol.Serializer;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.web3j.protocol.core.methods.response.EthBlock;
import rx.Subscriber;

import static com.codingchili.core.configuration.CoreStrings.ID_TIME;
import static com.codingchili.core.logging.Level.ERROR;
import static com.codingchili.ethereumingest.ApplicationContext.TX_ADDR;
import static com.codingchili.ethereumingest.ApplicationContext.getIpcClient;
import static com.codingchili.ethereumingest.ApplicationContext.getStartBlock;

public class BlockService implements CoreService {
    private ApplicationConfig config = ApplicationConfig.get();
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
                            logger.event("blockImportFailed", ERROR)
                                    .send(e.getMessage());
                        }

                        @Override
                        public void onNext(EthBlock ethBlock) {
                            String blockNumber = ethBlock.getBlock().getNumber().toString();
                            final long start = System.currentTimeMillis();

                            logger.event("processingBlock")
                                    .put("number", blockNumber)
                                    .send("persisting block");

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
                                }
                                if (config.isTxImport()) {
                                    context.bus().send(TX_ADDR, getTransactionList(ethBlock));
                                }
                                request(1);
                            });
                        }
                    });
            future.complete();
        });
    }

    private Object getTransactionList(EthBlock ethBlock) {
        JsonObject json = Serializer.json(ethBlock.getBlock().getTransactions());
        json.put(ID_TIME, ethBlock.getBlock().getTimestamp().longValue());
        return json;
    }
}