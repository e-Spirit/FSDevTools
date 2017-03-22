package com.espirit.moddev.cli.commands;

import com.espirit.moddev.cli.api.command.Command;

@com.github.rvesse.airline.annotations.Command(name = "throwexception")
public class CommandWithException implements Command {
    @Override
    public Object call() throws Exception {
        throw new Exception();
    }
}
