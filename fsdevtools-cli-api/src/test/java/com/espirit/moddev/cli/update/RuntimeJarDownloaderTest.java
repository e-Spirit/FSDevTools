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
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RuntimeJarDownloaderTest {

	private static final byte[] FAKE_JAR = new byte[]{'P', 'K', 3, 4, 42, 42, 42};

	@TempDir
	Path _tempDir;

	private HttpServer _server;
	private final AtomicReference<String> _receivedAuthorization = new AtomicReference<>();

	@BeforeEach
	public void setUp() throws IOException {
		_server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		_server.start();
	}

	@AfterEach
	public void tearDown() {
		_server.stop(0);
	}

	private void serveJarEndpoint(final int statusCode, final byte[] body) {
		_server.createContext(RuntimeJarDownloader.JAR_ENDPOINT_PATH, exchange -> {
			_receivedAuthorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
			exchange.sendResponseHeaders(statusCode, body.length);
			try (final OutputStream responseBody = exchange.getResponseBody()) {
				responseBody.write(body);
			}
		});
	}

	private int port() {
		return _server.getAddress().getPort();
	}

	@Test
	public void downloadFromJarEndpoint_downloadsJarWithBasicAuth() throws IOException {
		serveJarEndpoint(200, FAKE_JAR);
		final Path targetFile = _tempDir.resolve("fs-isolated-runtime-5.2.261105.jar");

		new RuntimeJarDownloader().downloadFromJarEndpoint("127.0.0.1", port(), FsConnectionType.HTTP, "Admin", "secret", targetFile);

		assertThat(targetFile).exists();
		assertThat(Files.readAllBytes(targetFile)).isEqualTo(FAKE_JAR);
		final String expectedCredentials = java.util.Base64.getEncoder().encodeToString("Admin:secret".getBytes(StandardCharsets.UTF_8));
		assertThat(_receivedAuthorization.get()).isEqualTo("Basic " + expectedCredentials);
		assertThat(_tempDir.resolve(targetFile.getFileName() + ".part")).doesNotExist();
	}

	@Test
	public void downloadFromJarEndpoint_rejectedCredentials() {
		serveJarEndpoint(401, new byte[0]);
		final Path targetFile = _tempDir.resolve("fs-isolated-runtime-5.2.261105.jar");

		assertThatThrownBy(() -> new RuntimeJarDownloader().downloadFromJarEndpoint("127.0.0.1", port(), FsConnectionType.HTTP, "Admin", "wrong", targetFile))
				.isInstanceOf(IOException.class)
				.hasMessageContaining("rejected the credentials");
		assertThat(targetFile).doesNotExist();
	}

	@Test
	public void downloadFromJarEndpoint_endpointNotAvailable() {
		// no context registered --> the server answers with 404 (e.g. a FirstSpirit server before 2026.11)
		final Path targetFile = _tempDir.resolve("fs-isolated-runtime-5.2.261105.jar");

		assertThatThrownBy(() -> new RuntimeJarDownloader().downloadFromJarEndpoint("127.0.0.1", port(), FsConnectionType.HTTP, "Admin", "secret", targetFile))
				.isInstanceOf(IOException.class)
				.hasMessageContaining("FirstSpirit 2026.11");
		assertThat(targetFile).doesNotExist();
	}

	@Test
	public void downloadFromJarEndpoint_responseIsNotAJar() {
		serveJarEndpoint(200, "<html>login form</html>".getBytes(StandardCharsets.UTF_8));
		final Path targetFile = _tempDir.resolve("fs-isolated-runtime-5.2.261105.jar");

		assertThatThrownBy(() -> new RuntimeJarDownloader().downloadFromJarEndpoint("127.0.0.1", port(), FsConnectionType.HTTP, "Admin", "secret", targetFile))
				.isInstanceOf(IOException.class)
				.hasMessageContaining("not a jar file");
		assertThat(targetFile).doesNotExist();
		assertThat(_tempDir.resolve(targetFile.getFileName() + ".part")).doesNotExist();
	}

	@Test
	public void downloadFromJarEndpoint_replacesExistingFile() throws IOException {
		serveJarEndpoint(200, FAKE_JAR);
		final Path targetFile = _tempDir.resolve("fs-isolated-runtime-5.2.261105.jar");
		Files.write(targetFile, "old content".getBytes(StandardCharsets.UTF_8));

		new RuntimeJarDownloader().downloadFromJarEndpoint("127.0.0.1", port(), FsConnectionType.HTTP, "Admin", "secret", targetFile);

		assertThat(Files.readAllBytes(targetFile)).isEqualTo(FAKE_JAR);
	}

}
