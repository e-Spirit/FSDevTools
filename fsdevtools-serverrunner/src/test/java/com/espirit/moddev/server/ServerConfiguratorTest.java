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

import com.espirit.moddev.connection.FsConnectionType;
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
import java.util.HashMap;

import static com.espirit.moddev.util.FsUtil.DIR_CONF;
import static com.espirit.moddev.util.FsUtil.DIR_FIRSTSPIRIT_5;
import static com.espirit.moddev.util.FsUtil.DIR_JETTY_SERVICE;
import static com.espirit.moddev.util.FsUtil.DIR_LIB_ISOLATED;
import static com.espirit.moddev.util.FsUtil.DIR_LIB_LEGACY;
import static com.espirit.moddev.util.FsUtil.DIR_MODULES;
import static com.espirit.moddev.util.FsUtil.DIR_SERVER;
import static com.espirit.moddev.util.FsUtil.FILE_FS_LICENSE_CONF;
import static com.espirit.moddev.util.FsUtil.FILE_FS_LOGGING_CONF;
import static com.espirit.moddev.util.FsUtil.FILE_FS_SERVER_CONF;
import static com.espirit.moddev.util.FsUtil.FILE_FS_WRAPPER_CONF;
import static com.espirit.moddev.util.FsUtil.FILE_FS_WRAPPER_ISOLATED_CONF;
import static com.espirit.moddev.util.FsUtil.FILE_JETTY_PROPERTIES;
import static com.espirit.moddev.util.FsUtil.FILE_SERVER_JAR_ISOLATED;
import static com.espirit.moddev.util.FsUtil.FILE_SERVER_JAR_LEGACY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ServerConfiguratorTest {

	private static final String TEST_INSTALLER_TAR_GZ = "/test_installer.tar.gz";
	private static final String SERVER_JAR_WITHOUT_LOGGING_CONF = "/fs-server_without_logging_conf.jar";
	private static final String SERVER_JAR = "/" + FILE_SERVER_JAR_LEGACY;
	private static final String SERVER_JAR_ISOLATED = "/" + FILE_SERVER_JAR_ISOLATED;

	@Rule
	public TemporaryFolder _temp = new TemporaryFolder();

	@Test
	public void execute_legacyServer() throws IOException {
		// setup
		final Path workingDir = _temp.getRoot().toPath().resolve("workingDir");
		final ServerInstaller installer = new ServerInstaller(workingDir) {
			@Override
			void updateExecutables(@NotNull final Path serverDir) throws IOException {
				// nothing to do in tests...
			}
		};
		final Path installerTar = new File(getClass().getResource(TEST_INSTALLER_TAR_GZ).getFile()).toPath();
		installer.setInstallerTarGz(installerTar);
		final Path serverJar = new File(getClass().getResource(SERVER_JAR).getFile()).toPath();
		installer.setServerJar(serverJar);
		installer.execute();

		// setup license file
		final Path licenseFile = workingDir.resolve("myLicense.conf");
		final String licenseContent = "myLicenseContent";
		Files.write(licenseFile, licenseContent.getBytes(), StandardOpenOption.CREATE_NEW);

		// test
		final ServerConfigurator serverConfigurator = new ServerConfigurator(workingDir);
		serverConfigurator.setXms(42);
		serverConfigurator.setXmx(1337);
		serverConfigurator.setLicenseFile(licenseFile);
		serverConfigurator.addAdditionalVMArg("additionalVMArgument");
		serverConfigurator.addServerConfValue("additionalServerConfValue", "customConfValue");
		serverConfigurator.addServerConfValue("HTTP_PORT", "9999");
		serverConfigurator.addLoggingConfValue("log4j.rootCategory", "CHANGED_LEVEL");
		serverConfigurator.addLoggingConfValue("additionalLoggingConfValue", "customLoggingValue");
		serverConfigurator.execute();

		// verify
		final Path confDir = workingDir.resolve(DIR_CONF);
		final Path libDir = workingDir.resolve(DIR_SERVER).resolve(DIR_LIB_LEGACY);
		// server jar
		assertTrue("fs-server.jar does not exist", libDir.resolve(FILE_SERVER_JAR_LEGACY).toFile().exists());
		{
			// fs-license.conf
			final Path targetLicenseFile = confDir.resolve(FILE_FS_LICENSE_CONF);
			assertTrue("fs-license.conf does not exist", targetLicenseFile.toFile().exists());
			assertEquals("contentMismatch", licenseContent, new String(Files.readAllBytes(targetLicenseFile), StandardCharsets.UTF_8));
		}
		{
			// fs-logging.conf
			final Path targetLoggingConf = confDir.resolve(FILE_FS_LOGGING_CONF);
			assertTrue("fs-logging.conf does not exist", targetLoggingConf.toFile().exists());
			final String content = new String(Files.readAllBytes(targetLoggingConf), StandardCharsets.UTF_8);
			assertTrue("content mismatch", content.contains("log4j.rootCategory=CHANGED_LEVEL"));
			assertTrue("content mismatch", content.contains("additionalLoggingConfValue=customLoggingValue"));
		}
		{
			// fs-server.conf
			final Path targetServerConf = confDir.resolve(FILE_FS_SERVER_CONF);
			assertTrue("fs-server.conf does not exist", targetServerConf.toFile().exists());
			final String content = new String(Files.readAllBytes(targetServerConf), StandardCharsets.UTF_8);
			assertTrue("content mismatch", content.contains("additionalServerConfValue=customConfValue"));
			assertTrue("content mismatch", content.contains("HOST=myHost"));
			assertTrue("content mismatch", content.contains("HTTP_PORT=9999"));
			assertTrue("content mismatch", content.contains("SOCKET_PORT=4567"));
		}
		// wrapper configs
		{
			final Path targetWrapperConf = confDir.resolve(FILE_FS_WRAPPER_CONF);
			final Path targetIsolatedWrapperConf = confDir.resolve(FILE_FS_WRAPPER_ISOLATED_CONF);
			assertTrue("fs-wrapper.conf does not exist", targetWrapperConf.toFile().exists());
			assertTrue("fs-wrapper.isolated.conf does not exist", targetIsolatedWrapperConf.toFile().exists());
			final String content = new String(Files.readAllBytes(targetWrapperConf), StandardCharsets.UTF_8);
			final String isolatedContent = new String(Files.readAllBytes(targetIsolatedWrapperConf), StandardCharsets.UTF_8);
			assertTrue("content mismatch", content.contains("wrapper.java.initmemory=42"));
			assertTrue("content mismatch", isolatedContent.contains("wrapper.java.initmemory=42"));
			assertTrue("content mismatch", content.contains("wrapper.java.maxmemory=1337"));
			assertTrue("content mismatch", isolatedContent.contains("wrapper.java.maxmemory=1337"));
			assertTrue("content mismatch", content.contains("wrapper.java.additional.79=anArgument"));
			assertTrue("content mismatch", isolatedContent.contains("wrapper.java.additional.79=anArgument"));
			assertTrue("content mismatch", content.contains("wrapper.java.additional.80=additionalVMArgument"));
			assertTrue("content mismatch", isolatedContent.contains("wrapper.java.additional.80=additionalVMArgument"));
			assertTrue("content mismatch", content.contains("#include myLicenseFile.txt"));
			assertTrue("content mismatch", isolatedContent.contains("#include myLicenseFile.txt"));
		}
	}

	@Test
	public void execute_isolatedServer() throws IOException {
		// setup
		final Path workingDir = _temp.getRoot().toPath().resolve("workingDir");
		final ServerInstaller installer = new ServerInstaller(workingDir) {
			@Override
			void updateExecutables(@NotNull final Path serverDir) throws IOException {
				// nothing to do in tests...
			}
		};
		final Path installerTar = new File(getClass().getResource(TEST_INSTALLER_TAR_GZ).getFile()).toPath();
		installer.setInstallerTarGz(installerTar);
		final Path serverJar = new File(getClass().getResource(SERVER_JAR_ISOLATED).getFile()).toPath();
		installer.setServerJar(serverJar);
		installer.execute();

		// setup license file
		final Path licenseFile = workingDir.resolve("myLicense.conf");
		final String licenseContent = "myLicenseContent";
		Files.write(licenseFile, licenseContent.getBytes(), StandardOpenOption.CREATE_NEW);

		// test
		final ServerConfigurator serverConfigurator = new ServerConfigurator(workingDir);
		serverConfigurator.setXms(42);
		serverConfigurator.setXmx(1337);
		serverConfigurator.setLicenseFile(licenseFile);
		serverConfigurator.addAdditionalVMArg("additionalVMArgument");
		serverConfigurator.addServerConfValue("additionalServerConfValue", "customConfValue");
		serverConfigurator.addServerConfValue("HTTP_PORT", "9999");
		serverConfigurator.addLoggingConfValue("log4j.rootCategory", "CHANGED_LEVEL");
		serverConfigurator.addLoggingConfValue("additionalLoggingConfValue", "customLoggingValue");
		serverConfigurator.execute();

		// verify
		final Path confDir = workingDir.resolve(DIR_CONF);
		final Path libDir = workingDir.resolve(DIR_SERVER).resolve(DIR_LIB_ISOLATED);
		// server jar
		assertTrue("fs-isolated-server.jar does not exist", libDir.resolve(FILE_SERVER_JAR_ISOLATED).toFile().exists());
		{
			// fs-license.conf
			final Path targetLicenseFile = confDir.resolve(FILE_FS_LICENSE_CONF);
			assertTrue("fs-license.conf does not exist", targetLicenseFile.toFile().exists());
			assertEquals("contentMismatch", licenseContent, new String(Files.readAllBytes(targetLicenseFile), StandardCharsets.UTF_8));
		}
		{
			// fs-logging.conf
			final Path targetLoggingConf = confDir.resolve(FILE_FS_LOGGING_CONF);
			assertTrue("fs-logging.conf does not exist", targetLoggingConf.toFile().exists());
			final String content = new String(Files.readAllBytes(targetLoggingConf), StandardCharsets.UTF_8);
			assertTrue("content mismatch", content.contains("log4j.rootCategory=CHANGED_LEVEL"));
			assertTrue("content mismatch", content.contains("additionalLoggingConfValue=customLoggingValue"));
		}
		{
			// fs-server.conf
			final Path targetServerConf = confDir.resolve(FILE_FS_SERVER_CONF);
			assertTrue("fs-server.conf does not exist", targetServerConf.toFile().exists());
			final String content = new String(Files.readAllBytes(targetServerConf), StandardCharsets.UTF_8);
			assertTrue("content mismatch", content.contains("additionalServerConfValue=customConfValue"));
			assertTrue("content mismatch", content.contains("HOST=myHost"));
			assertTrue("content mismatch", content.contains("HTTP_PORT=9999"));
			assertTrue("content mismatch", content.contains("SOCKET_PORT=4567"));
		}
		// wrapper configs
		{
			final Path targetWrapperConf = confDir.resolve(FILE_FS_WRAPPER_CONF);
			final Path targetIsolatedWrapperConf = confDir.resolve(FILE_FS_WRAPPER_ISOLATED_CONF);
			assertTrue("fs-wrapper.conf does not exist", targetWrapperConf.toFile().exists());
			assertTrue("fs-wrapper.isolated.conf does not exist", targetIsolatedWrapperConf.toFile().exists());
			final String content = new String(Files.readAllBytes(targetWrapperConf), StandardCharsets.UTF_8);
			final String isolatedContent = new String(Files.readAllBytes(targetIsolatedWrapperConf), StandardCharsets.UTF_8);
			assertTrue("content mismatch", content.contains("wrapper.java.initmemory=42"));
			assertTrue("content mismatch", isolatedContent.contains("wrapper.java.initmemory=42"));
			assertTrue("content mismatch", content.contains("wrapper.java.maxmemory=1337"));
			assertTrue("content mismatch", isolatedContent.contains("wrapper.java.maxmemory=1337"));
			assertTrue("content mismatch", content.contains("wrapper.java.additional.79=anArgument"));
			assertTrue("content mismatch", isolatedContent.contains("wrapper.java.additional.79=anArgument"));
			assertTrue("content mismatch", content.contains("wrapper.java.additional.80=additionalVMArgument"));
			assertTrue("content mismatch", isolatedContent.contains("wrapper.java.additional.80=additionalVMArgument"));
			assertTrue("content mismatch", content.contains("#include myLicenseFile.txt"));
			assertTrue("content mismatch", isolatedContent.contains("#include myLicenseFile.txt"));
		}
	}

	@Test(expected = FileNotFoundException.class)
	public void updateConfFile_fileDoesNotExist() throws IOException {
		ServerConfigurator.updateConfFile(Paths.get("nonExistingFile.conf"), new HashMap<>());
	}

	@Test
	public void updateConfFile() throws IOException {
		// setup
		final Path targetDir = _temp.getRoot().toPath().resolve("workingDir");
		final Path installerTar = new File(getClass().getResource(TEST_INSTALLER_TAR_GZ).getFile()).toPath();
		ServerInstaller.decompressInstaller(targetDir, installerTar);

		// test
		final Path targetFile = targetDir.resolve(DIR_FIRSTSPIRIT_5).resolve(DIR_CONF).resolve(FILE_FS_SERVER_CONF);
		final HashMap<String, String> map = new HashMap<>();
		map.put("HTTP_PORT", "1337");
		map.put("aNewProperty", "newValue");
		ServerConfigurator.updateConfFile(targetFile, map);

		// verify
		final String content = new String(Files.readAllBytes(targetFile), StandardCharsets.UTF_8);
		// unchanged values
		assertTrue("content mismatch", content.contains("HOST=myHost"));
		assertTrue("content mismatch", content.contains("SOCKET_PORT=4567"));
		// changed values
		assertTrue("content mismatch", content.contains("HTTP_PORT=1337"));
		assertTrue("content mismatch", content.contains("aNewProperty=newValue"));
	}

	@Test(expected = FileNotFoundException.class)
	public void updateServerConf_dirDoesNotExist() throws IOException {
		final Path workingDir = _temp.getRoot().toPath().resolve("workingDir");
		ServerConfigurator.updateServerConf(workingDir, new HashMap<>());
	}

	@Test(expected = FileNotFoundException.class)
	public void updateServerConf_fileDoesNotExist() throws IOException {
		final Path workingDir = _temp.getRoot().toPath().resolve("workingDir");
		final Path confDir = workingDir.resolve(DIR_CONF);
		FileUtil.mkDirs(confDir);
		assertTrue("directory should exist", confDir.toFile().exists());
		ServerConfigurator.updateServerConf(workingDir, new HashMap<>());
	}

	@Test
	public void updateServerConf() throws IOException {
		// setup
		final Path workingDir = _temp.getRoot().toPath().resolve("workingDir");
		final Path installerTar = new File(getClass().getResource(TEST_INSTALLER_TAR_GZ).getFile()).toPath();
		ServerInstaller.decompressInstaller(workingDir, installerTar);

		// test
		final Path fs5Dir = workingDir.resolve(DIR_FIRSTSPIRIT_5);
		final Path targetFile = fs5Dir.resolve(DIR_CONF).resolve(FILE_FS_SERVER_CONF);
		final HashMap<String, String> map = new HashMap<>();
		map.put("HTTP_PORT", "1337");
		map.put("aNewProperty", "newValue");
		ServerConfigurator.updateServerConf(fs5Dir, map);

		// verify
		final String content = new String(Files.readAllBytes(targetFile), StandardCharsets.UTF_8);
		// unchanged values
		assertTrue("content mismatch", content.contains("HOST=myHost"));
		assertTrue("content mismatch", content.contains("SOCKET_PORT=4567"));
		// changed values
		assertTrue("content mismatch", content.contains("HTTP_PORT=1337"));
		assertTrue("content mismatch", content.contains("aNewProperty=newValue"));
	}

	@Test(expected = FileNotFoundException.class)
	public void updateServerConf_fileDoesNotExistInJar() throws IOException {
		// setup
		final Path workingDir = _temp.getRoot().toPath().resolve("workingDir");
		final Path incompleteServerJar = new File(getClass().getResource(SERVER_JAR_WITHOUT_LOGGING_CONF).getFile()).toPath();

		// test
		ServerConfigurator.updateLoggingConf(workingDir, incompleteServerJar, new HashMap<>());
	}

	@Test
	public void updateLoggingConf() throws IOException {
		// setup
		final Path workingDir = _temp.getRoot().toPath().resolve("workingDir");
		final Path serverJar = new File(getClass().getResource(SERVER_JAR).getFile()).toPath();

		// test
		final HashMap<String, String> arguments = new HashMap<>();
		arguments.put("log4j.rootCategory", "CHANGED_LEVEL");
		arguments.put("anotherProperty", "customValue");
		ServerConfigurator.updateLoggingConf(workingDir, serverJar, arguments);

		// verify
		final Path loggingConf = workingDir.resolve(DIR_CONF).resolve(FILE_FS_LOGGING_CONF);
		final String content = new String(Files.readAllBytes(loggingConf), StandardCharsets.UTF_8);
		assertTrue("content mismatch", content.contains("log4j.rootCategory=CHANGED_LEVEL"));
		assertTrue("content mismatch", content.contains("anotherProperty=customValue"));
	}

	@Test
	public void updateLoggingConf_fileAlreadyExists() throws IOException {
		// setup
		final Path workingDir = _temp.getRoot().toPath().resolve("workingDir");
		final Path serverJar = new File(getClass().getResource(SERVER_JAR).getFile()).toPath();

		// create a logging file
		final Path confDir = workingDir.resolve(DIR_CONF);
		FileUtil.mkDirs(confDir);
		final Path loggingConf = confDir.resolve(FILE_FS_LOGGING_CONF);
		Files.write(loggingConf, "firstContent".getBytes(), StandardOpenOption.CREATE_NEW);

		// test
		final HashMap<String, String> arguments = new HashMap<>();
		arguments.put("log4j.rootCategory", "CHANGED_LEVEL");
		arguments.put("anotherProperty", "customValue");
		ServerConfigurator.updateLoggingConf(workingDir, serverJar, arguments);

		// verify
		final String content = new String(Files.readAllBytes(loggingConf), StandardCharsets.UTF_8);
		assertTrue("content mismatch", content.contains("log4j.rootCategory=CHANGED_LEVEL"));
		assertTrue("content mismatch", content.contains("anotherProperty=customValue"));
	}

	@Test
	public void updateJettyConf_fileDoesNotExist() throws IOException {
		// setup
		final Path workingDir = _temp.getRoot().toPath().resolve("workingDir");
		// test
		ServerConfigurator.updateJettyConf(workingDir);
		// verify
		final Path jettyDir = workingDir.resolve(DIR_CONF).resolve(DIR_MODULES).resolve(DIR_JETTY_SERVICE);
		final Path jettyConf = jettyDir.resolve(FILE_JETTY_PROPERTIES);
		assertFalse("directory should not exist", jettyDir.toFile().exists());
		assertFalse("file should not exist", jettyConf.toFile().exists());
	}

	@Test
	public void updateJettyConf_defaultValues() throws IOException {
		// setup
		final Path workingDir = _temp.getRoot().toPath().resolve("workingDir");
		final Path jettyDir = workingDir.resolve(DIR_CONF).resolve(DIR_MODULES).resolve(DIR_JETTY_SERVICE);
		final Path jettyConf = jettyDir.resolve(FILE_JETTY_PROPERTIES);
		final String configContent = "property=myValue\nPORT=abc\n";
		FileUtil.mkDirs(jettyDir);
		Files.write(jettyConf, configContent.getBytes(), StandardOpenOption.CREATE_NEW);
		assertTrue("file should exist", jettyConf.toFile().exists());
		// test
		ServerConfigurator.updateJettyConf(workingDir);
		// verify
		final String content = new String(Files.readAllBytes(jettyConf), StandardCharsets.UTF_8);
		assertTrue("content mismatch", content.contains("property=myValue"));
		assertTrue("content mismatch", content.contains("PORT=" + FsConnectionType.HTTP.getDefaultPort()));
	}

	@Test
	public void updateJettyConf_readValuesFromServerConf() throws IOException {
		// setup
		final Path workingDir = _temp.getRoot().toPath().resolve("workingDir");
		// create server conf
		final Path confDir = workingDir.resolve(DIR_CONF);
		FileUtil.mkDirs(confDir);
		final Path serverConf = confDir.resolve(FILE_FS_SERVER_CONF);
		final String configContent = "HTTP_PORT=1234";
		Files.write(serverConf, configContent.getBytes(), StandardOpenOption.CREATE_NEW);

		// create jetty conf
		final Path jettyDir = workingDir.resolve(DIR_CONF).resolve(DIR_MODULES).resolve(DIR_JETTY_SERVICE);
		final Path jettyConf = jettyDir.resolve(FILE_JETTY_PROPERTIES);
		final String jettyContent = "property=myValue\nPORT=abc\n";
		FileUtil.mkDirs(jettyDir);
		Files.write(jettyConf, jettyContent.getBytes(), StandardOpenOption.CREATE_NEW);
		assertTrue("file should exist", jettyConf.toFile().exists());
		// test
		ServerConfigurator.updateJettyConf(workingDir);
		// verify
		final String content = new String(Files.readAllBytes(jettyConf), StandardCharsets.UTF_8);
		assertTrue("content mismatch", content.contains("property=myValue"));
		assertTrue("content mismatch", content.contains("PORT=1234"));
	}

	@Test(expected = FileNotFoundException.class)
	public void updateWrapperConfFiles_dirDoesNotExist() throws IOException {
		final Path workingDir = _temp.getRoot().toPath().resolve("workingDir");
		ServerConfigurator.updateWrapperConfFiles(workingDir, 42, 1337, 50, false, new ArrayList<>());
	}

	@Test(expected = FileNotFoundException.class)
	public void updateWrapperConfFiles_legacyFileDoesNotExist() throws IOException {
		// setup
		final Path workingDir = _temp.getRoot().toPath().resolve("workingDir");
		final Path installerTar = new File(getClass().getResource(TEST_INSTALLER_TAR_GZ).getFile()).toPath();
		ServerInstaller.decompressInstaller(workingDir, installerTar);

		// delete the legacy file
		final Path fs5Dir = workingDir.resolve(DIR_FIRSTSPIRIT_5);
		final Path legacyConf = fs5Dir.resolve(DIR_CONF).resolve(FILE_FS_WRAPPER_CONF);
		final Path isolatedConf = fs5Dir.resolve(DIR_CONF).resolve(FILE_FS_WRAPPER_ISOLATED_CONF);
		final File legacyFile = legacyConf.toFile();
		assertTrue("file should exist", legacyFile.exists());
		assertTrue("cannot delete file", legacyFile.delete());
		assertTrue("file should exist", isolatedConf.toFile().exists());

		// test
		ServerConfigurator.updateWrapperConfFiles(fs5Dir, 42, 1337, 50, false, new ArrayList<>());
	}

	@Test(expected = FileNotFoundException.class)
	public void updateWrapperConfFiles_isolatedFileDoesNotExist() throws IOException {
		// setup
		final Path workingDir = _temp.getRoot().toPath().resolve("workingDir");
		final Path installerTar = new File(getClass().getResource(TEST_INSTALLER_TAR_GZ).getFile()).toPath();
		ServerInstaller.decompressInstaller(workingDir, installerTar);

		// delete the legacy file
		final Path fs5Dir = workingDir.resolve(DIR_FIRSTSPIRIT_5);
		final Path legacyConf = fs5Dir.resolve(DIR_CONF).resolve(FILE_FS_WRAPPER_CONF);
		final Path isolatedConf = fs5Dir.resolve(DIR_CONF).resolve(FILE_FS_WRAPPER_ISOLATED_CONF);
		final File isolatedFile = isolatedConf.toFile();
		assertTrue("file should exist", legacyConf.toFile().exists());
		assertTrue("file should exist", isolatedFile.exists());
		assertTrue("cannot delete file", isolatedFile.delete());

		// test
		ServerConfigurator.updateWrapperConfFiles(fs5Dir, 42, 1337, 50, false, new ArrayList<>());
	}

	@Test
	public void updateWrapperConfFiles() throws IOException {
		// setup
		final Path workingDir = _temp.getRoot().toPath().resolve("workingDir");
		final Path installerTar = new File(getClass().getResource(TEST_INSTALLER_TAR_GZ).getFile()).toPath();
		ServerInstaller.decompressInstaller(workingDir, installerTar);

		// delete the legacy file
		final Path fs5Dir = workingDir.resolve(DIR_FIRSTSPIRIT_5);
		final Path legacyConf = fs5Dir.resolve(DIR_CONF).resolve(FILE_FS_WRAPPER_CONF);
		final Path isolatedConf = fs5Dir.resolve(DIR_CONF).resolve(FILE_FS_WRAPPER_ISOLATED_CONF);
		assertTrue("file should exist", legacyConf.toFile().exists());
		assertTrue("file should exist", isolatedConf.toFile().exists());

		// test
		final ArrayList<String> additionalVMArgs = new ArrayList<>();
		additionalVMArgs.add("myFirstExtraVMArg");
		additionalVMArgs.add("mySecondExtraVMArg");
		ServerConfigurator.updateWrapperConfFiles(fs5Dir, 42, 1337, 50, false, additionalVMArgs);

		// verify
		{
			// check legacy file
			final String content = new String(Files.readAllBytes(legacyConf), StandardCharsets.UTF_8);
			assertTrue("content mismatch", content.contains("wrapper.java.initmemory=42"));
			assertTrue("content mismatch", content.contains("wrapper.java.maxmemory=1337"));
			assertTrue("content mismatch", content.contains("wrapper.startup.timeout=50"));
			assertTrue("content mismatch", content.contains("wrapper.disable_restarts.automatic=true"));
			assertTrue("content mismatch", content.contains("wrapper.java.additional.79=anArgument"));
			assertTrue("content mismatch", content.contains("wrapper.java.additional.80=myFirstExtraVMArg"));
			assertTrue("content mismatch", content.contains("wrapper.java.additional.81=mySecondExtraVMArg"));
			assertTrue("content mismatch", content.contains("#include myLicenseFile.txt"));
		}
		{
			// check isolated file
			final String content = new String(Files.readAllBytes(legacyConf), StandardCharsets.UTF_8);
			assertTrue("content mismatch", content.contains("wrapper.java.initmemory=42"));
			assertTrue("content mismatch", content.contains("wrapper.java.maxmemory=1337"));
			assertTrue("content mismatch", content.contains("wrapper.startup.timeout=50"));
			assertTrue("content mismatch", content.contains("wrapper.java.additional.79=anArgument"));
			assertTrue("content mismatch", content.contains("wrapper.java.additional.80=myFirstExtraVMArg"));
			assertTrue("content mismatch", content.contains("wrapper.java.additional.81=mySecondExtraVMArg"));
			assertTrue("content mismatch", content.contains("#include myLicenseFile.txt"));
		}
	}

	@Test(expected = FileNotFoundException.class)
	public void updateWrapperConfFile_fileDoesNotExist() throws IOException {
		// setup
		final Path workingDir = _temp.getRoot().toPath().resolve("workingDir");
		final Path fs5Dir = workingDir.resolve(DIR_FIRSTSPIRIT_5);
		final Path configFile = fs5Dir.resolve(DIR_CONF).resolve(FILE_FS_WRAPPER_CONF);
		assertFalse("file should not exist", configFile.toFile().exists());

		// test
		ServerConfigurator.updateWrapperConfFile(configFile, new HashMap<>());
	}

	@Test
	public void updateWrapperConfFile() throws IOException {
		// setup
		final Path workingDir = _temp.getRoot().toPath().resolve("workingDir");
		final Path installerTar = new File(getClass().getResource(TEST_INSTALLER_TAR_GZ).getFile()).toPath();
		ServerInstaller.decompressInstaller(workingDir, installerTar);

		// delete the legacy file
		final Path fs5Dir = workingDir.resolve(DIR_FIRSTSPIRIT_5);
		final Path configFile = fs5Dir.resolve(DIR_CONF).resolve(FILE_FS_WRAPPER_CONF);
		assertTrue("file should exist", configFile.toFile().exists());

		// test
		final HashMap<String, String> map = new HashMap<>();
		map.put("wrapper.java.initmemory", "42");
		map.put("additionalProperty", "additionalValue");
		ServerConfigurator.updateWrapperConfFile(configFile, map);

		// verify
		final String content = new String(Files.readAllBytes(configFile), StandardCharsets.UTF_8);
		assertTrue("content mismatch", content.contains("wrapper.java.initmemory=42"));
		assertTrue("content mismatch", content.contains("wrapper.java.maxmemory=1024"));
		assertTrue("content mismatch", content.contains("wrapper.java.additional.79=anArgument"));
		assertTrue("content mismatch", content.contains("wrapper.java.additional.80="));
		assertTrue("content mismatch", content.contains("additionalProperty=additionalValue"));
		assertTrue("content mismatch", content.contains("#include myLicenseFile.txt"));
	}

	@Test(expected = IllegalStateException.class)
	public void copyLicenseFile_fileIsNull() throws IOException {
		ServerConfigurator.copyLicenseFile(Paths.get("test"), null);
	}

	@Test(expected = FileNotFoundException.class)
	public void copyLicenseFile_fileDoesNotExist() throws IOException {
		ServerConfigurator.copyLicenseFile(Paths.get("test"), Paths.get("nonExistingFile.conf"));
	}

	@Test
	public void copyLicenseFile() throws IOException {
		// setup
		final Path workingDir = _temp.getRoot().toPath();
		final Path fileToCopy = workingDir.resolve("myLicense.conf");
		final String licenseContent = "myLicenseContent";
		Files.write(fileToCopy, licenseContent.getBytes(), StandardOpenOption.CREATE_NEW);
		assertTrue("source file should exist", fileToCopy.toFile().exists());

		// test
		ServerConfigurator.copyLicenseFile(workingDir, fileToCopy);

		// verify
		final Path targetLicenseFile = workingDir.resolve(DIR_CONF).resolve(FILE_FS_LICENSE_CONF);
		assertTrue("target file should exist", targetLicenseFile.toFile().exists());
		assertEquals("contentMismatch", licenseContent, new String(Files.readAllBytes(targetLicenseFile), StandardCharsets.UTF_8));
	}

}
