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

package com.espirit.moddev.cli;

import de.espirit.firstspirit.access.AdminService;
import de.espirit.firstspirit.access.BaseContext;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.admin.ProjectStorage;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.agency.BrokerAgent;
import de.espirit.firstspirit.agency.LanguageAgent;
import de.espirit.firstspirit.agency.SpecialistsBroker;

import com.espirit.moddev.cli.api.CliContext;
import com.espirit.moddev.cli.api.configuration.Config;
import com.espirit.moddev.cli.api.configuration.ImportConfig;
import com.espirit.moddev.connection.FsConnectionType;
import com.espirit.moddev.util.FsUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author e-Spirit GmbH
 */
public class CliContextImplTest {

	private ImportConfig clientConfig;
	private CliContext testling;
	private SpecialistsBroker specialistsBroker;
	private Connection connection;

	@BeforeEach
	public void setUp() throws Exception {
		clientConfig = mock(ImportConfig.class);
		when(clientConfig.getHost()).thenReturn("host");
		when(clientConfig.getPort()).thenReturn(1234);
		when(clientConfig.getUser()).thenReturn("horst");
		when(clientConfig.getProject()).thenReturn("myProject");
		when(clientConfig.getSynchronizationDirectoryString()).thenReturn("dir");
		when(clientConfig.getConnectionMode()).thenReturn(FsConnectionType.HTTP);
		when(clientConfig.isCreatingProjectIfMissing()).thenReturn(true);

		connection = mock(Connection.class);
		specialistsBroker = mock(SpecialistsBroker.class);
		when(connection.getBroker()).thenReturn(specialistsBroker);

		final AdminService adminService = mock(AdminService.class);
		when(connection.getService(AdminService.class)).thenReturn(adminService);
		final ProjectStorage projectStorage = mock(ProjectStorage.class);
		when(adminService.getProjectStorage()).thenReturn(projectStorage);

		final Project project = mock(Project.class);
		final String projectName = clientConfig.getProject();
		when(connection.getProjectByName(projectName)).thenReturn(project);
		when(project.getName()).thenReturn(projectName);
		when(projectStorage.createProject(projectName, projectName + " created by fs-filesync")).thenReturn(project);

		final BrokerAgent brokerAgent = mock(BrokerAgent.class);
		when(specialistsBroker.requireSpecialist(BrokerAgent.TYPE)).thenReturn(brokerAgent);
		when(brokerAgent.getBrokerByProjectName(any())).thenReturn(specialistsBroker);

		final LanguageAgent agent = mock(LanguageAgent.class);
		when(specialistsBroker.requireSpecialist(LanguageAgent.TYPE)).thenReturn(agent);
		when(specialistsBroker.requestSpecialist(LanguageAgent.TYPE)).thenReturn(agent);

		testling = new TestContext(clientConfig);
	}

	@Test
	public void testConstructor() throws Exception {
		Assertions.assertThrows(IllegalArgumentException.class, () -> new CliContextImpl(null));
	}

	@Test
	public void testObtainConnectionExceptionOnEmptyProject() throws Exception {
		when(clientConfig.getProject()).thenReturn(null);
		when(clientConfig.getHost()).thenReturn(FsUtil.VALUE_DEFAULT_HOST);
		new CliContextImpl(clientConfig) {
			@Override
			protected void openConnection() {
			}
		};
	}

	@Test
	public void testAutoCloseable() throws Exception {
		Connection firstSpiritConnection = null;
		try (TestContext context = new TestContext(clientConfig)) {
			firstSpiritConnection = context.getConnection();
		}
		assertThat("Expect a non-null value", firstSpiritConnection, is(notNullValue()));
		verify(firstSpiritConnection, times(2)).connect();
		verify(firstSpiritConnection, times(1)).close();
	}

	@ParameterizedTest
	@EnumSource(value = BaseContext.Env.class, names = {"PREVIEW", "WEBEDIT", "DROP", "FS_BUTTON"})
	public void testIsRest(final BaseContext.Env environment) throws Exception {
		assertThat("Expected false", testling.is(environment), is(Boolean.FALSE));
	}

	@Test
	public void testIsHeadless() throws Exception {
		assertThat("Expected true", testling.is(BaseContext.Env.HEADLESS), is(Boolean.TRUE));
	}

	@Test
	public void testRequireSpecialist() {
		when(testling.getSpecialistsBroker()).thenReturn(specialistsBroker);
		final LanguageAgent languageAgent = testling.requireSpecialist(LanguageAgent.TYPE);
		assertThat("Expected a non-null value", languageAgent, is(notNullValue()));
		verify(specialistsBroker, times(1)).requireSpecialist(LanguageAgent.TYPE);
	}

	@Test
	public void testRequireSpecialistWithNullBroker() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
			testling = spy(new TestContext(clientConfig));
			when(testling.getSpecialistsBroker()).thenReturn(null);
			final LanguageAgent languageAgent = testling.requireSpecialist(LanguageAgent.TYPE);
			assertThat("Expected a null value for a null specialistBroker", languageAgent, is(nullValue()));
		});
	}

	@Test
	public void testRequestSpecialist() throws Exception {
		when(clientConfig.getProject()).thenReturn(null);
		testling = new TestContext(clientConfig);
		assertNull(testling.getSpecialistsBroker());
		testling.requestSpecialist(LanguageAgent.TYPE);
	}

	private class TestContext extends CliContextImpl {

		/**
		 * Instantiates a new Vcs connect context.
		 *
		 * @param clientConfig the client config
		 */
		public TestContext(final Config clientConfig) {
			super(clientConfig);
		}

		@Override
		protected Connection obtainConnection() {
			return connection;
		}

	}
}
