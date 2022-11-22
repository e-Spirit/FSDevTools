/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2022 Crownpeak Technology GmbH
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

package com.espirit.moddev.cli.commands.server.utils;

import com.espirit.moddev.connection.FsConnection;
import com.espirit.moddev.connection.FsConnectionConfig;
import com.espirit.moddev.connection.FsConnectionType;
import com.espirit.moddev.util.FsUtil;
import com.espirit.moddev.util.OsUtil;
import de.espirit.firstspirit.access.AdminService;
import de.espirit.firstspirit.agency.RunLevelAgent;
import de.espirit.firstspirit.server.RunLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class ServerRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServerRunner.class);

	private static final String PROCESS_PROBLEM = "Problem gathering data from FirstSpirit server process!";
	private static final Duration LOG_LOOKUP_RETRY_WAIT = Duration.ofSeconds(1);

	@Nullable
	private final Path _serverDir;
	private Duration _timeout = Duration.ofMinutes(10);
	private RunLevel _runLevel = RunLevel.STARTED;

	private final ExecutorService _executorService;
	private final AtomicReference<Future<Optional<Process>>> _serverTask = new AtomicReference<>();
	private FsConnectionType _connectionType = FsConnectionType.HTTP;

	private String _user;
	private String _password;
	private final AtomicBoolean _wrapperExecutionFailedToExecute = new AtomicBoolean();

	public ServerRunner() {
		_serverDir = null;
		_executorService = Executors.newCachedThreadPool();
	}

	public ServerRunner(@NotNull final Path serverDir) {
		_serverDir = serverDir.toAbsolutePath();
		_executorService = Executors.newCachedThreadPool();
	}

	public void setTimeout(@NotNull final Duration timeout) {
		_timeout = timeout;
	}

	public void setUserCredentials(@NotNull final String user, @NotNull final String password) {
		_user = user;
		_password = password;
	}

	public void setConnectionType(@NotNull final FsConnectionType connectionType) {
		_connectionType = connectionType;
	}

	public void setRunLevel(@NotNull final RunLevel runLevel) {
		_runLevel = runLevel;
	}

	/**
	 * Waits for a given condition, retrying if necessary, blocking the thread in between.
	 *
	 * @param condition the condition to be checked
	 * @param waitTime  the time to wait between queries to `condition`
	 * @param triesLeft the number of tries that should be used at max until the condition needs to be true. Should be larger than 0.
	 * @return the value of the last call of `condition`.
	 */
	static boolean waitForCondition(@NotNull final Supplier<Boolean> condition, @NotNull final Duration waitTime, final int triesLeft) {
		if (triesLeft > 0) {
			if (condition.get()) {
				return true;
			} else {
				try {
					Thread.sleep(waitTime.toMillis());
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt(); //reset interrupt flag
				}
				return waitForCondition(condition, waitTime, triesLeft - 1);
			}
		} else {
			return false;
		}
	}

	@NotNull
	private Path getServerDir() {
		if (_serverDir == null) {
			throw new NullPointerException("serverDir is null");
		}
		return _serverDir;
	}

	public void start() throws IOException {
		// start FirstSpirit server ...
		startFirstSpiritServer();

		// ... and finally wait for a successful connection
		final FsConnection connection = new FsConnection(_connectionType, FsUtil.getPortFromConfig(getServerDir(), _connectionType), false);
		connection.setUserCredentials(_user, _password);
		final AtomicLong lastCheck = new AtomicLong(0);
		final long fiveSeconds = Duration.ofSeconds(5).toMillis();
		final int retryCount = (int) (_timeout.toMillis() / LOG_LOOKUP_RETRY_WAIT.toMillis());
		if (!waitForCondition(() -> {
			if (wrapperFailedToExecute()) {
				throw new IllegalStateException("Wrapper failed unexpectedly! See fs-wrapper.log for details...");
			}
			final boolean printMessage = lastCheck.get() + fiveSeconds < System.currentTimeMillis();
			if (printMessage) {
				lastCheck.set(System.currentTimeMillis());
			}
			if (!connection.isConnected()) {
				if (printMessage) {
					LOGGER.info("Waiting for connection to FirstSpirit server...");
				}
				connection.connect();
				if (connection.isConnected()) {
					LOGGER.info("Connection to FirstSpirit established.");
				}
				return false;
			} else {
				if (printMessage) {
					LOGGER.info("Waiting for server to complete the startup process...");
				}
				final RunLevelAgent runLevelAgent = connection.getBroker().requestSpecialist(RunLevelAgent.TYPE);
				return runLevelAgent.getRunLevel().level() >= _runLevel.level();
			}
		}, LOG_LOOKUP_RETRY_WAIT, retryCount)) {
			throw new IllegalStateException("Could not detect a started FirstSpirit server!");
		} else {
			LOGGER.info("Server successfully started.");
		}
	}

	private boolean wrapperFailedToExecute() {
		return _wrapperExecutionFailedToExecute.get();
	}

	public void stop(@NotNull final FsConnectionConfig config) throws IOException {
		final FsConnection connection = new FsConnection(config, true);
		connection.connect();
		if (connection.isConnected()) {
			try {
				// initiate shutdown via AdminService
				LOGGER.info("Initiating shutdown...");
				final AdminService adminService = connection.getService(AdminService.class);
				connection.setExceptionHandler((serverConnection, e) -> {/* exception is expected, because FirstSpirit server went down */});
				adminService.stopServer();
				connection.disconnect();
				// wait for the connection to get closed (maximum: 2 minutes)
				final int connectionRetryCount = (int) (Duration.ofMinutes(2).toMillis() / LOG_LOOKUP_RETRY_WAIT.toMillis());
				if (waitForCondition(() -> !connection.isConnected(), LOG_LOOKUP_RETRY_WAIT, connectionRetryCount)) {
					LOGGER.info("Connection disconnected.");
				} else {
					throw new IOException("Server shutdown failed, server may still be running...");
				}
				// wait for the ".fs.lock" file to get deleted (maximum: 3 minutes)
				if (_serverDir != null) {
					LOGGER.info("Server shutdown initiated. Waiting for server to shutdown...");
					final int lockFileRetryCount = (int) (Duration.ofMinutes(3).toMillis() / LOG_LOOKUP_RETRY_WAIT.toMillis());
					if (waitForCondition(() -> !FsUtil.lockFileExists(_serverDir), LOG_LOOKUP_RETRY_WAIT, lockFileRetryCount)) {
						LOGGER.info("FirstSpirit server shutdown completed!");
					} else {
						throw new IOException("Server shutdown initiated but the server is still shutting down. Server may hang on shutdown...");
					}
				} else {
					LOGGER.info("Remote server shutdown initiated. Server may take some time to shutdown successfully.");
				}
			} catch (final IllegalStateException e) {
				LOGGER.error("An unknown error occurred, server may still be running...");
				throw e;
			} catch (final Exception e) {
				// possible exception, because FirstSpirit server went down
			}
		} else {
			LOGGER.error("Could not connect to FirstSpirit server, maybe the server is not started.");
		}
	}

	/**
	 * Prepare system and generate startup parameter list. Performs side-effects on the pidFile system.
	 *
	 * @return startup parameter list
	 */
	@NotNull
	private List<String> constructExecuteCommand() {
		// Try fs5 first
		Path executable = getServerDir().resolve(FsUtil.DIR_BIN).resolve(FsUtil.FILE_WRAPPER_EXECUTABLE).toAbsolutePath();
		if (Files.notExists(executable)) {
			// switch to fs-server if legacy script is not present
			executable = getServerDir().resolve(FsUtil.DIR_BIN).resolve(FsUtil.FILE_FS_SERVER_EXECUTABLE).toAbsolutePath();
			if (Files.notExists(executable)) {
				throw new IllegalStateException("Neither fs5 nor fs-server file exists");
			}
		}
		List<String> commands = new ArrayList<>();
		commands.add(executable.toString());
		commands = com.espirit.moddev.util.OsUtil.convertForCurrentOs(commands);
		return commands;
	}

	private synchronized void startFirstSpiritServer() {
		// check if the ".fs.lock"-file exists
		if (FsUtil.lockFileExists(getServerDir())) {
			throw new IllegalStateException("Server lock file already exists! Server seems to be running.");
		}

		// check if the license-file exists
		if (!FsUtil.licenseFileExists(getServerDir())) {
			throw new IllegalStateException("License file does not exist!");
		}

		// start server only once
		_serverTask.compareAndSet(null, startFirstSpiritServer(_executorService));
	}

	@NotNull
	private Future<Optional<Process>> startFirstSpiritServer(@NotNull final ExecutorService executor) {
		final boolean isWindows = OsUtil.isWindows();
		final List<String> commands = constructExecuteCommand();
		if (isWindows) {
			commands.add("console");
		} else {
			commands.add("start");
		}
        /*
           Constructs an overseer-thread to check if the wrapper has been stopped unexpectedly.
           This is done by checking if the WRAPPER_ERROR.txt exists in the server directory. This file
           will be created by the wrapper (based on wrapper-events - configured in the wrapper.conf) if the
           wrapper stops unexpectedly.

          @see ServerConfigurator#updateWrapperConfFile
          @see ServerConfigurator#addWrapperEvent
         */
		_wrapperExecutionFailedToExecute.set(false);
		if (_serverDir != null) {
			final Path errorFilePath = _serverDir.resolve(FsUtil.FILE_WRAPPER_EXCEPTION_FILE);
			final File errorFile = errorFilePath.toFile();
			if (errorFile.exists()) {
				if (!errorFile.delete()) {
					throw new IllegalStateException("Could not delete " + errorFilePath.toAbsolutePath() + "!");
				}
			}
			executor.submit(() -> {
				while (true) {
					if (errorFile.exists()) {
						_wrapperExecutionFailedToExecute.set(true);
						return;
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			});
		}
		/*
		   Construct a cancellable logging task. It will be cancelled in `stopFirstSpiritServer`.
           The inner logTask is necessary since the implicit `readLine()` on the `BufferedReader` has a blocking API that cannot be interrupted. This
           task is stopped by destroying the process outputting data, which implicitly closes the input stream that is being blocked on. You can view
           future as an entity that does the very same job as logTask with the added functionality of gracefully shutting down on server
           stop.
         */
		return executor.submit(() -> {
			final ProcessBuilder builder = new ProcessBuilder(commands);
			builder.redirectErrorStream(true);
			if (_serverDir != null) {
				final Path legacyServerJar = _serverDir.resolve(FsUtil.DIR_SERVER).resolve(FsUtil.DIR_LIB_LEGACY).resolve(FsUtil.FILE_SERVER_JAR_LEGACY);
				if (legacyServerJar.toFile().exists()) {
					builder.environment().put("FS_MODE", "legacy");
				}
			}
			builder.environment().put("JAVA_HOME", System.getenv("JAVA_HOME"));
			builder.environment().put("FS_JAVA_HOME", System.getenv("JAVA_HOME"));
			try {
				LOGGER.info("Starting server: " + String.join(" ", commands));
				final Process process = builder.start();
				//start logging on another task to be able to be interrupted to destroy the original process because it hangs sometimes
				final Future<Void> logTask = executor.submit(() -> {
					new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))
							.lines()
							.forEach(LOGGER::info);
					return null;
				});
				try {
					logTask.get();
				} catch (final InterruptedException | ExecutionException e) {
					process.destroy();  //kill the process if it did not die on its own
					Thread.currentThread().interrupt();
					LOGGER.debug(PROCESS_PROBLEM, e);
				}
				return Optional.of(process);
			} catch (final IOException e) {
				LOGGER.error(PROCESS_PROBLEM, e);
				return Optional.empty();
			}
		});
	}

}
