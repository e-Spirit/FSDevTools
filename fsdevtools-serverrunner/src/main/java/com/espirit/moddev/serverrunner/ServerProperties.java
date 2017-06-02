package com.espirit.moddev.serverrunner;


import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;

@Getter
public class ServerProperties {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerProperties.class);

    public enum ConnectionMode {
        HTTP_MODE(8000);

        final int defaultPort;

        ConnectionMode(final int defaultPort) {
            this.defaultPort = defaultPort;
        }
    }

    /**
     * root of the first spirit server
     */
    private final Path serverRoot;

    /**
     * port under which the FirstSpirit server will be reachable (leave empty to use defaults)
     */
    private final int serverPort;

    /**
     * whether a GC log should be written or not
     */
    private final boolean serverGcLog;

    /**
     * whether first spirit should be installed or not
     */
    private final boolean serverInstall;

    /**
     * Additional server options
     */
    private final List<String> serverOps;

    /**
     * admin password for the first spirit server instance
     */
    private final String serverAdminPw;

    /**
     * host we bind the server on
     */
    private final String serverHost;

    /**
     * how often should action be retried?
     */
    private final int retryCount;

    /**
     * how long should we wait for the first spirit server before we consider it dead (tried `retryCount` times)
     */
    private final Duration retryWait;

    /**
     * Where the FirstSpirit jars are stored. You will need at least these jars to successfully start a server:
     * <ul>
     * <li>server</li>
     * <li>fs-access</li>
     * <li>wrapper</li>
     * </ul>
     *
     * If you give one dependency, you need to give all. If you give none, they will be taken from the classpath (if possible).
     */
    private final List<File> firstSpiritJars;

    private final File lockFile;
      
    /**
     * matches de/espirit/firstspirit/anything.jar on both unix and windows
     */
    static final Pattern FS_SERVER_JAR_PATTERN = Pattern.compile("de[\\\\/]espirit[\\\\/]firstspirit[\\\\/].+\\.jar");

    /**
     * A reference to a supplier for the license file. May come from the file system, or the class path, or anything else.
     * Is read from the class path if nothing is given.
     */
    private final Supplier<Optional<InputStream>> licenseFileSupplier;

    /**
     * In which mode to connect. Currently only HTTP_MODE is available, SOCKET_MODE might be added in the future.
     */
    private final ConnectionMode mode = ConnectionMode.HTTP_MODE;

    private final URL serverUrl;

    @SuppressWarnings("squid:S00107")
    @Builder
    ServerProperties(final Path serverRoot, final String serverHost, final Integer serverPort, final boolean serverGcLog,
                     final Boolean serverInstall,
                     @Singular final List<String> serverOps, final Duration threadWait, final String serverAdminPw,
                     final Integer retryCount, @Singular final List<File> firstSpiritJars,
                     final Supplier<Optional<InputStream>> licenseFileSupplier) {
        assertThatOrNull(serverPort, "serverPort", allOf(greaterThan(0), lessThanOrEqualTo(65536)));
        if (threadWait != null && threadWait.isNegative()) {
            throw new IllegalArgumentException("threadWait may not be negative.");
        }
        assertThatOrNull(retryCount, "retryCount", greaterThanOrEqualTo(0));

        this.serverRoot = serverRoot == null ? Paths.get(System.getProperty("user.home"), "opt", "FirstSpirit") : serverRoot;
        this.serverGcLog = serverGcLog;
        this.serverInstall = serverInstall == null ? true : serverInstall;
        this.serverOps = serverOps == null ? Collections.emptyList() :
                         serverOps.stream().filter(Objects::nonNull)
                             .collect(Collectors.toCollection(ArrayList::new));

        this.retryWait = threadWait == null ? Duration.ofSeconds(2) : threadWait;
        this.retryCount = retryCount == null ? 45 : retryCount;
        this.serverAdminPw = serverAdminPw == null ? "Admin" : serverAdminPw;
        this.serverHost = serverHost == null || serverHost.isEmpty() ? "localhost" : serverHost;
        this.serverPort = serverPort == null ? this.mode.defaultPort : serverPort;
        this.firstSpiritJars =
            firstSpiritJars == null || firstSpiritJars.isEmpty() ? getFsJarFiles() : firstSpiritJars.stream().filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
        assertThat(this.firstSpiritJars, "firstSpiritJars", hasSize(greaterThan(0)));

        //generate lock file reference, which can be found in the server directory
        this.lockFile = this.serverRoot.resolve(".fs.lock").toFile();

        //when we do not have fs-license.jar on the class path, we will not find the fs-license.conf and getResourceAsStream will return null
        this.licenseFileSupplier =
            licenseFileSupplier == null ? () -> Optional.ofNullable(ServerProperties.class.getResourceAsStream("/fs-license.conf")) : licenseFileSupplier;

        //this value should be lazily calculated
        try {
            this.serverUrl = new URL("http://" + this.serverHost + ":" + this.serverPort + "/");
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("either serverHost or serverPort had an illegal format", e);
        }
    }
    
    private static <T> void assertThat(final T obj, final String name, final Matcher<T> matcher) {
        if (!matcher.matches(obj)) {
            final StringDescription description = new StringDescription();
            description.appendText(name).appendText(" must fulfill this spec: ").appendDescriptionOf(matcher).appendText("\nbut: ");
            matcher.describeMismatch(obj, description);
            throw new IllegalArgumentException(description.toString());
        }
    }

    private static <T> void assertThatOrNull(final T obj, final String name, final Matcher<T> matcher) {
        if (obj != null) {
            assertThat(obj, name, matcher);
        }
    }

    private static List<File> getFsJarFiles() {
        final ClassLoader cl = ClassLoader.getSystemClassLoader();
        if (cl instanceof URLClassLoader) {
            final URL[] urls = ((URLClassLoader) cl).getURLs();

            return Arrays.stream(urls)
                .map(URL::getFile)
                .filter(x -> FS_SERVER_JAR_PATTERN.matcher(x).find())
                .map(File::new)
                .collect(Collectors.toList());
        } else {
            throw new IllegalStateException(
                "When the system classloader is not an UrlClassLoader, you need to manually specify the FirstSpirit jars.");
        }
    }

    /**
     * Tries to get the FirstSpirit server jar and wrapper jar from the classpath.
     * @return a list with one or two jar files or an empty list, if none of them can be found
     */
    public static List<File> getFirstSpiritJarsFromClasspath() {
        List<File> result = new ArrayList<>();
        getServerJarFileFromClasspath().ifPresent(result::add);
        getWrapperJarFileFromClasspath().ifPresent(result::add);
        getAccessJarFileFromClasspath().ifPresent(result::add);
        return result;
    }

    public static Optional<File> getJarFileFromClasspath(String name, String classname) {
    try {
        File jarFile = getJarFileForClass(classname);
        LOGGER.info("FirstSpirit "+name+" jar found in classpath: " + jarFile.getPath());
        return Optional.of(jarFile);
    } catch (ClassNotFoundException e) {
        LOGGER.info("FirstSpirit "+name+" class not found! Is the "+name+" jar file on the classpath?", e);
    } catch (URISyntaxException e) {
        LOGGER.info("FirstSpirit "+name+" jar location couldn't be translated to an URI!", e);
    }
    return Optional.empty();
}
    
    
    /**
     * Optionally gets the jar file of the FirstSpirit server, if the CMSServer class can be loaded
     * with the current classpath.
     * @return the server jar file or an empty {@link Optional}
     */
    public static Optional<File> getServerJarFileFromClasspath() {
        return getJarFileFromClasspath("server", "de.espirit.firstspirit.server.CMSServer");
    }

    /**
     * Optionally returns the jar file of the FirstSpirit wrapper, if the WrapperManager class can be
     * loaded with the current classpath.
     * @return the wrapper jar file or an empty {@link Optional}
     */
    public static Optional<File> getWrapperJarFileFromClasspath() {
        return getJarFileFromClasspath("wrapper", "org.tanukisoftware.wrapper.WrapperManager");
    }

    /**
     * Optionally returns the jar file of the FirstSpirit access API, if the ShutdownServer class can be
     * loaded with the current classpath.
     * @return the access jar file or an empty {@link Optional}
     */
    public static Optional<File> getAccessJarFileFromClasspath() {
        return getJarFileFromClasspath("access", "de.espirit.firstspirit.server.ShutdownServer");
    }

    /**
     * Tries to get the jar file of the class with the given fullQualifiedClassName string.
     * @param fullQualifiedClassName the name of the class the jar file should be located for
     * @return the corresponding jar file
     * @throws ClassNotFoundException if the given class couldn't be found
     * @throws URISyntaxException if the location of the class can not be converted to an URI
     */
    private static File getJarFileForClass(String fullQualifiedClassName) throws ClassNotFoundException, URISyntaxException {
        Class<?> serverClass = Class.forName(fullQualifiedClassName);
        CodeSource serverCodeSource = serverClass.getProtectionDomain().getCodeSource();
        return new File(serverCodeSource.getLocation().toURI().getPath());
    }
}
