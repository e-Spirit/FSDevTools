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

import com.espirit.moddev.connection.FsConnectionType;
import de.espirit.firstspirit.access.ConnectionManager;
import de.espirit.firstspirit.client.io.ServerCaller;
import de.espirit.firstspirit.io.FixedLengthInputStream;
import de.espirit.firstspirit.manager.ServerManager;
import de.espirit.firstspirit.server.RemoteManagerProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Base64;

/**
 * Downloads the {@code fs-isolated-runtime.jar} that matches the version of a FirstSpirit server.
 * <p>
 * Two mechanisms are supported, chosen by the caller based on the server version:
 * <ul>
 *     <li>{@link #downloadFromJarEndpoint(String, int, FsConnectionType, String, String, Path)} uses the
 *     http endpoint {@value #JAR_ENDPOINT_PATH} (available since FirstSpirit 2026.11, the same endpoint
 *     the {@code download} mode of the start scripts uses).</li>
 *     <li>{@link #download(String, int, FsConnectionType, String, Path)} works with older servers and
 *     uses the same low-level mechanism the FirstSpirit clients use to update themselves:
 *     a raw {@link ServerCaller} is connected with {@code ignoreVersionMismatch} (so a runtime jar of a
 *     different version can still talk to the server) and the jar is fetched via
 *     {@link ServerManager#getClientJar(String)}, which requires no authentication and serves the files
 *     located in the {@code data/fslib} directory of the server.</li>
 * </ul>
 */
public class RuntimeJarDownloader {

	private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeJarDownloader.class);

	/**
	 * Name of the runtime jar in the {@code data/fslib} directory of the FirstSpirit server.
	 */
	public static final String RUNTIME_JAR_NAME = "fs-isolated-runtime.jar";

	/**
	 * Path of the http endpoint that serves the runtime jar on FirstSpirit servers since
	 * version 2026.11 (same endpoint the {@code download} mode of the start scripts uses).
	 */
	public static final String JAR_ENDPOINT_PATH = "/jar/" + RUNTIME_JAR_NAME;

	/**
	 * Downloads the runtime jar of the given server to {@code targetFile} via the http endpoint
	 * {@value #JAR_ENDPOINT_PATH} (available since FirstSpirit 2026.11, authenticated via http basic
	 * auth). Like {@link #download(String, int, FsConnectionType, String, Path)}, the file is first
	 * written to a temporary {@code *.part} file and moved to {@code targetFile} after a successful
	 * download, so an aborted download never leaves a half-written jar behind.
	 *
	 * @param host           host of the FirstSpirit server
	 * @param port           port of the FirstSpirit server
	 * @param connectionType the connection type to use ({@link FsConnectionType#HTTP} or {@link FsConnectionType#HTTPS})
	 * @param user           the user for the http basic auth
	 * @param password       the password for the http basic auth
	 * @param targetFile     the file to write the runtime jar to
	 * @throws IOException if the server cannot be reached, the credentials are rejected or the server
	 *                     does not provide the runtime jar
	 */
	public void downloadFromJarEndpoint(@NotNull final String host, final int port, @NotNull final FsConnectionType connectionType, @Nullable final String user, @Nullable final String password, @NotNull final Path targetFile) throws IOException {
		final Path parentDir = targetFile.toAbsolutePath().getParent();
		if (parentDir != null) {
			Files.createDirectories(parentDir);
		}
		final Path partFile = targetFile.resolveSibling(targetFile.getFileName() + ".part");

		final String scheme = FsConnectionType.HTTPS == connectionType ? "https" : "http";
		final URL url = new URL(scheme + "://" + host + ":" + port + JAR_ENDPOINT_PATH);
		final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		try {
			connection.setInstanceFollowRedirects(false);
			connection.setConnectTimeout(60_000);
			connection.setReadTimeout(60_000);
			connection.setRequestProperty("User-Agent", "fs-cli");
			final String credentials = ("" + user) + ':' + ("" + password);
			connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8)));
			final int responseCode = connection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED || responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
				throw new IOException(String.format("The server at '%s' rejected the credentials of user '%s' (HTTP %d).", url, user, responseCode));
			}
			if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
				throw new IOException(String.format("The server at '%s' does not provide the runtime jar endpoint (HTTP 404) - it requires FirstSpirit 2026.11 or later.", url));
			}
			if (responseCode != HttpURLConnection.HTTP_OK) {
				throw new IOException(String.format("Unexpected response from '%s' (HTTP %d).", url, responseCode));
			}
			try (final InputStream jarStream = connection.getInputStream()) {
				Files.copy(jarStream, partFile, StandardCopyOption.REPLACE_EXISTING);
			}
			// cheap validation that the response is a jar and not an error page: zip files start with "PK"
			try (final InputStream partStream = Files.newInputStream(partFile)) {
				final byte[] header = partStream.readNBytes(2);
				if (header.length < 2 || header[0] != 'P' || header[1] != 'K') {
					throw new IOException(String.format("The response from '%s' is not a jar file.", url));
				}
			}
		} catch (final IOException exception) {
			Files.deleteIfExists(partFile);
			throw exception;
		} finally {
			connection.disconnect();
		}

		Files.move(partFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
		LOGGER.info("Downloaded '{}' from '{}' to '{}' ({} bytes).", RUNTIME_JAR_NAME, url, targetFile, Files.size(targetFile));
	}

	/**
	 * Downloads the runtime jar of the given server to {@code targetFile}. The file is first written
	 * to a temporary {@code *.part} file and moved to {@code targetFile} after a successful download,
	 * so an aborted download never leaves a half-written jar behind.
	 *
	 * @param host           host of the FirstSpirit server
	 * @param port           port of the FirstSpirit server
	 * @param connectionType the connection type to use
	 * @param servletZone    the servlet zone (may be {@code null} for the default zone)
	 * @param targetFile     the file to write the runtime jar to
	 * @throws IOException if the server cannot be reached or does not provide the runtime jar
	 */
	public void download(@NotNull final String host, final int port, @NotNull final FsConnectionType connectionType, @Nullable final String servletZone, @NotNull final Path targetFile) throws IOException {
		final Path parentDir = targetFile.toAbsolutePath().getParent();
		if (parentDir != null) {
			Files.createDirectories(parentDir);
		}
		final Path partFile = targetFile.resolveSibling(targetFile.getFileName() + ".part");

		ConnectionManager.setUseHttps(FsConnectionType.HTTPS == connectionType);
		// use uncompressed messages for the download: newer servers may use compression algorithms
		// this (older) runtime jar does not know (e.g. zstd) - COMPRESSION_NONE is understood by
		// every version (the jar content itself is already compressed anyway)
		ConnectionManager.setCompression(ConnectionManager.COMPRESSION_NONE);
		try (final ServerCaller caller = ConnectionManager.createCaller(host, port, connectionType.getFsMode(), servletZone)) {
			// NOTE: we deliberately do NOT use ServerCaller#connect here: this download is only ever
			// needed when the local runtime jar does NOT match the server version, and the late steps
			// of #connect (class loader/module manager setup) are known to fail across version
			// boundaries. Only the low-level transport (#doConnect) is required for #getClientJar -
			// this mirrors what the version check itself does, which is known to work across versions.
			openTransport(caller);
			final RemoteManagerProvider managerProvider = new RemoteManagerProvider(caller);
			final ServerManager serverManager = (ServerManager) managerProvider.getManager(ServerManager.class);
			// ServerManager#getClientJar is @NotAuthorized on the server --> no login required
			final FixedLengthInputStream inputStream = serverManager.getClientJar(RUNTIME_JAR_NAME);
			if (inputStream == null) {
				throw new IOException(String.format("The server at '%s:%d' did not provide '%s' - the file does not exist in the 'data/fslib' directory of the server.", host, port, RUNTIME_JAR_NAME));
			}
			try (final InputStream jarStream = inputStream) {
				Files.copy(jarStream, partFile, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (final IOException exception) {
			Files.deleteIfExists(partFile);
			throw exception;
		}

		Files.move(partFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
		LOGGER.info("Downloaded '{}' from '{}:{}' to '{}' ({} bytes).", RUNTIME_JAR_NAME, host, port, targetFile, Files.size(targetFile));
	}

	/**
	 * Opens the transport of the given caller via {@code ServerCaller#connect(true)} (ignoring the
	 * version mismatch). The late setup steps of {@code #connect} (class loader / module manager
	 * initialization) are known to fail when the client and server versions differ - at that point
	 * the transport is already open, so such errors are ignored as long as the caller reports a
	 * working connection afterwards. The low-level calls used for the download itself
	 * ({@code ServerManager#getClientJar}) are version-tolerant.
	 */
	private static void openTransport(@NotNull final ServerCaller caller) throws IOException {
		try {
			caller.connect(true);
		} catch (final RuntimeException | LinkageError exception) {
			if (!caller.isConnected() || !caller.testConnection()) {
				throw new IOException("Unable to connect to the server: " + exception, exception);
			}
			LOGGER.debug("Ignoring error during connection setup (expected for mismatching versions).", exception);
		}
	}

}
