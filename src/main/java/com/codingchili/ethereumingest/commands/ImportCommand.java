package com.codingchili.ethereumingest.commands;

import com.codingchili.core.context.Command;
import com.codingchili.core.context.CommandExecutor;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.listener.CoreService;
import com.codingchili.ethereumingest.importer.BlockService;
import com.codingchili.ethereumingest.importer.TransactionService;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;

import static com.codingchili.core.configuration.CoreStrings.getCommand;
import static com.codingchili.core.files.Configurations.launcher;

/**
 * A command that starts an import using appplication.json configuration if present.
 * If not present the import is started with the defaults in
 * #{@link com.codingchili.ethereumingest.model.ApplicationConfig}
 */
public class ImportCommand implements Command, CoreService {
    private CoreContext core;

    @Override
    public void execute(Future<Boolean> future, CommandExecutor executor) {
        launcher().deployable(ImportCommand.class);
        future.complete(false);
    }

    @Override
    public void init(CoreContext core) {
        this.core = core;
    }

    @Override
    public void start(Future<Void> start) {
        CompositeFuture.all(
                core.service(TransactionService::new),
                core.service(BlockService::new)
        ).setHandler((done) -> {
            if (done.succeeded()) {
                start.complete();
            } else {
                start.fail(done.cause());
            }
        });
    }

    @Override
    public String getDescription() {
        return "Starts an import using ./application.json or defaults if missing.";
    }

    @Override
    public String getName() {
        return getCommand("import");
    }
}
