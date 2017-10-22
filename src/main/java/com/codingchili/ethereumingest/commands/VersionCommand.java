package com.codingchili.ethereumingest.commands;

import com.codingchili.core.context.Command;
import com.codingchili.core.context.CommandExecutor;
import com.codingchili.core.logging.ConsoleLogger;
import io.vertx.core.Future;

import static com.codingchili.core.configuration.CoreStrings.getCommand;
import static com.codingchili.core.files.Configurations.launcher;

public class VersionCommand implements Command {

    @Override
    public void execute(Future<Boolean> future, CommandExecutor executor) {
        new ConsoleLogger(getClass()).log("Current version is " + launcher().getVersion());
        future.complete(true);
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
