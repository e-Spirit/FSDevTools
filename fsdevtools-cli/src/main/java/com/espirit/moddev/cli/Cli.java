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

import com.espirit.moddev.cli.api.CliContext;
import com.espirit.moddev.cli.api.command.Command;
import com.espirit.moddev.cli.api.configuration.Config;
import com.espirit.moddev.cli.api.result.Result;
import com.espirit.moddev.cli.commands.help.HelpCommand;
import com.espirit.moddev.cli.commands.help.DefaultCommand;
import com.espirit.moddev.cli.exception.FsLoggingBridge;
import com.espirit.moddev.cli.exception.SystemExitHandler;
import com.espirit.moddev.cli.reflection.CommandUtils;
import com.espirit.moddev.cli.reflection.GroupUtils;
import com.github.rvesse.airline.builder.CliBuilder;

import de.espirit.common.base.Logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.jar.Attributes;
import java.util.jar.JarFile;


/**
 * Class to represent a command line interface. Is meant to be used from the command line
 * or programmatically.
 * Exceptions during command execution and cli execution can be caught on top level.
 * The cli closes the optional connection before exceptions are rethrown. The main
 * method of this class (which is normally used from command line) uses a handler to
 * call System.exit() with an appropriate error code in case of regular termination or
 * in case of an exception.
 *
 * @author e-Spirit AG
 */
public final class Cli {

    private static final Logger LOGGER = LoggerFactory.getLogger(Cli.class);
    private static final Set<Class<? extends Command>> commandClasses = CommandUtils.scanForCommandClasses();
    private static final Set<Class<?>> groupClasses = GroupUtils.scanForGroupClasses();

    private final Properties buildProperties;
    private final Properties gitProperties;


    /**
     * Instantiates a new Cli.
     */
    public Cli() {
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
    }

    /**
     * The entry point of the cli application. Uses a {@link SystemExitHandler}
     * in order to exit correctly when called from the command line.
     * If you don't want this behaviour, instantiate a cli application programmatically on your own.
     *
     * @param args the input arguments
     */
    public static void main(final String[] args) {

        SystemExitHandler cliEventHandler = new SystemExitHandler();
        try {
            new Cli().execute(args);
            cliEventHandler.afterTermination();
        } catch (Exception e) {
            cliEventHandler.afterExceptionalTermination(e);
        }
    }

    /**
     * Start the cli application.
     *
     * @param args the input arguments
     */
    @SuppressWarnings("squid:S1162")
    public void execute(final String[] args) throws Exception {
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
        try {
            executeCommand(command);
        } catch (Exception e) {
            throw e;
        } finally {
            stopwatch.stop();
            logExecutionTime(stopwatch);
        }
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
    @SuppressWarnings("squid:S1162")
    public void executeCommand(Command<Result> command) throws Exception {
        LOGGER.info("Executing " + command.getClass().getSimpleName());
        CliContext context = null;
        try {
            context = getCliContextOrNull(command);
            Result result = command.call();
            logResult(result);
        } catch (ClassCastException e) {
            LOGGER.trace("Cannot perform a cast - most likely because the command's call method returns Object as a result, instead of Result.", e);
        } catch (Exception e) {
            LOGGER.trace("Exception occurred during context initialization or command execution", e);
            throw e;
        } finally {
            closeContext(context);
        }
    }

    static void closeContext(CliContext context) {
        if(context != null) {
            try {
                context.close();
            } catch (Exception e) {
                LOGGER.error("Closing context caused an exception!", e);
            }
        }
    }

    @SuppressWarnings("squid:S1162")
    private static void logResult(Result result) throws Exception{
        if (result != null) {
            result.log();
            if(result.isError()){
                throw result.getError();
            }
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

    private static void addHelpCommand(CliBuilder<Command> builder) {
        builder.withCommand(HelpCommand.class);
        builder.withDefaultCommand(DefaultCommand.class);
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
     * A getter for command classes that can be found on the classpath. The classes are loaded at class-load
     * time only once.
     *
     * @return a reference to the actual list of loaded commands
     */
    public static Set<Class<? extends Command>> getCommandClasses() {
        return Collections.unmodifiableSet(commandClasses);
    }

    /**
     * Look up all class that define command classes in the classpath.
     *
     * @return {@link java.util.Set} of all class that define command classes in the classpath
     */
    public static Set<Class<?>> getGroupClasses() {
        return Collections.unmodifiableSet(groupClasses);
    }
}
