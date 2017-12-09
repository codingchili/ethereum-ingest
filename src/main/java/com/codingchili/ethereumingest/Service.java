package com.codingchili.ethereumingest;

import com.codingchili.core.context.LaunchContext;
import com.codingchili.core.listener.CoreService;
import com.codingchili.ethereumingest.commands.CommandLine;
import io.vertx.core.Future;

import java.io.IOException;

import static com.codingchili.core.files.Configurations.launcher;
import static com.codingchili.core.files.Configurations.system;

/**
 * Application entry point, starts up the framework and parses the
 * commandline options.
 */
public class Service implements CoreService {

    public static void main(String[] args) throws IOException {
        system().setHandlers(1).setListeners(1);
        launcher().setApplication("Ethereum Ingest")
                .setVersion("1.0.1")
                .setAuthor("codingchili@github")
                .setClustered(true)
                .deployable(Service.class);

        LaunchContext context = new LaunchContext(args);
        context.setCommandExecutor(CommandLine.get()).start();
    }

    @Override
    public void start(Future start) {
        start.complete();
    }
}
