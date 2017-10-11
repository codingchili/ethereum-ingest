package com.codingchili.ethereumingest;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.Request;
import com.codingchili.core.logging.Level;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.protocol.Address;
import com.codingchili.core.protocol.Roles;
import com.codingchili.core.storage.AsyncStorage;
import com.codingchili.core.storage.StorageLoader;
import io.vertx.core.Future;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.ipc.UnixIpcService;
import org.web3j.protocol.ipc.WindowsIpcService;

import static com.codingchili.core.protocol.RoleMap.PUBLIC;

@Roles(PUBLIC)
@Address("api")
public class BlockHandler implements CoreHandler {
    private ApplicationConfig config = Configurations.get("application.json", ApplicationConfig.class);
    private CoreContext core;
    private Logger logger;

    @Override
    public void init(CoreContext core) {
        this.core = core;
        this.logger = core.logger(getClass());
    }

    @Override
    public void start(Future<Void> start) {
        logger.log("Loading block storage " + config.getStorage());
        blockStorage().setHandler(storage -> {
            logger.log("Block storage loaded, using index " + config.getIndex());
            logger.log("Subscribing to ipc.. " + config.getIpc() + " on " + config.getOs());
            Web3j web = getIpcClient();
            logger.log("Successfully connected to ipc, waiting for blocks..");
            start.complete();

            web.blockObservable(false).subscribe(block -> {
                String blockNumber = block.getBlock().getNumberRaw();
                logger.log("Received block number " + blockNumber);

                storage.result().put(new EthereumBlock(block.getBlock()), done -> {
                    if (done.succeeded()) {
                        logger.log("Persisted block " + blockNumber);
                    } else {
                        logger.log("Failed to persist block " + blockNumber, Level.SEVERE);
                    }
                });
            });
        });
    }

    private Web3j getIpcClient() {
        if (config.getOs().equals(ApplicationConfig.OSType.WINDOWS)) {
            return Web3j.build(new WindowsIpcService(config.getIpc()));
        } else {
            return Web3j.build(new UnixIpcService(config.getIpc()));
        }
    }

    private Future<AsyncStorage<EthereumBlock>> blockStorage() {
        Future<AsyncStorage<EthereumBlock>> future = Future.future();

        new StorageLoader<EthereumBlock>(core)
                .withDB(config.getIndex(), config.getIndex())
                .withClass(EthereumBlock.class)
                .withPlugin(config.getStoragePlugin())
                .build(future);

        return future;
    }

    @Override
    public void handle(Request request) {
        request.accept();
    }
}