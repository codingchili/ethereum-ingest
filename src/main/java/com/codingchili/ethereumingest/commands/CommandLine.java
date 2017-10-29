package com.codingchili.ethereumingest.commands;

import com.codingchili.core.context.CommandExecutor;
import com.codingchili.core.context.DefaultCommandExecutor;

/**
 * defines a set of commands available from the command line.
 */
public class CommandLine {

    /**
     * @return a commandexecutor prepared with the default commands.
     * help, version, import and gui.
     */
    public static CommandExecutor get() {
        CommandExecutor executor = new DefaultCommandExecutor();
        executor.add(new HelpCommand());
        executor.add(new GUICommand());
        executor.add(new ImportCommand());
        executor.add(new VersionCommand());
        return executor;
    }
}
