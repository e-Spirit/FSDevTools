/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2020 e-Spirit AG
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

import com.espirit.moddev.util.ArchiveUtil;
import com.espirit.moddev.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.espirit.moddev.util.FsUtil.DIR_BIN;
import static com.espirit.moddev.util.FsUtil.DIR_CONF;
import static com.espirit.moddev.util.FsUtil.DIR_FIRSTSPIRIT_5;
import static com.espirit.moddev.util.FsUtil.DIR_LIB_ISOLATED;
import static com.espirit.moddev.util.FsUtil.DIR_LIB_LEGACY;
import static com.espirit.moddev.util.FsUtil.DIR_SERVER;
import static com.espirit.moddev.util.FsUtil.FILE_FS_SERVER_CONF;
import static com.espirit.moddev.util.FsUtil.FILE_FS_WRAPPER_CONF;
import static com.espirit.moddev.util.FsUtil.FILE_FS_WRAPPER_ISOLATED_CONF;
import static com.espirit.moddev.util.FsUtil.FILE_SERVER_JAR_ISOLATED;
import static com.espirit.moddev.util.FsUtil.FILE_SERVER_JAR_LEGACY;
import static com.espirit.moddev.util.FsUtil.FILE_WRAPPER_EXECUTABLE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ServerInstallerTest {

	private static final String TEST_INSTALLER_TAR_GZ = "/test_installer.tar.gz";
	private static final String TEST_SERVER_JAR = "/" + FILE_SERVER_JAR_LEGACY;
	private static final String TEST_ISOLATED_SERVER_JAR = "/" + FILE_SERVER_JAR_ISOLATED;

	@Rule
	public TemporaryFolder _temp = new TemporaryFolder();

	@Test(expected = IllegalStateException.class)
	public void execute_installerNull() throws IOException {
		// setup
		final Path targetDir = _temp.getRoot().toPath().resolve("target");
		final ServerInstaller serverInstaller = new ServerInstaller(targetDir);
		serverInstaller.setServerJar(Paths.get("my.jar"));

		// test
		serverInstaller.execute();
	}

	@Test(expected = IllegalStateException.class)
	public void execute_serverJarNull() throws IOException {
		// setup
		final Path targetDir = _temp.getRoot().toPath().resolve("target");
		final ServerInstaller serverInstaller = new ServerInstaller(targetDir);
		serverInstaller.setInstallerTarGz(Paths.get("my.jar"));

		// test
		serverInstaller.execute();
	}

	@Test
	public void execute_legacy() throws IOException {
		// setup
		final Path targetDir = _temp.getRoot().toPath().resolve("target");
		final Path installerTar = new File(getClass().getResource(TEST_INSTALLER_TAR_GZ).getFile()).toPath();
		final Path serverJar = new File(getClass().getResource(TEST_SERVER_JAR).getFile()).toPath();
		final ServerInstaller serverInstaller = new ServerInstaller(targetDir) {
			@Override
			void updateExecutables(@NotNull final Path serverDir) throws IOException {
				// nothing to test..
			}
		};

		// test
		serverInstaller.setInstallerTarGz(installerTar);
		serverInstaller.setServerJar(serverJar);
		serverInstaller.execute();

		// verify
		final List<Path> toCheck = new ArrayList<>();
		toCheck.add(targetDir.resolve(DIR_CONF));
		toCheck.add(targetDir.resolve(DIR_CONF).resolve(FILE_FS_SERVER_CONF));
		toCheck.add(targetDir.resolve(DIR_CONF).resolve(FILE_FS_WRAPPER_CONF));
		toCheck.add(targetDir.resolve(DIR_CONF).resolve(FILE_FS_WRAPPER_ISOLATED_CONF));
		toCheck.add(targetDir.resolve(DIR_SERVER).resolve(DIR_LIB_LEGACY).resolve(FILE_SERVER_JAR_LEGACY));

		for (final Path path : toCheck) {
			assertTrue("file '" + path + "' should exist", path.toFile().exists());
		}
	}

	@Test
	public void execute_isolated() throws IOException {
		// setup
		final Path targetDir = _temp.getRoot().toPath().resolve("target");
		final Path installerTar = new File(getClass().getResource(TEST_INSTALLER_TAR_GZ).getFile()).toPath();
		final Path serverJar = new File(getClass().getResource(TEST_ISOLATED_SERVER_JAR).getFile()).toPath();
		final ServerInstaller serverInstaller = new ServerInstaller(targetDir) {
			@Override
			void updateExecutables(@NotNull final Path serverDir) throws IOException {
				// nothing to test..
			}
		};

		// test
		serverInstaller.setInstallerTarGz(installerTar);
		serverInstaller.setServerJar(serverJar);
		serverInstaller.execute();

		// verify
		final List<Path> toCheck = new ArrayList<>();
		toCheck.add(targetDir.resolve(DIR_CONF));
		toCheck.add(targetDir.resolve(DIR_CONF).resolve(FILE_FS_SERVER_CONF));
		toCheck.add(targetDir.resolve(DIR_CONF).resolve(FILE_FS_WRAPPER_CONF));
		toCheck.add(targetDir.resolve(DIR_CONF).resolve(FILE_FS_WRAPPER_ISOLATED_CONF));
		toCheck.add(targetDir.resolve(DIR_SERVER).resolve(DIR_LIB_ISOLATED).resolve(FILE_SERVER_JAR_ISOLATED));

		for (final Path path : toCheck) {
			assertTrue("file '" + path + "' should exist", path.toFile().exists());
		}
	}

	@Test
	public void cleanupTargetDirectory() throws IOException {
		// create a temporal directory with content
		final Path targetDir = _temp.getRoot().toPath().resolve("target");
		final Path installerTar = new File(getClass().getResource(TEST_INSTALLER_TAR_GZ).getFile()).toPath();
		FileUtil.mkDirs(targetDir);
		ArchiveUtil.decompressTarGz(installerTar, targetDir);

		// test
		assertTrue("target dir should exist with children", targetDir.toFile().list().length > 0);
		ServerInstaller.cleanupTargetDirectory(targetDir);

		// verify
		assertFalse("target dir should not exist", targetDir.toFile().exists());
	}

	@Test
	public void decompressInstaller() throws IOException {
		// setup
		final Path targetDir = _temp.getRoot().toPath().resolve("target");
		final Path installerTar = new File(getClass().getResource(TEST_INSTALLER_TAR_GZ).getFile()).toPath();

		// test
		assertFalse("server dir should not exist", targetDir.toFile().exists());
		ServerInstaller.decompressInstaller(targetDir, installerTar);

		// verify
		assertTrue("server dir should exist with children", targetDir.toFile().list().length > 0);
	}

	@Test
	public void removeUnneededDirectory() throws IOException {
		// create a temporal directory with content
		final Path targetDir = _temp.getRoot().toPath().resolve("target");
		final Path fsTargetDir = targetDir.resolve(DIR_FIRSTSPIRIT_5);
		final Path installerTar = new File(getClass().getResource(TEST_INSTALLER_TAR_GZ).getFile()).toPath();
		ServerInstaller.decompressInstaller(targetDir, installerTar);
		assertTrue(DIR_FIRSTSPIRIT_5 + " dir should exist", fsTargetDir.toFile().exists());

		// test
		ServerInstaller.removeUnneededDirectory(targetDir);

		// verify
		assertFalse(DIR_FIRSTSPIRIT_5 + " dir should have been deleted", fsTargetDir.toFile().exists());
	}

	@Test
	public void removeUnneededDirectory_directoryDoesNotExist() throws IOException {
		// setup
		final Path targetDir = _temp.getRoot().toPath().resolve("target");
		final Path fsTargetDir = targetDir.resolve(DIR_FIRSTSPIRIT_5);
		assertFalse(DIR_FIRSTSPIRIT_5 + " dir should not exist", fsTargetDir.toFile().exists());

		// test
		ServerInstaller.removeUnneededDirectory(targetDir);

		// verify
		assertFalse("targetDir dir should still exist", targetDir.toFile().exists());
		assertFalse(DIR_FIRSTSPIRIT_5 + " dir should not exist", fsTargetDir.toFile().exists());
	}

	@Test
	public void copyServerJar_legacy() throws IOException {
		// setup
		final Path targetDir = _temp.getRoot().toPath().resolve("target");
		final Path serverDir = targetDir.resolve("fs-server");
		final Path serverJar = new File(getClass().getResource(TEST_SERVER_JAR).getFile()).toPath();

		// test
		ServerInstaller.copyServerJar(serverDir, serverJar);

		// verify
		final Path legacyJar = serverDir.resolve(DIR_SERVER).resolve(DIR_LIB_LEGACY).resolve("fs-server.jar");
		final Path isolatedJar = serverDir.resolve(DIR_SERVER).resolve(DIR_LIB_ISOLATED).resolve("fs-isolated-server.jar");
		assertTrue("legacy server jar should have been copied", legacyJar.toFile().exists());
		assertFalse("isolated server jar should not exist", isolatedJar.toFile().exists());
	}

	@Test
	public void copyServerJar_isolated() throws IOException {
		// setup
		final Path targetDir = _temp.getRoot().toPath().resolve("target");
		final Path serverDir = targetDir.resolve("fs-server");
		final Path serverJar = new File(getClass().getResource(TEST_ISOLATED_SERVER_JAR).getFile()).toPath();

		// test
		ServerInstaller.copyServerJar(serverDir, serverJar);

		// verify
		final Path legacyJar = serverDir.resolve(DIR_SERVER).resolve(DIR_LIB_LEGACY).resolve("fs-server.jar");
		final Path isolatedJar = serverDir.resolve(DIR_SERVER).resolve(DIR_LIB_ISOLATED).resolve("fs-isolated-server.jar");
		assertTrue("isolated server jar should have been copied", isolatedJar.toFile().exists());
		assertFalse("legacy server jar should not exist", legacyJar.toFile().exists());
	}

	@Test(expected = FileNotFoundException.class)
	public void updateWrapperExecutable_fileDoesNotExist() throws IOException {
		// setup
		final Path workingDir = _temp.getRoot().toPath().resolve("workingDir");
		final Path fs5Dir = workingDir.resolve(DIR_FIRSTSPIRIT_5);
		final Path configFile = fs5Dir.resolve(DIR_BIN).resolve(FILE_WRAPPER_EXECUTABLE);
		assertFalse("file should not exist", configFile.toFile().exists());

		// test
		ServerInstaller.updateWrapperExecutable(fs5Dir, UUID.randomUUID());
	}

	@Test
	public void updateWrapperExecutable() throws IOException {
		// setup
		final Path workingDir = _temp.getRoot().toPath().resolve("workingDir");
		final Path fs5Dir = workingDir.resolve(DIR_FIRSTSPIRIT_5);
		final Path binDir = fs5Dir.resolve(DIR_BIN);
		final Path configFile = binDir.resolve(FILE_WRAPPER_EXECUTABLE);
		assertFalse("file should not exist", configFile.toFile().exists());
		final String originalContent = "MAXNOFILES=10000\n" +
				"# Application\n" +
				"APP_NAME=\"fs5\"\n" +
				"APP_LONG_NAME=\"FirstSpirit 5\"\n" +
				"APP_NAME_SECOND=\"one\"\n" +
				"APP_LONG_NAME_SECOND=\"two\"\n";

		FileUtil.mkDirs(binDir);
		Files.write(configFile, originalContent.getBytes(), StandardOpenOption.CREATE_NEW);
		assertTrue("file should exist", configFile.toFile().exists());

		// test
		final UUID uuid = UUID.nameUUIDFromBytes("myUUID".getBytes());
		ServerInstaller.updateWrapperExecutable(fs5Dir, uuid);

		// verify
		final String expectedContent = "MAXNOFILES=10000\n" +
				"# Application\n" +
				"APP_NAME=\"fs5_df04a4f5_b192_3387_81d5_baf2080def11\"\n" +
				"APP_LONG_NAME=\"FirstSpirit 5 - df04a4f5_b192_3387_81d5_baf2080def11\"\n" +
				"APP_NAME_SECOND=\"one\"\n" +
				"APP_LONG_NAME_SECOND=\"two\"\n";
		final String updatedContent = new String(Files.readAllBytes(configFile), StandardCharsets.UTF_8).replaceAll("\\r", "");
		assertEquals("content mismatch", expectedContent, updatedContent);
	}

}
