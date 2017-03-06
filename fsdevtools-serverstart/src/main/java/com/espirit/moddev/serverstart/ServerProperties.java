package com.espirit.moddev.serverstart;


import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
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
     * port under which the first spirit server will be reachable (leave empty to use defaults
     */
    private final int serverPort;

    /**
     * whether a GC log should be written or not
     */
    private final boolean serverGcLog;

    /**
     * whether first spirit should be installed or not TODO:really?
     */
    private final boolean serverInstall;

    /**
     * Additional server options
     */
    private final List<String> serverOps;

    /**
     * how long should we wait for the first spirit server thread before we consider it dead? TODO:really?
     */
    private final Duration threadWait;

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
     * which version of first spirit should be started?
     */
    private final String version;

    private final List<File> fsServerJars;

    private final File lockFile;

    private static <T> void assertThat(T obj, String name, Matcher<T> matcher) {
        if (!matcher.matches(obj)) {
            StringDescription description = new StringDescription();
            description.appendText(name).appendText(" must fulfil this spec: ").appendDescriptionOf(matcher).appendText("\nbut: ");
            matcher.describeMismatch(obj, description);
            throw new IllegalArgumentException(description.toString());
        }
    }

    private static <T> void assertThatOrNull(T obj, String name, Matcher<T> matcher) {
        if (obj != null) {
            assertThat(obj, name, matcher);
        }
    }

    private static Pattern versionPattern = Pattern.compile("\\d+\\..+");

    private static Pattern FS_SERVER_JAR_PATTERN = Pattern.compile("de/espirit/firstspirit/.+\\.jar");
    private static List<File> getFsJarFiles() {
        return Arrays.stream(System.getProperty("java.class.path").split(":"))
            .filter(x -> FS_SERVER_JAR_PATTERN.matcher(x).find())
            .map(File::new)
            .collect(Collectors.toList());
    }

    @Builder
    public ServerProperties(Path serverRoot, Integer serverPort, boolean serverGcLog, Boolean serverInstall, @Singular List<String> serverOps,
                            final Duration threadWait, String serverAdminPw, String serverHost, Integer connectionRetryCount, String version, List<File> fsServerJar) {
        assertThatOrNull(serverPort, "serverPort", allOf(greaterThan(0), lessThanOrEqualTo(65536)));
        if (threadWait != null && threadWait.isNegative()) {
            throw new IllegalArgumentException("threadWait may not be negative.");
        }
        assertThatOrNull(connectionRetryCount, "connectionRetryCount", greaterThanOrEqualTo(0));
        assertThat(version, "version", allOf(notNullValue(), matchesPattern(versionPattern)));

        this.serverRoot = serverRoot == null ? Paths.get(System.getProperty("user.home"), "opt", "FirstSpirit") : serverRoot;
        this.serverPort = serverPort == null ? 8000 : serverPort;
        this.serverGcLog = serverGcLog;
        this.serverInstall = serverInstall == null ? true : serverInstall;
        this.serverOps = serverOps == null ? Collections.EMPTY_LIST :
                         serverOps.stream().filter(Objects::nonNull)
                             .collect(Collectors.toCollection(ArrayList::new));//TODO: any way to make this less verbose Oo?
        //I want to write: serverOps.filter(_ != null)

        this.threadWait = threadWait == null ? Duration.ofSeconds(2) : threadWait;
        this.serverAdminPw = serverAdminPw == null ? "Admin" : serverAdminPw;
        this.serverHost = serverHost == null ? "localhost" : serverHost;
        this.connectionRetryCount = connectionRetryCount == null ? 30 : connectionRetryCount;
        this.version = version;
        this.fsServerJars = fsServerJar == null ? getFsJarFiles() : fsServerJar.stream().filter(Objects::nonNull)
            .collect(Collectors.toCollection(ArrayList::new));

        //generate lock file reference, which can be found in the server directory
        this.lockFile = this.serverRoot.resolve(".fs.lock").toFile();
    }
}
