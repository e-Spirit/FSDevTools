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

package com.espirit.moddev.cli.commands.module.configureCommand.json.components;

import com.espirit.moddev.cli.api.configuration.Config;
import com.espirit.moddev.shared.StringUtils;
import de.espirit.firstspirit.access.Connection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Context object used in a {@link Configurable}.
 */
public class ConfigurationContext {

	private final Connection _connection;
	private final Config _config;

	public ConfigurationContext(@NotNull final Connection connection, @NotNull final Config config) {
		_config = config;
		_connection = connection;
	}

	@NotNull
	public Connection getConnection() {
		return _connection;
	}

	@Nullable
	public String getGlobalProjectName() {
		final String project = _config.getProject();
		return StringUtils.isNullOrEmpty(project) ? null : project.trim();
	}

}
