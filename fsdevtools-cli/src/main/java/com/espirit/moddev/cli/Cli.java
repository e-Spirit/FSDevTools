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

import com.espirit.moddev.cli.api.CliContext;
import com.espirit.moddev.cli.api.command.Command;
import com.espirit.moddev.cli.api.configuration.Config;
import com.espirit.moddev.cli.api.event.CliEventHandler;
import com.espirit.moddev.cli.api.event.ExceptionHandler;
import com.espirit.moddev.cli.api.result.Result;
import com.espirit.moddev.cli.commands.HelpCommand;
import com.espirit.moddev.cli.exception.CliException;
import com.espirit.moddev.cli.exception.FsLoggingBridge;
import com.espirit.moddev.cli.exception.SystemExitHandler;
import com.espirit.moddev.cli.reflection.CommandUtils;
import com.espirit.moddev.cli.reflection.GroupUtils;
import com.github.rvesse.airline.builder.CliBuilder;
import com.google.common.base.Stopwatch;
import de.espirit.common.base.Logging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.jar.Attributes;
import java.util.jar.JarFile;


/**
 * Class to represent a command line interface. Is meant to be used from the command line
 * or programmatically.
 * Exceptions during command execution and cli execution termination can be processed by handlers
 * that can be registered. Exception handlers can be registered with {@link Cli#addExceptionHandler(ExceptionHandler)},
 * while a {@link CliEventHandler} can be passed to the constructor. The latter one is called
 * in case of an exception during command execution, but after the registered exception handlers and
 * after the command execution has finished - whether with or without exception.
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

    private final List<ExceptionHandler> handlers = new LinkedList<>();
    private final Properties buildProperties;
    private final Properties gitProperties;

    private final CliEventHandler cliEventHandler;

    /**
     * Instantiates a new Cli. Adds an empty implementation of the {@link CliEventHandler} interface.
     */
    public Cli() {
        this(new CliEventHandler() {});
    }

    /**
     * Instantiates a new Cli. Adds an empty implementation of the {@link CliEventHandler} interface
     * if the passed handler is null.
     * @param terminationHandler the event handler to use for this Cli instance
     */
    public Cli(CliEventHandler terminationHandler) {
        buildProperties = new Properties();
        gitProperties = new Properties();
        try (InputStream resourceAsStream = ClassLoader.getSystemClassLoader().getResourceAsStream("CliBuild.properties")) {
            buildProperties.load(resourceAsStream);
        } catch (IOException e) {
            LOGGER.error("Failed to load BuildProperties", e);
        }
        try (InputStream resourceAsStream = ClassLoader.getSystemClassLoader().getResourceAsStream("CliGit.properties")) {
            gitProperties.load(resourceAsStream);
        } catch (IOException e) {
            LOGGER.error("Failed to load GitProperties", e);
        }
        cliEventHandler = terminationHandler == null ? new CliEventHandler() {} : terminationHandler;
    }

    /**
     * The entry point of the cli application. This adds a {@link SystemExitHandler} to the cli application
     * in order to exit correctly when called from the command line.
     * If you don't want this behaviour, instantiate a cli application programmatically on your own.
     *
     * @param args the input arguments
     */
    public static void main(final String[] args) {
        new Cli(new SystemExitHandler()).execute(args);
    }

    /**
     * Start the cli application.
     *
     * @param args the input arguments
     */
    public void execute(final String[] args) {
        setLoggingSystemProperties();

        try {
            logVersionsAndGitHash();
        } catch (IOException e) {
            LOGGER.error("Error with version and/or git information, aborting operation...", e);
            return;
        }

        final CliBuilder<Command> builder = getDefaultCliBuilder();
        final Command command = parseCommandLine(args, builder);
        Stopwatch stopwatch = new Stopwatch();
        stopwatch.start();
        executeCommand(command);
        stopwatch.stop();
        logExecutionTime(stopwatch);
        cliEventHandler.afterExecution();
    }

    private static void logExecutionTime(final Stopwatch stopwatch) {
        double milliseconds = stopwatch.elapsedTime(TimeUnit.MILLISECONDS);
        final String executionTime = String.format("Execution time: %ss", milliseconds / CliConstants.ONE_SECOND_IN_MILLIS.valueAsInt());
        LOGGER.info(executionTime);
    }

    private void logVersionsAndGitHash() throws IOException {
        final Object[] argsVersion =
            {CliConstants.FS_CLI, buildProperties.getProperty("fs.cli.build.version"), gitProperties.getProperty("git.hash")};
        LOGGER.info("{} version {} / git hash {}", argsVersion);
        LOGGER.info("Build for FirstSpirit version {}", new Object[]{buildProperties.getProperty("fs.cli.fs.version")});
        String jarFilePath = System.getenv("jarfile") != null ? System.getenv("jarfile") : System.getenv("JARFILE");
        if (jarFilePath != null) {
            String fsAccessPath = normalizePath(jarFilePath);
            try (JarFile jar = new JarFile(fsAccessPath)) {
                final Attributes mainAttributes = jar.getManifest().getMainAttributes();
                final String fsVersionJar = mainAttributes.getValue("FirstSpirit-Version");
                final String fsImplVersionJar = mainAttributes.getValue("Implementation-Version");
                final Object[] argsFsVersion = {fsVersionJar, fsImplVersionJar, fsAccessPath};
                LOGGER.info("Using FirstSpirit Access API version {}.{} (see {})", argsFsVersion);
            }
        }
    }

    private static String normalizePath(String filePath) {
        String jarFilePath = filePath.replace("\"", "");
        final String libPath = new File(jarFilePath).getParentFile().getAbsolutePath();
        jarFilePath = libPath + "/fs-access.jar";
        jarFilePath = jarFilePath.replace("\\", "/");
        return jarFilePath;
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

    private static void setLoggingSystemProperties() {
        // Set logging directory for Log4J configuration
        final String logDir = System.getProperty(CliConstants.USER_HOME.value()) + CliConstants.FS_CLI_DIR;
        System.setProperty(CliConstants.FS_CLI_LOG_DIR.value(), logDir);

        // Pipe through Log4J debug switch
        if (System.getenv(CliConstants.LOG4J_DEBUG.value()) != null) {
            System.setProperty(CliConstants.LOG4J_DEBUG.value(), System.getenv(CliConstants.LOG4J_DEBUG.value()));
        }

        // Enable full FS logging
        Logging.init(new FsLoggingBridge());
        Logging.logDebug("FS-Logging initialized!", Cli.class);
    }

    /**
     * Executes an already instantiated command. First, the command
     * is used as a configuration object for obtaining a FirstSpirit connection.
     * Second, the command is executed. Afterwards, the context is closed.
     *
     * Exceptions occurring during context creation or command execution
     * are propagated to registered handlers.
     *
     * @param command the command instance to execute
     */
    public void executeCommand(Command<Result> command) {
        LOGGER.info("Executing " + command.getClass().getSimpleName());
        CliContext context = null;
        try {
            context = getCliContextOrNull(command);
            Result result = command.call();
            logResult(result);
        } catch (CliException e) {
            LOGGER.trace("CliException occurred during context initialization or command execution", e);
            handleCliException(e);
        } catch (Exception e) {
            notifyExceptionHandlers(e);
        } finally {
            closeContext(context);
        }
    }

    private static void closeContext(CliContext context) {
        if(context != null) {
            try {
                context.close();
            } catch (Exception e) {
                LOGGER.error("Closing context caused an exception!", e);
            }
        }
    }

    private void handleCliException(CliException e) {
        Throwable cause = e.getCause();
        if(cause != null) {
            notifyExceptionHandlers(cause);
        }
    }

    private static void logResult(Result result) {
        if (result != null) {
            result.log();
        } else {
            LOGGER.warn("Command returned a null result, which should be avoided");
        }
    }

    private static CliContext getCliContextOrNull(Command<Result> command) {
        CliContext context = null;
        if (command instanceof Config) {
            Config commandAsConfig = (Config) command;
            if (commandAsConfig.needsContext()) {
                context = new CliContextImpl(commandAsConfig);
                commandAsConfig.setContext(context);
            }
        }
        return context;
    }

    /**
     * Notifies all registered exception handlers. In case of an exception in one
     * of the handler implementations, the cliEventHandler is notified nonetheless.
     * @param t the occurred exception
     */
    private void notifyExceptionHandlers(Throwable t) {
        try {
            for(ExceptionHandler handler : handlers) {
                handler.handle(t);
            }
        } catch (Exception e) {
            LOGGER.warn("Exception in exception handler implementation occurred!", e);
        } finally {
            cliEventHandler.handle(t);
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
     * Add an exception handler to this Cli. This handler is invoked in case of an exception.
     *
     * @param handler the handler to add
     * @return true if the handler was added (see {@link java.util.List#add(Object)})
     */
    public boolean addExceptionHandler(ExceptionHandler handler) {
        return handlers.add(handler);
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
}
