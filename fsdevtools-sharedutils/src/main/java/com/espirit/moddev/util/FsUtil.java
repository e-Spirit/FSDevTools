package com.espirit.moddev.util;

import com.espirit.moddev.connection.FsConnectionType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

public enum FsUtil {

	;

	private static final Logger LOGGER = LoggerFactory.getLogger(FsUtil.class);

	public static final String FILE_SERVER_JAR_ISOLATED = "fs-isolated-server.jar";
	public static final String FILE_SERVER_JAR_LEGACY = "fs-server.jar";
	public static final String FILE_FS_SERVER_CONF = "fs-server.conf";
	public static final String FILE_FS_LOGGING_CONF = "fs-logging.conf";
	public static final String FILE_FS_LICENSE_CONF = "fs-license.conf";
	public static final String FILE_FS_WRAPPER_CONF = "fs-wrapper.conf";
	public static final String FILE_WRAPPER_EXECUTABLE = "fs5";
	public static final String FILE_WRAPPER_EXCEPTION_EXECUTABLE = "WRAPPER_ERROR";
	public static final String FILE_WRAPPER_EXCEPTION_FILE = FILE_WRAPPER_EXCEPTION_EXECUTABLE + ".txt";
	public static final String FILE_FS_WRAPPER_ISOLATED_CONF = "fs-wrapper.isolated.conf";
	public static final String FILE_SERVER_LOCK = ".fs.lock";
	public static final String FILE_JETTY_PROPERTIES = "jetty.properties";

	public static final String DIR_FIRSTSPIRIT_5 = "firstspirit5";
	public static final String DIR_SERVER = "server";
	public static final String DIR_BIN = "bin";
	public static final String DIR_LIB_ISOLATED = "lib-isolated";
	public static final String DIR_LIB_LEGACY = "lib";
	public static final String DIR_CONF = "conf";
	public static final String DIR_MODULES = "modules";
	public static final String DIR_JETTY_SERVICE = "FirstSpirit Jetty Server.JettyService";

	public static final String PROPERTY_HOST = "HOST";
	public static final String VALUE_DEFAULT_HOST = "localhost";
	public static final String VALUE_DEFAULT_USER = "Admin";

	public static boolean lockFileExists(@NotNull final Path serverDir) {
		final File serverLockFile = serverDir.resolve(FILE_SERVER_LOCK).toFile();
		return serverLockFile.exists();
	}

	public static boolean licenseFileExists(@NotNull final Path serverDir) {
		final File licenseFile = serverDir.resolve(DIR_CONF).resolve(FILE_FS_LICENSE_CONF).toFile();
		return licenseFile.exists();
	}

	public static boolean isIsolatedJar(@NotNull final Path jarFile) {
		return jarFile.getFileName().toString().contains("-isolated-");
	}

	@NotNull
	public static String getPropertyFromConfig(@NotNull final Path confFilePath, @NotNull final String propertyName, @NotNull final String defaultValue) {
		try {
			final File confFile = confFilePath.toFile();
			final String fileName = confFile.getName();
			if (!confFile.exists()) {
				throw new IllegalStateException("Config file '" + fileName + "' does not exist!");
			}
			// load properties from file
			final Properties properties = new Properties();
			try (final InputStream inputStream = new BufferedInputStream(new FileInputStream(confFile))) {
				LOGGER.debug("Reading '" + fileName + "'...");
				properties.load(inputStream);
			}
			return properties.getProperty(propertyName, defaultValue);
		} catch (final Exception e) {
			LOGGER.debug("Error fetching property '" + propertyName + "' from config, using default...", e);
			return defaultValue;
		}
	}

	@NotNull
	public static String getHostFromConfig(@NotNull final Path serverDir) {
		// read config from fs-server.conf
		return getPropertyFromConfig(serverDir.resolve(DIR_CONF).resolve(FILE_FS_SERVER_CONF), FsUtil.PROPERTY_HOST, FsUtil.VALUE_DEFAULT_HOST);
	}

	public static int getPortFromConfig(@NotNull final Path serverDir, @NotNull final FsConnectionType connectionType) {
		try {
			return Integer.parseInt(getPropertyFromConfig(serverDir.resolve(DIR_CONF).resolve(FILE_FS_SERVER_CONF), connectionType.getPropertyName() + "_PORT", String.valueOf(connectionType.getDefaultPort())));
		} catch (final Exception e) {
			LOGGER.debug("Error fetching port from fs-server.conf, using default...", e);
			return connectionType.getDefaultPort();
		}
	}

}