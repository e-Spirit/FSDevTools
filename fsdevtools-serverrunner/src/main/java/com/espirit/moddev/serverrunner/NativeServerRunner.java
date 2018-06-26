package com.espirit.moddev.serverrunner;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import de.espirit.firstspirit.access.AdminService;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.ServicesBroker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.function.Supplier;

import static java.util.stream.Collectors.joining;

/**
 * This class is an implementation of a ServerRunner. It provides the functionality to control a FirstSpirit Server
 *
 * @author e-Spirit AG
 */
@Slf4j
public class NativeServerRunner implements ServerRunner {

    private static final String PROBLEM_READING = "Problem reading data from FirstSpirit server process";
    private static final Logger LOGGER = LoggerFactory.getLogger(NativeServerRunner.class);

    protected ServerProperties serverProperties;
    /**
     * potentially contains the task that logs the FirstSpirit output (might not be filled in case we did not start the server ourselves)
     */
    protected Optional<FutureTask<Void>> serverTask = Optional.empty();
    protected ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * Creates and initializes the ServerRunner with given ServerProperties.
     *
     * @param serverProperties needed to determine, which server to start/stop. May not be null!
     * @throws NullPointerException
     */
    public NativeServerRunner(final ServerProperties serverProperties) {
        this.serverProperties = Objects.requireNonNull(serverProperties);
    }

    /**
     * Waits for a given condition, retrying if necessary, blocking the thread in between.
     *
     * @param condition the condition to be checked
     * @param waitTime the time to wait between queries to `condition`
     * @param triesLeft the number of tries that should be used at max until the condition needs to be true. Should be larger than 0.
     * @return the value of the last call of `condition`.
     */
    static boolean waitForCondition(final Supplier<Boolean> condition, final Duration waitTime, final int triesLeft) {
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
    static List<String> prepareFilesystem(final ServerProperties serverProperties) throws IOException {
        final List<String> args = new ArrayList<>();
        final Path fsServerRoot = serverProperties.getServerRoot();

        final Path serverDir = fsServerRoot.resolve("server");
        final Path confDir = fsServerRoot.resolve("conf");

        final Path initFile = serverDir.resolve("fs-init");
        final Path policyFile = confDir.resolve("fs-server.policy");
        final Path confFile = confDir.resolve("fs-server.conf");

        Files.createDirectories(serverDir);
        Files.createDirectories(confDir);

        Files.write(initFile, Collections.emptyList());

        if (serverProperties.getLicenseFileSupplier().get().isPresent()) {
            Files
                .copy(serverProperties.getLicenseFileSupplier().get().get(), confDir.resolve("fs-license.conf"), StandardCopyOption.REPLACE_EXISTING);
        }

        //either update an existing conf file, or if none exists, use the one from the class path
        try (BufferedReader reader = confFile.toFile().exists()
            ? Files.newBufferedReader(confFile)
            : new BufferedReader(
                new InputStreamReader(NativeServerRunner.class.getResourceAsStream("/" + confFile.getFileName().toString()),
                    StandardCharsets.UTF_8)
            )) {
            final Properties properties = new Properties();
            properties.load(reader);
            properties.setProperty("HTTP_PORT", String.valueOf(serverProperties.getServerPort()));

            try (FileWriter fileWriter = new FileWriter(confFile.toFile())) {
                properties.store(fileWriter, "");
            }
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
    static List<String> prepareStartup(final ServerProperties serverProperties) throws IOException {
        final Path fsServerRoot = serverProperties.getServerRoot();
        final ArrayList<String> args = new ArrayList<>();
        args.add("java");

        if (serverProperties.isServerGcLog()) {
            args.add("-Xloggc:" + serverProperties.getServerRoot().resolve("log").resolve("fs-gc.log"));
        }

        args.addAll(serverProperties.getServerOps());

        if (serverProperties.isServerInstall()) {
            prepareFilesystem(serverProperties);
        }
        args.add("-Dcmsroot=" + fsServerRoot);
        args.add("-Djava.security.policy=" + fsServerRoot.resolve("conf").resolve("fs-server.policy"));
        args.addAll(Arrays.asList("-cp", serverProperties.getFirstSpiritJars().stream().map(File::toString).collect(joining(String.valueOf(java.io.File.pathSeparatorChar)))));

        URLClassLoader loader = new URLClassLoader(toUrlArray(serverProperties.getFirstSpiritJars()));
        Optional<Class> startupClass = ServerProperties.tryFindStartUpClass(loader);

        if (startupClass.isPresent()) {
            args.add(startupClass.get().getCanonicalName());
        } else {
            throw new IllegalStateException("No startup class for a FirstSpirit server could be found on the class.");
        }

        return args;
    }

    private static URL[] toUrlArray(List<File> files) {
        List<URL> urls = new LinkedList<>();
        for (File file : files) {
            // We do not want to have null elements in the array, so first we store all successfully transformed urls in a list.
            try {
                urls.add(file.toURI().toURL());
            } catch (MalformedURLException e) {
                LOGGER.error("Location of a jar file could not be transformed to an URL.", e);
            }
        }
        return urls.toArray(new URL[0]);
    }

    /**
     * Tests if a connection to the booted FirstSpirit server can be made, or not
     *
     * @param serverProperties the server properties to be used
     * @return whether the connection was successfully established
     */
    static boolean testConnection(final ServerProperties serverProperties) {
        //currently HTTP_MODE is the only mode available - just want to point out clearly that this will only work for that mode.
        if (serverProperties.getMode() == ServerProperties.ConnectionMode.HTTP_MODE) {
            return HttpConnectionTester.testConnection(serverProperties.getServerUrl());
        } else {
            return false;
        }
    }

    /**
     * Boots a FirstSpirit server, according to configuration
     *
     * @param serverProperties The server properties to be used
     * @param executor The executor where tasks should be run on. Needs to supply at least 2 threads at the same time.
     * @return a cancellable task that is already running
     * @throws java.io.IOException on file system access problems
     */
    @SuppressWarnings({"squid:S1141", "squid:S1188"}) //nested try and too long lambda
    static FutureTask<Void> startFirstSpiritServer(final ServerProperties serverProperties, final ExecutorService executor) throws IOException {
        final List<String> commands = Collections.unmodifiableList(new ArrayList<>(prepareStartup(serverProperties)));
        if (log.isInfoEnabled()) {
            log.info("Execute command " + String.join(" ", commands));
        }

        //start FirstSpirit async
        /* Construct a cancellable logging task. It will be cancelled in `stopFirstSpiritServer`.
           The inner logTask is necessary since the implicit `readLine()` on the `BufferedReader` has a blocking API that cannot be interrupted. This
           task is stopped by destroying the process outputting data, which implicitly closes the input stream that is being blocked on. You can view
           cancellableLogTask as an entity that does the very same job as logTask with the added functionality of gracefully shutting down on server
           stop.
         */
        final FutureTask<Void> cancellableLogTask = new FutureTask<>(() -> {
            final ProcessBuilder builder = new ProcessBuilder(commands);
            builder.redirectErrorStream(true);
            final Process process;
            try {
                process = builder.start();
                //start logging on another task to be able to be interrupted to destroy the original process because it hangs sometimes
                final FutureTask<Void> logTask = new FutureTask<>(() -> {
                    new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)).lines()
                        .forEach(line -> log.info("FirstSpirit Server log:" + line));
                    return null; //that one hurts
                });
                executor.submit(logTask);
                try {
                    logTask.get();
                } catch (final InterruptedException ie) {
                    process.destroy();  //kill the process if it did not die on its own
                    Thread.currentThread().interrupt();
                }
            } catch (final IOException ioe) {
                log.error(PROBLEM_READING, ioe);
            }
            return null; //that one hurts
        });

        executor.submit(cancellableLogTask);
        return cancellableLogTask;
    }

    private static boolean isServerLocal(final ServerProperties serverProperties) {
        if ("localhost".equals(serverProperties.getServerHost())) {
            return true;
        }

        return getAllLocalAddresses().stream().anyMatch(
            (localAddress) -> serverProperties.getServerHost().equals(localAddress.getHostAddress())
            || serverProperties.getServerHost().equals(localAddress.getHostName()));
    }

    private static List<InetAddress> getAllLocalAddresses() {
        final List<InetAddress> addresses = new ArrayList<>();
        try {
            final Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                final NetworkInterface currentInterface = networkInterfaces.nextElement();
                if (currentInterface.isUp()) { //interface must be up to get addresses
                    Enumeration<InetAddress> interfaceAddresses = currentInterface.getInetAddresses();
                    getLocalAddressesFromInterface(interfaceAddresses, addresses);
                }
            }
            return addresses;
        } catch (final SocketException ex) {
            log.debug("Error while working with network interfaces " + ex);
        }
        return addresses;
    }

    private static void getLocalAddressesFromInterface(final Enumeration<InetAddress> interfaceAddresses, final List<InetAddress> addresses) {
        while (interfaceAddresses.hasMoreElements()) {
            final InetAddress addr = interfaceAddresses.nextElement();
            addresses.add(addr);
        }
    }

    @Override
    public boolean start() {
        if (!testConnection(serverProperties)) {
            log.info("Starting FirstSpirit Server...");
            boolean serverRunning = false;
            try {
                if (!serverTask.isPresent()) {
                    serverTask = Optional.of(startFirstSpiritServer(serverProperties, executor));
                }

                serverRunning = waitForCondition(() -> {
                    log.info("Trying to connect to FirstSpirit server...");
                    return testConnection(serverProperties);
                }, serverProperties.getRetryWait(),
                    serverProperties.getRetryCount()
                    + 1); //retry count means we try one more time allover
            } catch (final IOException ioe) {
                //nothing to do, server will not be running in this case, normal behaviour following
                log.error(PROBLEM_READING, ioe);
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
        return testConnection(serverProperties);
    }

    @Override
    public boolean stop() {
        log.info("Stopping FirstSpirit Server...");
        Optional<Connection> optionalConnection = serverProperties.tryOpenAdminConnection();
        if (optionalConnection.isPresent()) {
            stopFirstSpiritServerAndDisconnect(optionalConnection.get());
            ensureFsLockFileIsRemoved(serverProperties);
            killRunningProcess();
            return !testConnection(serverProperties);
        } else {
            throw new IllegalStateException("Stopping the FirstSpirit server failed, due to a connection failure.");
        }
    }

    private void stopFirstSpiritServerAndDisconnect(final Connection connection) {
        ServicesBroker servicesBroker = connection.getBroker().requireSpecialist(ServicesBroker.TYPE);
        AdminService adminService = servicesBroker.getService(AdminService.class);
        adminService.stopServer();
        try {
            connection.disconnect();
        } catch (IOException e) {
            LOGGER.error("Error while closing FirstSpirit Connection", e);
        }
    }

    private void ensureFsLockFileIsRemoved(ServerProperties serverProperties) {
        // indicates that the server is still running if the lock file is still thereueck.de

        if (isServerLocal(serverProperties)) {
            waitForCondition(() -> !serverProperties.getLockFile().exists(), serverProperties.getRetryWait(), serverProperties.getRetryCount());
        } else {
            log.warn("WARNING: ", "Server is not local! After stopping it the server may still be running for some time.");
        }
        try {
            Thread.sleep(500);
        } catch (final InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private void killRunningProcess() {
        serverTask.ifPresent(x -> x.cancel(true));
    }
}
