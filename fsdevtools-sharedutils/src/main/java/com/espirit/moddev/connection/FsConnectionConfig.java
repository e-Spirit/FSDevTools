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

package com.espirit.moddev.connection;

import com.espirit.moddev.util.FsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FsConnectionConfig {

	private FsConnectionType _connectionType = FsConnectionType.HTTP;
	private String _user = FsUtil.VALUE_DEFAULT_USER;
	private String _password = FsUtil.VALUE_DEFAULT_USER;
	private String _host = FsUtil.VALUE_DEFAULT_HOST;
	private int _port;
	private String _httpProxyHost;
	private int _httpProxyPort;

	@NotNull
	public FsConnectionType getType() {
		return _connectionType;
	}

	public void setConnectionType(@NotNull final FsConnectionType connectionType) {
		_connectionType = connectionType;
	}

	@NotNull
	public String getUser() {
		return _user;
	}

	public void setUser(@NotNull final String user) {
		_user = user;
	}

	@NotNull
	public String getPassword() {
		return _password;
	}

	public void setPassword(@NotNull final String password) {
		_password = password;
	}

	@NotNull
	public String getHost() {
		return _host;
	}

	public void setHost(@Nullable final String host) {
		_host = host == null ? FsUtil.VALUE_DEFAULT_HOST : host;
	}

	public int getPort() {
		return _port;
	}

	public void setPort(final int port) {
		_port = port;
	}

	@Nullable
	public String getHttpProxyHost() {
		return _httpProxyHost;
	}

	public void setHttpProxyHost(@Nullable final String httpProxyHost) {
		_httpProxyHost = httpProxyHost;
	}

	public int getHttpProxyPort() {
		return _httpProxyPort;
	}

	public void setHttpProxyPort(final int httpProxyPort) {
		_httpProxyPort = httpProxyPort;
	}

}
