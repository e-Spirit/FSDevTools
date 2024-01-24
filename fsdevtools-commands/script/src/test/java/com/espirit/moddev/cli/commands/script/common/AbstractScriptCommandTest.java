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

import com.espirit.moddev.cli.commands.script.runCommand.RunScriptCommand;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.agency.BrokerAgent;
import de.espirit.firstspirit.agency.SpecialistsBroker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

class AbstractScriptCommandTest {

	@Test
	void createScriptContext_without_project() {
		// setup
		final Connection connection = mock(Connection.class);
		final RunScriptCommand command = new RunScriptCommand();

		// test
		final Map<String, Object> scriptContext = command.createScriptContext(connection);

		// verify
		assertThat(scriptContext).hasSize(1);
		final CLIScriptContext context = (CLIScriptContext) scriptContext.get(AbstractScriptCommand.ATTR_CONTEXT);
		assertThat(context).isNotNull();
		assertThat(context.getProject()).isNull();
	}

	@Test
	void createScriptContext_with_project() {
		// setup
		final Project project = mock(Project.class, RETURNS_DEEP_STUBS);
		doReturn(4711L).when(project).getId();
		final Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
		final SpecialistsBroker broker = mock(SpecialistsBroker.class);
		final BrokerAgent brokerAgent = mock(BrokerAgent.class);
		doReturn(project).when(connection).getProjectByName(any());
		doReturn(broker).when(connection).getBroker();
		doReturn(brokerAgent).when(broker).requireSpecialist(BrokerAgent.TYPE);
		doReturn(broker).when(brokerAgent).getBrokerByProjectId(any(Long.class));
		final RunScriptCommand command = spy(new RunScriptCommand());
		doReturn("projectName").when(command).getProject();

		// test
		final Map<String, Object> scriptContext = command.createScriptContext(connection);

		// verify
		assertThat(scriptContext).hasSize(1);
		final CLIScriptContext context = (CLIScriptContext) scriptContext.get(AbstractScriptCommand.ATTR_CONTEXT);
		assertThat(context).isNotNull();
		assertThat(context.getProject()).isSameAs(project);
	}

	@Test
	void createScriptContext_project_not_found() {
		// setup
		final Project project = mock(Project.class, RETURNS_DEEP_STUBS);
		doReturn(4711L).when(project).getId();
		final Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
		doReturn(null).when(connection).getProjectByName(any());
		final RunScriptCommand command = spy(new RunScriptCommand());
		doReturn("projectName").when(command).getProject();

		// test
		Assertions.assertThrows(IllegalStateException.class, () -> {
			command.createScriptContext(connection);
		});
	}

	@Test
	void createScriptContext_projectBroker_not_found() {
		// setup
		final Project project = mock(Project.class, RETURNS_DEEP_STUBS);
		doReturn(4711L).when(project).getId();
		final Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
		final SpecialistsBroker broker = mock(SpecialistsBroker.class);
		final BrokerAgent brokerAgent = mock(BrokerAgent.class);
		doReturn(project).when(connection).getProjectByName(any());
		doReturn(broker).when(connection).getBroker();
		doReturn(brokerAgent).when(broker).requireSpecialist(BrokerAgent.TYPE);
		doReturn(null).when(brokerAgent).getBrokerByProjectId(any(Long.class));
		final RunScriptCommand command = spy(new RunScriptCommand());
		doReturn("projectName").when(command).getProject();

		// test
		Assertions.assertThrows(IllegalStateException.class, () -> {
			command.createScriptContext(connection);
		});
	}

}