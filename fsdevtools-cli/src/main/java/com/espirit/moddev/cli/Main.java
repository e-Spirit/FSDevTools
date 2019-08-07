package com.espirit.moddev.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(final String[] args) {
        try {
            Cli.main(args);
        } catch (NoClassDefFoundError e) {
            if(e.getMessage().contains("de/espirit/common/base/Logger")) {
                LOGGER.error("Couldn't find FirstSpirit classes - have you placed your FirstSpirit api jar (fs-isolated-runtime) into the fs-cli lib folder?");
                LOGGER.debug("", e);
                System.exit(1);
            }
            // rethrow all others
            throw e;
        }
    }
}
