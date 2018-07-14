package com.codingchili.ethereumingest.commands;

import com.codingchili.core.context.*;

import com.codingchili.ethereumingest.views.Form;
import io.vertx.core.Future;

import static com.codingchili.core.configuration.CoreStrings.getCommand;

/**
 * Starting the application with the #{@link #getName()} flag opens
 * the graphical interface.
 */
public class GUICommand implements Command {

    @Override
    public void execute(Future<CommandResult> future, CommandExecutor executor) {
        new Form().start();
        future.complete(CommandResult.CONTINUE);
    }

    @Override
    public String getDescription() {
        return "Starts the application with a GUI.";
    }

    @Override
    public String getName() {
        return getCommand("gui");
    }
}
