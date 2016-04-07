package com.espirit.moddev.cli.commands;

import com.espirit.moddev.cli.results.SimpleResult;
import com.github.rvesse.airline.model.GlobalMetadata;

/**
 * Specialization of {@link com.espirit.moddev.cli.results.SimpleResult} that can be used in conjunction with help commands.
 */
public class HelpResult extends SimpleResult<GlobalMetadata<Object>> {

    /**
     * Creates a new instance using the given command result.
     *
     * @param metadata Result produced by the command
     * @see com.espirit.moddev.cli.results.SimpleResult#SimpleResult(Object)
     */
    public HelpResult(GlobalMetadata metadata) {
        super(metadata);
    }

    /**
     * Creates a new error result using the given exception.
     *
     * @param exception Exception produced by the command
     * @see com.espirit.moddev.cli.results.SimpleResult#SimpleResult(Exception)
     */
    public HelpResult(Exception exception) {
        super(exception);
    }
}
