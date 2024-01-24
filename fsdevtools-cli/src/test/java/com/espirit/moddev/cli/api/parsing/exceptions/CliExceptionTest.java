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

package com.espirit.moddev.cli.api.parsing.exceptions;

import com.espirit.moddev.cli.api.configuration.Config;
import com.espirit.moddev.cli.exception.CliError;
import com.espirit.moddev.cli.exception.CliException;
import com.espirit.moddev.connection.FsConnectionType;
import com.espirit.moddev.util.FsUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CliExceptionTest {

	private Config config;
	private Exception cause;

	private CliException testling;

	@NotNull
	private static Stream<Arguments> provideParameters() {
		return Stream.of(Arguments.of((Object[]) CliError.values()));
	}

	@BeforeEach
	public void setUp() throws Exception {
		config = mock(Config.class);
		when(config.getUser()).thenReturn(FsUtil.VALUE_DEFAULT_USER);
		when(config.getHost()).thenReturn(FsUtil.VALUE_DEFAULT_HOST);
		when(config.getPort()).thenReturn(FsConnectionType.HTTP.getDefaultPort());
		cause = new Exception("JUnit");
	}

	@ParameterizedTest
	@MethodSource("provideParameters")
	public void testToString(final CliError error) throws Exception {
		testling = new CliException(error, config, cause);
		assertThat("Expected a specific value", testling.toString(), is(error.getMessage(config)));
	}

	@Test
	public void testToStringWithException() throws Exception {
		testling = new CliException(cause);
		assertThat("Expected a specific value", testling.toString(), is("JUnit"));
	}

}
