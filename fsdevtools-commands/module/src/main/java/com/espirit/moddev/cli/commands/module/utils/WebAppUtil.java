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

package com.espirit.moddev.cli.commands.module.utils;

import com.espirit.moddev.cli.api.result.ExecutionErrorResult;
import com.espirit.moddev.cli.api.result.ExecutionResult;
import com.espirit.moddev.cli.api.result.ExecutionResults;
import com.espirit.moddev.shared.annotation.VisibleForTesting;
import com.espirit.moddev.shared.webapp.WebAppIdentifier;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.agency.GlobalWebAppId;
import de.espirit.firstspirit.agency.ModuleAdminAgent;
import de.espirit.firstspirit.agency.WebAppId;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static de.espirit.firstspirit.access.ConnectionManager.SOCKET_MODE;

/**
 * Utility class for things related to FirstSpirit web apps.
 */
public class WebAppUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(WebAppUtil.class);

	@VisibleForTesting
	static final String SOCKET_FS_5_ROOT_ERROR_MESSAGE = "Cannot use a non socket connection to deploy the FirstSpirit root web app. You must use SOCKET as connection mode.";

	/**
	 * Deploys the given {@link WebAppId web apps} and returns the {@link ExecutionResults results}.
	 *
	 * @param connection                   the {@link Connection} to use
	 * @param webAppsToDeploy              the {@link WebAppId web apps} that should be deployed
	 * @return the {@link ExecutionResults results} for the deployment
	 */
	@NotNull
	public static ExecutionResults deployWebApps(@NotNull final Connection connection, @NotNull Collection<WebAppId> webAppsToDeploy) {
		final ExecutionResults results = new ExecutionResults();
		if (webAppsToDeploy.isEmpty()) {
			LOGGER.info("No web apps to deploy.");
			return results;
		}

		// distinct web apps (using WebAppId#equals, which is correctly implemented)
		final List<WebAppId> distinctWebApps = webAppsToDeploy.stream().distinct().collect(Collectors.toList());

		// finally deploy
		final ModuleAdminAgent moduleAdminAgent = connection.getBroker().requireSpecialist(ModuleAdminAgent.TYPE);
		LOGGER.info("Deploying web apps [ {} ]...", distinctWebApps.stream().map(WebAppIdentifier::getName).collect(Collectors.joining(", ")));
		for (final WebAppId webAppId : distinctWebApps) {
			// check for fs5root web app if the connection mode != SOCKET
			if (isRootWebAppAndNotInSocketMode(connection, webAppId)) {
				LOGGER.error(SOCKET_FS_5_ROOT_ERROR_MESSAGE);
				results.add(new RootWebAppDeployNotAllowedResult());
				continue;
			}

			// deploy the web app
			LOGGER.info("Deploying web app '{}'...", WebAppIdentifier.getName(webAppId));
			if (moduleAdminAgent.deployWebApp(webAppId)) {
				LOGGER.info("Successfully deployed web app '{}'.", WebAppIdentifier.getName(webAppId));
				results.add(new WebAppDeployedResult(webAppId));
			} else {
				LOGGER.error("Error deploying web app '{}'!", WebAppIdentifier.getName(webAppId));
				results.add(new WebAppDeployFailedResult(webAppId));
			}
		}
		LOGGER.info("Web apps [ {} ] successfully deployed.", distinctWebApps.stream().map(WebAppIdentifier::getName).collect(Collectors.joining(", ")));
		return results;
	}

	@VisibleForTesting
	static boolean isRootWebAppAndNotInSocketMode(@NotNull final Connection connection, @NotNull final WebAppId webAppId) {
		if (SOCKET_MODE == connection.getMode()) {
			return false;
		}
		return WebAppIdentifier.isFs5RootWebApp(webAppId);
	}

	@VisibleForTesting
	static class RootWebAppDeployNotAllowedResult implements ExecutionErrorResult<IllegalStateException> {

		private final IllegalStateException _exception;

		public RootWebAppDeployNotAllowedResult() {
			_exception = new IllegalStateException(toString());
		}

		@NotNull
		@Override
		public IllegalStateException getException() {
			return _exception;
		}

		@Override
		public String toString() {
			return SOCKET_FS_5_ROOT_ERROR_MESSAGE;
		}

	}

	@VisibleForTesting
	public static class WebAppDeployedResult implements ExecutionResult {

		@VisibleForTesting
		static final String MESSAGE = "Successfully deployed web app '%s'.";

		private final WebAppId _webAppId;

		public WebAppDeployedResult(@NotNull final WebAppId webAppId) {
			_webAppId = webAppId;
		}

		@Override
		public String toString() {
			return String.format(MESSAGE, WebAppIdentifier.getName(_webAppId));
		}

	}

	@VisibleForTesting
	static class WebAppDeployFailedResult implements ExecutionErrorResult<IllegalStateException> {

		@VisibleForTesting
		static final String MESSAGE = "Error deploying web app '%s'!";

		private final WebAppId _webAppId;
		private final IllegalStateException _exception;

		public WebAppDeployFailedResult(@NotNull final WebAppId webAppId) {
			_webAppId = webAppId;
			_exception = new IllegalStateException(toString());
		}

		@NotNull
		@Override
		public IllegalStateException getException() {
			return _exception;
		}

		@Override
		public String toString() {
			return String.format(MESSAGE, WebAppIdentifier.getName(_webAppId));
		}

	}

}
