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
import com.espirit.moddev.cli.api.event.CliErrorEvent;
import com.espirit.moddev.cli.api.event.CliListener;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * Helper class to handle exceptions.
 *
 * @author e-Spirit AG
 */
public final class ExceptionHandler implements Thread.UncaughtExceptionHandler, CliListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(Cli.class);
    private final List<String> arguments;
    private final Cli app;
    private final String appName;

    /**
     * Instantiates a new instance.
     * @param app the commandline app
     * @param appName
     * @param args the commandline arguments
     */
    public ExceptionHandler(final Cli app, final String appName, final String[] args) {
        this.app = app;
        this.appName = appName;
        if (args != null) {
            arguments = Arrays.asList(args);
        } else {
            arguments = Collections.emptyList();
        }
    }

    @Override
    public void uncaughtException(final Thread t, final Throwable e) {
        logException(e);
        //If we are called from a thread then we need to inform about a abnormal exit
        app.fireErrorOccurredEvent(new CliErrorEvent(this, e));
    }

    @Override
    public void errorOccurred(final CliErrorEvent e) {
        if (sourceOfEventNotThisInstance(e)) {
            logException(e.getError());
        }
    }

    private boolean sourceOfEventNotThisInstance(final CliErrorEvent e) {
        return !this.equals(e.getSource());
    }

    /**
     * Log the given {@link java.lang.Throwable}.
     * If the -e switch is set, the full stack trace will be logged. See {@link com.espirit.moddev.cli.configuration.GlobalConfig#isError()}.
     *
     * @param error the error to be logged
     */
    private void logException(final Throwable error) {
        if (argumentsContains("-e")) {
            LOGGER.error("An error occurred!", error);
        } else {
            final Throwable rootCause = ExceptionUtils.getRootCause(error);
            if (rootCause != null) {
                LOGGER.error("{}\nError reason: {}", error.getMessage(), rootCause);
            } else {
                LOGGER.error("{}", error.getMessage());
            }
            if (!arguments.isEmpty()) {
                String commandString = arguments.stream()
                        .filter(s -> ("import".equals(s) || "export".equals(s) || s.indexOf("store") != -1) && !"help".equals(s))
                        .reduce("", (s1, s2) -> s1 + " " + s2);
                LOGGER.info("See '{} help {}' for more information on a specific command.", appName, commandString);
            }
        }
    }

    /**
     * Checks if arguments contains a certain element.
     *
     * @param key the argument to be checked
     * @return the true if the arguments contain the given element
     */
    public boolean argumentsContains(final String key) {
        return arguments.contains(key);
    }
}
