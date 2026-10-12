/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2026 Crownpeak Technology GmbH
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

package com.espirit.moddev.cli.scripts;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.mockwebserver.SocketPolicy;
import okio.Buffer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests the {@code download} mode of the start scripts (DEVEX-714) against a mock FirstSpirit server.
 * <p>
 * Every test runs the real script as an external process on a fake distribution, so the scenarios are the same for
 * {@code fs-cli.sh} and {@code fs-cli.cmd}. {@link #flavour()} selects the script.
 */
@Timeout(180)
abstract class AbstractDownloadScriptTest {

    static final String VERSION = "2026.8.1.abc1234";
    static final String JAR_NAME = "fs-isolated-runtime-" + VERSION + ".jar";
    private static final String URL_PLACEHOLDER = "@URL@";
    private static final String VERSION_PATH = "/version.txt";
    private static final String JAR_PATH = "/jar/fs-isolated-runtime.jar";
    private static final String USER = "Admin";
    private static final String PASSWORD = "secret";

    /** a jar-like payload: starts with the zip magic and contains NUL, CR/LF and CTRL-Z, which a text-mode copy would damage */
    static final byte[] JAR_BYTES = jarBytes(300_000);

    @TempDir
    Path work;

    private Path dist;
    private Path script;
    private MockWebServer server;
    private final List<String> requests = new CopyOnWriteArrayList<>();
    private final Map<String, String> authorizations = new ConcurrentHashMap<>();
    private volatile Function<RecordedRequest, MockResponse> versionResponse = request -> new MockResponse().setBody(VERSION + "\n");
    private volatile Function<RecordedRequest, MockResponse> jarResponse = request -> new MockResponse().setBody(new Buffer().write(JAR_BYTES));

    abstract ScriptFlavour flavour();

    @BeforeEach
    void setUp() throws IOException {
        final ScriptFlavour flavour = flavour();
        assumeTrue(flavour.isAvailable(), "interpreter or curl for " + flavour.scriptName() + " not available");
        dist = Files.createDirectories(work.resolve("fs-cli"));
        Files.createDirectories(dist.resolve("lib"));
        script = flavour.install(dist);
        server = new MockWebServer();
        server.setDispatcher(new Dispatcher() {
            @NotNull
            @Override
            public MockResponse dispatch(@NotNull final RecordedRequest request) {
                final String path = request.getPath();
                requests.add(path);
                final String authorization = request.getHeader("Authorization");
                authorizations.put(path, authorization == null ? "" : authorization);
                if (VERSION_PATH.equals(path)) {
                    return versionResponse.apply(request);
                }
                if (JAR_PATH.equals(path)) {
                    return jarResponse.apply(request);
                }
                return new MockResponse().setResponseCode(404);
            }
        });
        server.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (server != null) {
            server.shutdown();
        }
    }

    // ---------------------------------------------------------------------------------------------------------------
    // happy path
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void downloadSavesJarWithVersionInName() throws Exception {
        final ScriptResult result = download(url(), USER, PASSWORD);

        assertSuccess(result);
        assertThat(result.stdout()).contains("Downloaded " + JAR_NAME).contains(JAR_BYTES.length + " bytes");
        assertThat(libFiles()).containsExactly(JAR_NAME);
        assertThat(Files.readAllBytes(lib().resolve(JAR_NAME))).as("the jar is stored byte for byte").isEqualTo(JAR_BYTES);
    }

    @Test
    void downloadAsksForVersionFirstAndSendsBasicAuthWithBothRequests() throws Exception {
        assertSuccess(download(url(), USER, PASSWORD));

        assertThat(requests).containsExactly(VERSION_PATH, JAR_PATH);
        assertThat(authorizations.get(VERSION_PATH)).isEqualTo(basic(USER, PASSWORD));
        assertThat(authorizations.get(JAR_PATH)).isEqualTo(basic(USER, PASSWORD));
    }

    @Test
    void downloadNeitherNeedsJavaHomeNorJava() throws Exception {
        final Map<String, String> env = new HashMap<>();
        env.put("JAVA_HOME", null); // not set at all

        assertSuccess(download(env, url(), USER, PASSWORD));
        assertThat(libFiles()).containsExactly(JAR_NAME);
    }

    @Test
    void downloadKeepsOtherLibraries() throws Exception {
        // a fresh distribution: lib only has the cli jar, which must stay untouched
        Files.write(lib().resolve("fsdevtools-cli-4.9.0.jar"), new byte[]{1, 2, 3});

        assertSuccess(download(url(), USER, PASSWORD));
        assertThat(libFiles()).containsExactlyInAnyOrder("fsdevtools-cli-4.9.0.jar", JAR_NAME);
    }

    @Test
    void downloadAcceptsTrailingSlashInUrl() throws Exception {
        assertSuccess(download(url() + "/", USER, PASSWORD));
        assertThat(requests).containsExactly(VERSION_PATH, JAR_PATH);
    }

    @Test
    void downloadReadsPasswordFromFspwdWhenNotGiven() throws Exception {
        assertSuccess(download(Map.of("fspwd", "from-env"), url(), USER));
        assertThat(authorizations.get(JAR_PATH)).isEqualTo(basic(USER, "from-env"));
    }


    private static Map<String, String> fsurlEnv(final String fsurl) {
        return new HashMap<>(Map.of("fsurl", fsurl));
    }

    @Test
    void urlSchemeIsCaseInsensitive() throws Exception {
        assertSuccess(download(url().replace("http://", "HTTP://"), USER, PASSWORD));
        assertThat(requests).containsExactly(VERSION_PATH, JAR_PATH);
    }

    @Test
    void fsurlSchemeIsCaseInsensitive() throws Exception {
        assertSuccess(download(fsurlEnv(url().replace("http://", "HTTP://")), USER, PASSWORD));
        assertThat(requests).containsExactly(VERSION_PATH, JAR_PATH);
    }

    @Test
    void fsurlWithThreeArgumentsWithoutSchemeIsRejected() throws Exception {
        final ScriptResult result = download(fsurlEnv(url()), "Admin", "secret", "extra");

        assertFailed(result, "Too many arguments", "Usage: fs-cli download");
        assertThat(requests).isEmpty();
    }

    @Test
    void fsurlWithOnlyUserArgumentReadsPasswordFromFspwd() throws Exception {
        final Map<String, String> env = fsurlEnv(url());
        env.put("fspwd", "from-env");
        assertSuccess(download(env, USER));
        assertThat(authorizations.get(JAR_PATH)).isEqualTo(basic(USER, "from-env"));
    }

    @Test
    void fsurlWithoutUserArgumentAndWithoutFsuserIsRejected() throws Exception {
        final Map<String, String> env = fsurlEnv(url());
        env.put("fspwd", "from-env");
        final ScriptResult result = download(env);

        assertFailed(result, "user is missing", "Usage: fs-cli download");
        assertThat(requests).isEmpty();
    }

    @Test
    void fsurlWithoutSchemeDefaultsToHttp() throws Exception {
        final String hostAndPort = url().substring("http://".length());
        assertSuccess(download(fsurlEnv(hostAndPort), USER, PASSWORD));
        assertThat(requests).containsExactly(VERSION_PATH, JAR_PATH);
    }

    @Test
    void fsurlWithHttpsSchemeIsKept() throws Exception {
        final ScriptResult result = download(fsurlEnv(url().replace("http://", "https://")), USER, PASSWORD);
        assertThat(result.exitCode()).isEqualTo(1);
        assertThat(result.stderr()).contains("Could not reach https://");
    }

    @Test
    void downloadReadsUrlFromFsurl() throws Exception {
        assertSuccess(download(fsurlEnv(url()), USER, PASSWORD));
        assertThat(requests).containsExactly(VERSION_PATH, JAR_PATH);
        assertThat(authorizations.get(JAR_PATH)).isEqualTo(basic(USER, PASSWORD));
    }

    @Test
    void downloadReadsEverythingFromEnvironment() throws Exception {
        final Map<String, String> env = fsurlEnv(url());
        env.put("fsuser", "env-user");
        env.put("fspwd", "from-env");
        assertSuccess(download(env));
        assertThat(authorizations.get(JAR_PATH)).isEqualTo(basic("env-user", "from-env"));
    }

    @Test
    void downloadUrlArgumentWinsOverFsurl() throws Exception {
        final Map<String, String> env = Map.of("fsurl", "http://unreachable.invalid:1");
        assertSuccess(download(env, url(), USER, PASSWORD));
        assertThat(requests).containsExactly(VERSION_PATH, JAR_PATH);
    }


    @Test
    void downloadReadsUserFromFsuserWhenNotGiven() throws Exception {
        assertSuccess(download(Map.of("fsuser", "env-user", "fspwd", "from-env"), url()));
        assertThat(authorizations.get(JAR_PATH)).isEqualTo(basic("env-user", "from-env"));
    }

    @Test
    void userArgumentWinsOverFsuser() throws Exception {
        assertSuccess(download(Map.of("fsuser", "env-user"), url(), USER, PASSWORD));
        assertThat(authorizations.get(JAR_PATH)).isEqualTo(basic(USER, PASSWORD));
    }

    @Test
    void passwordArgumentWinsOverFspwd() throws Exception {
        assertSuccess(download(Map.of("fspwd", "from-env"), url(), USER, PASSWORD));
        assertThat(authorizations.get(JAR_PATH)).isEqualTo(basic(USER, PASSWORD));
    }

    @Test
    void downloadPassesSpecialCharactersInPasswordUnchanged() throws Exception {
        final String password = flavour().trickyPassword();

        assertSuccess(download(url(), USER, password));
        assertThat(authorizations.get(JAR_PATH)).isEqualTo(basic(USER, password));
    }

    @Test
    void downloadPassesSpecialCharactersInFspwdUnchanged() throws Exception {
        final String password = flavour().trickyPassword();

        assertSuccess(download(Map.of("fspwd", password), url(), USER));
        assertThat(authorizations.get(JAR_PATH)).isEqualTo(basic(USER, password));
    }

    @Test
    void secondRunIsUpToDate() throws Exception {
        assertSuccess(download(url(), USER, PASSWORD));
        requests.clear();

        final ScriptResult second = download(url(), USER, PASSWORD);

        assertSuccess(second);
        assertThat(second.stdout()).contains("already up to date");
        assertThat(requests).as("only the version is asked").containsExactly(VERSION_PATH);
        assertThat(libFiles()).containsExactly(JAR_NAME);
    }

    @ParameterizedTest(name = "version.txt body [{0}]")
    @ValueSource(strings = {VERSION, VERSION + "\n", VERSION + "\r\n", VERSION + "  \r\n", "  " + VERSION, "   " + VERSION + "  \r\n"})
    void versionIsTrimmed(final String body) throws Exception {
        versionResponse = request -> new MockResponse().setBody(body);

        assertSuccess(download(url(), USER, PASSWORD));
        assertThat(libFiles()).containsExactly(JAR_NAME);
    }

    @Test
    void versionWithMaximumLengthIsAccepted() throws Exception {
        final String longest = "v".repeat(64);
        versionResponse = request -> new MockResponse().setBody(longest + "\n");

        assertSuccess(download(url(), USER, PASSWORD));
        assertThat(libFiles()).containsExactly("fs-isolated-runtime-" + longest + ".jar");
    }

    // ---------------------------------------------------------------------------------------------------------------
    // lib/ handling: up to date, replacing other jars, --force
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void sameVersionInLibMeansNothingIsDownloaded() throws Exception {
        final byte[] existing = {'P', 'K', 9, 9};
        Files.write(lib().resolve(JAR_NAME), existing);

        final ScriptResult result = download(url(), USER, PASSWORD);

        assertSuccess(result);
        assertThat(result.stdout()).contains("already up to date").contains(JAR_NAME);
        assertThat(requests).as("the jar is never requested").containsExactly(VERSION_PATH);
        assertThat(Files.readAllBytes(lib().resolve(JAR_NAME))).isEqualTo(existing);
    }

    @Test
    void forceDownloadsEvenIfTheSameVersionIsInLib() throws Exception {
        Files.write(lib().resolve(JAR_NAME), new byte[]{'P', 'K', 9, 9});

        final ScriptResult result = download(url(), USER, PASSWORD, "--force");

        assertSuccess(result);
        assertThat(result.stdout()).doesNotContain("already up to date");
        assertThat(requests).as("the jar is requested").containsExactly(VERSION_PATH, JAR_PATH);
        assertThat(libFiles()).containsExactly(JAR_NAME);
        assertThat(Files.readAllBytes(lib().resolve(JAR_NAME))).isEqualTo(JAR_BYTES);
    }

    @Test
    void failedForcedDownloadKeepsTheSameVersionJar() throws Exception {
        final byte[] existing = {'P', 'K', 9, 9};
        Files.write(lib().resolve(JAR_NAME), existing);
        jarResponse = request -> new MockResponse().setResponseCode(500).setBody("oops");

        assertFailed(download(url(), USER, PASSWORD, "--force"), "HTTP 500");
        assertThat(libFiles()).containsExactly(JAR_NAME);
        assertThat(Files.readAllBytes(lib().resolve(JAR_NAME))).isEqualTo(existing);
    }

    @Test
    void upToDateCheckIgnoresCaseOfFileName() throws Exception {
        final String upper = JAR_NAME.toUpperCase();
        Files.write(lib().resolve(upper), new byte[]{'P', 'K'});

        final ScriptResult result = download(url(), USER, PASSWORD);

        assertSuccess(result);
        assertThat(result.stdout()).contains("already up to date");
        assertThat(requests).containsExactly(VERSION_PATH);
        assertThat(libFiles()).containsExactly(upper);
    }

    @Test
    void upToDateRunIgnoresProblemsOfTheJarEndpoint() throws Exception {
        Files.write(lib().resolve(JAR_NAME), new byte[]{'P', 'K'});
        jarResponse = request -> new MockResponse().setResponseCode(401);

        assertSuccess(download(url(), USER, PASSWORD));
    }

    @ParameterizedTest(name = "other jar {0} is replaced without force")
    @ValueSource(strings = {"fs-isolated-runtime.jar", "fs-isolated-runtime-2025.1.0.abcdef.jar", "FS-ISOLATED-RUNTIME-OLD.JAR"})
    void otherRuntimeJarIsReplacedWithoutForce(final String other) throws Exception {
        Files.write(lib().resolve(other), new byte[]{'P', 'K', 7});

        final ScriptResult result = download(url(), USER, PASSWORD);

        assertSuccess(result);
        assertThat(result.stdout()).contains("Removed").contains(other);
        assertThat(libFiles()).containsExactly(JAR_NAME);
        assertThat(Files.readAllBytes(lib().resolve(JAR_NAME))).isEqualTo(JAR_BYTES);
    }

    @Test
    void allOtherRuntimeJarsAreReplacedWithoutForce() throws Exception {
        Files.write(lib().resolve("fs-isolated-runtime.jar"), new byte[]{'P', 'K'});
        Files.write(lib().resolve("fs-isolated-runtime-1.0.jar"), new byte[]{'P', 'K'});
        Files.write(lib().resolve("other-library.jar"), new byte[]{'P', 'K', 4});

        assertSuccess(download(url(), USER, PASSWORD));
        assertThat(libFiles()).containsExactlyInAnyOrder(JAR_NAME, "other-library.jar");
    }

    @Test
    void failedDownloadWithoutForceKeepsTheOldJars() throws Exception {
        Files.write(lib().resolve("fs-isolated-runtime.jar"), new byte[]{'P', 'K', 1});
        jarResponse = request -> new MockResponse().setResponseCode(500).setBody("oops");

        assertFailed(download(url(), USER, PASSWORD), "HTTP 500");
        assertThat(libFiles()).as("the old jar is only removed after a verified download").containsExactly("fs-isolated-runtime.jar");
    }

    @ParameterizedTest(name = "--force at position {0}")
    @ValueSource(ints = {0, 1, 2, 3})
    void forceReplacesAllOtherRuntimeJars(final int position) throws Exception {
        Files.write(lib().resolve("fs-isolated-runtime.jar"), new byte[]{'P', 'K', 1});
        Files.write(lib().resolve("fs-isolated-runtime-1.0.jar"), new byte[]{'P', 'K', 2});
        Files.write(lib().resolve("fsdevtools-cli-4.9.0.jar"), new byte[]{'P', 'K', 3});
        Files.write(lib().resolve("other-library.jar"), new byte[]{'P', 'K', 4});
        final List<String> args = new ArrayList<>(List.of(url(), USER, PASSWORD));
        args.add(position, "--force");

        final ScriptResult result = download(args.toArray(String[]::new));

        assertSuccess(result);
        assertThat(result.stdout()).contains("Removed").contains("fs-isolated-runtime.jar").contains("fs-isolated-runtime-1.0.jar");
        assertThat(libFiles()).containsExactlyInAnyOrder(JAR_NAME, "fsdevtools-cli-4.9.0.jar", "other-library.jar");
        assertThat(Files.readAllBytes(lib().resolve(JAR_NAME))).isEqualTo(JAR_BYTES);
    }

    @Test
    void forceWithPasswordFromFspwd() throws Exception {
        Files.write(lib().resolve("fs-isolated-runtime.jar"), new byte[]{'P', 'K'});

        assertSuccess(download(Map.of("fspwd", PASSWORD), url(), USER, "--force"));
        assertThat(libFiles()).containsExactly(JAR_NAME);
    }

    @ParameterizedTest(name = "similar names are kept, with force: {0}")
    @ValueSource(booleans = {false, true})
    void filesWhoseNamesOnlyLookSimilarAreKept(final boolean force) throws Exception {
        Files.write(lib().resolve("fs-isolated-runtime.jar.bak"), new byte[]{1});
        Files.write(lib().resolve("my-fs-isolated-runtime.jar"), new byte[]{2});
        Files.write(lib().resolve("fs-isolated-runtime.jar"), new byte[]{'P', 'K'});

        assertSuccess(force ? download(url(), USER, PASSWORD, "--force") : download(url(), USER, PASSWORD));
        assertThat(libFiles()).containsExactlyInAnyOrder(JAR_NAME, "fs-isolated-runtime.jar.bak", "my-fs-isolated-runtime.jar");
    }

    @ParameterizedTest(name = "backup files are not touched, with force: {0}")
    @ValueSource(booleans = {false, true})
    void backupFilesAreNeitherReplacedNorCountedAsUpToDate(final boolean force) throws Exception {
        // *.jar.bak or *.jar_old are no runtime jars (on Windows their 8.3 names match the *.jar wildcard): nothing is deleted
        Files.write(lib().resolve("fs-isolated-runtime.jar.bak"), new byte[]{1});
        Files.write(lib().resolve("fs-isolated-runtime-old.jar_old"), new byte[]{2});

        assertSuccess(force ? download(url(), USER, PASSWORD, "--force") : download(url(), USER, PASSWORD));
        assertThat(libFiles()).containsExactlyInAnyOrder(JAR_NAME, "fs-isolated-runtime.jar.bak", "fs-isolated-runtime-old.jar_old");
    }

    @Test
    void failedDownloadWithForceKeepsTheOldJars() throws Exception {
        Files.write(lib().resolve("fs-isolated-runtime.jar"), new byte[]{'P', 'K', 1});
        jarResponse = request -> new MockResponse().setResponseCode(500).setBody("oops");

        assertFailed(download(url(), USER, PASSWORD, "--force"), "HTTP 500");
        assertThat(libFiles()).as("the old jar is only removed after a verified download").containsExactly("fs-isolated-runtime.jar");
    }

    // ---------------------------------------------------------------------------------------------------------------
    // argument and environment errors: nothing is requested, lib/ stays untouched
    // ---------------------------------------------------------------------------------------------------------------

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidArguments")
    void invalidArgumentsAreRejected(final String name, final List<String> args, final String message) throws Exception {
        Files.write(lib().resolve("untouched.jar"), new byte[]{1});

        final ScriptResult result = download(args.stream().map(a -> a.replace(URL_PLACEHOLDER, url())).toArray(String[]::new));

        assertFailed(result, message, "Usage: fs-cli download");
        assertThat(requests).isEmpty();
        assertThat(libFiles()).containsExactly("untouched.jar");
    }

    static Stream<Arguments> invalidArguments() {
        return Stream.of(
                Arguments.of("no arguments at all", List.of(), "must start with http://"),
                Arguments.of("url without scheme", List.of("localhost:8000", "Admin", "secret"), "must start with http://"),
                Arguments.of("ftp url", List.of("ftp://host/", "Admin", "secret"), "must start with http://"),
                Arguments.of("scheme only (http)", List.of("http://", "Admin", "secret"), "must start with http://"),
                Arguments.of("scheme only (https)", List.of("https://", "Admin", "secret"), "must start with http://"),
                Arguments.of("user is missing", List.of(URL_PLACEHOLDER), "user is missing"),
                Arguments.of("password is missing and fspwd unset", List.of(URL_PLACEHOLDER, "Admin"), "password is missing"),
                Arguments.of("only --force", List.of("--force"), "must start with http://"),
                Arguments.of("unknown option", List.of(URL_PLACEHOLDER, "Admin", "secret", "--insecure"), "Unknown option"),
                Arguments.of("unknown option before values", List.of("--foo", URL_PLACEHOLDER, "Admin", "secret"), "Unknown option"),
                Arguments.of("too many arguments", List.of(URL_PLACEHOLDER, "Admin", "secret", "extra"), "Too many arguments")
        );
    }

    @Test
    void missingLibDirectoryIsReported() throws Exception {
        Files.delete(lib());

        final ScriptResult result = download(url(), USER, PASSWORD);

        assertThat(result.exitCode()).as(result.toString()).isEqualTo(1);
        assertThat(result.stderr()).contains("Cannot write to");
        assertThat(requests).isEmpty();
    }

    @Test
    void missingCurlIsReported() throws Exception {
        final Map<String, String> env = flavour().environmentWithoutCurl(work);
        assumeTrue(env != null, "curl cannot be hidden on this machine");

        assertFailed(download(env, url(), USER, PASSWORD), "curl is required");
        assertThat(requests).isEmpty();
        assertThat(libFiles()).isEmpty();
    }

    // ---------------------------------------------------------------------------------------------------------------
    // version.txt errors
    // ---------------------------------------------------------------------------------------------------------------

    @ParameterizedTest(name = "version.txt answers HTTP {0}")
    @ValueSource(ints = {301, 302, 401, 403, 404, 500, 503})
    void versionRequestWithoutHttp200IsRejected(final int status) throws Exception {
        versionResponse = request -> new MockResponse().setResponseCode(status).setHeader("Location", "/elsewhere").setBody("1.0");

        assertFailed(download(url(), USER, PASSWORD), "Could not read the server version", "HTTP " + status);
        assertThat(requests).as("redirects are not followed").containsExactly(VERSION_PATH);
        assertThat(libFiles()).isEmpty();
    }

    @ParameterizedTest(name = "version.txt body [{0}] is empty")
    @ValueSource(strings = {"", "\n", "\r\n\r\n"})
    void emptyVersionIsRejected(final String body) throws Exception {
        versionResponse = request -> new MockResponse().setBody(body);

        assertFailed(download(url(), USER, PASSWORD), "Could not read the server version");
        assertThat(requests).containsExactly(VERSION_PATH);
        assertThat(libFiles()).isEmpty();
    }

    @Test
    void blankVersionIsRejected() throws Exception {
        versionResponse = request -> new MockResponse().setBody("   \r\n");

        assertFailed(download(url(), USER, PASSWORD), "Could not read the server version");
        assertThat(libFiles()).isEmpty();
    }

    @ParameterizedTest(name = "version [{0}]")
    @ValueSource(strings = {"../evil", "..\\evil", "1.0;rm", "a*b", "1.0|x", "$(id)", "a\"b", "a/b"})
    void unsafeVersionIsRejected(final String version) throws Exception {
        versionResponse = request -> new MockResponse().setBody(version + "\n");

        assertFailed(download(url(), USER, PASSWORD), "refusing to use it in a file name");
        assertThat(requests).containsExactly(VERSION_PATH);
        assertThat(libFiles()).isEmpty();
    }

    @Test
    void tooLongVersionIsRejected() throws Exception {
        versionResponse = request -> new MockResponse().setBody("v".repeat(65));

        assertFailed(download(url(), USER, PASSWORD), "refusing to use it in a file name");
        assertThat(libFiles()).isEmpty();
    }

    @Test
    void unreachableServerIsReported() throws Exception {
        assertFailed(download("http://127.0.0.1:" + freePort(), USER, PASSWORD), "Could not reach", "curl exit");
        assertThat(libFiles()).isEmpty();
    }

    @Test
    void serverClosingTheConnectionOnVersionRequestIsReported() throws Exception {
        versionResponse = request -> new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST);

        assertFailed(download(url(), USER, PASSWORD), "Could not reach");
        assertThat(libFiles()).isEmpty();
    }

    // ---------------------------------------------------------------------------------------------------------------
    // jar download errors: nothing may be left in lib/
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void wrongCredentialsAreReported() throws Exception {
        jarResponse = request -> new MockResponse().setResponseCode(401).setHeader("WWW-Authenticate", "Basic realm=\"FirstSpirit\"");

        assertFailed(download(url(), USER, PASSWORD), "Authentication failed for user", USER);
        assertThat(libFiles()).isEmpty();
    }

    @Test
    void errorMessagesNeverContainThePassword() throws Exception {
        jarResponse = request -> new MockResponse().setResponseCode(401);

        final ScriptResult result = download(url(), USER, "t0p-s3cret");

        assertFailed(result, "Authentication failed");
        assertThat(result.output()).doesNotContain("t0p-s3cret");
    }

    @Test
    void missingEndpointIsReported() throws Exception {
        jarResponse = request -> new MockResponse().setResponseCode(404);

        assertFailed(download(url(), USER, PASSWORD), "Endpoint not found", "/jar/fs-isolated-runtime.jar");
        assertThat(libFiles()).isEmpty();
    }

    @ParameterizedTest(name = "jar request answers HTTP {0}")
    @ValueSource(ints = {301, 302, 307, 308})
    void redirectOnJarRequestIsNotFollowed(final int status) throws Exception {
        jarResponse = request -> new MockResponse().setResponseCode(status).setHeader("Location", "/elsewhere");

        assertFailed(download(url(), USER, PASSWORD), "redirected");
        assertThat(requests).containsExactly(VERSION_PATH, JAR_PATH);
        assertThat(libFiles()).isEmpty();
    }

    @ParameterizedTest(name = "jar request answers HTTP {0}")
    @ValueSource(ints = {400, 403, 500, 502, 503})
    void otherHttpErrorsOnJarRequestAreReported(final int status) throws Exception {
        jarResponse = request -> new MockResponse().setResponseCode(status).setBody("PK-looking error page");

        assertFailed(download(url(), USER, PASSWORD), "Unexpected response", "HTTP " + status, "nothing was written");
        assertThat(libFiles()).isEmpty();
    }

    @Test
    void successWithoutBodyIsRejected() throws Exception {
        jarResponse = request -> new MockResponse().setResponseCode(200);

        assertFailed(download(url(), USER, PASSWORD), "Unexpected response", "HTTP 200");
        assertThat(libFiles()).isEmpty();
    }

    @ParameterizedTest(name = "200 with body [{0}]")
    @ValueSource(strings = {"<html><body>Login</body></html>", "P", "pk-lowercase"})
    void successWithoutZipMagicIsRejected(final String body) throws Exception {
        jarResponse = request -> new MockResponse().setBody(body);

        assertFailed(download(url(), USER, PASSWORD), "Unexpected response", "HTTP 200");
        assertThat(libFiles()).isEmpty();
    }

    @Test
    void connectionLostBeforeTheJarIsReported() throws Exception {
        jarResponse = request -> new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST);

        assertFailed(download(url(), USER, PASSWORD), "Could not reach", "curl exit");
        assertThat(libFiles()).isEmpty();
    }

    @Test
    void connectionLostDuringTheJarLeavesNoPartialFile() throws Exception {
        jarResponse = request -> new MockResponse().setBody(new Buffer().write(JAR_BYTES)).setSocketPolicy(SocketPolicy.DISCONNECT_DURING_RESPONSE_BODY);

        assertFailed(download(url(), USER, PASSWORD), "Could not reach");
        assertThat(libFiles()).as("neither a *.jar nor the temporary file remains").isEmpty();
    }

    @ParameterizedTest(name = "connection lost keeps the old jar, with force: {0}")
    @ValueSource(booleans = {false, true})
    void connectionLostDuringTheJarKeepsOldJar(final boolean force) throws Exception {
        Files.write(lib().resolve("fs-isolated-runtime-old.jar"), new byte[]{'P', 'K', 1});
        jarResponse = request -> new MockResponse().setBody(new Buffer().write(JAR_BYTES)).setSocketPolicy(SocketPolicy.DISCONNECT_DURING_RESPONSE_BODY);

        assertFailed(force ? download(url(), USER, PASSWORD, "--force") : download(url(), USER, PASSWORD), "Could not reach");
        assertThat(libFiles()).containsExactly("fs-isolated-runtime-old.jar");
    }

    // ---------------------------------------------------------------------------------------------------------------
    // helpers
    // ---------------------------------------------------------------------------------------------------------------

    final ScriptResult download(final String... args) throws Exception {
        return download(Map.of(), args);
    }

    final ScriptResult download(final Map<String, String> env, final String... args) throws Exception {
        final List<String> command = new ArrayList<>();
        command.add("download");
        command.addAll(Arrays.asList(args));
        final Map<String, String> environment = new HashMap<>();
        // a bogus JAVA_HOME proves that the download mode does not touch Java; fsurl, fsuser and fspwd must come from the test only
        environment.put("JAVA_HOME", work.resolve("no-java-here").toString());
        environment.put("fsuser", null);
        environment.put("fsurl", null);
        environment.put("fspwd", null);
        environment.putAll(env);
        return flavour().run(script, work, command, environment);
    }

    /** base url of the mock server without trailing slash */
    final String url() {
        final String url = server.url("/").toString();
        return url.substring(0, url.length() - 1);
    }

    final Path lib() {
        return dist.resolve("lib");
    }

    /** names of all entries in lib, sorted */
    final List<String> libFiles() throws IOException {
        try (Stream<Path> files = Files.list(lib())) {
            return files.map(p -> p.getFileName().toString()).sorted().collect(Collectors.toList());
        }
    }

    private static String basic(final String user, final String password) {
        return "Basic " + Base64.getEncoder().encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8));
    }

    private static int freePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private static byte[] jarBytes(final int size) {
        final byte[] bytes = new byte[size];
        new Random(714).nextBytes(bytes);
        bytes[0] = 'P';
        bytes[1] = 'K';
        bytes[2] = 3;
        bytes[3] = 4;
        bytes[10] = 0;
        bytes[11] = '\r';
        bytes[12] = '\n';
        bytes[13] = 0x1A; // CTRL-Z, which text-mode readers on Windows treat as end of file
        return bytes;
    }

    private void assertSuccess(final ScriptResult result) throws IOException {
        assertThat(result.exitCode()).as(result.toString()).isZero();
        assertThat(libFiles()).as("no temporary file left").noneMatch(name -> name.endsWith(".download"));
    }

    private void assertFailed(final ScriptResult result, final String... messages) throws IOException {
        assertThat(result.exitCode()).as(result.toString()).isEqualTo(1);
        assertThat(result.stderr()).as(result.toString()).contains(messages);
        assertThat(result.stdout()).as("errors go to stderr").doesNotContain("Downloaded ");
        assertThat(libFiles()).as("no temporary file left").noneMatch(name -> name.endsWith(".download"));
    }
}
