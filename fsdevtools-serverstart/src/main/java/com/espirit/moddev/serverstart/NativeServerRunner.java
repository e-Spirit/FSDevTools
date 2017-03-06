package com.espirit.moddev.serverstart;

import com.google.common.annotations.VisibleForTesting;

import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.ConnectionManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NativeServerRunner implements ServerRunner {

    private static Logger log = LoggerFactory.getLogger(NativeServerRunner.class);

    protected ServerProperties serverProperties;
    /**
     * potentially contains the task that logs the FirstSpirit output (might not be filled in case we did not start the server ourselves)
     */
    protected Optional<FutureTask<Void>> serverTask = Optional.empty();
    protected ExecutorService executor = Executors.newCachedThreadPool();

    public NativeServerRunner(ServerProperties serverProperties) {
        this.serverProperties = serverProperties;
    }

    /**
     * Waits for a given condition, retrying if necessary, blocking the thread in between.
     *
     * @param condition the condition to be checked
     * @param waitTime  the time to wait between queries to `condition`
     * @param triesLeft the number of tries that should be used at max until the condition needs to be true. Should be larger than 0.
     * @return the value of the last call of `condition`.
     */
    @VisibleForTesting
    protected static boolean waitForCondition(Supplier<Boolean> condition, Duration waitTime, int triesLeft) {
        if (triesLeft > 0) {
            if (condition.get()) {
                return true;
            } else {
                try {
                    Thread.sleep(waitTime.toMillis());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); //reset interrupt flag
                }
                return waitForCondition(condition, waitTime, triesLeft - 1);
            }
        } else {
            return false;
        }
    }

    /**
     * Prepares the file system for startup of a FirstSpirit server, e.g. creates `fs-init`, `fs-server.policy` and `fs-license.conf`.
     *
     * @param serverProperties the server properties to be used
     * @return Commandline arguments that should be added to the startup of the server
     * @throws java.io.IOException on file system access problems
     */
    @VisibleForTesting
    protected static List<String> prepareFilesystem(ServerProperties serverProperties) throws IOException {
        List<String> args = new ArrayList<>();
        Path fsServerRoot = serverProperties.getServerRoot();

        Path serverDir = fsServerRoot.resolve("server");
        Path confDir = fsServerRoot.resolve("conf");

        Path initFile = serverDir.resolve("fs-init");
        Path policyFile = confDir.resolve("fs-server.policy");
        Path licenseFile = confDir.resolve("fs-license.conf");
        Path confFile = confDir.resolve("fs-server.conf");

        Files.createDirectories(serverDir);
        Files.createDirectories(confDir);

        if (serverProperties.getVersion().startsWith("4")) {
            args.add("-Dinstall=yes"); // commandline param for FS4
        } else {
            Files.write(initFile, Collections.emptyList());
        }
        Files.copy(NativeServerRunner.class.getResourceAsStream("/" + licenseFile.getFileName().toString()), licenseFile,
                   StandardCopyOption.REPLACE_EXISTING);
        try (BufferedReader reader = confFile.toFile().exists() ?
                                     Files.newBufferedReader(confFile) :
                                     new BufferedReader(
                                         new InputStreamReader(NativeServerRunner.class.getResourceAsStream("/" + confFile.getFileName().toString()))
                                     )) {
            //matches HTTP_PORT=123 with whitespace allowed in between
            Pattern pattern = Pattern.compile("HTTP_PORT\\s*=\\s*\\d+");
            List<String> lines = reader.lines().map(line -> {
                if (pattern.matcher(line).matches()) {
                    return "HTTP_PORT=" + serverProperties.getServerPort();
                } else {
                    return line;
                }
            }).collect(Collectors.toList());
            reader.close();
            Files.write(confFile, lines);
        }
        Files.write(policyFile, Arrays.asList(
            "/* policies for CMS-Server */",
            "",
            "grant {",
            "  permission java.security.AllPermission;",
            "};")); // basic file

        return args;
    }

    /**
     * Prepare system and generate startup parameter list. Performs side-effects on the file system.
     *
     * @param serverProperties the server properties to be used
     * @return startup parameter list
     * @throws java.io.IOException on file system access problems
     */
    @VisibleForTesting
    protected static List<String> prepareStartup(ServerProperties serverProperties) throws IOException {
        Path fsServerRoot = serverProperties.getServerRoot();
        ArrayList<String> args = new ArrayList<>();
        args.add("java");

        if (serverProperties.isServerGcLog()) {
            args.add("-Xloggc:" + serverProperties.getServerRoot() + "/log/fs-gc.log");
        }

        args.addAll(serverProperties.getServerOps());

        if (serverProperties.isServerInstall()) {
            prepareFilesystem(serverProperties);
        }
        args.add("-Dcmsroot=" + fsServerRoot);
        args.add("-Djava.security.policy=" + fsServerRoot.resolve("conf").resolve("fs-server.policy"));
        args.addAll(Arrays.asList("-cp", serverProperties.getFsServerJars().stream().map(File::toString).collect(Collectors.joining(":"))));
        args.add("de.espirit.firstspirit.server.CMSServer");

        return args;
    }

    /**
     * Tests if a connection to the booted FirstSpirit server can be made, or not
     *
     * @param connection a connection to a FirstSpirit instance
     * @return whether the connection was successfully established
     */
    @VisibleForTesting
    protected static boolean testConnection(Connection connection) {
        try {
            connection.connect();
            if (connection.isConnected()) {
                return true;
            }
        } catch (Exception e) {
            return false;
        } finally {
            if (connection != null) {
                try {
                    connection.disconnect();
                    connection.close();
                } catch (IOException ignore) {
                }
            }
        }
        return false;
    }

    protected static Connection getFSConnection(ServerProperties serverProperties) {
        return ConnectionManager
            .getConnection(serverProperties.getServerHost(), serverProperties.getServerPort(), ConnectionManager.HTTP_MODE, "Admin",
                           serverProperties.getServerAdminPw());
    }

    /**
     * Boots a FirstSpirit server, according to configuration
     *
     * @param serverProperties The server properties to be used
     * @param executor         The executor where tasks should be run on. Needs to supply at least 2 threads at the same time.
     * @return a cancellable task that is already running
     * @throws java.io.IOException on file system access problems
     */
    @VisibleForTesting
    protected static FutureTask<Void> bootFirstSpiritServer(ServerProperties serverProperties, ExecutorService executor) throws IOException {
        List<String> commands = Collections.unmodifiableList(prepareStartup(serverProperties));
        log.info("Execute command {}", String.join(" ", commands));

        FutureTask<Void> task = new FutureTask<>(() -> {
            ProcessBuilder builder = new ProcessBuilder(new ArrayList<>(commands)); //copy list because we run on a different thread here
            builder.redirectErrorStream(true);
            final Process process;
            try {
                process = builder.start();
                //start logging on another task to be able to be interrupted to destroy the original process because it hangs sometimes
                FutureTask<Void> logTask = new FutureTask<>(() -> {
                    new BufferedReader(new InputStreamReader(process.getInputStream())).lines()
                        .forEach(line -> log.info("FirstSpirit Server log:" + line));
                    return null; //that one hurts
                });
                executor.submit(logTask);
                try {
                    logTask.get();
                } catch (InterruptedException ie) {
                    process.destroy();  //kill the process if it did not die on its own
                    Thread.currentThread().interrupt();
                }
            } catch (IOException ioe) {
                log.error("Problem reading data from FirstSpirit server process");
            }
            return null; //that one hurts
        });

        executor.submit(task);
        return task;
    }

    /**
     * Prepare command line arguments to stop the server
     *
     * @param serverProperties the server properties to be used
     * @return command line arguments to stop the server
     */
    @VisibleForTesting
    protected static List<String> prepareStop(ServerProperties serverProperties) {
        List<String> args = new ArrayList<>();
        args.add("java");
        args.addAll(Arrays.asList("-cp", serverProperties.getFsServerJars().stream().map(File::toString).collect(Collectors.joining(":"))));
        args.add("-Dhost=" + serverProperties.getServerHost());
        args.add("-Dport=" + serverProperties.getServerPort());
        args.add("-Dmode=HTTP");
        args.add("de.espirit.firstspirit.server.ShutdownServer");
        return args;
    }

    @VisibleForTesting
    protected static boolean shutdownFirstSpiritServer(ServerProperties serverProperties, Optional<FutureTask<Void>> serverTask) {
        ProcessBuilder builder = new ProcessBuilder(prepareStop(serverProperties));
        builder.redirectErrorStream(true);
        Process process;
        try {
            process = builder.start();
            new BufferedReader(new InputStreamReader(process.getInputStream())).lines().forEach(line -> log.info("FirstSpirit shutdown log:" + line));
        } catch (IOException ioe) {
            log.error("Problem reading data from FirstSpirit server process");
            return false;
        }
        //ensure the FS lock file is removed (indicates that the server is still running if the lock file is still there)
        waitForCondition(() -> !serverProperties.getLockFile().exists(), Duration.ofSeconds(1), 15);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        serverTask.ifPresent(x -> x.cancel(true)); //kill running process if it did not die itself
        return !testConnection(getFSConnection(serverProperties));
    }

    @Override
    public boolean start() {
        if (!testConnection(getFSConnection(serverProperties))) {
            log.info("Starting FirstSpirit Server...");
            boolean serverRunning = false;
            try {
                if (!serverTask.isPresent()) {
                    serverTask = Optional.of(bootFirstSpiritServer(serverProperties, executor));
                }

                serverRunning = waitForCondition(() -> {
                                                     log.info("Trying to connect to FirstSpirit server...");
                                                     return testConnection(getFSConnection(serverProperties));
                                                 }, serverProperties.getThreadWait(),
                                                 serverProperties.getConnectionRetryCount()
                                                 + 1); //retry count means we try one more time allover
            } catch (IOException ioe) {
                //nothing to do, server will not be running in this case, normal behaviour following
            }
            if (!serverRunning) {
                log.error("Could not start FirstSpirit server.");
            }
            return serverRunning;

        } else {
            log.info("FirstSpirit Server already running.");
            return true;
        }
    }

    @Override
    public boolean isRunning() {
        return testConnection(getFSConnection(serverProperties));
    }

    @Override
    public boolean stop() {
        return shutdownFirstSpiritServer(serverProperties, serverTask);
    }
}
