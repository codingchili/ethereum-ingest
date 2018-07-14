package com.codingchili.ethereumingest.commands;

import com.codingchili.core.context.*;
import com.codingchili.core.logging.ConsoleLogger;
import io.vertx.core.Future;

import static com.codingchili.core.configuration.CoreStrings.getCommand;
import static com.codingchili.core.files.Configurations.launcher;

/**
 * When inovked from the command line with the --version option displays
 * the current version of the application.
 */
public class VersionCommand implements Command {

    @Override
    public void execute(Future<CommandResult> future, CommandExecutor executor) {
        new ConsoleLogger(getClass()).log("Current version is " + launcher().getVersion());
        future.complete(CommandResult.SHUTDOWN);
    }

    @Override
    public String getDescription() {
        return "Prints the version information";
    }

    @Override
    public String getName() {
        return getCommand("version");
    }
}
