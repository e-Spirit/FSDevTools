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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CliRelauncherTest {

	@TempDir
	Path _tempDir;

	@Test
	public void buildClassPath_expandsWildcardAndReplacesRuntimeJar() throws IOException {
		// setup: a lib dir with a regular jar, a runtime jar and a non-jar file
		final Path libDir = Files.createDirectories(_tempDir.resolve("lib"));
		final Path cliJar = Files.createFile(libDir.resolve("fsdevtools-cli-4.8.7.jar"));
		Files.createFile(libDir.resolve("fs-isolated-runtime-5.2.230106.jar"));
		Files.createFile(libDir.resolve("readme.txt"));
		final Path newRuntimeJar = _tempDir.resolve("api-lib").resolve("fs-isolated-runtime-5.2.260513.jar");

		final String classPath = libDir + File.separator + "*";
		final List<String> result = Arrays.asList(CliRelauncher.buildClassPath(classPath, newRuntimeJar).split(File.pathSeparator));

		assertThat(result).containsExactly(cliJar.toAbsolutePath().toString(), newRuntimeJar.toAbsolutePath().toString());
	}

	@Test
	public void buildClassPath_removesExplicitRuntimeJarEntries() {
		final Path oldRuntimeJar = _tempDir.resolve("api-lib").resolve("fs-isolated-runtime-5.2.230106.jar");
		final Path otherJar = _tempDir.resolve("other.jar");
		final Path newRuntimeJar = _tempDir.resolve("api-lib").resolve("fs-isolated-runtime-5.2.260513.jar");

		final String classPath = otherJar + File.pathSeparator + oldRuntimeJar;
		final List<String> result = Arrays.asList(CliRelauncher.buildClassPath(classPath, newRuntimeJar).split(File.pathSeparator));

		assertThat(result).containsExactly(otherJar.toString(), newRuntimeJar.toAbsolutePath().toString());
	}

	@Test
	public void buildClassPath_keepsNonExistingWildcardDirectory() {
		// a wildcard for a directory that does not exist simply vanishes (nothing to expand)
		final Path newRuntimeJar = _tempDir.resolve("fs-isolated-runtime-5.2.260513.jar");
		final String classPath = _tempDir.resolve("does-not-exist") + File.separator + "*";

		final List<String> result = Arrays.asList(CliRelauncher.buildClassPath(classPath, newRuntimeJar).split(File.pathSeparator));

		assertThat(result).containsExactly(newRuntimeJar.toAbsolutePath().toString());
	}

	@Test
	public void buildClassPath_emptyClassPath() {
		final Path newRuntimeJar = _tempDir.resolve("fs-isolated-runtime-5.2.260513.jar");
		final List<String> result = Arrays.asList(CliRelauncher.buildClassPath("", newRuntimeJar).split(File.pathSeparator));
		assertThat(result).containsExactly(newRuntimeJar.toAbsolutePath().toString());
	}

	@Test
	public void buildClassPath_caseInsensitiveRuntimeJarDetection() {
		final Path oldRuntimeJar = _tempDir.resolve("FS-ISOLATED-RUNTIME-5.2.230106.JAR");
		final Path newRuntimeJar = _tempDir.resolve("fs-isolated-runtime-5.2.260513.jar");

		final List<String> result = Arrays.asList(CliRelauncher.buildClassPath(oldRuntimeJar.toString(), newRuntimeJar).split(File.pathSeparator));

		assertThat(result).containsExactly(newRuntimeJar.toAbsolutePath().toString());
	}

}
