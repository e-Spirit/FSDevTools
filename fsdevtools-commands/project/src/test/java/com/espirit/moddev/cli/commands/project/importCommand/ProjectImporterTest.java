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

package com.espirit.moddev.cli.commands.project.importCommand;

import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.export.ProjectInfo;
import de.espirit.firstspirit.access.schedule.DeployTask;
import de.espirit.firstspirit.access.schedule.MailTask;
import de.espirit.firstspirit.access.schedule.ScheduleStorage;
import de.espirit.firstspirit.access.schedule.ScheduleTask;
import de.espirit.firstspirit.access.schedule.ScheduleTaskTemplate;
import de.espirit.firstspirit.access.schedule.ScriptTask;
import de.espirit.firstspirit.server.scheduler.DeployTaskDTO;
import de.espirit.firstspirit.server.scheduler.MailTaskDTO;
import de.espirit.firstspirit.server.scheduler.ScheduleTaskDTO;
import de.espirit.firstspirit.server.scheduler.ScheduleTaskTemplateDTO;
import de.espirit.firstspirit.server.scheduler.ScriptTaskDTO;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProjectImporterTest {

	private File _fileMock;
	private ProjectImporter _testling;

	@NotNull
	private ScheduleTaskTemplateDTO getScheduleTaskTemplateDTO(@NotNull final Class<? extends ScheduleTaskDTO> clazz, final long id, final String taskName, final String taskDescription) {
		final ScheduleTaskDTO scheduleTaskDTO = mock(clazz);
		when(scheduleTaskDTO.getName()).thenReturn(taskName);
		when(scheduleTaskDTO.getDescription()).thenReturn(taskDescription);
		final ScheduleTaskTemplateDTO taskTemplate = mock(ScheduleTaskTemplateDTO.class);
		when(taskTemplate.getId()).thenReturn(id);
		when(taskTemplate.getTask()).thenReturn(scheduleTaskDTO);
		return taskTemplate;
	}

	@NotNull
	private ScheduleTaskTemplate getScheduleTaskTemplate(@NotNull final Class<? extends ScheduleTask> clazz, final long id, final String taskName, final String taskDescription) {
		final ScheduleTask scheduleTask = mock(clazz);
		when(scheduleTask.getName()).thenReturn(taskName);
		when(scheduleTask.getDescription()).thenReturn(taskDescription);
		final ScheduleTaskTemplate taskTemplate = mock(ScheduleTaskTemplate.class);
		when(taskTemplate.getId()).thenReturn(id);
		when(taskTemplate.getTask()).thenReturn(scheduleTask);
		return taskTemplate;
	}

	@BeforeEach
	public void setUp() {
		_testling = new ProjectImporter();

		_fileMock = mock(File.class);
		when(_fileMock.exists()).thenReturn(true);
		when(_fileMock.isFile()).thenReturn(true);
	}

	private void addProperty(final ArrayList<Properties> properties, final String thirdLayer) {
		final Properties property = new Properties();
		property.setProperty("name", thirdLayer);
		properties.add(property);
	}

	@Test
	public void testExceptionWhenNotConnected() throws Exception {
		Connection connectionMock = mock(Connection.class);
		when(connectionMock.isConnected()).thenReturn(false);
		ProjectImportParameters importParameters = new ProjectImportParametersBuilder().setProjectName("asd").setProjectFile(_fileMock).create();
		Assertions.assertThrows(IllegalStateException.class, () -> {
			_testling.importProject(connectionMock, importParameters);
		});
	}

	@Test
	public void testLayerMapping_existing_layers() {
		// setup source infos
		final ProjectInfo mock = mock(ProjectInfo.class);
		final ArrayList<Properties> properties = new ArrayList<>();
		final String firstLayer = "firstLayer";
		final String secondLayer = "secondLayer";
		final String thirdLayer = "thirdLayer";
		final String fourthLayer = "fourthLayer";

		addProperty(properties, firstLayer);
		addProperty(properties, secondLayer);
		addProperty(properties, thirdLayer);
		addProperty(properties, fourthLayer);
		when(mock.getUsedLayers()).thenReturn(properties);

		// setup target mapping
		final HashMap<String, String> preImportMapping = new HashMap<>();
		final String targetLayer1 = "mappedFirstLayer";
		final String targetLayer2 = "mappedSecondLayer";
		preImportMapping.put(firstLayer, targetLayer1);
		preImportMapping.put(secondLayer, targetLayer1);
		preImportMapping.put(thirdLayer, targetLayer2);

		// execute
		final ProjectImportParameters parameters = new ProjectImportParameters("abc", "", _fileMock, preImportMapping, false);
		final Map<String, String> mapping = ProjectImporter.getLayerMapping(parameters, mock);

		// verify
		assertThat(mapping).hasSize(4);
		assertThat(mapping.get(firstLayer)).isEqualTo(targetLayer1);
		assertThat(mapping.get(secondLayer)).isEqualTo(targetLayer1);
		assertThat(mapping.get(thirdLayer)).isEqualTo(targetLayer2);
		assertThat(mapping.get(fourthLayer)).isNull();
	}

	@Test
	public void testLayerMapping_wildcard() {
		// setup source infos
		final ProjectInfo mock = mock(ProjectInfo.class);
		final ArrayList<Properties> properties = new ArrayList<>();
		final String firstLayer = "firstLayer";
		final String secondLayer = "secondLayer";

		addProperty(properties, firstLayer);
		addProperty(properties, secondLayer);
		when(mock.getUsedLayers()).thenReturn(properties);

		// setup target mapping
		final HashMap<String, String> preImportMapping = new HashMap<>();
		final String targetLayer = "mappedLayer";
		preImportMapping.put("*", targetLayer);

		// execute
		final ProjectImportParameters parameters = new ProjectImportParameters("abc", "", _fileMock, preImportMapping, false);
		final Map<String, String> mapping = ProjectImporter.getLayerMapping(parameters, mock);

		// verify
		assertThat(mapping).hasSize(2);
		assertThat(mapping.get(firstLayer)).isEqualTo(targetLayer);
		assertThat(mapping.get(secondLayer)).isEqualTo(targetLayer);
	}

	@Test
	public void testLayerMapping_wildcard_should_not_override_specific_mapping() {
		// setup source infos
		final ProjectInfo mock = mock(ProjectInfo.class);
		final ArrayList<Properties> properties = new ArrayList<>();
		final String firstLayer = "firstLayer";
		final String secondLayer = "secondLayer";

		addProperty(properties, firstLayer);
		addProperty(properties, secondLayer);
		when(mock.getUsedLayers()).thenReturn(properties);

		// setup target mapping
		final HashMap<String, String> preImportMapping = new HashMap<>();
		final String targetLayer1 = "targetLayer1";
		final String targetLayer2 = "targetLayer2";
		preImportMapping.put("*", targetLayer1);
		preImportMapping.put(secondLayer, targetLayer2);

		// execute
		final ProjectImportParameters parameters = new ProjectImportParameters("abc", "", _fileMock, preImportMapping, false);
		final Map<String, String> mapping = ProjectImporter.getLayerMapping(parameters, mock);

		// verify
		assertThat(mapping).hasSize(2);
		assertThat(mapping.get(firstLayer)).isEqualTo(targetLayer1);
		assertThat(mapping.get(secondLayer)).isEqualTo(targetLayer2);
	}

	@Test
	public void test_schedule_task_template_mapping_for_different_server_with_no_templates() {
		// setup storage
		final ScheduleStorage scheduleStorage = mock(ScheduleStorage.class);
		final List<ScheduleTaskTemplate> scheduleTaskTemplates = new ArrayList<>();
		when(scheduleStorage.getScheduleTaskTemplates(null)).thenReturn(scheduleTaskTemplates);
		// setup source infos
		final ProjectInfo projectInfo = mock(ProjectInfo.class);
		final List<ScheduleTaskTemplateDTO> scheduleTaskTemplateDTOs = new ArrayList<>();
		final long firstId = 10L;
		final long secondId = 20L;
		{
			final ScheduleTaskTemplateDTO taskTemplate = mock(ScheduleTaskTemplateDTO.class);
			when(taskTemplate.getId()).thenReturn(firstId);
			scheduleTaskTemplateDTOs.add(taskTemplate);
		}
		{
			final ScheduleTaskTemplateDTO taskTemplate = mock(ScheduleTaskTemplateDTO.class);
			when(taskTemplate.getId()).thenReturn(secondId);
			scheduleTaskTemplateDTOs.add(taskTemplate);
		}
		when(projectInfo.getServerScheduleTaskTemplates()).thenReturn(scheduleTaskTemplateDTOs);
		final Map<Long, Long> mapping = ProjectImporter.getScheduleTaskTemplateMapping(scheduleStorage, projectInfo);

		// verify
		assertThat(mapping).hasSize(2);
		assertThat(mapping.get(firstId)).isEqualTo(-1L);
		assertThat(mapping.get(secondId)).isEqualTo(-1L);
	}

	@Test
	public void test_schedule_task_template_mapping_for_different_server() {
		// common vars
		final long firstTaskId = 10L;
		final long firstMappedTaskId = 11L;
		final long secondTaskId = 20L;
		final long secondMappedTaskId = 21L;
		final long thirdTaskId = 30L;
		final long thirdMappedTaskId = 31L;
		// setup storage
		final ScheduleStorage scheduleStorage = mock(ScheduleStorage.class);
		final List<ScheduleTaskTemplate> scheduleTaskTemplates = new ArrayList<>();
		when(scheduleStorage.getScheduleTaskTemplates(null)).thenReturn(scheduleTaskTemplates);

		// setup source infos
		final ProjectInfo projectInfo = mock(ProjectInfo.class);
		final List<ScheduleTaskTemplateDTO> scheduleTaskTemplateDTOs = new ArrayList<>();

		// mail task
		scheduleTaskTemplateDTOs.add(getScheduleTaskTemplateDTO(MailTaskDTO.class, firstTaskId, "mailTask", "mailTaskDesc"));
		scheduleTaskTemplates.add(getScheduleTaskTemplate(MailTask.class, firstMappedTaskId, "mailTask", "mailTaskDesc"));
		// script task
		scheduleTaskTemplateDTOs.add(getScheduleTaskTemplateDTO(ScriptTaskDTO.class, secondTaskId, "scriptTask", "scriptTaskDesc"));
		scheduleTaskTemplates.add(getScheduleTaskTemplate(ScriptTask.class, secondMappedTaskId, "scriptTask", "scriptTaskDesc"));
		// deploy task
		scheduleTaskTemplateDTOs.add(getScheduleTaskTemplateDTO(DeployTaskDTO.class, thirdTaskId, "deployTask", "deployTaskDesc"));
		scheduleTaskTemplates.add(getScheduleTaskTemplate(DeployTask.class, thirdMappedTaskId, "deployTask", "deployTaskDesc"));

		when(projectInfo.getServerScheduleTaskTemplates()).thenReturn(scheduleTaskTemplateDTOs);
		final Map<Long, Long> mapping = ProjectImporter.getScheduleTaskTemplateMapping(scheduleStorage, projectInfo);

		// verify
		assertThat(mapping).hasSize(3);
		assertThat(mapping.get(firstTaskId)).isEqualTo(firstMappedTaskId);
		assertThat(mapping.get(secondTaskId)).isEqualTo(secondMappedTaskId);
		assertThat(mapping.get(thirdTaskId)).isEqualTo(thirdMappedTaskId);
	}

	@Test
	public void test_schedule_task_template_mapping_for_equal_server() {
		// common vars
		final long firstId = 10L;
		final long secondId = 20L;
		final long thirdId = 30L;
		// setup storage
		final ScheduleStorage scheduleStorage = mock(ScheduleStorage.class);
		final List<ScheduleTaskTemplate> scheduleTaskTemplates = new ArrayList<>();
		when(scheduleStorage.getScheduleTaskTemplates(null)).thenReturn(scheduleTaskTemplates);

		// setup source infos
		final ProjectInfo projectInfo = mock(ProjectInfo.class);
		final List<ScheduleTaskTemplateDTO> scheduleTaskTemplateDTOs = new ArrayList<>();

		// setup first task (should be mapped)
		scheduleTaskTemplateDTOs.add(getScheduleTaskTemplateDTO(ScriptTaskDTO.class, firstId, String.valueOf(firstId), String.valueOf(firstId)));
		scheduleTaskTemplates.add(getScheduleTaskTemplate(ScriptTask.class, firstId, String.valueOf(firstId), String.valueOf(firstId)));

		// setup second task (no mapping)
		// we use  a different name --> no mapping applied
		scheduleTaskTemplateDTOs.add(getScheduleTaskTemplateDTO(DeployTaskDTO.class, secondId, String.valueOf(secondId), String.valueOf(secondId)));
		scheduleTaskTemplates.add(getScheduleTaskTemplate(DeployTask.class, secondId, secondId + "_changed", String.valueOf(secondId)));

		// setup third task (no mapping)
		// we use  a different description --> no mapping applied
		scheduleTaskTemplateDTOs.add(getScheduleTaskTemplateDTO(MailTaskDTO.class, thirdId, String.valueOf(thirdId), String.valueOf(thirdId)));
		scheduleTaskTemplates.add(getScheduleTaskTemplate(MailTask.class, thirdId, String.valueOf(thirdId), thirdId + "_changed"));

		when(projectInfo.getServerScheduleTaskTemplates()).thenReturn(scheduleTaskTemplateDTOs);
		final Map<Long, Long> mapping = ProjectImporter.getScheduleTaskTemplateMapping(scheduleStorage, projectInfo);

		// verify
		assertThat(mapping).hasSize(3);
		assertThat(mapping.get(firstId)).isEqualTo(firstId);
		assertThat(mapping.get(secondId)).isEqualTo(-1L);
		assertThat(mapping.get(thirdId)).isEqualTo(-1L);
	}

}
