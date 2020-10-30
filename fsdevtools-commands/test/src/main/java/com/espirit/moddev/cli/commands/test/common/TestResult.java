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

package com.espirit.moddev.cli.commands.test.common;

import com.espirit.moddev.cli.configuration.GlobalConfig;
import com.espirit.moddev.cli.results.SimpleResult;
import org.jetbrains.annotations.NotNull;

/**
 * Specialization of {@link SimpleResult} that can be used in conjunction with test project commands.
 *
 * @author e-Spirit AG
 */
public class TestResult extends SimpleResult<Void> {

	private final GlobalConfig _config;

	/**
	 * Creates a new instance using the given config and exception.
	 *
	 * @param config Config that was used for command execution
	 * @param e      Optional exception that was produced by the command
	 * @throws IllegalArgumentException if config is null
	 */
	public TestResult(@NotNull final GlobalConfig config, @NotNull final Exception e) {
		super(e);
		_config = config;
	}

	/**
	 * Creates a new instance using the given config and exception.
	 *
	 * @param config Config that was used for config execution
	 * @throws IllegalArgumentException if config is null
	 */
	public TestResult(@NotNull GlobalConfig config) {
		super();
		_config = config;
	}

	@Override
	public void log() {
		LOGGER.info("#########");
		if (isError()) {
			LOGGER.info("Test was not successful");
			LOGGER.error(exception.getMessage());
			if (_config.isError()) {
				LOGGER.error("", exception);
			}
		} else {
			LOGGER.info("Test was successful");
		}
		LOGGER.info("######### Supplied config parameters:");
		LOGGER.info("User: " + _config.getUser());
		LOGGER.info("Host: " + _config.getHost());
		LOGGER.info("Port: " + _config.getPort());
		LOGGER.info("Servlet zone: " + _config.getServletZone());
		LOGGER.info("Connection Mode: " + _config.getConnectionMode());
		LOGGER.info("Project: " + _config.getProject());
	}

}
