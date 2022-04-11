package com.espirit.moddev.cli;

/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2021 e-Spirit GmbH
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

import com.espirit.moddev.cli.api.configuration.Config;
import com.espirit.moddev.cli.api.validation.DefaultConnectionConfigValidator;
import com.espirit.moddev.cli.api.validation.Violation;
import com.espirit.moddev.connection.FsConnectionType;
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
import de.espirit.firstspirit.agency.ServerInformationAgent;
import de.espirit.firstspirit.agency.SpecialistsBroker;
import de.espirit.firstspirit.common.MaximumNumberOfSessionsExceededException;
import de.espirit.firstspirit.server.authentication.AuthenticationException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

/**
 * Default builder for FirstSpirit {@link Connection}s.
 *
 * @author e-Spirit GmbH
 */
public class ConnectionBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionBuilder.class);

	private final Config _config;

	private ConnectionBuilder(@NotNull final Config config) {
		_config = Objects.requireNonNull(config, "Config is null!");
	}

	/**
	 * Creates a builder with a config.
	 *
	 * @param config the config
	 * @return the connection builder
	 */
	@NotNull
	public static ConnectionBuilder with(@NotNull final Config config) {
		return new ConnectionBuilder(config);
	}

	/**
	 * Build a FirstSpirit connection based on the initial config which is checked first.
	 *
	 * @return the FirstSpirit connection
	 */
	@NotNull
	public Connection build() {
		// validate configuration
		validateConfiguration();

		// use https, if needed
		final FsConnectionType connectionMode = _config.getConnectionMode();
		if (FsConnectionType.HTTPS == connectionMode) {
			ConnectionManager.setUseHttps(true);
		} else {
			ConnectionManager.setUseHttps(false);
		}

		// if set: use proxy for http / https
		if (!_config.getHttpProxyHost().isEmpty()) {
			if (_config.getConnectionMode() == FsConnectionType.HTTP || _config.getConnectionMode() == FsConnectionType.HTTPS) {
				LOGGER.info("Using http proxy '{}:{}'", _config.getHttpProxyHost(), _config.getHttpProxyPort());
				ConnectionManager.setProxy(new Proxy(_config.getHttpProxyHost(), _config.getHttpProxyPort()));
			}
		}

		// setup user, host & port
		final String user = _config.getUser();
		final Integer port = _config.getPort();
		final String host = _config.getHost();
		final String servletZone = _config.getServletZone();

		// logging
		final Object[] args = {host, port, user};
		LOGGER.debug("Create connection for FirstSpirit server at '{}:{}' with user '{}'...", args);

		// create connection
		final Connection connection = ConnectionManager.getConnection(host, port, connectionMode.getFsMode(), servletZone, user, _config.getPassword());
		return new DelegateConnection(connection);
	}

	private void validateConfiguration() throws IllegalStateException {
		// validate configuration
		final DefaultConnectionConfigValidator validator = new DefaultConnectionConfigValidator();
		final Set<Violation> violations = validator.validate(_config);

		// violations found --> build error message
		if (!violations.isEmpty()) {
			// build message
			final StringBuilder errorMessage = new StringBuilder("The configuration is invalid:");
			errorMessage.append(System.lineSeparator());
			for (final Violation violation : violations) {
				errorMessage.append(violation.toString());
				errorMessage.append(System.lineSeparator());
			}

			// finally throw exception
			throw new IllegalStateException(errorMessage.toString());
		}
	}

	private static final class DelegateConnection implements Connection {

		private final Connection _connection;

		private DelegateConnection(final Connection delegate) {
			_connection = delegate;
		}

		public void connect() throws IOException, AuthenticationException, MaximumNumberOfSessionsExceededException {
			_connection.connect();
			final ServerInformationAgent serverInformationAgent = _connection.getBroker().requestSpecialist(ServerInformationAgent.TYPE);
			if (serverInformationAgent != null) {
				final ServerInformationAgent.VersionInfo serverVersion = serverInformationAgent.getServerVersion();
				final ServerInformationAgent.VersionInfo.Mode mode = serverVersion.getMode();
				LOGGER.info("Connected to FirstSpirit server at {} of version {} ({})", getHost(), serverVersion.getFullVersionString(), mode);
			}
		}

		@Override
		public <T> T getService(final Class<T> aClass) throws ServiceNotFoundException {
			return _connection.getService(aClass);
		}

		@Override
		public void close() throws IOException {
			_connection.close();
		}

		public boolean isConnected() {
			return _connection.isConnected();
		}

		public void disconnect() throws IOException {
			_connection.disconnect();
		}

		public String getHost() {
			return _connection.getHost();
		}

		public int getPort() {
			return _connection.getPort();
		}

		public int getMode() {
			return _connection.getMode();
		}

		public String getServletZone() {
			return _connection.getServletZone();
		}

		public SpecialistsBroker getBroker() {
			return _connection.getBroker();
		}

		public Project[] getProjects() {
			return _connection.getProjects();
		}

		public Project getProjectByName(String s) {
			return _connection.getProjectByName(s);
		}

		public Project getProjectById(long l) {
			return _connection.getProjectById(l);
		}

		public User getUser() throws InvalidSessionException {
			return _connection.getUser();
		}

		public Object getService(String s) throws ServiceNotFoundException {
			return _connection.getService(s);
		}

		public String createTicket() {
			return _connection.createTicket();
		}

		public String createTicket(boolean b) {
			return _connection.createTicket(b);
		}

		public void removeTicket(String s) {
			_connection.removeTicket(s);
		}

		public Connection getRemoteConnection(RemoteProjectConfiguration remoteProjectConfiguration) throws IOException, AuthenticationException, MaximumNumberOfSessionsExceededException {
			return _connection.getRemoteConnection(remoteProjectConfiguration);
		}

		public boolean isRemote() {
			return _connection.isRemote();
		}

		public ExceptionHandler getExceptionHandler() {
			return _connection.getExceptionHandler();
		}

		public void setExceptionHandler(ExceptionHandler exceptionHandler) {
			_connection.setExceptionHandler(exceptionHandler);
		}

		public ServerConfiguration getServerConfiguration() {
			return _connection.getServerConfiguration();
		}

		public ClassLoader getClassLoader() {
			return _connection.getClassLoader();
		}
	}

}
