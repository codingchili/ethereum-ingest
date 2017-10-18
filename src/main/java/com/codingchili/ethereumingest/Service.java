package com.codingchili.ethereumingest;

import com.codingchili.core.Launcher;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.listener.CoreService;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;

import static com.codingchili.core.files.Configurations.launcher;
import static com.codingchili.core.files.Configurations.system;

public class Service implements CoreService {
    private CoreContext core;

    public static void main(String[] args) {
        system().setHandlers(1).setListeners(1);
        launcher().setApplication("EthereumIngest")
                .setVersion("1.0.0")
                .deployable(Service.class);
        Launcher.main(args);
    }

    @Override
    public void init(CoreContext core) {
        this.core = core;
    }

    @Override
    public void start(Future start) {
        CompositeFuture.all(
          core.service(TransactionService::new),
          core.service(TransactionService::new),
          core.service(BlockService::new)
        );
    }
}
