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

import com.google.common.collect.Lists;
import de.espirit.firstspirit.agency.ModuleAdminAgent;
import de.espirit.firstspirit.module.descriptor.ComponentDescriptor;
import de.espirit.firstspirit.module.descriptor.ModuleDescriptor;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ModuleUtilTest {

	@Test
	public void getModuleByName() {
		// setup
		final String moduleName = "moduleName";
		final ModuleAdminAgent moduleAdminAgent = mock(ModuleAdminAgent.class);
		final ModuleDescriptor moduleDescriptor = mock(ModuleDescriptor.class);
		when(moduleDescriptor.getModuleName()).thenReturn(moduleName);
		when(moduleAdminAgent.getModules()).thenReturn(Lists.newArrayList(moduleDescriptor));

		// test
		final Optional<ModuleDescriptor> result = ModuleUtil.getModuleByName(moduleAdminAgent, moduleName);

		// verify
		assertThat(result).isPresent();
		assertThat(result).containsSame(moduleDescriptor);
	}

	@Test
	public void getModuleByName_emptyResult() {
		// setup
		final String moduleName = "moduleName";
		final ModuleAdminAgent moduleAdminAgent = mock(ModuleAdminAgent.class);
		final ModuleDescriptor moduleDescriptor = mock(ModuleDescriptor.class);
		when(moduleDescriptor.getModuleName()).thenReturn(moduleName);
		when(moduleAdminAgent.getModules()).thenReturn(Lists.newArrayList(moduleDescriptor));

		// test
		final Optional<ModuleDescriptor> result = ModuleUtil.getModuleByName(moduleAdminAgent, "unknownName");

		// verify
		assertThat(result).isNotPresent();
	}

	@Test
	public void getModulesByDisplayName() {
		// setup
		final String moduleDisplayName = "moduleDisplayName";
		final ModuleAdminAgent moduleAdminAgent = mock(ModuleAdminAgent.class);
		final ModuleDescriptor moduleDescriptor = mock(ModuleDescriptor.class);
		when(moduleDescriptor.getDisplayName()).thenReturn(moduleDisplayName);
		when(moduleAdminAgent.getModules()).thenReturn(Lists.newArrayList(moduleDescriptor));

		// test
		final List<ModuleDescriptor> result = ModuleUtil.getModulesByDisplayName(moduleAdminAgent, moduleDisplayName);

		// verify
		assertThat(result).hasSize(1);
		assertThat(result).containsExactly(moduleDescriptor);
	}

	@Test
	public void getModulesByDisplayName_emptyResult() {
		// setup
		final String moduleDisplayName = "moduleDisplayName";
		final ModuleAdminAgent moduleAdminAgent = mock(ModuleAdminAgent.class);
		final ModuleDescriptor moduleDescriptor = mock(ModuleDescriptor.class);
		when(moduleDescriptor.getDisplayName()).thenReturn(moduleDisplayName);
		when(moduleAdminAgent.getModules()).thenReturn(Lists.newArrayList(moduleDescriptor));

		// test
		final List<ModuleDescriptor> result = ModuleUtil.getModulesByDisplayName(moduleAdminAgent, "unknownDisplayName");

		// verify
		assertThat(result).isEmpty();
	}

	@Test
	public void getModulesByDisplayName_multipleEntries() {
		// setup
		final ModuleAdminAgent moduleAdminAgent = mock(ModuleAdminAgent.class);
		final ModuleDescriptor moduleDescriptor1 = mock(ModuleDescriptor.class);
		when(moduleDescriptor1.getDisplayName()).thenReturn("moduleDisplayName");
		final ModuleDescriptor moduleDescriptor2 = mock(ModuleDescriptor.class);
		when(moduleDescriptor2.getDisplayName()).thenReturn("moduleDisplayName");
		when(moduleAdminAgent.getModules()).thenReturn(Lists.newArrayList(moduleDescriptor1, moduleDescriptor2));

		// test
		final List<ModuleDescriptor> result = ModuleUtil.getModulesByDisplayName(moduleAdminAgent, "moduleDisplayName");

		// verify
		assertThat(result).hasSize(2);
		assertThat(result).containsExactly(moduleDescriptor1, moduleDescriptor2);
	}

	@Test
	public void getComponentByName() {
		// setup
		final String componentName = "componentName";
		final ComponentDescriptor.Type type = ComponentDescriptor.Type.WEBAPP;
		final ModuleDescriptor moduleDescriptor = mock(ModuleDescriptor.class);
		final ComponentDescriptor componentDescriptor = mock(ComponentDescriptor.class);
		when(componentDescriptor.getName()).thenReturn(componentName);
		when(componentDescriptor.getType()).thenReturn(type);
		when(moduleDescriptor.getComponents()).thenReturn(new ComponentDescriptor[]{componentDescriptor});

		// test
		final Optional<ComponentDescriptor> result = ModuleUtil.getComponentByName(moduleDescriptor, type, componentName);
		final List<ComponentDescriptor> lookup = ModuleUtil.findComponentByNameOrDisplayName(moduleDescriptor, type, componentName);

		// verify
		assertThat(result).isPresent();
		assertThat(result).containsSame(componentDescriptor);
		assertThat(lookup).hasSize(1);
		assertThat(lookup).containsExactly(componentDescriptor);
	}

	@Test
	public void getComponentByName_emptyResult_byName() {
		// setup
		final String componentName = "componentName";
		final ComponentDescriptor.Type type = ComponentDescriptor.Type.WEBAPP;
		final ModuleDescriptor moduleDescriptor = mock(ModuleDescriptor.class);
		final ComponentDescriptor componentDescriptor = mock(ComponentDescriptor.class);
		when(componentDescriptor.getName()).thenReturn(componentName);
		when(componentDescriptor.getType()).thenReturn(type);
		when(moduleDescriptor.getComponents()).thenReturn(new ComponentDescriptor[]{componentDescriptor});

		// test
		final Optional<ComponentDescriptor> result = ModuleUtil.getComponentByName(moduleDescriptor, type, "anotherName");
		final List<ComponentDescriptor> lookup = ModuleUtil.findComponentByNameOrDisplayName(moduleDescriptor, type, "anotherName");

		// verify
		assertThat(result).isNotPresent();
		assertThat(lookup).isEmpty();
	}

	@Test
	public void getComponentByName_emptyResult_byType() {
		// setup
		final String componentName = "componentName";
		final ComponentDescriptor.Type type = ComponentDescriptor.Type.WEBAPP;
		final ModuleDescriptor moduleDescriptor = mock(ModuleDescriptor.class);
		final ComponentDescriptor componentDescriptor = mock(ComponentDescriptor.class);
		when(componentDescriptor.getName()).thenReturn(componentName);
		when(componentDescriptor.getType()).thenReturn(type);
		when(moduleDescriptor.getComponents()).thenReturn(new ComponentDescriptor[]{componentDescriptor});

		// test
		final Optional<ComponentDescriptor> result = ModuleUtil.getComponentByName(moduleDescriptor, ComponentDescriptor.Type.PROJECTAPP, componentName);
		final List<ComponentDescriptor> lookup = ModuleUtil.findComponentByNameOrDisplayName(moduleDescriptor, ComponentDescriptor.Type.PROJECTAPP, componentName);

		// verify
		assertThat(result).isNotPresent();
		assertThat(lookup).isEmpty();
	}

	@Test
	public void getComponentsByDisplayName() {
		// setup
		final String componentDisplayName = "componentDisplayName";
		final ComponentDescriptor.Type type = ComponentDescriptor.Type.WEBAPP;
		final ModuleDescriptor moduleDescriptor = mock(ModuleDescriptor.class);
		final ComponentDescriptor componentDescriptor = mock(ComponentDescriptor.class);
		when(componentDescriptor.getDisplayName()).thenReturn(componentDisplayName);
		when(componentDescriptor.getType()).thenReturn(type);
		when(moduleDescriptor.getComponents()).thenReturn(new ComponentDescriptor[]{componentDescriptor});

		// test
		final List<ComponentDescriptor> result = ModuleUtil.getComponentsByDisplayName(moduleDescriptor, type, componentDisplayName);
		final List<ComponentDescriptor> lookup = ModuleUtil.findComponentByNameOrDisplayName(moduleDescriptor, type, componentDisplayName);

		// verify
		assertThat(result).hasSize(1);
		assertThat(result).containsExactly(componentDescriptor);
		assertThat(lookup).hasSize(1);
		assertThat(lookup).containsExactly(componentDescriptor);
	}

	@Test
	public void getComponentsByDisplayName_multipleEntries() {
		// setup
		final String componentDisplayName = "componentDisplayName";
		final ComponentDescriptor.Type type = ComponentDescriptor.Type.WEBAPP;
		final ModuleDescriptor moduleDescriptor = mock(ModuleDescriptor.class);
		final ComponentDescriptor componentDescriptor1 = mock(ComponentDescriptor.class);
		when(componentDescriptor1.getDisplayName()).thenReturn(componentDisplayName);
		when(componentDescriptor1.getType()).thenReturn(type);
		final ComponentDescriptor componentDescriptor2 = mock(ComponentDescriptor.class);
		when(componentDescriptor2.getDisplayName()).thenReturn(componentDisplayName);
		when(componentDescriptor2.getType()).thenReturn(type);
		final ComponentDescriptor componentDescriptorDifferentType = mock(ComponentDescriptor.class);
		when(componentDescriptorDifferentType.getDisplayName()).thenReturn(componentDisplayName);
		when(componentDescriptorDifferentType.getType()).thenReturn(ComponentDescriptor.Type.PROJECTAPP);
		when(moduleDescriptor.getComponents()).thenReturn(new ComponentDescriptor[]{componentDescriptor1, componentDescriptor2, componentDescriptorDifferentType});

		// test
		final List<ComponentDescriptor> result = ModuleUtil.getComponentsByDisplayName(moduleDescriptor, type, componentDisplayName);
		final List<ComponentDescriptor> lookup = ModuleUtil.findComponentByNameOrDisplayName(moduleDescriptor, type, componentDisplayName);

		// verify
		assertThat(result).hasSize(2);
		assertThat(result).containsExactly(componentDescriptor1, componentDescriptor2);
		assertThat(lookup).hasSize(2);
		assertThat(lookup).containsExactly(componentDescriptor1, componentDescriptor2);
	}

}
