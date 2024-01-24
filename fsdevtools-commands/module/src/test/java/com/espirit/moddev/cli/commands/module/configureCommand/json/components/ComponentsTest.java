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

package com.espirit.moddev.cli.commands.module.configureCommand.json.components;

import com.espirit.moddev.cli.commands.module.configureCommand.json.JsonTestUtil;
import com.espirit.moddev.cli.commands.module.utils.WebAppUtil;
import com.espirit.moddev.cli.configuration.GlobalConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.module.descriptor.ModuleDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_PROJECT_COMPONENTS;
import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_SERVICES;
import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_WEB_COMPONENTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ComponentsTest {

	private static final String MODULE_NAME = "testModule";

	private ObjectMapper _objectMapper;
	private ModuleDescriptor _moduleDescriptor;
	private Connection _connection;
	private ConfigurationContext _context;

	@BeforeEach
	public void setup() {
		_objectMapper = JsonTestUtil.createMapper();
		_moduleDescriptor = mock(ModuleDescriptor.class);
		when(_moduleDescriptor.getModuleName()).thenReturn(MODULE_NAME);
		_connection = mock(Connection.class);
		_context = new ConfigurationContext(_connection, mock(GlobalConfig.class));
	}

	@Test
	public void deserialize_components_with_elements() throws IOException {
		// setup
		final String serviceName = "testService";
		final String projectComponentName = "projectComponent";
		final String projectName = "testProject";
		final String webComponentName = "webComponent";
		final String webAppName = "global(test-web-app)";
		final Service service = new Service(serviceName);
		final List<ComponentProjectApps.ProjectApp> projectApps = new ArrayList<>();
		final ComponentProjectApps.ProjectApp projectApp = new ComponentProjectApps.ProjectApp();
		projectApp._projectName = projectName;
		projectApps.add(projectApp);
		final ComponentProjectApps componentProjectApps = new ComponentProjectApps(projectComponentName, projectApps);
		final List<ComponentWebApps.WebApp> webApps = new ArrayList<>();
		webApps.add(new ComponentWebApps.WebApp(webAppName));
		final ComponentWebApps componentWebApps = new ComponentWebApps(webComponentName, webApps);
		final Components components = new Components();
		components._projectComponents = Lists.newArrayList(componentProjectApps);
		components._services = Lists.newArrayList(service);
		components._webComponents = Lists.newArrayList(componentWebApps);
		final String json = JsonTestUtil.toJsonObject(
				JsonTestUtil.createMap(
						JsonTestUtil.createEntry(ATTR_WEB_COMPONENTS, Lists.newArrayList(componentWebApps)),
						JsonTestUtil.createEntry(ATTR_PROJECT_COMPONENTS, Lists.newArrayList(componentProjectApps)),
						JsonTestUtil.createEntry(ATTR_SERVICES, Lists.newArrayList(service))
				)
		);

		// test
		final Components deserialized = _objectMapper.readValue(json, Components.class);

		// verify
		assertThat(deserialized._webComponents).isNotNull();
		assertThat(deserialized._webComponents).hasSize(1);
		assertThat(deserialized._webComponents.get(0).getComponentName()).isEqualTo(webComponentName);
		assertThat(WebAppUtil.getReadableWebAppName(deserialized._webComponents.get(0).getWebApps().get(0).getWebAppName().createWebAppId(null))).isEqualTo(webAppName);
		assertThat(deserialized._projectComponents).isNotNull();
		assertThat(deserialized._projectComponents).hasSize(1);
		assertThat(deserialized._projectComponents.get(0).getComponentName()).isEqualTo(projectComponentName);
		assertThat(deserialized._projectComponents.get(0).getProjectApps().get(0).getProjectName(_context)).isEqualTo(projectName);
		assertThat(deserialized._services).isNotNull();
		assertThat(deserialized._services).hasSize(1);
		assertThat(deserialized._services.get(0).getServiceName()).isEqualTo(serviceName);
	}

	@Test
	public void deserialize_components_are_null() throws IOException {
		// setup
		final String json = JsonTestUtil.toJsonObject(
				JsonTestUtil.createMap(
						JsonTestUtil.createEntry(ATTR_WEB_COMPONENTS, null),
						JsonTestUtil.createEntry(ATTR_PROJECT_COMPONENTS, null),
						JsonTestUtil.createEntry(ATTR_SERVICES, null)
				)
		);

		// test
		final Components deserialized = _objectMapper.readValue(json, Components.class);

		// verify
		assertThat(deserialized._webComponents).isNull();
		assertThat(deserialized._projectComponents).isNull();
		assertThat(deserialized._services).isNull();
	}

	@Test
	public void deserialize_components_are_empty_lists() throws IOException {
		// setup
		final String json = JsonTestUtil.toJsonObject(
				JsonTestUtil.createMap(
						JsonTestUtil.createEntry(ATTR_WEB_COMPONENTS, Collections.emptyList()),
						JsonTestUtil.createEntry(ATTR_PROJECT_COMPONENTS, Collections.emptyList()),
						JsonTestUtil.createEntry(ATTR_SERVICES, Collections.emptyList())
				)
		);

		// test
		final Components deserialized = _objectMapper.readValue(json, Components.class);

		// verify
		assertThat(deserialized._webComponents).isNotNull();
		assertThat(deserialized._webComponents).hasSize(0);
		assertThat(deserialized._projectComponents).isNotNull();
		assertThat(deserialized._projectComponents).hasSize(0);
		assertThat(deserialized._services).isNotNull();
		assertThat(deserialized._services).hasSize(0);
	}

	@Test
	public void configure_for_empty_lists() {
		// setup
		final Components components = spy(new Components());

		// test
		components.configure(_context, _moduleDescriptor);

		// verify
		verify(components, times(1)).configure(_context, _moduleDescriptor, Collections.emptyList(), "service");
		verify(components, times(1)).configure(_context, _moduleDescriptor, Collections.emptyList(), "project component");
		verify(components, times(1)).configure(_context, _moduleDescriptor, Collections.emptyList(), "web component");
	}

	@Test
	public void configure_verify_call_order() {
		// setup
		final Service service = mock(Service.class);
		final ComponentProjectApps projectApps = mock(ComponentProjectApps.class);
		final ComponentWebApps webApps = mock(ComponentWebApps.class);
		final Components components = spy(new Components());
		components._projectComponents = Lists.newArrayList(projectApps);
		components._services = Lists.newArrayList(service);
		components._webComponents = Lists.newArrayList(webApps);
		final InOrder inOrder = inOrder(service, projectApps, webApps);

		// test
		components.configure(_context, _moduleDescriptor);

		// verify
		inOrder.verify(service).configure(_context, _moduleDescriptor);
		inOrder.verify(projectApps).configure(_context, _moduleDescriptor);
		inOrder.verify(webApps).configure(_context, _moduleDescriptor);
		verify(components, times(1)).configure(_context, _moduleDescriptor, components._services, "service");
		verify(components, times(1)).configure(_context, _moduleDescriptor, components._projectComponents, "project component");
		verify(components, times(1)).configure(_context, _moduleDescriptor, components._webComponents, "web component");
	}

}
