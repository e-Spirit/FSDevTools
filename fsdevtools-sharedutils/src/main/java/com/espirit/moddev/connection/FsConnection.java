package com.espirit.moddev.connection;

/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2020 e-Spirit AG
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License"),
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

import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.ConnectionManager;
import de.espirit.firstspirit.access.ExceptionHandler;
import de.espirit.firstspirit.access.InvalidSessionException;
import de.espirit.firstspirit.access.Proxy;
import de.espirit.firstspirit.access.ServerConfiguration;
import de.espirit.firstspirit.access.ServiceNotFoundException;
import de.espirit.firstspirit.access.User;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.access.project.RemoteProjectConfiguration;
import de.espirit.firstspirit.agency.SpecialistsBroker;
import de.espirit.firstspirit.common.MaximumNumberOfSessionsExceededException;
import de.espirit.firstspirit.server.authentication.AuthenticationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Default builder for FirstSpirit {@link Connection}s.
 *
 * @author e -Spirit AG
 */
public class FsConnection implements Connection {

	private static final Logger LOGGER = LoggerFactory.getLogger(FsConnection.class);

	private final FsConnectionConfig _config;
	private final boolean _logMessages;

	private Connection _connection;

	public FsConnection(@NotNull final FsConnectionConfig config, final boolean logMessages) {
		_config = config;
		_logMessages = logMessages;
	}

	public FsConnection(@NotNull final FsConnectionType connectionType, final int port, final boolean logMessages) {
		_config = new FsConnectionConfig();
		_config.setConnectionType(connectionType);
		_config.setPort(port);
		_logMessages = logMessages;
	}

	public void setUserCredentials(@NotNull final String user, @NotNull final String password) {
		_config.setUser(user);
		_config.setPassword(password);
	}

	public void setHost(@NotNull final String host) {
		_config.setHost(host);
	}

	public void setHttpProxySettings(@NotNull final String httpProxyHost, final int httpProxyPort) {
		_config.setHttpProxyHost(httpProxyHost);
		_config.setHttpProxyPort(httpProxyPort);
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Delegate methods for "de.espirit.firstspirit.access.Connection"
	//
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public synchronized void connect() {
		if (_connection != null) {
			if (_logMessages) {
				LOGGER.debug("Connection already established...");
			}
			return;
		}
		final FsConnectionType connectionType = _config.getType();
		final String host = _config.getHost();
		final int port = _config.getPort();
		final String user = _config.getUser();
		final String password = _config.getPassword();
		final Object[] args = {host, port, user};
		try {
			// use https, if needed
			if (FsConnectionType.HTTPS == connectionType) {
				ConnectionManager.setUseHttps(true);
			} else {
				ConnectionManager.setUseHttps(false);
			}

			// if set: use proxy for http / https
			if (_config.getHttpProxyHost() != null && FsConnectionType.SOCKET != connectionType) {
				ConnectionManager.setProxy(new Proxy(_config.getHttpProxyHost(), _config.getHttpProxyPort()));
			}

			// logging
			if (_logMessages) {
				LOGGER.info("Tyring to connect to FirstSpirit server at '{}:{}' with user '{}'...", args);
			}
			// create connection
			final Connection connection = ConnectionManager.getConnection(host, port, connectionType.getFsMode(), user, password);
			connection.connect();
			if (_logMessages) {
				LOGGER.info("Connection established to FirstSpirit server at '{}:{}' with user '{}'...", args);
			}
			_connection = connection;
		} catch (final Exception e) {
			if (_logMessages) {
				LOGGER.trace("Connection to FirstSpirit server failed.", e);
			}
			_connection = null;
		}
	}

	private void verifyConnection() {
		if (_connection == null) {
			throw new IllegalStateException("Not connected.");
		}
	}

	@NotNull
	@Override
	public <T> T getService(@NotNull final Class<T> serviceClass) throws ServiceNotFoundException {
		verifyConnection();
		return _connection.getService(serviceClass);
	}

	@Override
	public void close() throws IOException {
		if (_connection != null) {
			_connection.close();
		}
		_connection = null;
	}

	public boolean isConnected() {
		return _connection != null && _connection.isConnected();
	}

	public void disconnect() throws IOException {
		verifyConnection();
		_connection = null;
	}

	@NotNull
	public String getHost() {
		verifyConnection();
		return _connection.getHost();
	}

	public int getPort() {
		verifyConnection();
		return _connection.getPort();
	}

	public int getMode() {
		verifyConnection();
		return _connection.getMode();
	}

	@Nullable
	public String getServletZone() {
		verifyConnection();
		return _connection.getServletZone();
	}

	@NotNull
	public SpecialistsBroker getBroker() {
		verifyConnection();
		return _connection.getBroker();
	}

	@NotNull
	public Project[] getProjects() {
		verifyConnection();
		return _connection.getProjects();
	}

	@Nullable
	public Project getProjectByName(@NotNull final String projectName) {
		verifyConnection();
		return _connection.getProjectByName(projectName);
	}

	@Nullable
	public Project getProjectById(final long projectId) {
		verifyConnection();
		return _connection.getProjectById(projectId);
	}

	@NotNull
	public User getUser() throws InvalidSessionException {
		verifyConnection();
		return _connection.getUser();
	}

	@NotNull
	public Object getService(@NotNull final String serviceName) throws ServiceNotFoundException {
		verifyConnection();
		return _connection.getService(serviceName);
	}

	@NotNull
	public String createTicket() {
		verifyConnection();
		return _connection.createTicket();
	}

	@NotNull
	public String createTicket(final boolean oneTimeTicket) {
		verifyConnection();
		return _connection.createTicket(oneTimeTicket);
	}

	public void removeTicket(@NotNull final String ticket) {
		verifyConnection();
		_connection.removeTicket(ticket);
	}

	@NotNull
	public Connection getRemoteConnection(@NotNull final RemoteProjectConfiguration remoteProjectConfiguration) throws IOException, AuthenticationException, MaximumNumberOfSessionsExceededException {
		verifyConnection();
		return _connection.getRemoteConnection(remoteProjectConfiguration);
	}

	public boolean isRemote() {
		verifyConnection();
		return _connection.isRemote();
	}

	@Nullable
	public ExceptionHandler getExceptionHandler() {
		verifyConnection();
		return _connection.getExceptionHandler();
	}

	public void setExceptionHandler(ExceptionHandler exceptionHandler) {
		verifyConnection();
		_connection.setExceptionHandler(exceptionHandler);
	}

	@NotNull
	public ServerConfiguration getServerConfiguration() {
		verifyConnection();
		return _connection.getServerConfiguration();
	}

	@NotNull
	public ClassLoader getClassLoader() {
		verifyConnection();
		return _connection.getClassLoader();
	}

}
