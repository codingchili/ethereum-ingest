package com.codingchili.ethereumingest.commands;

import com.codingchili.core.context.Command;
import com.codingchili.core.context.CommandExecutor;
import com.codingchili.ethereumingest.views.Form;
import io.vertx.core.Future;

import static com.codingchili.core.configuration.CoreStrings.getCommand;

public class GUICommand implements Command {

    @Override
    public void execute(Future<Boolean> future, CommandExecutor executor) {
        new Form().start();
        future.complete(false);
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
