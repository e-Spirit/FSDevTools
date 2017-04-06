package com.espirit.moddev.serverstart;

/**
 * Interface for a FirstSpirit ServerRunner. Exists to allow instantiation of native or docker versions of the server.
 */
public interface ServerRunner {

    /**
     * Makes sure a FirstSpirit server is running. If another server is already running at the given destination, it will not try to start another
     * one.
     *
     * @return whether a FirstSpirit server is running or not
     */
    boolean start();

    /**
     * @return whether the server is running and can be contacted. Should not care about whether the server was started using this class, or
     * externally.
     */
    boolean isRunning();

    /**
     * Stops a running FirstSpirit server. Even if the FirstSpirit server was not started using this class, an attempt to stop the server is executed.
     * Will not try to stop the server if it cannot be contacted beforehand.
     *
     * @return whether stopping the server was successful
     */
    boolean stop();
}
