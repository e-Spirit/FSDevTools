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

package com.espirit.moddev.cli.commands.server.stopCommand;

import com.espirit.moddev.cli.commands.server.ServerCommandGroup;
import com.espirit.moddev.cli.commands.server.ServerCommandNames;
import com.espirit.moddev.cli.commands.server.common.AbstractServerCommand;
import com.espirit.moddev.cli.commands.server.utils.ServerRunner;
import com.espirit.moddev.cli.results.SimpleResult;
import com.espirit.moddev.connection.FsConnectionConfig;
import org.jetbrains.annotations.VisibleForTesting;
import com.espirit.moddev.util.FsUtil;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.help.Examples;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

@Command(name = ServerCommandNames.STOP, groupNames = ServerCommandGroup.NAME, description = "Stops a FirstSpirit server. Needs an fs-access.jar on the classpath. " +
		"WARNING: If you execute commands asynchronously, you may end up in unpredictable behavior.")
@Examples(examples =
		{
				"server stop -sr \"D:\\FirstSpirit5.2.2001\"",
				"server stop",
				"server stop -h localhost -port 9000"
		},
		descriptions = {
				"Scans the configuration of the server and stops it.",
				"Stops the remote server running on localhost:8000.",
				"Stops the remote server running on localhost:9000."
		})
public class ServerStopCommand extends AbstractServerCommand implements com.espirit.moddev.cli.api.command.Command<SimpleResult<Boolean>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServerStopCommand.class);

	@VisibleForTesting
	static final String MSG_ERROR = "The server couldn't be stopped!";
	@VisibleForTesting
	static final String MSG_LOCK_FILE_NOT_FOUND = "Server lock file not found! Server seems to be offline.";
	@VisibleForTesting
	static final String MSG_USING_REMOTE_HOST_PORT = "Using remote host:port '{}://{}:{}'...";
	@VisibleForTesting
	static final String MSG_USING_LOCAL_HOST_PORT = "Using local host:port '{}://{}:{}' from fs-server.conf...";

	@Option(name = {"-sr", "--server-root"}, description = "A FirstSpirit server's installation directory.", title = "serverRoot")
	private String _serverDir;
	@Option(name = {"-h", "--host"}, description = "The host to connect to.", title = "host")
	private String _host;
	@Option(name = {"-port", "--port"}, description = "The port to connect to.", title = "port")
	private int _port;

	@Override
	public SimpleResult<Boolean> call() {
		try {
			if (getServerDir() == null) {
				if (getHost() == null || getPort() == 0) {
					throw new IllegalStateException("Incomplete parameters. The server directory or host/port must be defined.");
				}
			}
			final FsConnectionConfig config = new FsConnectionConfig();
			config.setConnectionType(getFsMode());
			config.setUser(getUser());
			config.setPassword(getPassword());
			if (getServerDir() != null) {
				if (!updateLocalConfig(config)) {
					return new SimpleResult<>(true);
				}
			} else {
				updateRemoteConfig(config);
			}
			final ServerRunner serverRunner = getServerRunner(_serverDir);
			serverRunner.stop(config);
			return new SimpleResult<>(true);
		} catch (final Exception e) {
			return new SimpleResult<>(new IllegalStateException(MSG_ERROR, e));
		}
	}

	@VisibleForTesting
	void updateRemoteConfig(@NotNull final FsConnectionConfig config) {
		LOGGER.info(MSG_USING_REMOTE_HOST_PORT, getFsMode().name(), getHost(), getPort());
		config.setHost(getHost());
		config.setPort(getPort());
	}

	@VisibleForTesting
	boolean updateLocalConfig(@NotNull final FsConnectionConfig config) {
		// pre-condition: server dir must exist
		if (getServerDir() == null) {
			throw new IllegalStateException("Server dir not set!");
		}
		// check if the server is running by checking the existance of the .fs.lock file
		final Path serverDir = Paths.get(getServerDir()).toAbsolutePath();
		if (!FsUtil.lockFileExists(serverDir)) {
			LOGGER.warn(MSG_LOCK_FILE_NOT_FOUND);
			return false;
		}
		final String host = FsUtil.getHostFromConfig(serverDir);
		final int port = FsUtil.getPortFromConfig(serverDir, getFsMode());
		config.setHost(host);
		config.setPort(port);
		LOGGER.info(MSG_USING_LOCAL_HOST_PORT, getFsMode().name(), host, port);
		return true;
	}

	@VisibleForTesting
	@NotNull
	ServerRunner getServerRunner(@Nullable final String serverDir) {
		if (serverDir == null) {
			return new ServerRunner();
		} else {
			return new ServerRunner(Paths.get(serverDir));
		}
	}

	@VisibleForTesting
	@Nullable
	String getHost() {
		return _host;
	}

	@VisibleForTesting
	void setHost(@Nullable final String host) {
		_host = host;
	}

	@VisibleForTesting
	int getPort() {
		return _port;
	}

	@VisibleForTesting
	void setPort(final int port) {
		_port = port;
	}

	@VisibleForTesting
	@Nullable
	String getServerDir() {
		return _serverDir;
	}

	@VisibleForTesting
	public void setServerDir(@Nullable final String serverDir) {
		_serverDir = serverDir;
	}

}
