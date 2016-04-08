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

package com.espirit.moddev.cli;

import com.google.common.base.Stopwatch;

import com.espirit.moddev.cli.api.command.Command;
import com.espirit.moddev.cli.api.configuration.Config;
import com.espirit.moddev.cli.api.event.CliListener;
import com.espirit.moddev.cli.api.result.Result;
import com.espirit.moddev.cli.commands.HelpCommand;
import com.espirit.moddev.cli.api.event.CliErrorEvent;
import com.espirit.moddev.cli.exception.ExceptionHandler;
import com.espirit.moddev.cli.exception.SystemExitListener;
import com.espirit.moddev.cli.reflection.CommandUtils;
import com.espirit.moddev.cli.reflection.GroupUtils;
import com.espirit.moddev.cli.reflection.ReflectionUtils;
import com.github.rvesse.airline.builder.CliBuilder;
import com.github.rvesse.airline.builder.GroupBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


/**
 * Start point of the cli application.
 *
 * @author e-Spirit AG
 */
public final class Cli {

    /**
     * Default package for classes that define cli command groups.
     */
    public static final String DEFAULT_GROUP_PACKAGE_NAME = "com.espirit.moddev.cli.groups";

    /**
     * Default package for classes that define cli commands.
     */
    public static final String DEFAULT_COMMAND_PACKAGE_NAME = "com.espirit.moddev.cli.commands";

    private static final Logger LOGGER = LoggerFactory.getLogger(Cli.class);
    private static Set<Class<? extends Command>> commandClasses = CommandUtils.scanForCommandClasses(DEFAULT_COMMAND_PACKAGE_NAME);
    private static Set<Class<?>> groupClasses = GroupUtils.scanForGroupClasses(DEFAULT_GROUP_PACKAGE_NAME);
    private final List<CliListener> listeners = new LinkedList<>();

    /**
     * The entry point of the cli application.
     *
     * @param args the input arguments
     */
    public static void main(final String[] args) {
        new Cli().execute(args);
    }

    /**
     * A getter for command classes from the package specified by {@link #DEFAULT_COMMAND_PACKAGE_NAME} only. The classes are loaded at class-load
     * time only once.
     *
     * @return a reference to the actual list of loaded commands
     */
    public static Set<Class<? extends Command>> getCommandClasses() {
        return Collections.unmodifiableSet(commandClasses);
    }

    /**
     * Look up all class that define command classes in the package specified by {@link #DEFAULT_GROUP_PACKAGE_NAME}.
     *
     * @return {@link java.util.Set} of all class that define command classes in the package specified by {@link #DEFAULT_GROUP_PACKAGE_NAME}
     */
    public static Set<Class<?>> getGroupClasses() {
        return Collections.unmodifiableSet(groupClasses);
    }

    /**
     * Start the cli application.
     *
     * @param args the input arguments
     */
    public void execute(final String[] args) {
        migrateEnvironmentVariableToSystemProperties();

        final ExceptionHandler exceptionHandler = new ExceptionHandler(this, CliConstants.FS_CLI.value(), args);

        //Order of listeners registered is important!
        listeners.add(exceptionHandler);
        //Make sure that in case of error there will be a System-exit(1)
        listeners.add(new SystemExitListener());

        try {
            final CliBuilder<Command> builder = getDefaultCliBuilder();
            final Command command = parseCommandLine(args, builder);
            Stopwatch stopwatch = new Stopwatch();
            stopwatch.start();
            executeCommand(exceptionHandler, command);
            stopwatch.stop();
            double milliseconds = stopwatch.elapsedTime(TimeUnit.MILLISECONDS);
            LOGGER.info("Time: " + (milliseconds / CliConstants.ONE_SECOND_IN_MILLIS.valueAsInt()) + "s");
        } catch (Exception e) { //NOSONAR
            fireErrorOccurredEvent(new CliErrorEvent(new Cli(), e));
        }
    }

    /**
     * Get the default {@link com.github.rvesse.airline.builder.CliBuilder} for this cli application. The {@link
     * com.github.rvesse.airline.builder.CliBuilder} will be initialized with all available commands and groups.
     *
     * @return the default {@link com.github.rvesse.airline.builder.CliBuilder} for this cli application.
     */
    public static CliBuilder<Command> getDefaultCliBuilder() {
        final CliBuilder<Command> builder = com.github.rvesse.airline.Cli.<Command>builder(CliConstants.FS_CLI.value());
        initializeAllCommandsAndGroups(builder);
        return builder;
    }

    private static void initializeAllCommandsAndGroups(CliBuilder<Command> builder) {
        addHelpCommand(builder);
        CliBuilderHelper.buildCommandGroups(builder);
    }

    private static void migrateEnvironmentVariableToSystemProperties() {
        final String logDir = System.getProperty(CliConstants.USER_HOME.value()) + CliConstants.FS_CLI_DIR;
        System.setProperty(CliConstants.FS_CLI_LOG_DIR.value(), logDir);

        if (System.getenv(CliConstants.LOG4J_DEBUG.value()) != null) {
            System.setProperty(CliConstants.LOG4J_DEBUG.value(), System.getenv(CliConstants.LOG4J_DEBUG.value()));
        }
    }

    private void executeCommand(ExceptionHandler exceptionHandler, Command<Result> command) {
        LOGGER.info("Executing command");
        try {
            if (command instanceof Config) {
                Config commandAsConfig = (Config) command;
                if (commandAsConfig.needsContext()) {
                    commandAsConfig.setContext(new CliContextImpl(commandAsConfig));
                }
            }
            Result result = command.call();
            if (result != null) {
                result.log();
            } else {
                LOGGER.warn("Command returned a null result, which should be avoided");
            }
        } catch (Exception e) {
            exceptionHandler.errorOccurred(new CliErrorEvent(this, e));
        }
    }

    private static void addHelpCommand(CliBuilder<Command> builder) {
        builder.withCommand(HelpCommand.class);
        builder.withDefaultCommand(HelpCommand.class);
    }

    /**
     * Parses an array of arguments with the default cli builder.
     *
     * @param args the arguments that should be parsed as a command line input
     * @return a generic command
     */
    public static Command<Result> parseCommandLine(String[] args) {
        return parseCommandLine(args, getDefaultCliBuilder());
    }

    /**
     * Parses an array of arguments with a given cli builder.
     *
     * @param args    the arguments that should be parsed as a command line input
     * @param builder the builder that is used to retrieve a parser for parsing
     * @return the parsed command
     */
    public static Command parseCommandLine(String[] args, CliBuilder<Command> builder) {
        final com.github.rvesse.airline.Cli<Command> cliParser = builder.build();
        return cliParser.parse(args);
    }


    /**
     * Notify all registered listeners of the error event.
     *
     * @param e the error event
     */
    public void fireErrorOccurredEvent(CliErrorEvent e) {
        for (CliListener listener : listeners) {
            listener.errorOccurred(e);
        }
    }

    /**
     * Add a listener to this Cli.
     *
     * @param listener the listener to add
     * @return true if the listener was added (see {@link java.util.List#add(Object)})
     */
    public boolean addListener(CliListener listener) {
        return listeners.add(listener);
    }
}
