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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Restarts the currently running cli in a child process with a different FirstSpirit runtime jar on
 * the classpath. The child process inherits the java executable, all vm arguments and the original
 * program arguments of the current process; stdin/stdout/stderr are shared with the current process.
 */
public class CliRelauncher {

	/**
	 * System property holding the relaunch depth of the current process. Prevents endless relaunch
	 * loops: every process started by {@link CliRelauncher} carries the depth of its parent plus one,
	 * and the auto update aborts when a maximum depth is reached.
	 */
	public static final String RELAUNCH_DEPTH_PROPERTY = "fs-cli.autoUpdate.depth";

	private static final Pattern RUNTIME_JAR_PATTERN = Pattern.compile("(?i)fs-isolated-runtime.*\\.jar");

	/**
	 * Relaunches the cli with the given runtime jar and waits for the child process to terminate.
	 *
	 * @param runtimeJar   the runtime jar to put on the classpath (replaces all fs-isolated-runtime jars of the current classpath)
	 * @param mainClass    the main class to launch
	 * @param originalArgs the original program arguments
	 * @return the exit code of the child process
	 * @throws IOException          if the child process could not be started
	 * @throws InterruptedException if the current thread is interrupted while waiting for the child process
	 */
	public int relaunch(@NotNull final Path runtimeJar, @NotNull final String mainClass, @NotNull final String[] originalArgs) throws IOException, InterruptedException {
		final List<String> command = new ArrayList<>();
		command.add(getJavaExecutable());
		for (final String vmArgument : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
			// the relaunch depth is passed explicitly below - never inherit it
			if (!vmArgument.startsWith("-D" + RELAUNCH_DEPTH_PROPERTY)) {
				command.add(vmArgument);
			}
		}
		command.add("-D" + RELAUNCH_DEPTH_PROPERTY + "=" + (getRelaunchDepth() + 1));
		command.add("-cp");
		command.add(buildClassPath(System.getProperty("java.class.path", ""), runtimeJar));
		command.add(mainClass);
		command.addAll(Arrays.asList(originalArgs));

		final Process process = new ProcessBuilder(command).inheritIO().start();
		return process.waitFor();
	}

	/**
	 * Returns the relaunch depth of the current process ({@code 0} for a process that was not
	 * started by a {@link CliRelauncher}).
	 */
	public static int getRelaunchDepth() {
		try {
			return Math.max(0, Integer.getInteger(RELAUNCH_DEPTH_PROPERTY, 0));
		} catch (final RuntimeException exception) {
			return 0;
		}
	}

	@NotNull
	private static String getJavaExecutable() {
		final boolean isWindows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("windows");
		return Paths.get(System.getProperty("java.home"), "bin", isWindows ? "java.exe" : "java").toString();
	}

	/**
	 * Rebuilds the given classpath for the child process: wildcard entries ({@code dir/*}) are
	 * expanded to the jar files they match, every fs-isolated-runtime jar is removed and the given
	 * runtime jar is appended. Expanding the wildcards is required because the classpath order of a
	 * wildcard entry is undefined - the new runtime jar must be the only runtime jar on the classpath.
	 *
	 * @param classPath  the classpath of the current process ({@code java.class.path})
	 * @param runtimeJar the runtime jar to append
	 * @return the classpath for the child process
	 */
	@VisibleForTesting
	@NotNull
	static String buildClassPath(@NotNull final String classPath, @NotNull final Path runtimeJar) {
		final List<String> entries = new ArrayList<>();
		for (final String entry : classPath.split(Pattern.quote(File.pathSeparator))) {
			if (entry.isBlank()) {
				continue;
			}
			if (entry.equals("*") || entry.endsWith(File.separator + "*") || entry.endsWith("/*")) {
				// wildcard entry --> expand to the matching jar files (excluding runtime jars)
				final Path directory = Paths.get(entry.equals("*") ? "." : entry.substring(0, entry.length() - 2));
				if (Files.isDirectory(directory)) {
					try (final Stream<Path> files = Files.list(directory)) {
						files.filter(file -> file.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar"))
								.filter(file -> !isRuntimeJar(file.getFileName().toString()))
								.sorted()
								.forEach(file -> entries.add(file.toAbsolutePath().toString()));
					} catch (final IOException exception) {
						// directory not readable --> keep the wildcard entry as-is
						entries.add(entry);
					}
				}
			} else if (!isRuntimeJar(Paths.get(entry).getFileName().toString())) {
				entries.add(entry);
			}
		}
		entries.add(runtimeJar.toAbsolutePath().toString());
		return String.join(File.pathSeparator, entries);
	}

	private static boolean isRuntimeJar(@NotNull final String fileName) {
		return RUNTIME_JAR_PATTERN.matcher(fileName).matches();
	}

}
