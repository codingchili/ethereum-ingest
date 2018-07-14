package com.codingchili.ethereumingest.commands;

import com.codingchili.core.context.*;
import com.codingchili.core.logging.ConsoleLogger;
import com.codingchili.core.logging.Logger;
import io.vertx.core.Future;

/**
 * The default command: invoked when no commands are provided or when
 * explicitly called using #{@link #getName()}.
 */
public class HelpCommand implements Command {
    private Logger logger = new ConsoleLogger(getClass());

    @Override
    public void execute(Future<CommandResult> future, CommandExecutor executor) {
        logger.log("Following commands are available");
        executor.list().forEach(command -> {
            logger.log("\t" + command.getName() + " " + command.getDescription());
        });
        future.complete(CommandResult.SHUTDOWN);
    }

    @Override
    public String getDescription() {
        return "Prints this help information";
    }

    @Override
    public String getName() {
        return "--help";
    }
}
