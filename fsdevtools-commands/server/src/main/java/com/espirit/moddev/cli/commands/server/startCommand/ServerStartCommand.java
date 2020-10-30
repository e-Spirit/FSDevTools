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

package com.espirit.moddev.cli.commands.server.startCommand;

import com.espirit.moddev.cli.commands.server.ServerCommandGroup;
import com.espirit.moddev.cli.commands.server.ServerCommandNames;
import com.espirit.moddev.cli.commands.server.common.AbstractServerCommand;
import com.espirit.moddev.cli.results.SimpleResult;
import com.espirit.moddev.cli.commands.server.utils.ServerRunner;
import com.espirit.moddev.shared.annotation.VisibleForTesting;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.help.Examples;
import com.github.rvesse.airline.annotations.restrictions.Required;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Paths;
import java.time.Duration;

/**
 * This Command can start a FirstSpirit server. Uses ServerRunner implementations to achieve
 * It makes use of its command arguments to decide which server to start.
 *
 * @author e-Spirit AG
 */
@Command(name = ServerCommandNames.START, groupNames = ServerCommandGroup.NAME, description = "Starts a FirstSpirit server in the given directory." +
		"WARNING: If you execute commands asynchronously, you may end up in unpredictable behavior.")
@Examples(examples =
		{
				"server start -sr \"D:\\FirstSpirit5.2.2001\"",
				"server start -sr \"D:\\FirstSpirit5.2.2001\" -wt 1200",
		},
		descriptions = {
				"Simply starts the server in the given path",
				"Simply starts the server in the given path - sets the timeout to 1200 seconds (20 minutes)",
		})
public class ServerStartCommand extends AbstractServerCommand implements com.espirit.moddev.cli.api.command.Command<SimpleResult<String>> {

	@VisibleForTesting
	static final String MSG_SUCCESS = "Server successfully started.";
	@VisibleForTesting
	static final String MSG_ERROR = "The server couldn't be started or it takes longer than expected (use --wait-time parameter to increase the time to wait)!";
	@VisibleForTesting
	static final long DEFAULT_WAIT_TIME = Duration.ofMinutes(10).getSeconds();

	@Required
	@Option(name = {"-sr", "--server-root"}, description = "A FirstSpirit server's installation directory.", title = "serverRoot")
	private String _serverDir;
	@Option(name = {"-wt", "--wait-time"}, description = "The time in seconds to wait for a successful connection (default: 10 minutes)", title = "waitTimeInSeconds")
	private long _waitTimeInSeconds = DEFAULT_WAIT_TIME;

	private ServerRunner _serverRunner;

	@Override
	public SimpleResult<String> call() {
		try {
			final ServerRunner serverRunner = getServerRunner();
			serverRunner.setUserCredentials(getUser(), getPassword());
			serverRunner.setConnectionType(getFsMode());
			serverRunner.setTimeout(Duration.ofSeconds(_waitTimeInSeconds));
			serverRunner.start();
			return new SimpleResult<>(MSG_SUCCESS);
		} catch (final Exception e) {
			return new SimpleResult<>(new IllegalStateException(MSG_ERROR, e));
		}
	}

	@VisibleForTesting
	@NotNull
	synchronized ServerRunner getServerRunner() {
		if (_serverRunner == null) {
			_serverRunner = new ServerRunner(Paths.get(_serverDir).toAbsolutePath());
		}
		return _serverRunner;
	}

	@VisibleForTesting
	@Nullable
	String getServerDir() {
		return _serverDir;
	}

	@VisibleForTesting
	public void setServerDir(@NotNull final String serverDir) {
		_serverDir = serverDir;
	}

	@VisibleForTesting
	long getWaitTimeInSeconds() {
		return _waitTimeInSeconds;
	}

	@VisibleForTesting
	public void setWaitTimeInSeconds(final long waitTimeInSeconds) {
		_waitTimeInSeconds = waitTimeInSeconds;
	}
}
