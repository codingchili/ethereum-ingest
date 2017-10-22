package com.codingchili.ethereumingest.importer;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.logging.ConsoleLogger;
import com.codingchili.core.storage.AsyncStorage;
import com.codingchili.core.storage.Storable;
import com.codingchili.core.storage.StorageLoader;
import com.codingchili.ethereumingest.model.ApplicationConfig;
import com.codingchili.ethereumingest.model.EthereumBlock;
import com.codingchili.ethereumingest.model.EthereumTransaction;
import io.vertx.core.Future;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.ipc.UnixIpcService;
import org.web3j.protocol.ipc.WindowsIpcService;

import java.math.BigInteger;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static com.codingchili.core.logging.Level.STARTUP;

public class ApplicationContext extends SystemContext {
    public static final String TX_ADDR = "tx";
    private static ApplicationConfig config = ApplicationConfig.get();
    private static Web3j web = getIpcClient();
    private static ConsoleLogger logger = new ConsoleLogger(ApplicationContext.class);
    private CoreContext context;

    public ApplicationContext(CoreContext context) {
        super(context);
        this.context = context;

        synchronized (ApplicationContext.class) {
            if (web == null) {
                logger.log("Subscribing to ipc.. " + config.getIpc() + " on " + config.getOs());
                if (config.getOs().equals(ApplicationConfig.OSType.WINDOWS)) {
                    web = Web3j.build(new WindowsIpcService(config.getIpc()));
                } else {
                    web = Web3j.build(new UnixIpcService(config.getIpc()));
                }
                logger.log("Successfully connected to ipc, waiting for blocks..");

                web.ethSyncing().observable().subscribe(is -> {
                    logger.event("synchronizing").put("is", is.isSyncing()).send();
                });
            }
        }
    }

    private <E extends Storable> Future<AsyncStorage<E>> storage(Class<E> storable, String index) {
        Future<AsyncStorage<E>> future = Future.future();

        logger.event("onStorageLoad", STARTUP)
                .put("storage", config.getStorage().name())
                .put("plugin", storable.getSimpleName())
                .put("index", index).send();

        new StorageLoader<E>(context)
                .withDB(index, index)
                .withClass(storable)
                .withPlugin(config.getStoragePlugin())
                .build((storage) -> {
                    if (storage.succeeded()) {
                        future.complete(storage.result());
                    } else {
                        logger.log("Failed to load storage " + config.getStoragePlugin().getSimpleName() +
                                " using index" + index);
                        future.fail(storage.cause());
                    }
                });

        return future;
    }

    public static DefaultBlockParameter getStartBlock() {
        return new DefaultBlockParameterNumber(new BigInteger(config.getStartBlock()));
    }

    public static Web3j getIpcClient() {
        return web;
    }

    public Future<AsyncStorage<EthereumBlock>> blockStorage() {
        return storage(EthereumBlock.class, config.getBlockIndex());
    }

    public Future<AsyncStorage<EthereumTransaction>> txStorage() {
        return storage(EthereumTransaction.class, config.getTxIndex());
    }

    public static String timestampFrom(Long epochSecond) {
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochSecond),
                ZoneId.systemDefault()).toOffsetDateTime().toString();
    }

    public static String shorten(String hash) {
        return hash.substring(0, 8) + "..";
    }
}
