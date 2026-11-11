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
import de.espirit.firstspirit.access.store.VersionMismatchException;
import de.espirit.firstspirit.common.ConnectError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class RuntimeJarAutoUpdateTest {

	@TempDir
	Path _tempDir;

	@Test
	public void extractVersion_stripsRevisionSuffix() {
		assertThat(RuntimeJarAutoUpdate.extractVersion("5.2.260513.fc8c53d")).isEqualTo("5.2.260513");
	}

	@Test
	public void extractVersion_plainVersion() {
		assertThat(RuntimeJarAutoUpdate.extractVersion("5.2.230106")).isEqualTo("5.2.230106");
	}

	@Test
	public void extractVersion_trimsWhitespace() {
		assertThat(RuntimeJarAutoUpdate.extractVersion(" 5.2.260513 ")).isEqualTo("5.2.260513");
	}

	@Test
	public void extractVersion_unparsableVersionIsSanitized() {
		assertThat(RuntimeJarAutoUpdate.extractVersion("weird/version\\string")).isEqualTo("weirdversionstring");
	}

	@Test
	public void extractVersion_nullVersion() {
		assertThat(RuntimeJarAutoUpdate.extractVersion(null)).isEqualTo("unknown");
	}

	@Test
	public void findVersionMismatch_directException() {
		final VersionMismatchException mismatch = new VersionMismatchException("mismatch", "5.2.230106", "5.2.260513.fc8c53d");
		assertThat(RuntimeJarAutoUpdate.findVersionMismatch(mismatch)).isSameAs(mismatch);
	}

	@Test
	public void findVersionMismatch_nestedCause() {
		final VersionMismatchException mismatch = new VersionMismatchException("mismatch", "5.2.230106", "5.2.260513.fc8c53d");
		final ConnectError connectError = new ConnectError("connect failed", mismatch);
		final RuntimeException wrapped = new RuntimeException(connectError);
		assertThat(RuntimeJarAutoUpdate.findVersionMismatch(wrapped)).isSameAs(mismatch);
		assertThat(RuntimeJarAutoUpdate.isVersionMismatch(wrapped)).isTrue();
	}

	@Test
	public void findVersionMismatch_noMismatch() {
		assertThat(RuntimeJarAutoUpdate.findVersionMismatch(new IOException("some other error"))).isNull();
		assertThat(RuntimeJarAutoUpdate.isVersionMismatch(new IOException("some other error"))).isFalse();
	}

	@Test
	public void findVersionMismatch_circularCauseChainTerminates() {
		final RuntimeException first = new RuntimeException("first");
		final RuntimeException second = new RuntimeException("second", first);
		first.initCause(second);
		assertThat(RuntimeJarAutoUpdate.findVersionMismatch(second)).isNull();
	}

	@Test
	public void parseBuildNumber_variants() {
		assertThat(RuntimeJarAutoUpdate.parseBuildNumber("5.2.260513.fc8c53d")).isEqualTo(260513L);
		assertThat(RuntimeJarAutoUpdate.parseBuildNumber("5.2.230817")).isEqualTo(230817L);
		assertThat(RuntimeJarAutoUpdate.parseBuildNumber("no-version")).isNull();
		assertThat(RuntimeJarAutoUpdate.parseBuildNumber(null)).isNull();
	}

	@Test
	public void findBridgeJar_prefersJarClosestToServerVersion() throws IOException {
		Files.createFile(_tempDir.resolve("fs-isolated-runtime-5.2.230106.jar"));
		Files.createFile(_tempDir.resolve("fs-isolated-runtime-5.2.250311.jar"));
		Files.createFile(_tempDir.resolve("fs-isolated-runtime-5.2.260712.jar"));

		// server 230817, current runtime 260712 --> 250311 is closer, but 230106 is closest
		final Path bridge = RuntimeJarAutoUpdate.findBridgeJar(_tempDir, "5.2.230817", "5.2.260712.0bedf1d");
		assertThat(bridge).isNotNull();
		assertThat(bridge.getFileName().toString()).isEqualTo("fs-isolated-runtime-5.2.230106.jar");
	}

	@Test
	public void findBridgeJar_returnsNullIfCurrentJarIsAlreadyClosest() throws IOException {
		Files.createFile(_tempDir.resolve("fs-isolated-runtime-5.2.250311.jar"));
		Files.createFile(_tempDir.resolve("fs-isolated-runtime-5.2.260712.jar"));

		// current runtime 250311 is already the closest to server 230817
		assertThat(RuntimeJarAutoUpdate.findBridgeJar(_tempDir, "5.2.230817", "5.2.250311")).isNull();
	}

	@Test
	public void findBridgeJar_ignoresCurrentVersionAndUnparsableFiles() throws IOException {
		Files.createFile(_tempDir.resolve("fs-isolated-runtime-5.2.260712.jar"));
		Files.createFile(_tempDir.resolve("fs-isolated-runtime-weird.jar"));
		Files.createFile(_tempDir.resolve("other.jar"));

		// only the currently loaded version exists --> no bridge
		assertThat(RuntimeJarAutoUpdate.findBridgeJar(_tempDir, "5.2.230817", "5.2.260712")).isNull();
	}

	@Test
	public void findBridgeJar_unparsableVersions() {
		assertThat(RuntimeJarAutoUpdate.findBridgeJar(_tempDir, null, "5.2.260712")).isNull();
		assertThat(RuntimeJarAutoUpdate.findBridgeJar(_tempDir, "5.2.230817", null)).isNull();
	}

	@Test
	public void useJarEndpoint_serverSince2026_11() {
		assertThat(RuntimeJarAutoUpdate.useJarEndpoint(FsConnectionType.HTTP, 261100L)).isTrue();
		assertThat(RuntimeJarAutoUpdate.useJarEndpoint(FsConnectionType.HTTPS, 261105L)).isTrue();
		assertThat(RuntimeJarAutoUpdate.useJarEndpoint(FsConnectionType.HTTP, 270101L)).isTrue();
	}

	@Test
	public void useJarEndpoint_olderServersUseTheFirstSpiritProtocol() {
		assertThat(RuntimeJarAutoUpdate.useJarEndpoint(FsConnectionType.HTTP, 261011L)).isFalse();
		assertThat(RuntimeJarAutoUpdate.useJarEndpoint(FsConnectionType.HTTP, 260513L)).isFalse();
		assertThat(RuntimeJarAutoUpdate.useJarEndpoint(FsConnectionType.HTTPS, 230106L)).isFalse();
	}

	@Test
	public void useJarEndpoint_requiresHttpConnection() {
		assertThat(RuntimeJarAutoUpdate.useJarEndpoint(FsConnectionType.SOCKET, 261100L)).isFalse();
		assertThat(RuntimeJarAutoUpdate.useJarEndpoint(null, 261100L)).isFalse();
	}

	@Test
	public void useJarEndpoint_unknownServerBuild() {
		assertThat(RuntimeJarAutoUpdate.useJarEndpoint(FsConnectionType.HTTP, null)).isFalse();
	}

}
