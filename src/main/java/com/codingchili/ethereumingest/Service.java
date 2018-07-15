package com.codingchili.ethereumingest;

import com.codingchili.ethereumingest.commands.CommandLine;
import io.vertx.core.Future;

import com.codingchili.core.context.LaunchContext;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.listener.CoreService;

import static com.codingchili.core.files.Configurations.*;

/**
 * Application entry point, starts up the framework and parses the
 * commandline options.
 */
public class Service implements CoreService {

    public static void main(String[] args) {
        system().setHandlers(1).setListeners(1);
        launcher().setApplication("Ethereum Ingest")
                .setVersion("1.0.3")
                .setAuthor("codingchili@github")
                .setClustered(true)
                .deployable(Service.class);

        // make sure configuration file for storage is available.
        Configurations.storage().save();

        LaunchContext context = new LaunchContext(args);
        context.setCommandExecutor(CommandLine.get()).start();
    }

    @Override
    public void start(Future start) {
        start.complete();
    }
}
