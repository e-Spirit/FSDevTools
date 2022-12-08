/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2022 Crownpeak Technology GmbH
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

package com.espirit.moddev.cli.api.parsing.exceptions;

import com.espirit.moddev.cli.CliConstants;
import com.espirit.moddev.cli.api.configuration.Config;
import com.espirit.moddev.cli.exception.CliError;
import com.espirit.moddev.connection.FsConnectionType;
import com.espirit.moddev.util.FsUtil;
import de.espirit.firstspirit.io.FileHandle;
import de.espirit.firstspirit.io.FileSystem;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CliErrorTest {

	private ResourceBundle bundle = ResourceBundle.getBundle(CliError.class.getSimpleName());


	@Test
	public void testToString() throws Exception {
		assertThat("Expecting a specific value", CliError.AUTHENTICATION.toString(), containsString("code " + CliError.AUTHENTICATION.getErrorCode()));
	}

	@ParameterizedTest
	@EnumSource(CliError.class)
	public void testGetMessageNullConfig(CliError testCase) {
		assertThat("Expect non-null value", testCase.getMessage(null), is(notNullValue()));
	}

	@ParameterizedTest
	@EnumSource(CliError.class)
	public void testGetMessageConfig(CliError testCase) {
		final Config config = new Config() {
			@Override
			public String getHost() {
				return FsUtil.VALUE_DEFAULT_HOST;
			}

			@Override
			public Integer getPort() {
				return FsConnectionType.HTTP.getDefaultPort();
			}

			@Override
			public String getHttpProxyHost() {
				return "";
			}

			@Override
			public Integer getHttpProxyPort() {
				return 8080;
			}

			@Override
			public String getServletZone() {
				return CliConstants.DEFAULT_SERVLET_ZONE.value();
			}

			@Override
			public FsConnectionType getConnectionMode() {
				return null;
			}

			@NotNull
			@Override
			public String getResultFile() {
				return FsUtil.VALUE_DEFAULT_RESULT_FILE;
			}

			@Override
			public String getUser() {
				return FsUtil.VALUE_DEFAULT_USER;
			}

			@Override
			public String getPassword() {
				return FsUtil.VALUE_DEFAULT_USER;
			}

			@Override
			public String getProject() {
				return null;
			}

			@Override
			public String getSynchronizationDirectoryString() {
				return "test";
			}

			@Override
			public <F extends FileHandle> FileSystem<F> getSynchronizationDirectory() {
				return null;
			}

			@Override
			public boolean isActivateProjectIfDeactivated() {
				return false;
			}

		};
		final Object[] args = {config.getHost(), config.getPort(), config.getConnectionMode(), config.getUser(), config.getPassword()};
		assertThat("Expect non-null value", testCase.getMessage(config),
				is(testCase.toString() + ": " + MessageFormat.format(bundle.getString(testCase.name()), args)));
	}

}
