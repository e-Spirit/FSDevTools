package com.espirit.moddev.serverstart;


import com.google.common.annotations.VisibleForTesting;

import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;

@Getter
public class ServerProperties {

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
     * how often should connections be retried?
     */
    private final int connectionRetryCount;

    /**
     * how long should we wait for the first spirit server thread before we consider it dead (tried `connectionRetry` times)
     */
    private final Duration threadWait;

    /**
     * which version of first spirit should be started?
     */
    private final String version;

    /**
     * Where the FirstSpirit jars are stored. You will need at list these jars to successfully start a server:
     * <ul>
     * <li>server</li>
     * <li>fs-access</li>
     * <li>wrapper</li>
     * </ul>
     *
     * If you give one dependency, you need to give all. If you give none, they will be taken from the classpath (if possible).
     */
    private final List<File> fsServerJars;

    private final File lockFile;

    private static final Pattern VERSION_PATTERN = Pattern.compile("\\d+\\..+");
    private static final Pattern FS_SERVER_JAR_PATTERN = Pattern.compile("de/espirit/firstspirit/.+\\.jar");

    /**
     * A reference to a supplier for the license file. May come from the file system, or the class path, or anything else.
     * Is read from the class path if nothing is given.
     */
    private final Supplier<InputStream> licenseFileSupplier;

    @VisibleForTesting
    @SuppressWarnings("squid:S00107")
    @Builder
    ServerProperties(final Path serverRoot, final String serverHost, final Integer serverPort, final boolean serverGcLog, final Boolean serverInstall,
                     @Singular final List<String> serverOps, final Duration threadWait, final String serverAdminPw,
                     final Integer connectionRetryCount, final String version, @Singular final List<File> fsServerJars,
                     final Supplier<InputStream> licenseFileSupplier) {
        assertThatOrNull(serverPort, "serverPort", allOf(greaterThan(0), lessThanOrEqualTo(65536)));
        if (threadWait != null && threadWait.isNegative()) {
            throw new IllegalArgumentException("threadWait may not be negative.");
        }
        assertThatOrNull(connectionRetryCount, "connectionRetryCount", greaterThanOrEqualTo(0));
        assertThat(version, "version", allOf(notNullValue(), matchesPattern(VERSION_PATTERN)));

        this.serverRoot = serverRoot == null ? Paths.get(System.getProperty("user.home"), "opt", "FirstSpirit") : serverRoot;
        this.serverPort = serverPort == null ? 8000 : serverPort;
        this.serverGcLog = serverGcLog;
        this.serverInstall = serverInstall == null ? true : serverInstall;
        this.serverOps = serverOps == null ? Collections.emptyList() :
                         serverOps.stream().filter(Objects::nonNull)
                             .collect(Collectors.toCollection(ArrayList::new));

        this.threadWait = threadWait == null ? Duration.ofSeconds(2) : threadWait;
        this.serverAdminPw = serverAdminPw == null ? "Admin" : serverAdminPw;
        this.serverHost = serverHost == null || serverHost.isEmpty() ? "localhost" : serverHost;
        this.connectionRetryCount = connectionRetryCount == null ? 30 : connectionRetryCount;
        this.version = version;
        this.fsServerJars = fsServerJars == null || fsServerJars.isEmpty() ? getFsJarFiles() : fsServerJars.stream().filter(Objects::nonNull)
            .collect(Collectors.toCollection(ArrayList::new));
        assertThat(this.fsServerJars, "fsServerJars", hasSize(greaterThan(0)));

        //generate lock file reference, which can be found in the server directory
        this.lockFile = this.serverRoot.resolve(".fs.lock").toFile();
        this.licenseFileSupplier =
            licenseFileSupplier == null ? () -> ServerProperties.class.getResourceAsStream("/fs-license.conf") : licenseFileSupplier;
    }

    private static <T> void assertThat(final T obj, final String name, final Matcher<T> matcher) {
        if (!matcher.matches(obj)) {
            StringDescription description = new StringDescription();
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
        return Arrays.stream(System.getProperty("java.class.path").split(":"))
            .filter(x -> FS_SERVER_JAR_PATTERN.matcher(x).find())
            .map(File::new)
            .collect(Collectors.toList());
    }
}
