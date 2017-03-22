package com.espirit.moddev.cli.api.event;

/**
 * Interface for handler implementations that can be used for Cli event handling.
 * Is meant to provide an action in case an exception occurs and when the command
 * execution is finished - whether with or without exception.
 */
public interface CliEventHandler extends ExceptionHandler, TerminationHandler {

    /**
     * Place to implement logic that should be executed after every execution
     * a Cli instance performs. Is called whether or not an exception occurred
     * before.
     */
    @Override
    default void afterExecution() {}

    /**
     * Place to implement logic that should be executed when an exception
     * occurs during command execution.
     * @param t the throwable instance to handle
     */
    @Override
    default void handle(Throwable t) {}
}
