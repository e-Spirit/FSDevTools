/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2016 e-Spirit AG
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

package com.espirit.moddev.cli.commands.server;

import com.espirit.moddev.cli.CliConstants;
import com.espirit.moddev.connection.FsConnectionType;
import com.espirit.moddev.shared.annotation.VisibleForTesting;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.restrictions.AllowedRawValues;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractServerCommand {

	@Option(type = OptionType.GLOBAL, name = {"-u", "--user"}, description = "FirstSpirit user. Default is Admin.", title = "userName")
	private String _user = CliConstants.DEFAULT_USER.value();

	@Option(type = OptionType.GLOBAL, name = {"-pwd", "--password"}, description = "FirstSpirit user's password. Default is Admin.", title = "password")
	private String _password = CliConstants.DEFAULT_USER.value();

	@Option(type = OptionType.GLOBAL, name = {"-c", "--conn-mode"}, description = "FirstSpirit connection mode. Default is HTTP.", title = "mode")
	@AllowedRawValues(allowedValues = {"HTTP", "HTTPS", "SOCKET"})
	private FsConnectionType _fsMode = FsConnectionType.HTTP;

	@NotNull
	public String getUser() {
		return _user;
	}

	@VisibleForTesting
	void setUser(@NotNull final String user) {
		_user = user;
	}

	@NotNull
	public String getPassword() {
		return _password;
	}

	@VisibleForTesting
	void setPassword(@NotNull final String password) {
		_password = password;
	}

	@NotNull
	public FsConnectionType getFsMode() {
		return _fsMode;
	}

	@VisibleForTesting
	void setFsMode(@NotNull final FsConnectionType fsMode) {
		_fsMode = fsMode;
	}

}
