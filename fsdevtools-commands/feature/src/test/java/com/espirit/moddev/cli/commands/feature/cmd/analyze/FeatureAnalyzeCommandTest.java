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

package com.espirit.moddev.cli.commands.feature.cmd.analyze;

import com.espirit.moddev.cli.commands.feature.common.ExtendedFeatureAnalyseResult;
import com.espirit.moddev.cli.commands.feature.common.FeatureHelper;
import com.espirit.moddev.cli.commands.feature.common.FsObjectsLoggingFormatHelper;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.project.Project;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeatureAnalyzeCommandTest {

	@Mock
	private FeatureAnalyzeCommand _subjectUnderTest;
	@Mock
	private Connection _connection;
	@Mock
	private Project _project;
	@Mock
	private FeatureHelper _featureHelper;
	@Mock
	private FsObjectsLoggingFormatHelper _fsObjectsLoggingFormatHelper;
	@Mock
	private ExtendedFeatureAnalyseResult _extendedFeatureAnalyseResult;

	@Test
	void execute_delegates_to_helpers() throws Exception {
		// GIVEN
		when(_subjectUnderTest.getFeatureHelper()).thenReturn(_featureHelper);
		when(_subjectUnderTest.getFsObjectsLoggingFormatHelper()).thenReturn(_fsObjectsLoggingFormatHelper);
		final String pathToFeatureZip = "test/path/to/feature01.zip";
		when(_subjectUnderTest.getPathToFeatureZip()).thenReturn(pathToFeatureZip);
		when(_featureHelper.getFeatureAnalyseResult(any(), any(), any(File.class))).thenReturn(_extendedFeatureAnalyseResult);
		doCallRealMethod().when(_subjectUnderTest).execute(any(), any());
		// WHEN
		_subjectUnderTest.execute(_connection, _project);
		// THEN
		verify(_subjectUnderTest).getFeatureHelper();
		verify(_subjectUnderTest).getPathToFeatureZip();
		verify(_featureHelper).getFeatureAnalyseResult(_connection, _project, new File(pathToFeatureZip));
		verify(_fsObjectsLoggingFormatHelper).logFeatureAnalyseResult(_extendedFeatureAnalyseResult);
	}
}
