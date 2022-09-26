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

package com.espirit.moddev.cli.commands.feature.cmd.list;

import com.espirit.moddev.cli.commands.feature.common.FeatureHelper;
import com.espirit.moddev.cli.commands.feature.common.FsObjectsLoggingFormatHelper;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.feature.FeatureAgent;
import de.espirit.firstspirit.feature.FeatureDescriptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.Invocation;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeatureListCommandTest {

	@Mock
	private FeatureListCommand _subjectUnderTest;
	@Mock
	private Connection _connection;
	@Mock
	private Project _project;
	@Mock
	private FeatureHelper _featureHelper;
	@Mock
	private FeatureAgent _featureAgent;
	@Mock
	private FsObjectsLoggingFormatHelper _fsObjectsLoggingFormatHelper;

	@Test
	void execute_delegates_to_helper_and_agent() {
		// GIVEN
		when(_subjectUnderTest.getFeatureHelper()).thenReturn(_featureHelper);
		when(_featureHelper.getFeatureAgent(any(), any())).thenReturn(_featureAgent);
		final List<FeatureDescriptor> featureDescriptors = List.of();
		when(_featureHelper.getFeatureDescriptors(any())).thenReturn(featureDescriptors);
		doCallRealMethod().when(_subjectUnderTest).execute(_connection, _project);
		// WHEN
		_subjectUnderTest.execute(_connection, _project);
		// THEN
		verify(_subjectUnderTest).getFeatureHelper();
		verify(_featureHelper).getFeatureAgent(_connection, _project);
		verify(_featureHelper).getFeatureDescriptors(_featureAgent);
		verify(_subjectUnderTest).logFeatureDescriptors(_project, featureDescriptors);
	}

	@Test
	void logFeatureDescriptors_no_descriptors() {
		// GIVEN
		doCallRealMethod().when(_subjectUnderTest).logFeatureDescriptors(eq(_project), anyList());
		// WHEN
		_subjectUnderTest.logFeatureDescriptors(_project, List.of());
		// THEN
		final Collection<Invocation> invocations = mockingDetails(_subjectUnderTest)
				.getInvocations();
		assertThat(invocations)
				.isNotNull()
				.as("no sibling methods should have been called")
				.hasSize(1);
	}

	@Test
	void logFeatureDescriptors_with_descriptors() {
		// GIVEN
		final FeatureDescriptor featureDescriptor = mock(FeatureDescriptor.class);
		final List<FeatureDescriptor> featureDescriptors = List.of(featureDescriptor);
		when(_subjectUnderTest.getFsObjectsLoggingFormatHelper()).thenReturn(_fsObjectsLoggingFormatHelper);
		doCallRealMethod().when(_subjectUnderTest).logFeatureDescriptors(eq(_project), anyList());
		// WHEN
		_subjectUnderTest.logFeatureDescriptors(_project, featureDescriptors);
		// THEN
		verify(_subjectUnderTest).getFsObjectsLoggingFormatHelper();
		verify(_fsObjectsLoggingFormatHelper).shortFormatFeatureDescriptor(featureDescriptor);
	}
}