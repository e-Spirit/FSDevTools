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

package com.espirit.moddev.server;

import com.espirit.moddev.connection.FsConnectionType;
import com.espirit.moddev.util.ArchiveUtil;
import com.espirit.moddev.util.FileUtil;
import com.espirit.moddev.util.FsUtil;
import com.espirit.moddev.util.OsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static com.espirit.moddev.util.FsUtil.DIR_CONF;
import static com.espirit.moddev.util.FsUtil.DIR_JETTY_SERVICE;
import static com.espirit.moddev.util.FsUtil.DIR_MODULES;
import static com.espirit.moddev.util.FsUtil.DIR_SERVER;
import static com.espirit.moddev.util.FsUtil.FILE_FS_LICENSE_CONF;
import static com.espirit.moddev.util.FsUtil.FILE_FS_LOGGING_CONF;
import static com.espirit.moddev.util.FsUtil.FILE_FS_LOGGING_XML;
import static com.espirit.moddev.util.FsUtil.FILE_FS_SERVER_CONF;
import static com.espirit.moddev.util.FsUtil.FILE_FS_WRAPPER_CONF;
import static com.espirit.moddev.util.FsUtil.FILE_FS_WRAPPER_ISOLATED_CONF;
import static com.espirit.moddev.util.FsUtil.FILE_JETTY_PROPERTIES;
import static com.espirit.moddev.util.FsUtil.getPortFromConfig;

public class ServerConfigurator {

	/**
	 * Represents the various levels of logging severity.
	 * The logging levels, in order of increasing severity, are:
	 * <ul>
	 *     <li>{@link #TRACE} - Finest-grained informational events. Typically used for detailed debugging.</li>
	 *     <li>{@link #DEBUG} - Informational events useful for debugging the application.</li>
	 *     <li>{@link #INFO} - Informational messages that highlight the progress of the application at a coarse-grained level.</li>
	 *     <li>{@link #WARN} - Potentially harmful situations of interest to end users or system managers that indicate potential problems.</li>
	 *     <li>{@link #ERROR} - Error events that might still allow the application to continue running.</li>
	 *     <li>{@link #FATAL} - Very severe error events that will presumably lead the application to abort.</li>
	 * </ul>
	 */
	public enum LogLevel {
		/**
		 * Finest-grained informational events. Typically used for detailed debugging.
		 */
		TRACE,

		/**
		 * Informational events useful for debugging the application.
		 */
		DEBUG,

		/**
		 * Informational messages that highlight the progress of the application at a coarse-grained level.
		 */
		INFO,

		/**
		 * Potentially harmful situations of interest to end users or system managers that indicate potential problems.
		 */
		WARN,

		/**
		 * Error events that might still allow the application to continue running.
		 */
		ERROR,

		/**
		 * Very severe error events that will presumably lead the application to abort.
		 */
		FATAL
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(ServerConfigurator.class);

	@NotNull
	private final Path _serverDir;
	@Nullable
	private Path _licenseFile;
	@NotNull
	private final Map<String, String> _serverConf;
	@NotNull
	private final Map<String, String> _loggingConf;
	@NotNull
	private final List<String> _additionalVMArgs;
	private int _xms;
	private int _xmx;
	private long _wrapperTimeout;
	private boolean _enableServerRestartOnFailure;

	private LogLevel _logLevel = LogLevel.INFO;
	private boolean _logToConsole = false;

	public ServerConfigurator(@NotNull final Path serverDir) {
		_serverDir = serverDir.toAbsolutePath();
		_serverConf = new HashMap<>();
		_loggingConf = new HashMap<>();
		_additionalVMArgs = new ArrayList<>();
		_xms = 4096;
		_xmx = 4096;
		_wrapperTimeout = 90;
		_enableServerRestartOnFailure = true;
	}

	/**
	 * Sets the {@link Path path} to the license file for this {@link ServerConfigurator configurator}. The license fill will be copied to the server/conf-directory during {@link #execute()}
	 *
	 * @param licenseFile the {@link Path path} to the license file
	 * @see #execute()
	 */
	public void setLicenseFile(@NotNull final Path licenseFile) {
		_licenseFile = licenseFile.toAbsolutePath();
	}

	/**
	 * Sets a key/value-pair for the fs-server.conf
	 *
	 * @param name  the name of the property
	 * @param value the value for the property
	 * @return the current instance of this {@link ServerConfigurator}
	 */
	@NotNull
	public ServerConfigurator addServerConfValue(@NotNull final String name, @NotNull final String value) {
		_serverConf.put(name, value);
		return this;
	}

	/**
	 * Sets the log level for the FirstSpirit server (default = {@link LogLevel#INFO}).
	 *
	 * @param logLevel the log level to use
	 * @return the current instance of this {@link ServerConfigurator configurator}
	 */
	@NotNull
	public ServerConfigurator logLevel(@NotNull final LogLevel logLevel) {
		_logLevel = logLevel;
		return this;
	}

	/**
	 * Sets whether to log the FirstSpirit server logging to the console (default = {@code false}).
	 *
	 * @param logToConsole whether to log to the console.
	 * @return the current instance of this {@link ServerConfigurator configurator}
	 */
	@NotNull
	public ServerConfigurator logToConsole(final boolean logToConsole) {
		_logToConsole = logToConsole;
		return this;
	}

	/**
	 * Sets a key/value-pair for the fs-logging.conf.
	 *
	 * @param name  the name of the property
	 * @param value the value for the property
	 * @return the current instance of this {@link ServerConfigurator configurator}
	 * @deprecated This method will be removed in the future. Use {@link #logLevel(LogLevel)} and {@link #logToConsole(boolean)} to configure the logging.
	 */
	@Deprecated
	@NotNull
	public ServerConfigurator addLoggingConfValue(@NotNull final String name, @NotNull final String value) {
		// DEVEX-654: add support for log level and log-to-console for the FirstSpirit Gradle Plugin
		if (name.equalsIgnoreCase("log4j.rootCategory")) {
			String levelValue = value.toLowerCase();
			final int lastIndex = value.lastIndexOf(", fs");
			if (lastIndex != -1) {
				levelValue = value.substring(0, lastIndex);
			}
			try {
				logLevel(LogLevel.valueOf(levelValue.toUpperCase()));
			} catch (IllegalArgumentException e) {
				LOGGER.warn(String.format("Unsupported loglevel '%s'. Using INFO logging...", levelValue));
				logLevel(LogLevel.INFO);
			}
		} else if (name.equalsIgnoreCase("log4j.appender.fs.consoleLogging")) {
			logToConsole(Boolean.parseBoolean(value));
		}
		_loggingConf.put(name, value);
		return this;
	}

	/**
	 * Adds an additional VM argument for the wrapper.conf.
	 *
	 * @param argument the vm argument to add
	 * @return the current instance of this {@link ServerConfigurator configurator}
	 */
	@NotNull
	public ServerConfigurator addAdditionalVMArg(@NotNull final String argument) {
		_additionalVMArgs.add(argument);
		return this;
	}

	@NotNull
	public ServerConfigurator setXms(final int xms) {
		_xms = xms;
		return this;
	}

	@NotNull
	public ServerConfigurator setXmx(final int xmx) {
		_xmx = xmx;
		return this;
	}

	@NotNull
	public ServerConfigurator setWrapperTimeout(final long timeout) {
		_wrapperTimeout = timeout;
		return this;
	}

	/**
	 * Configures whether the server should automatically
	 * be restarted after a crash
	 *
	 * @param enableServerRestartOnFailure {@code true}, if the JVM should be restarted after a crash,
	 *                                     {@code false} otherwise
	 * @see #execute()
	 */
	public void setEnableServerRestartOnFailure(final boolean enableServerRestartOnFailure) {
		_enableServerRestartOnFailure = enableServerRestartOnFailure;
	}

	/**
	 * Configures the server in the specified target directory.
	 *
	 * @throws IOException if an I/O error occurs
	 * @see ServerConfigurator#ServerConfigurator(Path)
	 */
	public void execute() throws IOException {
		LOGGER.info("Configuring server in '" + _serverDir.toAbsolutePath() + "'...");
		final Path serverJar = getServerJar(_serverDir);
		// update fs-server.conf, fs-logging.conf, fs-wrapper.conf and extract license file
		updateServerConf(_serverDir, _serverConf);
		updateLoggingConf(_serverDir, serverJar, _loggingConf, _logLevel, _logToConsole);
		updateJettyConf(_serverDir);
		updateWrapperConfFiles(_serverDir, _xms, _xmx, _wrapperTimeout, _enableServerRestartOnFailure, _additionalVMArgs);
		copyLicenseFile(_serverDir, _licenseFile);
		// final message
		LOGGER.info("Server in '" + _serverDir.toAbsolutePath() + "' successfully configured.");
	}

	@VisibleForTesting
	@NotNull
	static Path getServerJar(@NotNull final Path serverDir) throws IOException {
		final Path wrapperServerDir = serverDir.resolve(DIR_SERVER);
		final Path legacyServerJar = wrapperServerDir.resolve(FsUtil.DIR_LIB_LEGACY).resolve(FsUtil.FILE_SERVER_JAR_LEGACY);
		final Path isolatedServerJar = wrapperServerDir.resolve(FsUtil.DIR_LIB_ISOLATED).resolve(FsUtil.FILE_SERVER_JAR_ISOLATED);
		if (isolatedServerJar.toFile().exists()) {
			LOGGER.info("Found isolated server jar in '" + isolatedServerJar.toAbsolutePath() + "'.");
			return isolatedServerJar;
		} else if (legacyServerJar.toFile().exists()) {
			LOGGER.info("Found legacy server jar in '" + legacyServerJar.toAbsolutePath() + "'.");
			return legacyServerJar;
		}
		throw new FileNotFoundException("Server jar not found!");
	}

	@VisibleForTesting
	static void updateServerConf(@NotNull final Path serverDir, @NotNull final Map<String, String> config) throws IOException {
		final Path confDir = serverDir.resolve(DIR_CONF);
		if (!confDir.toFile().exists()) {
			throw new FileNotFoundException("Directory '" + confDir.toAbsolutePath() + "' does not exist!");
		}

		// update fs-server.conf
		final Path fsServerConf = confDir.resolve(FILE_FS_SERVER_CONF);
		updateConfFile(fsServerConf, config);
	}

	@VisibleForTesting
	static void updateLoggingConf(@NotNull final Path serverDir, @NotNull final Path serverJar, @NotNull final Map<String, String> config, @NotNull final LogLevel logLevel, final boolean logToConsole) {
		updateLoggingConf_Log4J1(serverDir, serverJar, config);
		updateLoggingConf_Log4J2(serverDir, serverJar, logLevel, logToConsole);
	}

	@VisibleForTesting
	static void updateLoggingConf_Log4J1(@NotNull final Path serverDir, @NotNull final Path serverJar, @NotNull final Map<String, String> config) {
		try {
			final Path confDir = serverDir.resolve(DIR_CONF);
			FileUtil.mkDirs(confDir);

			// decompress & update the fs-logging.conf
			final Path loggingConf = confDir.resolve(FILE_FS_LOGGING_CONF);
			ArchiveUtil.decompressJarEntry(serverJar, FILE_FS_LOGGING_CONF, loggingConf);
			updateConfFile(loggingConf, config);
		} catch (final IOException ignore) {
			// simply ignore
			LOGGER.debug(FILE_FS_LOGGING_CONF + " not found. Ignoring...");
		} catch (final Exception e) {
			LOGGER.warn("Error updating " + FILE_FS_LOGGING_CONF, e);
			throw e;
		}
	}

	@VisibleForTesting
	static void updateLoggingConf_Log4J2(@NotNull final Path serverDir, @NotNull final Path serverJar, @NotNull final LogLevel logLevel, final boolean logToConsole) {
		try {
			final Path confDir = serverDir.resolve(DIR_CONF);
			FileUtil.mkDirs(confDir);

			// decompress & update the fs-logging.xml
			final Path loggingConf = confDir.resolve(FILE_FS_LOGGING_XML);
			ArchiveUtil.decompressJarEntry(serverJar, FILE_FS_LOGGING_XML, loggingConf);
			// update fs-logging.xml
			final List<String> input = Files.readAllLines(loggingConf);
			final List<String> output = new ArrayList<>();
			input.forEach(line -> {
				String replacedLine = line.replaceAll("consoleLogging=\"false\"", "consoleLogging=\"" + logToConsole + "\"");
				replacedLine = replacedLine.replaceAll("Root level=\"INFO\"", "Root level=\"" + logLevel.name() + "\"");
				output.add(replacedLine);
			});
			Files.write(loggingConf, output);
		} catch (final IOException ignore) {
			// simply ignore
			LOGGER.debug(FILE_FS_LOGGING_XML + " not found. Ignoring...");
		} catch (final Exception e) {
			LOGGER.warn("Error updating " + FILE_FS_LOGGING_XML, e);
			throw e;
		}
	}

	@VisibleForTesting
	static void updateJettyConf(@NotNull final Path serverDir) throws IOException {
		final Path jettyDir = serverDir.resolve(DIR_CONF).resolve(DIR_MODULES).resolve(DIR_JETTY_SERVICE);
		// check if the file exists
		final Path jettyConf = jettyDir.resolve(FILE_JETTY_PROPERTIES);
		if (!jettyConf.toFile().exists()) {
			return;
		}
		// update the jetty configuration
		final Map<String, String> config = new HashMap<>();
		config.put("PORT", String.valueOf(getPortFromConfig(serverDir, FsConnectionType.HTTP)));
		updateConfFile(jettyConf, config);
	}

	@VisibleForTesting
	static void updateConfFile(@NotNull final Path targetFile, @NotNull final Map<String, String> config) throws IOException {
		final File confFile = targetFile.toFile();
		if (!confFile.exists()) {
			throw new FileNotFoundException("File '" + targetFile.toAbsolutePath() + "' does not exist!");
		}

		final String fileName = confFile.getName();

		if (config.isEmpty()) {
			LOGGER.info("'" + fileName + "' is up to date.");
			return;
		}
		LOGGER.info("Updating '" + fileName + "'...");

		// load properties from file
		final Properties properties = new Properties();
		try (final InputStream inputStream = new BufferedInputStream(new FileInputStream(confFile))) {
			LOGGER.debug("Reading '" + fileName + "'...");
			properties.load(inputStream);
		}

		// replace configured properties
		for (final Map.Entry<String, String> entry : config.entrySet()) {
			final String propertyName = entry.getKey();
			final Object currentValue = properties.get(propertyName);
			final String newValue = entry.getValue();
			if (currentValue == null) {
				LOGGER.info("Setting '" + propertyName + "' to '" + newValue + "'...");
			} else {
				LOGGER.info("Setting '" + propertyName + "' to '" + newValue + "' (previous: '" + currentValue + "')...");
			}
			properties.setProperty(propertyName, newValue);
		}

		// write back to disk
		try (final OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(confFile))) {
			LOGGER.debug("Writing '" + fileName + "'...");
			properties.store(outputStream, null);
		}
	}

	@VisibleForTesting
	static void updateWrapperConfFiles(@NotNull final Path serverDir, final int xms, final int xmx, final long wrapperTimeout, final boolean enableRestart, @NotNull final List<String> additionalVMArgs) throws IOException {
		final Path confDir = serverDir.resolve(DIR_CONF);
		if (!confDir.toFile().exists()) {
			throw new FileNotFoundException("Directory '" + confDir.toAbsolutePath() + "' does not exist!");
		}

		// build additional args
		final Map<String, String> config = new HashMap<>();
		int index = 80;
		for (final String vmArg : additionalVMArgs) {
			config.put("wrapper.java.additional." + index, vmArg);
			index++;
		}
		config.put("wrapper.java.initmemory", String.valueOf(xms));
		config.put("wrapper.java.maxmemory", String.valueOf(xmx));
		config.put("wrapper.startup.timeout", String.valueOf(wrapperTimeout));
		config.put("wrapper.disable_restarts.automatic", String.valueOf(!enableRestart));
		{
			// fs-wrapper.conf
			final Path wrapperConf = confDir.resolve(FILE_FS_WRAPPER_CONF);
			if (wrapperConf.toFile().exists()) {
				updateWrapperConfFile(wrapperConf, config);
			} else {
				LOGGER.info("File '" + wrapperConf + "' does not exist. Continuing.");
			}
		}
		{
			// fs-wrapper.isolated.conf
			final Path isolatedWrapperConf = confDir.resolve(FILE_FS_WRAPPER_ISOLATED_CONF);
			if (!isolatedWrapperConf.toFile().exists()) {
				throw new FileNotFoundException("File '" + isolatedWrapperConf.toAbsolutePath() + "' does not exist!");
			}
			updateWrapperConfFile(isolatedWrapperConf, config);
		}
	}

	@VisibleForTesting
	static void updateWrapperConfFile(@NotNull final Path wrapperConfFile, @NotNull final Map<String, String> arguments) throws IOException {
		// read original file
		final List<String> inputLines = Files.readAllLines(wrapperConfFile);
		// replace existing lines
		final List<String> outputLines = inputLines.stream().map(line -> {
			for (final Map.Entry<String, String> entry : arguments.entrySet()) {
				final String key = entry.getKey();
				if (line.startsWith(key + '=')) {
					return key + '=' + entry.getValue();
				}
			}
			return line;
		}).collect(Collectors.toList());
		// add missing lines
		for (final Map.Entry<String, String> entry : arguments.entrySet()) {
			final String key = entry.getKey();
			boolean found = false;
			for (final String line : outputLines) {
				if (line.startsWith(key + '=')) {
					found = true;
					break;
				}
			}
			if (!found) {
				outputLines.add(key + '=' + entry.getValue());
			}
		}
		addWrapperEvent(outputLines, "jvm_ping_timeout");
		addWrapperEvent(outputLines, "jvm_failed_invocation");
		addWrapperEvent(outputLines, "jvm_unexpected_exit");
		addWrapperEvent(outputLines, "jvm_deadlock");
		// save changed file
		Files.write(wrapperConfFile, outputLines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
	}

	private static void addWrapperEvent(@NotNull final List<String> outputLines, @NotNull final String eventName) {
		final String command;
		if (OsUtil.isWindows()) {
			command = "wrapper.event." + eventName + ".command.argv.1=" + FsUtil.FILE_WRAPPER_EXCEPTION_EXECUTABLE + ".bat";
		} else {
			command = "wrapper.event." + eventName + ".command.argv.1=./" + FsUtil.FILE_WRAPPER_EXCEPTION_EXECUTABLE;
		}
		outputLines.remove(command);
		outputLines.add(command);
		final String argument = "wrapper.event." + eventName + ".command.argv.2=" + eventName;
		outputLines.remove(argument);
		outputLines.add(argument);
	}

	@VisibleForTesting
	static void copyLicenseFile(@NotNull final Path serverDir, @Nullable final Path licenseFile) throws IOException {
		if (licenseFile == null) {
			throw new IllegalStateException("License file not set!");
		}
		final Path confDir = serverDir.resolve(DIR_CONF);
		FileUtil.mkDirs(confDir);
		final Path targetLicenseFile = confDir.resolve(FILE_FS_LICENSE_CONF);
		if (!licenseFile.toFile().exists()) {
			throw new FileNotFoundException("License file '" + licenseFile.toAbsolutePath() + "' does not exist!");
		}
		LOGGER.info("Copying license file to '" + targetLicenseFile.toAbsolutePath() + "'...");
		Files.copy(licenseFile, targetLicenseFile, StandardCopyOption.REPLACE_EXISTING);
	}

}
