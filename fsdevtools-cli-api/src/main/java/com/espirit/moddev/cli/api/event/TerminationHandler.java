package com.espirit.moddev.cli.api.event;

/**
 * Interface for classes, that implement post-termination logic for a Cli.
 */
@FunctionalInterface
public interface TerminationHandler {

    /**
     * Should implement logic that is executed after a successful termination
     * of a Cli.
     */
    void afterExecution();

}
