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
import de.espirit.firstspirit.access.AdminService;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.ServicesBroker;
import de.espirit.firstspirit.access.admin.ProjectStorage;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.agency.SpecialistsBroker;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

@ExtendWith(MockitoExtension.class)
class AbstractFeatureCommandTest {

	@Mock
	private AbstractFeatureCommand _subjectUnderTest;
	@Mock
	private Connection _connection;
	@Mock
	private SpecialistsBroker _connectionBroker;
	@Mock
	private ServicesBroker _servicesBroker;
	@Mock
	private AdminService _adminService;
	@Mock
	private ProjectStorage _projectStorage;
	@Mock
	private Project _project;

	@Test
	void needsContext_returns_false_and_thus_prevents_automatic_creation_of_first_spirit_connection() {
		// GIVEN
		Mockito.when(_subjectUnderTest.needsContext()).thenCallRealMethod();
		// WHEN
		final boolean result = _subjectUnderTest.needsContext();
		// THEN
		Assertions.assertThat(result).isFalse();
	}

	@Test
	void call_returns_wrapped_exception_on_connection_error() throws Exception {
		// GIVEN
		Mockito.when(_subjectUnderTest.getConnection()).thenReturn(_connection);
		final IOException testError = new IOException("test connection error");
		Mockito.doThrow(testError).when(_connection).connect();
		Mockito.when(_subjectUnderTest.call()).thenCallRealMethod();
		// WHEN
		final SimpleResult<Boolean> result = _subjectUnderTest.call();
		// THEN
		Assertions.assertThat(result)
				.isNotNull();
		Assertions.assertThat(result.isError())
				.isTrue();
		Assertions.assertThat(result.getError())
				.isNotNull()
				.isSameAs(testError);
		Mockito.verify(_subjectUnderTest).getConnection();
		Mockito.verify(_connection).connect();
	}

	@Test
	void call_returns_wrapped_exception_when_project_is_not_found_on_server() throws Exception {
		// GIVEN
		Mockito.when(_subjectUnderTest.getConnection()).thenReturn(_connection);
		final IllegalStateException testError = new IllegalStateException("test project error");
		Mockito.doThrow(testError).when(_subjectUnderTest).getFirstSpiritProject(Mockito.any());
		Mockito.when(_subjectUnderTest.call()).thenCallRealMethod();
		// WHEN
		final SimpleResult<Boolean> result = _subjectUnderTest.call();
		// THEN
		Assertions.assertThat(result)
				.isNotNull();
		Assertions.assertThat(result.isError())
				.isTrue();
		Assertions.assertThat(result.getError())
				.isNotNull()
				.isSameAs(testError);
		Mockito.verify(_subjectUnderTest).getConnection();
		Mockito.verify(_connection).connect();
		Mockito.verify(_subjectUnderTest).getFirstSpiritProject(_connection);
	}

	@Test
	void call_delegates_to_execute_which_may_throw() throws Exception {
		// GIVEN
		Mockito.when(_subjectUnderTest.getConnection()).thenReturn(_connection);
		Mockito.when(_subjectUnderTest.getFirstSpiritProject(Mockito.any())).thenReturn(_project);
		final RuntimeException testError = new RuntimeException("test command error");
		Mockito.doThrow(testError).when(_subjectUnderTest).execute(Mockito.any(), Mockito.any());
		Mockito.when(_subjectUnderTest.call()).thenCallRealMethod();
		// WHEN
		final SimpleResult<Boolean> result = _subjectUnderTest.call();
		// THEN
		Assertions.assertThat(result)
				.isNotNull();
		Assertions.assertThat(result.isError())
				.isTrue();
		Assertions.assertThat(result.getError())
				.isNotNull()
				.isSameAs(testError);
		Mockito.verify(_subjectUnderTest).getConnection();
		Mockito.verify(_connection).connect();
		Mockito.verify(_subjectUnderTest).getFirstSpiritProject(_connection);
		Mockito.verify(_subjectUnderTest).execute(_connection, _project);
	}

	@Test
	void call_returns_success_if_execute_does_not_throw() throws Exception {
		// GIVEN
		Mockito.when(_subjectUnderTest.getConnection()).thenReturn(_connection);
		Mockito.when(_subjectUnderTest.getFirstSpiritProject(Mockito.any())).thenReturn(_project);
		Mockito.when(_subjectUnderTest.call()).thenCallRealMethod();
		// WHEN
		final SimpleResult<Boolean> result = _subjectUnderTest.call();
		// THEN
		Assertions.assertThat(result)
				.isNotNull();
		Assertions.assertThat(result.isError())
				.isFalse();
		Assertions.assertThat(result.get())
				.isNotNull()
				.isTrue();
		Mockito.verify(_subjectUnderTest).getConnection();
		Mockito.verify(_connection).connect();
		Mockito.verify(_subjectUnderTest).getFirstSpiritProject(_connection);
		Mockito.verify(_subjectUnderTest).execute(_connection, _project);
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"", " ", "     ", "  \t\n\t "})
	void getFirstSpiritProject_no_project_is_specified(final String projectName) {
		// GIVEN
		Mockito.when(_subjectUnderTest.getProject()).thenReturn(projectName);
		Mockito.when(_subjectUnderTest.getFirstSpiritProject(Mockito.any())).thenCallRealMethod();
		// WHEN
		Assertions.assertThatThrownBy(() -> _subjectUnderTest.getFirstSpiritProject(_connection))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("project is not specified");
		// THEN
		Mockito.verify(_subjectUnderTest).getProject();
		Mockito.verifyNoInteractions(_connection);
	}

	@Test
	void getFirstSpiritProject_project_not_found_on_server() {
		// GIVEN
		Mockito.when(_subjectUnderTest.getProject()).thenReturn("test proj 01");
		initMocksToDeliverProjectStorage();
		Mockito.when(_subjectUnderTest.getFirstSpiritProject(Mockito.any())).thenCallRealMethod();
		// WHEN
		Assertions.assertThatThrownBy(() -> _subjectUnderTest.getFirstSpiritProject(_connection))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("could not find project \"test proj 01\" on the server (typo in the project name?)");
		// THEN
		Mockito.verify(_subjectUnderTest).getProject();
		verifyCallsWhichDeliveredProjectStorage();
	}

	@Test
	void getFirstSpiritProject_project_exists_on_server() {
		// GIVEN
		final String projectName = "test proj 02";
		Mockito.when(_subjectUnderTest.getProject()).thenReturn(projectName);
		initMocksToDeliverProjectStorage();
		Mockito.when(_projectStorage.getProject(Mockito.anyString())).thenReturn(_project);
		Mockito.when(_subjectUnderTest.getFirstSpiritProject(Mockito.any())).thenCallRealMethod();
		// WHEN
		final Project result = _subjectUnderTest.getFirstSpiritProject(_connection);
		// THEN
		Assertions.assertThat(result)
				.isNotNull()
				.isSameAs(_project);
		Mockito.verify(_subjectUnderTest).getProject();
		verifyCallsWhichDeliveredProjectStorage();
		Mockito.verify(_projectStorage).getProject(projectName);
	}

	@Test
	void getConnection_delegates_to_ConnectionBuilder() {
		// GIVEN
		final ConnectionBuilder connectionBuilder = Mockito.mock(ConnectionBuilder.class);
		Mockito.when(_subjectUnderTest.getConnectionBuilder()).thenReturn(connectionBuilder);
		Mockito.when(_subjectUnderTest.getConnection()).thenCallRealMethod();
		// WHEN
		_subjectUnderTest.getConnection();
		// THEN
		Mockito.verify(_subjectUnderTest).getConnectionBuilder();
		Mockito.verify(connectionBuilder).build();
	}

	@Test
	void getConnectionBuilder_passes_subject_under_test_to_ConnectionBuilder() {
		// GIVEN
		Mockito.when(_subjectUnderTest.getConnectionBuilder()).thenCallRealMethod();
		// WHEN
		final ConnectionBuilder result = _subjectUnderTest.getConnectionBuilder();
		// THEN
		Assertions.assertThat(result)
				.isNotNull();
		Assertions.assertThat(result.getConfig())
				.isNotNull()
				.isSameAs(_subjectUnderTest);
	}

	private void initMocksToDeliverProjectStorage() {
		Mockito.when(_connection.getBroker()).thenReturn(_connectionBroker);
		Mockito.when(_connectionBroker.requireSpecialist(ServicesBroker.TYPE)).thenReturn(_servicesBroker);
		Mockito.when(_servicesBroker.getService(AdminService.class)).thenReturn(_adminService);
		Mockito.when(_adminService.getProjectStorage()).thenReturn(_projectStorage);
	}

	private void verifyCallsWhichDeliveredProjectStorage() {
		Mockito.verify(_connection).getBroker();
		Mockito.verify(_connectionBroker).requireSpecialist(ServicesBroker.TYPE);
		Mockito.verify(_servicesBroker).getService(AdminService.class);
		Mockito.verify(_adminService).getProjectStorage();
	}
}