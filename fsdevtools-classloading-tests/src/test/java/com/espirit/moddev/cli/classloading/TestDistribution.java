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

package com.espirit.moddev.cli.classloading;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * A private copy of the test distribution (see the {@code testDist} task) in its own directory, which runs the CLI
 * exactly like {@code fs-cli.sh}: a separate JVM with {@code -cp lib/*}. The directory name contains a space on purpose.
 */
final class TestDistribution {

	private static final String CLI_MAIN = "com.espirit.moddev.cli.Main";
	private static final String PROBE_MAIN = "com.espirit.moddev.cli.classloading.ProbeMain";
	private static final long TIMEOUT_SECONDS = 120;

	/**
	 * Output and exit code of one JVM run.
	 */
	record Run(int exitCode, String output) {
	}

	private final Path _workDir;
	private final Path _root;

	private TestDistribution(final Path workDir, final Path root) {
		_workDir = workDir;
		_root = root;
	}

	/**
	 * Copies the shared distribution to {@code <workDir>/fs cli}.
	 */
	static TestDistribution create(final Path workDir) throws IOException {
		final Path source = Path.of(System.getProperty("fscli.testDist"));
		final Path root = workDir.resolve("fs cli");
		try (final Stream<Path> files = Files.walk(source)) {
			for (final Path file : (Iterable<Path>) files::iterator) {
				final Path target = root.resolve(source.relativize(file).toString());
				if (Files.isDirectory(file)) {
					Files.createDirectories(target);
				} else {
					Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
				}
			}
		}
		Files.createDirectories(root.resolve("plugins"));
		return new TestDistribution(workDir, root);
	}

	/**
	 * @return true if the FirstSpirit runtime jar could be resolved when the distribution was assembled
	 */
	boolean hasFirstSpiritRuntime() throws IOException {
		try (final Stream<Path> files = Files.list(_root.resolve("lib"))) {
			return files.anyMatch(file -> file.getFileName().toString().startsWith("fs-isolated-runtime"));
		}
	}

	/**
	 * The directory the JVMs run in; relative result file paths end up here.
	 */
	Path workDir() {
		return _workDir;
	}

	/**
	 * Copies a fixture jar (see build.gradle.kts, property {@code fscli.fixture.<name>}) into {@code lib/} or {@code plugins/}.
	 */
	void install(final String fixture, final String directory) throws IOException {
		final Path jar = Path.of(System.getProperty("fscli.fixture." + fixture));
		Files.copy(jar, _root.resolve(directory).resolve(jar.getFileName().toString()));
	}

	Run cli(final String... args) throws Exception {
		return run(CLI_MAIN, args);
	}

	Run probe(final String... args) throws Exception {
		return run(PROBE_MAIN, args);
	}

	private Run run(final String mainClass, final String... args) throws Exception {
		final List<String> command = new ArrayList<>();
		command.add(Path.of(System.getProperty("java.home"), "bin", "java").toString());
		// the flags of fs-cli.sh
		command.add("--add-opens=java.base/sun.reflect.annotation=ALL-UNNAMED");
		command.add("--add-opens=java.base/java.util=ALL-UNNAMED");
		command.add("-Xmx512m");
		command.add("-Dlog4j.configurationFile=" + _root.resolve("conf").resolve("log4j2.xml"));
		command.add("-cp");
		// the JVM expands the wildcard itself, so the application classloader is built as in production
		command.add(_root.resolve("lib") + "/*");
		command.add(mainClass);
		command.addAll(List.of(args));

		final Path outputFile = Files.createTempFile(_workDir, "run-", ".log");
		final Process process = new ProcessBuilder(command)
				.directory(_workDir.toFile())
				.redirectErrorStream(true)
				.redirectOutput(outputFile.toFile())
				.start();
		if (!process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
			process.destroyForcibly();
			throw new IllegalStateException("Timeout after " + TIMEOUT_SECONDS + "s: " + command + "\n" + read(outputFile));
		}
		return new Run(process.exitValue(), read(outputFile));
	}

	private static String read(final Path file) {
		try {
			return Files.readString(file, StandardCharsets.UTF_8);
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}
