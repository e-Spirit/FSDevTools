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

package com.espirit.moddev.cli.commands.service.common;

import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.project.ProjectScriptContext;
import de.espirit.firstspirit.agency.ModuleAdminAgent;
import de.espirit.firstspirit.agency.SpecialistsBroker;
import de.espirit.firstspirit.module.descriptor.ModuleDescriptor;
import de.espirit.firstspirit.module.descriptor.ServiceDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public abstract class ServiceProcessCommandBaseTest<T extends AbstractServiceCommand> {

	protected T testling;
	protected Connection mockConnection;
	protected ProjectScriptContext mockContext;
	protected SpecialistsBroker mockBroker;
	protected ModuleAdminAgent mockModuleAdminAgent;

	public void setUp(@NotNull final T instance) {
		this.testling = spy(instance);
		mockConnection = mock(Connection.class);
		mockContext = mock(ProjectScriptContext.class);
		mockBroker = mock(SpecialistsBroker.class);
		mockModuleAdminAgent = mock(ModuleAdminAgent.class);

		when(mockContext.getConnection()).thenReturn(mockConnection);
		when(mockConnection.getBroker()).thenReturn(mockBroker);
		when(mockBroker.requestSpecialist(ModuleAdminAgent.TYPE)).thenReturn(mockModuleAdminAgent);
		this.testling.setContext(mockContext);

		ModuleDescriptor mockModuleDescriptor = mock(ModuleDescriptor.class);
		ServiceDescriptor[] testServiceDescriptor = {
				new ServiceDescriptor("StoppedTestService", "TestModule", "1.0.0"),
				new ServiceDescriptor("StoppedTestService2", "TestModule", "1.0.0"),
				new ServiceDescriptor("RunningTestService", "TestModule", "1.0.0")
		};
		when(mockModuleDescriptor.getComponents()).thenReturn(testServiceDescriptor);
		List<ModuleDescriptor> moduleDescriptors = new ArrayList<>();
		moduleDescriptors.add(mockModuleDescriptor);
		when(mockModuleAdminAgent.getModules()).thenReturn(moduleDescriptors);

		when(mockModuleAdminAgent.isRunning("StoppedTestService")).thenReturn(false);
		when(mockModuleAdminAgent.isRunning("StoppedTestService2")).thenReturn(false);
		when(mockModuleAdminAgent.isRunning("RunningTestService")).thenReturn(true);

	}
}
