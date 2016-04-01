/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2016 e-Spirit AG
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * *********************************************************************
 *
 */

package com.espirit.moddev.cli.exception;

import com.espirit.moddev.cli.Cli;
import com.espirit.moddev.cli.CliListener;
import com.espirit.moddev.cli.configuration.CliConstants;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * The type Exception handler.
 *
 * @author e-Spirit AG
 */
public final class ExceptionHandler implements Thread.UncaughtExceptionHandler, CliListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(Cli.class);
    private final List<String> arguments;
    private final Cli app;

    /**
     * Instantiates a new Exception handler.
     *
     * @param app  the commandline app
     * @param args the commandline arguments
     */
    public ExceptionHandler(Cli app, String[] args) {
        this.app = app;
        if (args != null) {
            arguments = Arrays.asList(args);
        } else {
            arguments = Collections.emptyList();
        }
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        logException(e);
        //If we are called from a thread then we need to inform about a abnormal exit
        app.fireErrorOccurredEvent(new CliErrorEvent(this, e));
    }

    @Override
    public void errorOccurred(CliErrorEvent e) {
        if (sourceOfEventNotThisInstance(e)) {
            logException(e.getError());
        }
    }

    private boolean sourceOfEventNotThisInstance(CliErrorEvent e) {
        return !this.equals(e.getSource());
    }

    /**
     * Log exception.
     *
     * @param error the error
     */
    private void logException(Throwable error) {
        if (argumentsContains("-e")) {
            LOGGER.error(error.toString(), error);
        } else {
            final Throwable rootCause = ExceptionUtils.getRootCause(error);
            if (rootCause != null) {
                LOGGER.error("{}\nError reason: {}", error.getMessage(), rootCause);
            } else {
                LOGGER.error("{}", error.getMessage());
            }
            if (!arguments.isEmpty()) {
                LOGGER.info("See '" + CliConstants.FS_CLI + " help {}' for more information on a specific command.",
                            arguments.stream()
                                .filter(s -> ("import".equals(s) || "export".equals(s) || s.indexOf("store") != -1) && !"help".equals(s))
                                .reduce("", (s1, s2) -> s1 + " " + s2));
            }
        }
    }

    /**
     * Checks if arguments contains a certain element.
     *
     * @param key the key
     * @return the boolean
     */
    public boolean argumentsContains(String key) {
        return arguments.contains(key);
    }
}
