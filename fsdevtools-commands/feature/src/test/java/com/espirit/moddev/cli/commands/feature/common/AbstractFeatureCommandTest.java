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

package com.espirit.moddev.cli.commands.feature.common;

import com.espirit.moddev.cli.ConnectionBuilder;
import com.espirit.moddev.cli.results.SimpleResult;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.project.Project;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbstractFeatureCommandTest {

	@Mock
	private AbstractFeatureCommand _subjectUnderTest;
	@Mock
	private Connection _connection;
	@Mock
	private Project _project;

	@Test
	void needsContext_returns_false_and_thus_prevents_automatic_creation_of_first_spirit_connection() {
		// GIVEN
		when(_subjectUnderTest.needsContext()).thenCallRealMethod();
		// WHEN
		final boolean result = _subjectUnderTest.needsContext();
		// THEN
		assertThat(result).isFalse();
	}

	@Test
	void call_returns_wrapped_exception_on_connection_error() throws Exception {
		// GIVEN
		when(_subjectUnderTest.getConnection()).thenReturn(_connection);
		final IOException testError = new IOException("test connection error");
		doThrow(testError).when(_connection).connect();
		when(_subjectUnderTest.call()).thenCallRealMethod();
		// WHEN
		final SimpleResult<Boolean> result = _subjectUnderTest.call();
		// THEN
		assertThat(result)
				.isNotNull();
		assertThat(result.isError())
				.isTrue();
		assertThat(result.getError())
				.isNotNull()
				.isSameAs(testError);
		verify(_subjectUnderTest).getConnection();
		verify(_connection).connect();
	}

	@Test
	void call_returns_wrapped_exception_when_project_is_not_found_on_server() throws Exception {
		// GIVEN
		when(_subjectUnderTest.getConnection()).thenReturn(_connection);
		final IllegalStateException testError = new IllegalStateException("test project error");
		doThrow(testError).when(_subjectUnderTest).getFirstSpiritProject(any());
		when(_subjectUnderTest.call()).thenCallRealMethod();
		// WHEN
		final SimpleResult<Boolean> result = _subjectUnderTest.call();
		// THEN
		assertThat(result)
				.isNotNull();
		assertThat(result.isError())
				.isTrue();
		assertThat(result.getError())
				.isNotNull()
				.isSameAs(testError);
		verify(_subjectUnderTest).getConnection();
		verify(_connection).connect();
		verify(_subjectUnderTest).getFirstSpiritProject(_connection);
	}

	@Test
	void call_delegates_to_execute_which_may_throw() throws Exception {
		// GIVEN
		when(_subjectUnderTest.getConnection()).thenReturn(_connection);
		when(_subjectUnderTest.getFirstSpiritProject(any())).thenReturn(_project);
		final RuntimeException testError = new RuntimeException("test command error");
		doThrow(testError).when(_subjectUnderTest).execute(any(), any());
		when(_subjectUnderTest.call()).thenCallRealMethod();
		// WHEN
		final SimpleResult<Boolean> result = _subjectUnderTest.call();
		// THEN
		assertThat(result)
				.isNotNull();
		assertThat(result.isError())
				.isTrue();
		assertThat(result.getError())
				.isNotNull()
				.isSameAs(testError);
		verify(_subjectUnderTest).getConnection();
		verify(_connection).connect();
		verify(_subjectUnderTest).getFirstSpiritProject(_connection);
		verify(_subjectUnderTest).execute(_connection, _project);
	}

	@Test
	void call_returns_success_if_execute_does_not_throw() throws Exception {
		// GIVEN
		when(_subjectUnderTest.getConnection()).thenReturn(_connection);
		when(_subjectUnderTest.getFirstSpiritProject(any())).thenReturn(_project);
		when(_subjectUnderTest.call()).thenCallRealMethod();
		// WHEN
		final SimpleResult<Boolean> result = _subjectUnderTest.call();
		// THEN
		assertThat(result)
				.isNotNull();
		assertThat(result.isError())
				.isFalse();
		assertThat(result.get())
				.isNotNull()
				.isTrue();
		verify(_subjectUnderTest).getConnection();
		verify(_connection).connect();
		verify(_subjectUnderTest).getFirstSpiritProject(_connection);
		verify(_subjectUnderTest).execute(_connection, _project);
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"", " ", "     ", "  \t\n\t "})
	void getFirstSpiritProject_no_project_is_specified(final String projectName) {
		// GIVEN
		when(_subjectUnderTest.getProject()).thenReturn(projectName);
		when(_subjectUnderTest.getFirstSpiritProject(any())).thenCallRealMethod();
		// WHEN
		Assertions.assertThatThrownBy(() -> _subjectUnderTest.getFirstSpiritProject(_connection))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("Project is not specified");
		// THEN
		verify(_subjectUnderTest).getProject();
		verifyNoInteractions(_connection);
	}

	@Test
	void getFirstSpiritProject_project_not_found_on_server() {
		// GIVEN
		when(_subjectUnderTest.getProject()).thenReturn("test proj 01");
		when(_connection.getProjectByName(anyString())).thenReturn(null);
		when(_subjectUnderTest.getFirstSpiritProject(any())).thenCallRealMethod();
		// WHEN
		assertThatThrownBy(() -> _subjectUnderTest.getFirstSpiritProject(_connection))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("Could not find project 'test proj 01' on the server (typo in the project name?)");
		// THEN
		verify(_subjectUnderTest).getProject();
	}

	@Test
	void getFirstSpiritProject_project_exists_on_server() {
		// GIVEN
		final String projectName = "test proj 02";
		when(_subjectUnderTest.getProject()).thenReturn(projectName);
		when(_connection.getProjectByName(projectName)).thenReturn(_project);
		when(_subjectUnderTest.getFirstSpiritProject(any())).thenCallRealMethod();
		// WHEN
		final Project result = _subjectUnderTest.getFirstSpiritProject(_connection);
		// THEN
		assertThat(result)
				.isNotNull()
				.isSameAs(_project);
		verify(_subjectUnderTest).getProject();
	}

	@Test
	void getConnection_delegates_to_ConnectionBuilder() {
		// GIVEN
		final ConnectionBuilder connectionBuilder = mock(ConnectionBuilder.class);
		when(_subjectUnderTest.getConnectionBuilder()).thenReturn(connectionBuilder);
		when(_subjectUnderTest.getConnection()).thenCallRealMethod();
		// WHEN
		_subjectUnderTest.getConnection();
		// THEN
		verify(_subjectUnderTest).getConnectionBuilder();
		verify(connectionBuilder).build();
	}

	@Test
	void getConnectionBuilder_passes_subject_under_test_to_ConnectionBuilder() {
		// GIVEN
		when(_subjectUnderTest.getConnectionBuilder()).thenCallRealMethod();
		// WHEN
		final ConnectionBuilder result = _subjectUnderTest.getConnectionBuilder();
		// THEN
		assertThat(result).isNotNull();
		assertThat(result.getConfig()).isNotNull().isSameAs(_subjectUnderTest);
	}

}