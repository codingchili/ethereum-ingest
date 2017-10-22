package com.codingchili.ethereumingest.commands;

import com.codingchili.core.context.CommandExecutor;
import com.codingchili.core.context.DefaultCommandExecutor;

public class CommandLine {

    public static CommandExecutor get() {
        CommandExecutor executor = new DefaultCommandExecutor();
        executor.add(new HelpCommand());
        executor.add(new GUICommand());
        executor.add(new ImportCommand());
        executor.add(new VersionCommand());
        return executor;
    }
}
