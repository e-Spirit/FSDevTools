package com.espirit.moddev.cli.update;

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

import com.espirit.moddev.cli.api.configuration.Config;
import com.espirit.moddev.connection.FsConnectionType;
import de.espirit.common.VersionManager;
import de.espirit.firstspirit.access.store.VersionMismatchException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.IdentityHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles a {@link VersionMismatchException} by downloading the matching fs-isolated-runtime jar
 * from the server into the {@code api-lib} directory of the cli installation and relaunching the
 * cli with the downloaded jar on the classpath.
 * <p>
 * FirstSpirit requires the runtime jar of a client to exactly match the server version. Instead of
 * failing with a version mismatch, the cli keeps one runtime jar per server version in the
 * {@code api-lib} directory (e.g. {@code api-lib/fs-isolated-runtime-5.2.260513.jar}) and transparently
 * restarts itself with the matching jar. Jars that were downloaded once are reused on the next mismatch.
 * <p>
 * The auto update is enabled by default and can be disabled with the system property
 * {@code -Dfs-cli.autoUpdate=false} or the environment variable {@code FS_CLI_AUTO_UPDATE=false}.
 */
public final class RuntimeJarAutoUpdate {

	private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeJarAutoUpdate.class);

	/**
	 * System property to disable the automatic runtime update ({@code -Dfs-cli.autoUpdate=false}).
	 */
	public static final String AUTO_UPDATE_PROPERTY = "fs-cli.autoUpdate";

	/**
	 * Environment variable to disable the automatic runtime update ({@code FS_CLI_AUTO_UPDATE=false}).
	 */
	public static final String AUTO_UPDATE_ENVIRONMENT_VARIABLE = "FS_CLI_AUTO_UPDATE";

	/**
	 * System property to override the location of the {@code api-lib} directory. Defaults to the
	 * {@code api-lib} directory next to the {@code lib} directory of the cli installation.
	 */
	public static final String API_LIB_DIR_PROPERTY = "fs-cli.apiLibDir";

	private static final String DEFAULT_MAIN_CLASS = "com.espirit.moddev.cli.Main";
	private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+\\.\\d+\\.\\d+).*");
	private static final Pattern API_LIB_JAR_PATTERN = Pattern.compile("(?i)fs-isolated-runtime-(.+)\\.jar");
	private static final int MAX_RELAUNCH_DEPTH = 3;

	/**
	 * First FirstSpirit build (version 2026.11) whose server serves the runtime jar via the http
	 * endpoint {@value RuntimeJarDownloader#JAR_ENDPOINT_PATH}. Older servers are handled via
	 * {@link RuntimeJarDownloader#download(String, int, FsConnectionType, String, Path)}.
	 */
	@VisibleForTesting
	static final long MIN_JAR_ENDPOINT_BUILD = 261100;

	private RuntimeJarAutoUpdate() {
	}

	/**
	 * Handles a version mismatch: if the given throwable (or one of its causes) is a
	 * {@link VersionMismatchException} and the auto update is applicable, this method downloads the
	 * matching runtime jar (if it is not already present in the {@code api-lib} directory), relaunches
	 * the cli with the downloaded jar and terminates the current process with the exit code of the
	 * relaunched process. In every other case this method simply returns.
	 *
	 * @param throwable the throwable to inspect
	 * @param config    the configuration of the executed command (provides host, port and connection mode)
	 * @param args      the original program arguments used to relaunch the cli
	 */
	public static void handleVersionMismatch(@NotNull final Throwable throwable, @Nullable final Config config, @NotNull final String[] args) {
		final VersionMismatchException mismatch = findVersionMismatch(throwable);
		if (mismatch == null || config == null) {
			return;
		}
		if (!isEnabled()) {
			LOGGER.info("Automatic runtime update is disabled ('{}'/'{}') - not updating the FirstSpirit runtime jar.", AUTO_UPDATE_PROPERTY, AUTO_UPDATE_ENVIRONMENT_VARIABLE);
			return;
		}
		if (CliRelauncher.getRelaunchDepth() >= MAX_RELAUNCH_DEPTH) {
			LOGGER.error("The version mismatch persists after {} automatic relaunches - aborting to avoid a relaunch loop. Please verify that the '{}' in the 'data/fslib' directory of the server matches the server version. ({})", CliRelauncher.getRelaunchDepth(), RuntimeJarDownloader.RUNTIME_JAR_NAME, mismatch.getMessage());
			return;
		}
		LOGGER.info("The FirstSpirit runtime jar (version {}) does not match the server version {} - starting automatic runtime update...", extractVersion(mismatch.getClientVersion()), extractVersion(mismatch.getServerVersion()));
		updateAndRelaunch(mismatch.getServerVersion(), config, args);
	}

	/**
	 * Checks the version of the FirstSpirit server BEFORE a connection is established: for HTTP(S)
	 * connections the server exposes its version at {@code http(s)://<host>:<port>/version.txt}.
	 * If the version differs from the version of the runtime jar of the current process, this method
	 * relaunches the cli with the matching runtime jar from the {@code api-lib} directory (downloading
	 * it first if it is not present) and terminates the current process with the exit code of the
	 * relaunched process. In every other case (matching versions, socket connections, unreachable
	 * version endpoint, auto update disabled, ...) this method simply returns and the normal connect
	 * (with the {@link #handleVersionMismatch(Throwable, Config, String[]) mismatch handling} as
	 * fallback) takes over.
	 *
	 * @param config the configuration of the command to execute (provides host, port and connection mode)
	 * @param args   the original program arguments used to relaunch the cli
	 */
	public static void preSelectRuntimeJar(@Nullable final Config config, @NotNull final String[] args) {
		try {
			if (config == null || !isEnabled() || CliRelauncher.getRelaunchDepth() >= MAX_RELAUNCH_DEPTH) {
				return;
			}
			final FsConnectionType connectionMode = config.getConnectionMode();
			if (FsConnectionType.HTTP != connectionMode && FsConnectionType.HTTPS != connectionMode) {
				// version.txt is only reachable on the http port
				return;
			}
			if (getApiLibDir() == null) {
				// not running from an installation with an api-lib directory
				return;
			}
			final String serverVersion = fetchServerVersion(connectionMode, config.getHost(), config.getPort());
			if (serverVersion == null) {
				return;
			}
			final String runtimeVersion = getRuntimeJarVersion();
			if (runtimeVersion == null || extractVersion(runtimeVersion).equals(extractVersion(serverVersion))) {
				return;
			}
			LOGGER.info("The server at '{}:{}' reports version {} (version.txt), the current FirstSpirit runtime jar is {} - switching runtime jar...", config.getHost(), config.getPort(), extractVersion(serverVersion), extractVersion(runtimeVersion));
			updateAndRelaunch(serverVersion, config, args);
		} catch (final RuntimeException | LinkageError exception) {
			// never break the normal flow - the version mismatch handling acts as fallback
			LOGGER.debug("Unable to pre-select the runtime jar.", exception);
		}
	}

	/**
	 * Downloads the runtime jar for the given server version into the {@code api-lib} directory (if
	 * it is not already present), relaunches the cli with that jar and terminates the current process
	 * with the exit code of the relaunched process. Returns without relaunching if the {@code api-lib}
	 * directory cannot be determined or the download fails.
	 */
	private static void updateAndRelaunch(@Nullable final String serverVersionString, @NotNull final Config config, @NotNull final String[] args) {
		final Path apiLibDir = getApiLibDir();
		if (apiLibDir == null) {
			LOGGER.info("Unable to determine the 'api-lib' directory of this installation - not updating the FirstSpirit runtime jar. Use '-D{}=<path>' to set the directory explicitly.", API_LIB_DIR_PROPERTY);
			return;
		}
		final String serverVersion = extractVersion(serverVersionString);
		final Path targetJar = apiLibDir.resolve("fs-isolated-runtime-" + serverVersion + ".jar");
		try {
			if (Files.isRegularFile(targetJar)) {
				LOGGER.info("Matching runtime jar already present: '{}'", targetJar);
			} else if (useJarEndpoint(config.getConnectionMode(), parseBuildNumber(serverVersion))) {
				// servers since FirstSpirit 2026.11 serve the runtime jar via a plain http endpoint -
				// no FirstSpirit protocol involved, so any runtime jar version can perform the download
				LOGGER.info("Downloading runtime jar for server version {} from '{}:{}{}'...", serverVersion, config.getHost(), config.getPort(), RuntimeJarDownloader.JAR_ENDPOINT_PATH);
				new RuntimeJarDownloader().downloadFromJarEndpoint(config.getHost(), config.getPort(), config.getConnectionMode(), config.getUser(), config.getPassword(), targetJar);
			} else {
				// older servers: the download runs over the FirstSpirit protocol and therefore needs a
				// runtime jar whose low-level protocol is compatible with the server (e.g. the connection
				// handshake or the compression may change over the years). If the api-lib directory
				// contains a jar whose version is closer to the server version than the currently loaded
				// one, relaunch with that jar first and let it perform the download ("bridge" jar).
				final Path bridgeJar = findBridgeJar(apiLibDir, serverVersion, getRuntimeJarVersion());
				if (bridgeJar != null) {
					LOGGER.info("Using '{}' to download the runtime jar - its version is closer to the server version {} than the current runtime jar.", bridgeJar.getFileName(), serverVersion);
					relaunch(bridgeJar, args);
					return; // relaunch failed - the error has already been logged
				}
				LOGGER.info("Downloading runtime jar for server version {} from '{}:{}'...", serverVersion, config.getHost(), config.getPort());
				new RuntimeJarDownloader().download(config.getHost(), config.getPort(), config.getConnectionMode(), config.getServletZone(), targetJar);
			}
			relaunch(targetJar, args);
		} catch (final Exception | LinkageError exception) {
			LOGGER.error("Automatic runtime update failed - please update the FirstSpirit runtime jar in the 'api-lib' directory manually (server version: {}).", serverVersion, exception);
		}
	}

	/**
	 * Relaunches the cli with the given runtime jar and terminates the current process with the exit
	 * code of the relaunched process. Only returns if the relaunch itself failed.
	 */
	private static void relaunch(@NotNull final Path runtimeJar, @NotNull final String[] args) {
		try {
			LOGGER.info("Restarting fs-cli with runtime jar '{}'...", runtimeJar);
			final int exitCode = new CliRelauncher().relaunch(runtimeJar, getMainClass(), args);
			System.exit(exitCode);
		} catch (final InterruptedException exception) {
			Thread.currentThread().interrupt();
			LOGGER.error("Automatic runtime update was interrupted.", exception);
		} catch (final Exception | LinkageError exception) {
			LOGGER.error("Unable to restart fs-cli with runtime jar '{}'.", runtimeJar, exception);
		}
	}

	/**
	 * Returns whether the runtime jar should be downloaded via the http endpoint
	 * {@value RuntimeJarDownloader#JAR_ENDPOINT_PATH}: the endpoint exists since FirstSpirit 2026.11
	 * (build {@value #MIN_JAR_ENDPOINT_BUILD}) and is only reachable for HTTP(S) connections.
	 * For older servers (or unparsable versions) the FirstSpirit protocol based download is used.
	 */
	@VisibleForTesting
	static boolean useJarEndpoint(@Nullable final FsConnectionType connectionMode, @Nullable final Long serverBuild) {
		if (FsConnectionType.HTTP != connectionMode && FsConnectionType.HTTPS != connectionMode) {
			return false;
		}
		return serverBuild != null && serverBuild >= MIN_JAR_ENDPOINT_BUILD;
	}

	/**
	 * Searches the {@code api-lib} directory for a runtime jar whose version is strictly closer to
	 * the given server version than the version of the currently loaded runtime jar. Returns
	 * {@code null} if there is no such jar (or the versions cannot be compared).
	 */
	@VisibleForTesting
	@Nullable
	static Path findBridgeJar(@NotNull final Path apiLibDir, @Nullable final String serverVersion, @Nullable final String currentVersion) {
		final Long serverBuild = parseBuildNumber(serverVersion);
		final Long currentBuild = parseBuildNumber(currentVersion);
		if (serverBuild == null || currentBuild == null || !Files.isDirectory(apiLibDir)) {
			return null;
		}
		Path bestJar = null;
		long bestDistance = Math.abs(currentBuild - serverBuild);
		try (final java.util.stream.Stream<Path> files = Files.list(apiLibDir)) {
			for (final Path file : files.filter(Files::isRegularFile).sorted().toList()) {
				final Matcher matcher = API_LIB_JAR_PATTERN.matcher(file.getFileName().toString());
				if (!matcher.matches()) {
					continue;
				}
				final Long build = parseBuildNumber(matcher.group(1));
				if (build == null || build.equals(currentBuild)) {
					continue;
				}
				final long distance = Math.abs(build - serverBuild);
				if (distance < bestDistance) {
					bestDistance = distance;
					bestJar = file;
				}
			}
		} catch (final java.io.IOException exception) {
			LOGGER.debug("Unable to scan the 'api-lib' directory '{}'.", apiLibDir, exception);
			return null;
		}
		return bestJar;
	}

	/**
	 * Extracts the numeric build number (the last segment) from a FirstSpirit version string
	 * (e.g. {@code 5.2.260513.fc8c53d} --> {@code 260513}). Returns {@code null} if the version
	 * cannot be parsed.
	 */
	@VisibleForTesting
	@Nullable
	static Long parseBuildNumber(@Nullable final String versionString) {
		if (versionString == null) {
			return null;
		}
		final String version = extractVersion(versionString);
		final int lastDot = version.lastIndexOf('.');
		final String buildPart = lastDot == -1 ? version : version.substring(lastDot + 1);
		try {
			return Long.parseLong(buildPart);
		} catch (final NumberFormatException exception) {
			return null;
		}
	}

	/**
	 * Fetches the version of a FirstSpirit server from its {@code /version.txt} endpoint.
	 * Best-effort: returns {@code null} if the endpoint is not reachable within a short timeout.
	 */
	@VisibleForTesting
	@Nullable
	static String fetchServerVersion(@NotNull final FsConnectionType connectionMode, @NotNull final String host, final int port) {
		final String scheme = FsConnectionType.HTTPS == connectionMode ? "https" : "http";
		try {
			final URL url = new URL(scheme + "://" + host + ":" + port + "/version.txt");
			final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			try {
				connection.setConnectTimeout(2000);
				connection.setReadTimeout(2000);
				connection.setRequestProperty("User-Agent", "fs-cli");
				final int responseCode = connection.getResponseCode();
				if (responseCode >= 300 && responseCode < 400) {
					// http --> https redirects are not followed automatically - give the user a hint,
					// because the FirstSpirit connect will fail on the plain http port as well
					final String location = connection.getHeaderField("Location");
					if (FsConnectionType.HTTP == connectionMode && location != null && location.toLowerCase(java.util.Locale.ROOT).startsWith("https:")) {
						LOGGER.warn("The server at '{}:{}' redirects to '{}' - it is most likely only reachable via HTTPS (use '-c HTTPS' and the https port, usually 443).", host, port, location);
					}
					return null;
				}
				if (responseCode != HttpURLConnection.HTTP_OK) {
					return null;
				}
				try (final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
					final String line = reader.readLine();
					return (line == null || line.isBlank()) ? null : line.trim();
				}
			} finally {
				connection.disconnect();
			}
		} catch (final Exception exception) {
			LOGGER.debug("Unable to fetch '/version.txt' from '{}:{}'.", host, port, exception);
			return null;
		}
	}

	/**
	 * Returns the version of the FirstSpirit runtime jar of the current process (or {@code null} if
	 * it cannot be determined).
	 */
	@Nullable
	private static String getRuntimeJarVersion() {
		try {
			return VersionManager.getVersion();
		} catch (final Exception | LinkageError exception) {
			LOGGER.debug("Unable to determine the version of the FirstSpirit runtime jar.", exception);
			return null;
		}
	}

	/**
	 * Returns whether the given throwable (or one of its causes) is a {@link VersionMismatchException}.
	 *
	 * @param throwable the throwable to inspect
	 * @return {@code true} if the throwable indicates a version mismatch
	 */
	public static boolean isVersionMismatch(@NotNull final Throwable throwable) {
		return findVersionMismatch(throwable) != null;
	}

	@VisibleForTesting
	@Nullable
	static VersionMismatchException findVersionMismatch(@NotNull final Throwable throwable) {
		final IdentityHashMap<Throwable, Boolean> seen = new IdentityHashMap<>();
		Throwable current = throwable;
		while (current != null && seen.put(current, Boolean.TRUE) == null) {
			if (current instanceof VersionMismatchException) {
				return (VersionMismatchException) current;
			}
			current = current.getCause();
		}
		return null;
	}

	private static boolean isEnabled() {
		final String property = System.getProperty(AUTO_UPDATE_PROPERTY);
		if (property != null) {
			return !"false".equalsIgnoreCase(property.trim());
		}
		final String environment = System.getenv(AUTO_UPDATE_ENVIRONMENT_VARIABLE);
		if (environment != null) {
			return !"false".equalsIgnoreCase(environment.trim());
		}
		return true;
	}

	/**
	 * Extracts the plain version from a FirstSpirit version string: the revision suffix of the server
	 * version (e.g. {@code 5.2.260513.fc8c53d}) is stripped, so the result is suitable as part of a
	 * file name (e.g. {@code 5.2.260513}).
	 */
	@VisibleForTesting
	@NotNull
	static String extractVersion(@Nullable final String versionString) {
		if (versionString == null) {
			return "unknown";
		}
		final Matcher matcher = VERSION_PATTERN.matcher(versionString.trim());
		if (matcher.matches()) {
			return matcher.group(1);
		}
		// fallback: strip everything that is not a word character, digit, dot or dash
		final String sanitized = versionString.trim().replaceAll("[^\\w.\\-]", "");
		return sanitized.isEmpty() ? "unknown" : sanitized;
	}

	/**
	 * Determines the {@code api-lib} directory of this cli installation: the directory next to the
	 * directory containing the jar this class was loaded from (usually {@code <fs-cli>/lib/fsdevtools-cli-<version>.jar}
	 * --> {@code <fs-cli>/api-lib}). Returns {@code null} if this class was not loaded from a jar file
	 * (e.g. in unit tests) and the system property {@value #API_LIB_DIR_PROPERTY} is not set.
	 */
	@Nullable
	private static Path getApiLibDir() {
		final String override = System.getProperty(API_LIB_DIR_PROPERTY);
		if (override != null && !override.isBlank()) {
			return Paths.get(override);
		}
		try {
			final CodeSource codeSource = RuntimeJarAutoUpdate.class.getProtectionDomain().getCodeSource();
			if (codeSource == null || codeSource.getLocation() == null) {
				return null;
			}
			final Path location = Paths.get(codeSource.getLocation().toURI());
			if (!Files.isRegularFile(location) || !location.getFileName().toString().toLowerCase().endsWith(".jar")) {
				// not started from a jar file (IDE, unit tests, ...)
				return null;
			}
			final Path libDir = location.toAbsolutePath().getParent();
			if (libDir == null || libDir.getParent() == null) {
				return null;
			}
			return libDir.getParent().resolve("api-lib");
		} catch (final URISyntaxException | RuntimeException exception) {
			LOGGER.debug("Unable to determine the location of the cli installation.", exception);
			return null;
		}
	}

	@NotNull
	private static String getMainClass() {
		// "sun.java.command" contains the main class followed by the program arguments
		final String javaCommand = System.getProperty("sun.java.command", "");
		final String mainClass = javaCommand.isBlank() ? "" : javaCommand.split("\\s+")[0];
		// only use it if it is a class name (not a jar file passed via "java -jar")
		if (!mainClass.isBlank() && !mainClass.toLowerCase().endsWith(".jar")) {
			return mainClass;
		}
		return DEFAULT_MAIN_CLASS;
	}

}
