package com.espirit.moddev.cli.api.event;

/**
 * Interface for classes, that provide logic for handling an exception.
 */
@FunctionalInterface
public interface ExceptionHandler {

    /**
     * Should implement logic that handles an exception appropriately.
     * @param e the exception to handle
     */
    void handle(Throwable e);

}
