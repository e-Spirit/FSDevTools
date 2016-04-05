package com.espirit.moddev.cli.results;

import com.espirit.moddev.cli.configuration.GlobalConfig;

/**
 * Specialization of {@link SimpleResult} that can be used in conjunction with test project commands.
 *
 * @author e-Spirit AG
 */
public class TestResult extends SimpleResult {

    private final GlobalConfig config;

    /**
     * Creates a new instance using the given config and exception.
     *
     * @param config Config that was used for command execution
     * @param e      Optional exception that was produced by the command
     * @throws IllegalArgumentException if config is null
     */
    public TestResult(GlobalConfig config, Exception e) {
        super(e);
        validateConfig(config);
        this.config = config;
    }

    /**
     * Creates a new instance using the given config and exception.
     *
     * @param config Config that was used for config execution
     * @throws IllegalArgumentException if config is null
     */
    public TestResult(GlobalConfig config) {
        super();
        validateConfig(config);
        this.config = config;
    }

    @Override
    public void log() {
        LOGGER.info("#########");
        if (isError()) {
            LOGGER.info("Test was not successful");
            LOGGER.error(exception.getMessage());
            if (config.isError()) {
                LOGGER.error("", exception);
            }
        } else {
            LOGGER.info("Test was successful");
        }
        LOGGER.info("#########");
        LOGGER.info("User: " + config.getUser());
        LOGGER.info("Host: " + config.getHost());
        LOGGER.info("Port: " + config.getPort());
        LOGGER.info("Connection Mode: " + config.getConnectionMode());
        LOGGER.info("Project: " + config.getProject());
    }


    private void validateConfig(GlobalConfig command) {
        if (command == null) {
            throw new IllegalArgumentException("Command shouldn't be null");
        }
    }
}
