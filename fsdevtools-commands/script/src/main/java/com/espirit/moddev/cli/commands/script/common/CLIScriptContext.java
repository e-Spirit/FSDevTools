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

package com.espirit.moddev.cli.commands.script.common;

import de.espirit.common.base.Logging;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.ScriptContext;
import de.espirit.firstspirit.access.UserService;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.access.project.ProjectScriptContext;
import de.espirit.firstspirit.agency.BrokerAgent;
import de.espirit.firstspirit.agency.SpecialistType;
import de.espirit.firstspirit.agency.SpecialistsBroker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link ScriptContext} for the execution of external scripts.
 */
public class CLIScriptContext implements ProjectScriptContext {

	private static final Class<CLIScriptContext> LOGGER = CLIScriptContext.class;

	private final Connection _connection;
	private final Project _project;
	private final Map<String, Object> _properties;
	private final SpecialistsBroker _broker;
	private final UserService _userService;

	public CLIScriptContext(@NotNull final Connection connection, @Nullable final Project project, @NotNull final Map<String, String> properties) {
		_connection = connection;
		_project = project;
		_properties = new HashMap<>(properties);
		_properties.put("connection", connection);
		if (project != null) {
			_properties.put("project", project);
			_userService = _project.getUserService();
			_broker = connection.getBroker().requireSpecialist(BrokerAgent.TYPE).getBrokerByProjectId(project.getId());
			final SpecialistsBroker projectBroker = connection.getBroker().requireSpecialist(BrokerAgent.TYPE).getBrokerByProjectId(project.getId());
			if (projectBroker == null) {
				throw new IllegalStateException(String.format("Error retrieving project broker '%s'.", project.getName()));
			}
		} else {
			_userService = null;
			_broker = connection.getBroker();
		}
		_properties.put("broker", _broker);
	}

	@NotNull
	public SpecialistsBroker getBroker() {
		return _broker;
	}

	@Override
	public Connection getConnection() {
		return _connection;
	}

	@Override
	public UserService getUserService() {
		return _userService;
	}

	@Override
	public Project getProject() {
		return _project;
	}

	@Nullable
	@Override
	public Object getProperty(final String key) {
		return _properties.get(key);
	}

	@Override
	public void setProperty(@NotNull final String key, @Nullable final Object value) {
		_properties.put(key, value);
	}

	@Override
	public void removeProperty(final String key) {
		_properties.remove(key);
	}

	@Override
	public String[] getProperties() {
		return _properties.keySet().toArray(new String[0]);
	}

	@Override
	public void logDebug(final String message) {
		Logging.logDebug(message, LOGGER);
	}

	@Override
	public void logInfo(final String message) {
		Logging.logInfo(message, LOGGER);
	}

	@Override
	public void logWarning(final String message) {
		Logging.logWarning(message, LOGGER);
	}

	@Override
	public void logError(final String message) {
		Logging.logError(message, LOGGER);
	}

	@Override
	public void logError(final String message, final Throwable throwable) {
		Logging.logError(message, throwable, LOGGER);
	}

	@Override
	public boolean is(final Env environment) {
		return environment == Env.HEADLESS;
	}

	@Nullable
	@Override
	public <S> S requestSpecialist(final SpecialistType<S> specialistType) {
		return _broker.requestSpecialist(specialistType);
	}

}
