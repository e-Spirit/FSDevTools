/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2024 Crownpeak Technology GmbH
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

package com.espirit.moddev.util;

import com.espirit.moddev.connection.FsConnectionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static com.espirit.moddev.util.FsUtil.DIR_CONF;
import static com.espirit.moddev.util.FsUtil.FILE_FS_SERVER_CONF;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FsUtilTest {

	@TempDir
	public File _temp;

	@Test
	public void lockFileExists() throws IOException {
		final Path rootPath = _temp.toPath();
		assertFalse(FsUtil.lockFileExists(rootPath), "lock file should not exist");
		// create mock lock file
		final Path lockFilePath = rootPath.resolve(FsUtil.FILE_SERVER_LOCK);
		try (final OutputStream outputStream = new FileOutputStream(lockFilePath.toFile())) {
			outputStream.write("myText.txt".getBytes());
		}
		// re-test
		assertTrue(FsUtil.lockFileExists(rootPath), "lock file should exist");
	}

	@Test
	public void licenseFileExists() throws IOException {
		final Path rootPath = _temp.toPath();
		assertFalse(FsUtil.licenseFileExists(rootPath), "license file should not exist");
		// create mock license file
		final Path confDir = rootPath.resolve(DIR_CONF);
		FileUtil.mkDirs(confDir);
		final Path licenseFilePath = confDir.resolve(FsUtil.FILE_FS_LICENSE_CONF);
		try (final OutputStream outputStream = new FileOutputStream(licenseFilePath.toFile())) {
			outputStream.write("myText.txt".getBytes());
		}
		// re-test
		assertTrue(FsUtil.licenseFileExists(rootPath), "license file should exist");
	}

	@Test
	public void isIsolatedJar() {
		assertFalse(FsUtil.isIsolatedJar(Paths.get("fs-server.jar")), "server jar should be legacy");
		assertTrue(FsUtil.isIsolatedJar(Paths.get("fs-isolated-server.jar")), "server jar should be isolated");
		assertFalse(FsUtil.isIsolatedJar(Paths.get("fs-runtime.jar")), "runtime jar should be legacy");
		assertTrue(FsUtil.isIsolatedJar(Paths.get("fs-isolated-runtime.jar")), "runtime jar should be isolated");
	}

	@Test
	public void getPropertyFromConfig_fileDoesNotExist() {
		final Path rootPath = _temp.toPath();
		final Path configFile = rootPath.resolve(FILE_FS_SERVER_CONF);
		final String fallbackValue = "defaultValue";
		final String result = FsUtil.getPropertyFromConfig(configFile, "propertyName", fallbackValue);
		assertEquals(fallbackValue, result);
	}

	@Test
	public void getPropertyFromConfig_propertyDoesNotExist() throws IOException {
		final Path rootPath = _temp.toPath();
		// create file
		final Path configFile = rootPath.resolve(FILE_FS_SERVER_CONF);
		final String fallbackValue = "defaultValue";
		final Properties properties = new Properties();
		properties.setProperty("myValue", "customValue");
		try (final OutputStream outputStream = new FileOutputStream(configFile.toFile())) {
			properties.store(outputStream, null);
		}
		final String result = FsUtil.getPropertyFromConfig(configFile, "propertyName", fallbackValue);
		assertEquals(fallbackValue, result);
	}

	@Test
	public void getPropertyFromConfig_propertyExists() throws IOException {
		final Path rootPath = _temp.toPath();
		// create file
		final Path configFile = rootPath.resolve(FILE_FS_SERVER_CONF);
		final Properties properties = new Properties();
		final String expectedValue = "customValue";
		properties.setProperty("propertyName", expectedValue);
		try (final OutputStream outputStream = new FileOutputStream(configFile.toFile())) {
			properties.store(outputStream, null);
		}
		final String result = FsUtil.getPropertyFromConfig(configFile, "propertyName", "defaultValue");
		assertEquals(expectedValue, result);
	}

	@Test
	public void getHostFromConfig_defaultValue() throws IOException {
		final Path rootPath = _temp.toPath();
		// create file
		final Path confDir = rootPath.resolve(DIR_CONF);
		FileUtil.mkDirs(confDir);
		final Path configFile = confDir.resolve(FILE_FS_SERVER_CONF);
		final Properties properties = new Properties();
		properties.setProperty("anotherProperty", "anotherValue");
		try (final OutputStream outputStream = new FileOutputStream(configFile.toFile())) {
			properties.store(outputStream, null);
		}
		final String result = FsUtil.getHostFromConfig(rootPath);
		assertEquals(FsUtil.VALUE_DEFAULT_HOST, result);
	}

	@Test
	public void getHostFromConfig_customValue() throws IOException {
		final Path rootPath = _temp.toPath();
		// create file
		final Path confDir = rootPath.resolve(DIR_CONF);
		FileUtil.mkDirs(confDir);
		final Path configFile = confDir.resolve(FILE_FS_SERVER_CONF);
		final Properties properties = new Properties();
		final String expectedValue = "myHost";
		properties.setProperty("HOST", expectedValue);
		try (final OutputStream outputStream = new FileOutputStream(configFile.toFile())) {
			properties.store(outputStream, null);
		}
		final String result = FsUtil.getHostFromConfig(rootPath);
		assertEquals(expectedValue, result);
	}

	@Test
	public void getPortFromConfig_defaultValue_HTTP() throws IOException {
		final Path rootPath = _temp.toPath();
		// create file
		final Path confDir = rootPath.resolve(DIR_CONF);
		FileUtil.mkDirs(confDir);
		final Path configFile = confDir.resolve(FILE_FS_SERVER_CONF);
		final Properties properties = new Properties();
		properties.setProperty("anotherProperty", "anotherValue");
		try (final OutputStream outputStream = new FileOutputStream(configFile.toFile())) {
			properties.store(outputStream, null);
		}
		final int result = FsUtil.getPortFromConfig(rootPath, FsConnectionType.HTTP);
		assertEquals(FsConnectionType.HTTP.getDefaultPort(), result);
	}

	@Test
	public void getPortFromConfig_customValue_NAN_HTTP() throws IOException {
		final Path rootPath = _temp.toPath();
		// create file
		final Path confDir = rootPath.resolve(DIR_CONF);
		FileUtil.mkDirs(confDir);
		final Path configFile = confDir.resolve(FILE_FS_SERVER_CONF);
		final Properties properties = new Properties();
		properties.setProperty("HTTP_PORT", "NOT_A_NUMBER");
		try (final OutputStream outputStream = new FileOutputStream(configFile.toFile())) {
			properties.store(outputStream, null);
		}
		final int result = FsUtil.getPortFromConfig(rootPath, FsConnectionType.HTTP);
		assertEquals(FsConnectionType.HTTP.getDefaultPort(), result);
	}

	@Test
	public void getPortFromConfig_customValue_HTTP() throws IOException {
		final Path rootPath = _temp.toPath();
		// create file
		final Path confDir = rootPath.resolve(DIR_CONF);
		FileUtil.mkDirs(confDir);
		final Path configFile = confDir.resolve(FILE_FS_SERVER_CONF);
		final Properties properties = new Properties();
		final int portValue = 1234;
		properties.setProperty("HTTP_PORT", String.valueOf(portValue));
		try (final OutputStream outputStream = new FileOutputStream(configFile.toFile())) {
			properties.store(outputStream, null);
		}
		final int result = FsUtil.getPortFromConfig(rootPath, FsConnectionType.HTTP);
		assertEquals(portValue, result);
	}

	@Test
	public void getPortFromConfig_defaultValue_HTTPS() throws IOException {
		final Path rootPath = _temp.toPath();
		// create file
		final Path confDir = rootPath.resolve(DIR_CONF);
		FileUtil.mkDirs(confDir);
		final Path configFile = confDir.resolve(FILE_FS_SERVER_CONF);
		final Properties properties = new Properties();
		properties.setProperty("anotherProperty", "anotherValue");
		try (final OutputStream outputStream = new FileOutputStream(configFile.toFile())) {
			properties.store(outputStream, null);
		}
		final int result = FsUtil.getPortFromConfig(rootPath, FsConnectionType.HTTPS);
		assertEquals(FsConnectionType.HTTPS.getDefaultPort(), result);
	}

	@Test
	public void getPortFromConfig_customValue_NAN_HTTPS() throws IOException {
		final Path rootPath = _temp.toPath();
		// create file
		final Path confDir = rootPath.resolve(DIR_CONF);
		FileUtil.mkDirs(confDir);
		final Path configFile = confDir.resolve(FILE_FS_SERVER_CONF);
		final Properties properties = new Properties();
		properties.setProperty("HTTP_PORT", "NOT_A_NUMBER");
		try (final OutputStream outputStream = new FileOutputStream(configFile.toFile())) {
			properties.store(outputStream, null);
		}
		final int result = FsUtil.getPortFromConfig(rootPath, FsConnectionType.HTTPS);
		assertEquals(FsConnectionType.HTTPS.getDefaultPort(), result);
	}

	@Test
	public void getPortFromConfig_customValue_HTTPS() throws IOException {
		final Path rootPath = _temp.toPath();
		// create file
		final Path confDir = rootPath.resolve(DIR_CONF);
		FileUtil.mkDirs(confDir);
		final Path configFile = confDir.resolve(FILE_FS_SERVER_CONF);
		final Properties properties = new Properties();
		final int portValue = 1234;
		properties.setProperty("HTTP_PORT", String.valueOf(portValue));
		try (final OutputStream outputStream = new FileOutputStream(configFile.toFile())) {
			properties.store(outputStream, null);
		}
		final int result = FsUtil.getPortFromConfig(rootPath, FsConnectionType.HTTPS);
		assertEquals(portValue, result);
	}

	@Test
	public void getPortFromConfig_defaultValue_SOCKET() throws IOException {
		final Path rootPath = _temp.toPath();
		// create file
		final Path confDir = rootPath.resolve(DIR_CONF);
		FileUtil.mkDirs(confDir);
		final Path configFile = confDir.resolve(FILE_FS_SERVER_CONF);
		final Properties properties = new Properties();
		properties.setProperty("anotherProperty", "anotherValue");
		try (final OutputStream outputStream = new FileOutputStream(configFile.toFile())) {
			properties.store(outputStream, null);
		}
		final int result = FsUtil.getPortFromConfig(rootPath, FsConnectionType.SOCKET);
		assertEquals(FsConnectionType.SOCKET.getDefaultPort(), result);
	}

	@Test
	public void getPortFromConfig_customValue_NAN_SOCKET() throws IOException {
		final Path rootPath = _temp.toPath();
		// create file
		final Path confDir = rootPath.resolve(DIR_CONF);
		FileUtil.mkDirs(confDir);
		final Path configFile = confDir.resolve(FILE_FS_SERVER_CONF);
		final Properties properties = new Properties();
		properties.setProperty("SOCKET_PORT", "NOT_A_NUMBER");
		try (final OutputStream outputStream = new FileOutputStream(configFile.toFile())) {
			properties.store(outputStream, null);
		}
		final int result = FsUtil.getPortFromConfig(rootPath, FsConnectionType.SOCKET);
		assertEquals(FsConnectionType.SOCKET.getDefaultPort(), result);
	}

	@Test
	public void getPortFromConfig_customValue_SOCKET() throws IOException {
		final Path rootPath = _temp.toPath();
		// create file
		final Path confDir = rootPath.resolve(DIR_CONF);
		FileUtil.mkDirs(confDir);
		final Path configFile = confDir.resolve(FILE_FS_SERVER_CONF);
		final Properties properties = new Properties();
		final int portValue = 1234;
		properties.setProperty("SOCKET_PORT", String.valueOf(portValue));
		try (final OutputStream outputStream = new FileOutputStream(configFile.toFile())) {
			properties.store(outputStream, null);
		}
		final int result = FsUtil.getPortFromConfig(rootPath, FsConnectionType.SOCKET);
		assertEquals(portValue, result);
	}

}
