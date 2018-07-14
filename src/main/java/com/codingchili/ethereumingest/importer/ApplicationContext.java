package com.codingchili.ethereumingest.importer;

import com.codingchili.ethereumingest.model.*;
import io.vertx.core.Future;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.ipc.UnixIpcService;
import org.web3j.protocol.ipc.WindowsIpcService;

import java.time.*;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.logging.ConsoleLogger;
import com.codingchili.core.storage.*;

import static com.codingchili.core.logging.Level.STARTUP;

/**
 * Application contexts: contains shared logic between the deployed services.
 */
public class ApplicationContext extends SystemContext {
    public static final String TX_ADDR = "tx";
    private static ApplicationConfig config = ApplicationConfig.get();
    private static ConsoleLogger logger = new ConsoleLogger(ApplicationContext.class);
    private static Web3j node;
    private CoreContext context;

    /**
     * @param context core context that we are running on.
     */
    public ApplicationContext(CoreContext context) {
        super(context);
        this.context = context;

        synchronized (ApplicationContext.class) {
            String endpoint = config.getTargetNode();

            if (config.isTargetHttpNode()) {
                connectUsingHttp(endpoint);
            } else {
                connectUsingIpc(endpoint);
            }

            node.ethSyncing().observable().subscribe(is -> {
                logger.event("synchronizing").put("is", is.isSyncing()).send();
            });
        }
    }

    private void connectUsingHttp(String endpoint) {
        logger.log("Connecting to " + endpoint + " using http..");
        node = Web3j.build(new HttpService(endpoint));
        logger.log("Successfully connected to a remote node.");
    }

    private void connectUsingIpc(String endpoint) {
        logger.log("Subscribing to ipc.. " + endpoint + " on " + config.getOs());
        if (config.getOs().equals(ApplicationConfig.OSType.WINDOWS)) {
            node = Web3j.build(new WindowsIpcService(endpoint));
        } else {
            node = Web3j.build(new UnixIpcService(endpoint));
        }
        logger.log("Successfully connected to ipc, waiting for blocks..");
    }

    /**
     * @return a web3j client that uses either http or ipc depending on configuration.
     */
    public static Web3j getTargetNode() {
        return node;
    }

    /**
     * @param epochSecond the epoch second to create a timestamp of.
     * @return a timestamp.
     */
    public static String timestampFrom(Long epochSecond) {
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochSecond),
                ZoneId.systemDefault()).toOffsetDateTime().toString();
    }

    /**
     * @param hash a hash of a block or transaction.
     * @return a shorter and more readable hash.
     */
    public static String shorten(String hash) {
        return hash.substring(0, 8) + "..";
    }

    private <E extends Storable> Future<AsyncStorage<E>> storage(Class<E> storable, String index) {
        Future<AsyncStorage<E>> future = Future.future();

        logger.event("onStorageLoad", STARTUP)
                .put("storage", config.getStorage().name())
                .put("plugin", storable.getSimpleName())
                .put("index", index).send();

        new StorageLoader<E>(context)
                .withDB(index, index)
                .withValue(storable)
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

    /**
     * @return an implementation of a block storage.
     */
    public Future<AsyncStorage<StorableBlock>> blockStorage() {
        return storage(StorableBlock.class, config.getBlockIndex());
    }

    /**
     * @return an implementation of a transaction storage.
     */
    public Future<AsyncStorage<StorableTransaction>> txStorage() {
        return storage(StorableTransaction.class, config.getTxIndex());
    }
}
