package com.espirit.moddev.serverrunner;

import com.espirit.moddev.shared.annotation.VisibleForTesting;
import de.espirit.firstspirit.access.AdminService;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.ServicesBroker;
import de.espirit.firstspirit.agency.RunLevelAgent;
import de.espirit.firstspirit.server.RunLevel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
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
    private static final Duration CONNECTION_RETRY_WAIT = Duration.ofMillis(500);
    private static final String DEFAULT_XMX = "2G";

    protected final ServerProperties _serverProperties;
    /**
     * potentially contains the task that logs the FirstSpirit output (might not be filled in case we did not start the server ourselves)
     */
    protected Optional<Future<Void>> _serverTask = Optional.empty();
    protected final ExecutorService _executorService = Executors.newCachedThreadPool();

    /**
     * Creates and initializes the ServerRunner with given ServerProperties.
     *
     * @param serverProperties needed to determine, which server to start/stop. May not be null!
     * @throws NullPointerException
     */
    public NativeServerRunner(final ServerProperties serverProperties) {
        _serverProperties = Objects.requireNonNull(serverProperties);
    }

    /**
     * Waits for a given condition, retrying if necessary, blocking the thread in between.
     *
     * @param condition the condition to be checked
     * @param waitTime  the time to wait between queries to `condition`
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
     * Updates a properties file with the properties given
     *
     * @param fileToUpdate      the file that should be updated - may not exist, in that case `alternativeSource` is read
     * @param alternativeSource the source that should be read in case the file does not exist (e.g. an InputStream to a file from the resources)
     * @param propertySetter    side-effecting function to update the properties found in the file
     */
    @VisibleForTesting
    static void updatePropertiesFile(Path fileToUpdate, InputStream alternativeSource, Consumer<Properties> propertySetter) throws IOException {
        try (BufferedReader reader = fileToUpdate.toFile().exists()
                ? Files.newBufferedReader(fileToUpdate)
                : new BufferedReader(new InputStreamReader(alternativeSource, StandardCharsets.UTF_8))) {
            final Properties properties = new Properties();
            properties.load(reader);
            propertySetter.accept(properties);

            try (FileWriter fileWriter = new FileWriter(fileToUpdate.toFile())) {
                properties.store(fileWriter, "");
            }
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
        final Path loggingConfigFile = confDir.resolve("fs-logging.conf");

        Files.createDirectories(serverDir);
        Files.createDirectories(confDir);

        Files.write(initFile, Collections.emptyList());

        final Optional<InputStream> inputStream = serverProperties.getLicenseFileSupplier().get();
        if (inputStream.isPresent()) {
            Files.copy(inputStream.get(), confDir.resolve("fs-license.conf"), StandardCopyOption.REPLACE_EXISTING);
        }

        //either update an existing conf file, or if none exists, use the one from the class path
        updatePropertiesFile(confFile, NativeServerRunner.class.getResourceAsStream("/" + confFile.getFileName().toString()), properties -> {
            properties.setProperty("HTTP_PORT", String.valueOf(serverProperties.getHttpPort()));
            properties.setProperty("SOCKET_PORT", String.valueOf(serverProperties.getSocketPort()));
        });
        updatePropertiesFile(loggingConfigFile, NativeServerRunner.class.getResourceAsStream("/" + loggingConfigFile.getFileName().toString()), properties -> {
            properties.setProperty("log4j.rootCategory", serverProperties.getLogLevel().name() + ", fs");
        });

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

        boolean noXmxSet = true;
        for (String opt : serverProperties.getServerOps()){
            args.add(opt); // add all ServerOps
            if (opt.startsWith("-Xmx")){
                noXmxSet = false;
            }
        }
        if (noXmxSet){
            args.add("-Xmx" + DEFAULT_XMX);
        }

        if (serverProperties.isServerInstall()) {
            prepareFilesystem(serverProperties);
        }
        args.add("-Dcmsroot=" + fsServerRoot);
        args.add("-Djava.security.policy=" + fsServerRoot.resolve("conf").resolve("fs-server.policy"));
        args.addAll(Arrays.asList("-cp", serverProperties.getFirstSpiritJars().stream().map(File::toString).collect(joining(String.valueOf(java.io.File.pathSeparatorChar)))));

        URLClassLoader loader = new URLClassLoader(toUrlArray(serverProperties.getFirstSpiritJars()), null);
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
        switch (serverProperties.getMode()) {
            case HTTP_MODE:
                return HttpConnectionTester.testConnection(serverProperties.getServerUrl());
            case SOCKET_MODE:
                return SocketConnectionTester.testConnection(serverProperties.getServerHost(), serverProperties.getSocketPort(),
                        serverProperties.getServerAdminPw());
            default:
                return false;
        }
    }

    /**
     * Boots a FirstSpirit server, according to configuration
     *
     * @param serverProperties The server properties to be used
     * @param executor         The executor where tasks should be run on. Needs to supply at least 2 threads at the same time.
     * @return a cancellable task that is already running
     * @throws java.io.IOException on file system access problems
     */
    @SuppressWarnings({"squid:S1141", "squid:S1188"}) //nested try and too long lambda
    static Future startFirstSpiritServer(final ServerProperties serverProperties, final ExecutorService executor) throws IOException {
        final List<String> commands = Collections.unmodifiableList(new ArrayList<>(prepareStartup(serverProperties)));
        if (log.isInfoEnabled()) {
            log.info("Execute command " + String.join(" ", commands));
        }

        //start FirstSpirit async
        /* Construct a cancellable logging task. It will be cancelled in `stopFirstSpiritServer`.
           The inner logTask is necessary since the implicit `readLine()` on the `BufferedReader` has a blocking API that cannot be interrupted. This
           task is stopped by destroying the process outputting data, which implicitly closes the input stream that is being blocked on. You can view
           future as an entity that does the very same job as logTask with the added functionality of gracefully shutting down on server
           stop.
         */
        return executor.submit(() -> {
            final ProcessBuilder builder = new ProcessBuilder(commands);
            builder.redirectErrorStream(true);
            final Process process;
            try {
                process = builder.start();
                //start logging on another task to be able to be interrupted to destroy the original process because it hangs sometimes
                final Future logTask = executor.submit(() -> {
                    new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)).lines()
                            .forEach(line -> log.info("FirstSpirit Server log:" + line));
                });
                try {
                    logTask.get();
                } catch (final InterruptedException | ExecutionException ie) {
                    process.destroy();  //kill the process if it did not die on its own
                    Thread.currentThread().interrupt();
                    log.debug(PROBLEM_READING, ie);
                }
            } catch (final IOException ioe) {
                log.error(PROBLEM_READING, ioe);
            }
        });
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
        if (!testConnection(_serverProperties)) {
            log.info("Starting FirstSpirit Server...");

            ServerStatus serverStatus = determineServerStatus();

            logOptionalErrorState(serverStatus);

            return serverStatus.serverRunning;

        } else {
            log.info("FirstSpirit Server already running.");
            return true;
        }
    }

    @NotNull
    private ServerStatus determineServerStatus() {
        ServerStatus serverStatus = new ServerStatus(false, false);

        try {
            startFirstSpiritServerIfNotDoneYet();

            final Duration timeout = _serverProperties.getTimeout();
            final long started = System.currentTimeMillis();
            final boolean apiConnectionSuccessful = waitForSuccessfulApiConnection(timeout);
            final Duration remainingTimeout = timeout.minusMillis(System.currentTimeMillis() - started);

            boolean serverRunLevelIsStarted = false;
            if (apiConnectionSuccessful) {
                serverRunLevelIsStarted = connectAndWaitForRunLevelStarted(remainingTimeout);
            }
            serverStatus = new ServerStatus(apiConnectionSuccessful, serverRunLevelIsStarted);
        } catch (final IOException ioe) {
//            This information is not useful for most cases, so we shouldn't log this in error.
            log.debug(PROBLEM_READING, ioe);
        }
        return serverStatus;
    }

    private boolean connectAndWaitForRunLevelStarted(final Duration timeout) {
        Optional<Connection> optionalConnection = _serverProperties.tryOpenAdminConnection();
        return optionalConnection.map((Connection connectionParam) -> {
            try (Connection connection = connectionParam) {
                final RunLevelAgent runLevelAgent = connection.getBroker().requireSpecialist(RunLevelAgent.TYPE);
                runLevelAgent.waitForRunLevel(RunLevel.STARTED, timeout);
                return true;
            } catch (TimeoutException e) {
                log.error("FirstSpirit server could not be started within configured timeout of " + _serverProperties.getTimeout() + ".", e);
            } catch (IOException e) {
//                This information is not useful for most cases, so we shouldn't log this in error.
                log.debug("Not able to close connection properly...", e);
            }
            return false;
        }).orElse(false);
    }

    private void logOptionalErrorState(ServerStatus serverStatus) {
        if (!serverStatus.apiConnectionSuccessful) {
            log.error("Could not establish admin connection to FirstSpirit server.");
        }
        if (!serverStatus.serverRunning) {
            log.error("Could not start FirstSpirit server.");
        }
    }

    private boolean waitForSuccessfulApiConnection(final Duration timeout) {
        final int retryCount = (int) (timeout.toMillis() / CONNECTION_RETRY_WAIT.toMillis());

        return waitForCondition(() -> {
            final int port = _serverProperties.getMode() == ServerProperties.ConnectionMode.SOCKET_MODE ? _serverProperties.getSocketPort() : _serverProperties.getHttpPort();
            log.info("Trying to establish FirstSpirit server connection... " + _serverProperties.getServerHost() + ":" + port + "(" + _serverProperties.getMode() + ")");
            return testConnection(_serverProperties);
        }, CONNECTION_RETRY_WAIT, retryCount);
    }

    private void startFirstSpiritServerIfNotDoneYet() throws IOException {
        if (!_serverTask.isPresent()) {
            _serverTask = Optional.of(startFirstSpiritServer(_serverProperties, _executorService));
        }
    }

    @Override
    public boolean isRunning() {
        Optional<Connection> optionalConnection = _serverProperties.tryOpenAdminConnection();
        return optionalConnection.map((Connection connection) -> {
            RunLevel runLevel = connection.getBroker().requireSpecialist(RunLevelAgent.TYPE).getRunLevel();
            closeConnectionAndDebugLogErrors(connection);
            return runLevel == RunLevel.STARTED;
        }).orElse(false);
    }

    private void closeConnectionAndDebugLogErrors(Connection connection) {
        try {
            connection.close();
        } catch (IOException e) {
//            This information is not useful for most cases, so we shouldn't log this in error.
            log.debug("Not able to close connection properly...", e);
        }
    }

    @Override
    public boolean stop() {
        log.info("Stopping FirstSpirit Server...");
        final Optional<Connection> connection = _serverProperties.tryOpenAdminConnection();
        if (connection.isPresent()) {
            log.info("Stopping server...");
            stopFirstSpiritServerAndDisconnect(connection.get());
            log.info("Stopping server...");
            ensureFsLockFileIsRemoved(_serverProperties);
        } else {
            log.warn("Connection to server failed, trying to kill the process");
        }
        // finally kill the process
        killRunningProcess();
        return !testConnection(_serverProperties);
    }

    private void stopFirstSpiritServerAndDisconnect(final Connection connection) {
        final ServicesBroker servicesBroker = connection.getBroker().requireSpecialist(ServicesBroker.TYPE);
        final AdminService adminService = servicesBroker.getService(AdminService.class);
        adminService.stopServer();
        closeConnectionAndDebugLogErrors(connection);
    }

    private void ensureFsLockFileIsRemoved(ServerProperties serverProperties) {
        // indicates that the server is still running if the lock file is still there

        if (isServerLocal(serverProperties)) {
            final int retryCount = (int) (serverProperties.getTimeout().toMillis() / CONNECTION_RETRY_WAIT.toMillis());
            waitForCondition(() -> !serverProperties.getLockFile().exists(), CONNECTION_RETRY_WAIT, retryCount);
        } else {
            log.warn("Server is not local! After stopping it the server may still be running for some time.");
        }
        try {
            Thread.sleep(500);
        } catch (final InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private void killRunningProcess() {
        _serverTask.ifPresent(x -> x.cancel(true));
    }

    @Data
    @Setter(AccessLevel.NONE)
    @AllArgsConstructor
    private static class ServerStatus {
        private boolean apiConnectionSuccessful;
        private boolean serverRunning;
    }
}
