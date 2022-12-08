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

package com.espirit.moddev.cli.commands.feature.cmd.revision;

import com.espirit.moddev.cli.commands.feature.common.FeatureHelper;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.feature.FeatureAgent;
import de.espirit.firstspirit.feature.FeatureDescriptor;
import de.espirit.firstspirit.feature.FeatureModel;
import de.espirit.firstspirit.storage.HistoryProvider;
import de.espirit.firstspirit.storage.Revision;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class FeatureRevisionCommandTest {

	@NotNull
	private static FeatureDescriptor mockDescriptor(@NotNull final Revision revision) {
		final FeatureDescriptor descriptor = mock(FeatureDescriptor.class);
		doReturn(revision).when(descriptor).getRevision();
		return descriptor;
	}

	@NotNull
	private static Revision mockRevision(final long revisionId) {
		final Revision revision = mock(Revision.class);
		doReturn(revisionId).when(revision).getId();
		return revision;
	}

	@Test
	void determineRevision_latest() {
		// setup
		final long latestRevisionId = 1337L;
		final Revision revision = mockRevision(latestRevisionId);
		final Project project = mock(Project.class);
		doReturn(revision).when(project).getRevision(HistoryProvider.UNTIL_NOW);

		final FeatureRevisionCommand command = new FeatureRevisionCommand();

		// test
		final Optional<Revision> result = command.determineRevision(project, -1L);

		// verify
		assertThat(result).isPresent();
		assertThat(result).contains(revision);
		verify(project, times(1)).getRevision(HistoryProvider.UNTIL_NOW);
		verify(project, times(0)).getRevision(latestRevisionId);
	}

	@Test
	void determineRevision_specific() {
		// setup
		final long latestRevisionId = 1337L;
		final Revision revision = mockRevision(latestRevisionId);
		final Project project = mock(Project.class);
		doReturn(revision).when(project).getRevision(latestRevisionId);

		final FeatureRevisionCommand command = new FeatureRevisionCommand();

		// test
		final Optional<Revision> result = command.determineRevision(project, latestRevisionId);

		// verify
		assertThat(result).isPresent();
		assertThat(result).contains(revision);
		verify(project, times(1)).getRevision(latestRevisionId);
		verify(project, times(0)).getRevision(HistoryProvider.UNTIL_NOW);
	}

	@Test
	void determineRevision_specific_edgeCase() {
		// setup
		final long revisionId = 0L;
		final Revision revision = mockRevision(revisionId);
		final Project project = mock(Project.class);
		doReturn(revision).when(project).getRevision(revisionId);

		final FeatureRevisionCommand command = new FeatureRevisionCommand();

		// test
		final Optional<Revision> result = command.determineRevision(project, revisionId);

		// verify
		assertThat(result).isPresent();
		assertThat(result).contains(revision);
		verify(project, times(1)).getRevision(revisionId);
		verify(project, times(0)).getRevision(HistoryProvider.UNTIL_NOW);
	}

	@Test
	void determineRevision_specific_revisionNotFound() {
		// setup
		final long revisionId = 1337L;
		final Project project = mock(Project.class);
		doThrow(new IllegalArgumentException()).when(project).getRevision(revisionId);

		final FeatureRevisionCommand command = new FeatureRevisionCommand();

		// test
		final Optional<Revision> result = command.determineRevision(project, revisionId);

		// verify
		assertThat(result).isEmpty();
		verify(project, times(1)).getRevision(revisionId);
		verify(project, times(0)).getRevision(HistoryProvider.UNTIL_NOW);
	}

	@Test
	void updateFeatureRevision() {
		// setup
		final Revision oldRevision = mockRevision(1L);
		final FeatureDescriptor oldDescriptor = mockDescriptor(oldRevision);
		final Revision newRevision = mockRevision(2L);
		final FeatureDescriptor newDescriptor = mockDescriptor(newRevision);
		final FeatureModel newFeatureModel = mock(FeatureModel.class);
		final FeatureAgent featureAgent = mock(FeatureAgent.class);
		final FeatureAgent.FeatureBuilder featureBuilder = mock(FeatureAgent.FeatureBuilder.class);
		doReturn(true).when(oldDescriptor).isRelease();
		doReturn(featureBuilder).when(featureBuilder).useRelease(anyBoolean());
		doReturn(featureBuilder).when(featureBuilder).useRevision(any(Revision.class));
		doReturn(newDescriptor).when(featureBuilder).create();
		doReturn(featureBuilder).when(featureAgent).createFeatureBuilder(oldDescriptor);
		doReturn(newDescriptor).when(featureAgent).createFeature(any(), eq(newRevision), anyBoolean());
		doReturn(newFeatureModel).when(featureAgent).createFeatureModel(newDescriptor);

		final FeatureRevisionCommand command = new FeatureRevisionCommand();

		// test
		command.updateFeatureRevision(featureAgent, oldDescriptor, newRevision);

		// verify
		verify(featureBuilder, times(1)).useRevision(newRevision);
		verify(featureBuilder, times(1)).useRelease(oldDescriptor.isRelease());
		verify(featureAgent, times(1)).createFeatureBuilder(oldDescriptor);
		verify(featureAgent, times(1)).createFeatureModel(newDescriptor);
		verify(featureAgent, times(1)).saveFeature(newFeatureModel);
	}

	@Test
	void updateFeatureRevision_alreadyUpToDate() {
		// setup
		final Revision oldRevision = mockRevision(1L);
		final Revision toUseRevision = mockRevision(1L);
		final FeatureDescriptor oldDescriptor = mockDescriptor(oldRevision);
		final FeatureAgent featureAgent = mock(FeatureAgent.class);

		final FeatureRevisionCommand command = new FeatureRevisionCommand();

		// test
		command.updateFeatureRevision(featureAgent, oldDescriptor, toUseRevision);

		// verify
		verify(featureAgent, times(0)).createFeature(any(), any(), anyBoolean());
		verify(featureAgent, times(0)).createFeatureModel(any());
		verify(featureAgent, times(0)).saveFeature(any());
	}

	@Test
	void execute() throws Exception {
		// setup
		final long revisionId = -1;
		final Connection connection = mock(Connection.class);
		final Project project = mock(Project.class);
		final Revision currentProjectRevision = mockRevision(4211L);
		doReturn(currentProjectRevision).when(project).getRevision(HistoryProvider.UNTIL_NOW);
		doThrow(new IllegalArgumentException()).when(project).getRevision(anyLong());

		final FeatureHelper featureHelper = mock(FeatureHelper.class);
		final FeatureRevisionCommand command = spy(new FeatureRevisionCommand());
		doReturn(featureHelper).when(command).getFeatureHelper();
		doReturn(revisionId).when(command).getRevision();
		doNothing().when(command).updateFeatureRevision(any(), any(), any());
		doReturn(Optional.of(currentProjectRevision)).when(command).determineRevision(any(), anyLong());

		// test
		command.execute(connection, project);

		// verify
		verify(command, times(1)).updateFeatureRevision(any(), any(), any());
	}

	@Test
	void execute_revisionNotFound() {
		// setup
		final long revisionId = 1337L;
		final Connection connection = mock(Connection.class);
		final Project project = mock(Project.class);
		final Revision currentProjectRevision = mockRevision(4211L);
		doReturn(currentProjectRevision).when(project).getRevision(HistoryProvider.UNTIL_NOW);
		doThrow(new IllegalArgumentException()).when(project).getRevision(anyLong());

		final FeatureHelper featureHelper = mock(FeatureHelper.class);
		final FeatureRevisionCommand command = spy(new FeatureRevisionCommand());
		doReturn(featureHelper).when(command).getFeatureHelper();
		doReturn(revisionId).when(command).getRevision();
		doReturn(Optional.empty()).when(command).determineRevision(any(), anyLong());

		// test & verify
		assertThatThrownBy(() -> command.execute(connection, project)).isInstanceOf(IllegalArgumentException.class);
		verify(command, times(0)).updateFeatureRevision(any(), any(), any());
	}

}
