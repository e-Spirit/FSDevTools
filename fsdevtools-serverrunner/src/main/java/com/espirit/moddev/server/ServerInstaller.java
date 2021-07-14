/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2021 e-Spirit AG
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

package com.espirit.moddev.server;

import com.espirit.moddev.shared.annotation.VisibleForTesting;
import com.espirit.moddev.util.ArchiveUtil;
import com.espirit.moddev.util.FileUtil;
import com.espirit.moddev.util.FsUtil;
import com.espirit.moddev.util.OsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.espirit.moddev.util.FsUtil.DIR_BIN;
import static com.espirit.moddev.util.FsUtil.DIR_FIRSTSPIRIT_5;
import static com.espirit.moddev.util.FsUtil.DIR_LIB_ISOLATED;
import static com.espirit.moddev.util.FsUtil.DIR_LIB_LEGACY;
import static com.espirit.moddev.util.FsUtil.DIR_SERVER;

public class ServerInstaller {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServerInstaller.class);

	private static final List<Path> EXECUTABLES = Arrays.asList(
			Paths.get(DIR_BIN, FsUtil.FILE_WRAPPER_EXECUTABLE),
			Paths.get(DIR_BIN, FsUtil.FILE_WRAPPER_EXECUTABLE + ".cmd"),
			Paths.get(DIR_BIN, FsUtil.FILE_FS_SERVER_EXECUTABLE),
			Paths.get(DIR_BIN, FsUtil.FILE_FS_SERVER_EXECUTABLE + ".bat"),
			Paths.get(DIR_BIN, FsUtil.FILE_FS_SERVER_EXECUTABLE + ".shconf"),
			Paths.get(DIR_BIN, FsUtil.FILE_FS_SERVER_EXECUTABLE + "-custom.shconf"),
			Paths.get(DIR_BIN, "wrapper.exe"),
			Paths.get(DIR_BIN, "wrapper-linux-x86-64"),
			Paths.get(DIR_BIN, "wrapper-macosx-universal-64"),
			Paths.get(DIR_BIN, "wrapper-windows-x86-64.exe"),
			Paths.get(DIR_SERVER, DIR_LIB_LEGACY, "libwrapper-linux-x86-64.so"),
			Paths.get(DIR_SERVER, DIR_LIB_LEGACY, "libwrapper-macosx-universal-64.jnilib"),
			Paths.get(DIR_SERVER, DIR_LIB_LEGACY, "wrapper-windows-x86-64.dll"),
			Paths.get(DIR_SERVER, DIR_LIB_ISOLATED, "libwrapper-linux-x86-64.so"),
			Paths.get(DIR_SERVER, DIR_LIB_ISOLATED, "libwrapper-macosx-universal-64.jnilib"),
			Paths.get(DIR_SERVER, DIR_LIB_ISOLATED, "wrapper-windows-x86-64.dll")
	);

	@NotNull
	private final Path _serverDir;
	@Nullable
	private Path _serverJar;
	@Nullable
	private Path _installerTarGz;

	public ServerInstaller(@NotNull final Path serverDir) {
		_serverDir = serverDir.toAbsolutePath();
	}

	/**
	 * Sets the {@link Path path} to the installer archive (*.tar.gz) for this {@link ServerInstaller installer}. The installer archive will be decompressed to the target directory during {@link #execute()}
	 *
	 * @param installerTarGz the {@link Path path} to the installer archive (*.tar.gz)
	 * @see #execute()
	 */
	public void setInstallerTarGz(@NotNull final Path installerTarGz) {
		_installerTarGz = installerTarGz.toAbsolutePath();
	}

	/**
	 * Sets the {@link Path path} to the server jar for this {@link ServerInstaller installer}. The server jar will be copied to the server directory during {@link #execute()}
	 *
	 * @param serverJar the {@link Path path} to the server jar
	 * @see #execute()
	 */
	public void setServerJar(@NotNull final Path serverJar) {
		_serverJar = serverJar.toAbsolutePath();
	}

	/**
	 * Installs the configured server to the target directory.
	 *
	 * @throws IOException if an I/O error occurs
	 * @see #ServerInstaller(Path)
	 */
	public void execute() throws IOException {
		// preconditions
		if (_installerTarGz == null) {
			throw new IllegalStateException("Install archive not set");
		}
		if (_serverJar == null) {
			throw new IllegalStateException("Server.jar not set");
		}
		LOGGER.info("Installing server to '" + _serverDir.toAbsolutePath() + "'...");
		// extract the installer, copy the server jar and update the executables
		cleanupTargetDirectory(_serverDir);
		decompressInstaller(_serverDir, _installerTarGz);
		removeUnneededDirectory(_serverDir);
		copyServerJar(_serverDir, _serverJar);
		updateWrapperExecutable(_serverDir, UUID.randomUUID());
		installWrapperExceptionHandler(_serverDir);
		updateExecutables(_serverDir);
		// final message
		LOGGER.info("Server successfully installed to '" + _serverDir.toAbsolutePath() + "'.");
	}

	private static void installWrapperExceptionHandler(@NotNull final Path serverDir) throws IOException {
		FileUtil.mkDirs(serverDir);
		if (OsUtil.isWindows()) {
			final Path executable = serverDir.resolve(FsUtil.FILE_WRAPPER_EXCEPTION_EXECUTABLE + ".bat");
			final String content = "@ECHO OFF\n" +
					"set argument=%1\n" +
					"echo %1 > " + FsUtil.FILE_WRAPPER_EXCEPTION_FILE;
			Files.write(executable, content.getBytes(), StandardOpenOption.CREATE_NEW);
			FileUtil.setExecutable(executable);
		} else {
			final Path executable = serverDir.resolve(FsUtil.FILE_WRAPPER_EXCEPTION_EXECUTABLE);
			final String content = "echo $1 > " + FsUtil.FILE_WRAPPER_EXCEPTION_FILE;
			Files.write(executable, content.getBytes(), StandardOpenOption.CREATE_NEW);
			FileUtil.setExecutable(executable);
		}
	}

	@VisibleForTesting
	void updateExecutables(@NotNull final Path serverDir) throws IOException {
		LOGGER.info("Updating executables...");
		FileUtil.setExecutable(serverDir, EXECUTABLES);
	}

	@VisibleForTesting
	static void cleanupTargetDirectory(@NotNull final Path serverDir) throws IOException {
		if (serverDir.toAbsolutePath().toFile().exists()) {
			LOGGER.info("Deleting directory '" + serverDir.toAbsolutePath() + "'...");
			FileUtil.deleteDirectory(serverDir);
		}
	}

	@VisibleForTesting
	static void decompressInstaller(@NotNull final Path serverDir, @NotNull final Path installerTarGz) throws IOException {
		LOGGER.info("Decompressing installer to '" + serverDir.toAbsolutePath() + "'...");
		ArchiveUtil.decompressTarGz(installerTarGz, serverDir);
	}

	@VisibleForTesting
	static void removeUnneededDirectory(@NotNull final Path serverDir) throws IOException {
		LOGGER.info("Removing unneeded '" + DIR_FIRSTSPIRIT_5 + "' directory...");
		final Path firstspirit5Dir = serverDir.resolve(DIR_FIRSTSPIRIT_5);
		if (firstspirit5Dir.toFile().exists()) {
			FileUtil.moveContentsUp(firstspirit5Dir);
			if (!firstspirit5Dir.toFile().delete()) {
				throw new IOException("Could not delete '" + firstspirit5Dir.toAbsolutePath() + "'!");
			}
		}
	}

	@VisibleForTesting
	static void copyServerJar(@NotNull final Path serverDir, @NotNull final Path serverJar) throws IOException {
		final Path wrapperServerDir = serverDir.resolve(DIR_SERVER);
		final Path libDir;
		final boolean isolated = FsUtil.isIsolatedJar(serverJar);
		if (isolated) {
			libDir = wrapperServerDir.resolve(DIR_LIB_ISOLATED);
		} else {
			libDir = wrapperServerDir.resolve(DIR_LIB_LEGACY);
		}
		final String targetServerJarName = "fs-" + (isolated ? "isolated-" : "") + "server.jar";
		final Path targetPath = libDir.resolve(targetServerJarName);
		FileUtil.mkDirs(libDir);
		LOGGER.info("Copying '" + serverJar.toAbsolutePath() + "' to '" + targetPath.toAbsolutePath() + "'...");
		Files.copy(serverJar, targetPath, StandardCopyOption.REPLACE_EXISTING);
	}

	@VisibleForTesting
	static void updateWrapperExecutable(@NotNull final Path serverDir, @NotNull final UUID uuid) throws IOException {
		LOGGER.info("Updating wrapper executable...");
		String appName = "fs5";
		Path wrapperExecutable = serverDir.resolve(DIR_BIN).resolve(FsUtil.FILE_WRAPPER_EXECUTABLE).toAbsolutePath();
		if (Files.notExists(wrapperExecutable)) {
			// switch to fs-server if legacy script is not present
			wrapperExecutable = serverDir.resolve(DIR_BIN).resolve(FsUtil.FILE_FS_SERVER_EXECUTABLE).toAbsolutePath();
			if (Files.notExists(wrapperExecutable)) {
				throw new FileNotFoundException("Neither fs5 nor fs-server file exists");
			}
			appName = "fs-server";
		}

		// edit lines
		final List<String> input = Files.readAllLines(wrapperExecutable);
		final ArrayList<String> outputLines = new ArrayList<>();
		final String uuidBase = uuid.toString().replaceAll("-", "_");
		for (final String line : input) {
			if (line.startsWith("APP_NAME=\"" + appName + "\"")) {
				outputLines.add("APP_NAME=\"" + appName + "_" + uuidBase + "\"");
			} else if (line.startsWith("APP_LONG_NAME=\"FirstSpirit 5\"")) {
				outputLines.add("APP_LONG_NAME=\"FirstSpirit 5 - " + uuidBase + "\"");
			} else {
				outputLines.add(line);
			}
		}

		// save changed content
		Files.write(wrapperExecutable, outputLines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
	}

}
