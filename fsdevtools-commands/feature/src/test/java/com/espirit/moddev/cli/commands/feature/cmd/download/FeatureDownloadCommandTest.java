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

package com.espirit.moddev.cli.commands.feature.cmd.download;

import com.espirit.moddev.cli.commands.feature.common.FeatureHelper;
import com.espirit.moddev.cli.commands.feature.common.FsObjectsLoggingFormatHelper;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.ServerActionHandle;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.feature.FeatureAgent;
import de.espirit.firstspirit.feature.FeatureDescriptor;
import de.espirit.firstspirit.feature.FeatureFile;
import de.espirit.firstspirit.feature.FeatureProgress;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeatureDownloadCommandTest {

	@Mock
	private FeatureDownloadCommand _subjectUnderTest;
	@Mock
	private File _file;
	@Mock
	private ServerActionHandle<? extends FeatureProgress, Boolean> _serverActionHandle;
	@Mock
	private FeatureFile _featureFile;
	@Mock
	private FeatureProgress _featureProgress;
	@Mock
	private FeatureAgent _featureAgent;
	@Mock
	private FeatureHelper _featureHelper;
	@Mock
	private FsObjectsLoggingFormatHelper _fsObjectsLoggingFormatHelper;
	@Mock
	private FeatureDescriptor _originalFeatureDescriptor;
	@Mock
	private FeatureDescriptor _featureDescriptor;
	@Mock
	private Connection _connection;
	@Mock
	private Project _project;
	@Mock
	private FeatureAgent.FeatureBuilder _featureBuilder;

	@Test
	void execute_delegates_to_sibling_methods() throws Exception {
		// GIVEN
		when(_subjectUnderTest.ensureOutputFileExists()).thenReturn(_file);
		when(_subjectUnderTest.getFeatureAgent(any(), any())).thenReturn(_featureAgent);
		when(_subjectUnderTest.getOriginalFeatureDescriptor(any())).thenReturn(_originalFeatureDescriptor);
		when(_subjectUnderTest.getFsObjectsLoggingFormatHelper()).thenReturn(_fsObjectsLoggingFormatHelper);
		when(_subjectUnderTest.getFeatureDescriptor(any(), any())).thenReturn(_featureDescriptor);
		doCallRealMethod().when(_subjectUnderTest).execute(any(), any());
		// WHEN
		_subjectUnderTest.execute(_connection, _project);
		// THEN
		verify(_subjectUnderTest).ensureOutputFileExists();
		verify(_subjectUnderTest).getFeatureAgent(_connection, _project);
		verify(_subjectUnderTest).getOriginalFeatureDescriptor(_featureAgent);
		verify(_subjectUnderTest).getFsObjectsLoggingFormatHelper();
		verify(_fsObjectsLoggingFormatHelper).formatFeatureDescriptor(_featureDescriptor);
		verify(_subjectUnderTest).getFeatureDescriptor(_featureAgent, _originalFeatureDescriptor);
		verify(_fsObjectsLoggingFormatHelper).formatFeatureDescriptor(_originalFeatureDescriptor);
		verify(_subjectUnderTest).downloadFeature(_file, _featureAgent, _featureDescriptor);
	}

	@Test
	void ensureOutputFileExists_input_file_does_not_exist_and_must_be_created(
			@TempDir final Path tempDir
	) throws IOException {
		final Path pathToFeatureZip = tempDir.resolve(Paths.get("subdir1", "subdir2", "test-file"));
		// GIVEN
		when(_subjectUnderTest.getPathToFeatureZip()).thenReturn(pathToFeatureZip.toString());
		when(_subjectUnderTest.ensureOutputFileExists()).thenCallRealMethod();
		// WHEN
		final File result = _subjectUnderTest.ensureOutputFileExists();
		// THEN
		assertThat(result)
				.isNotNull()
				.exists();
	}

	@Test
	void ensureOutputFileExists_if_input_file_exists_no_exception_is_thrown(
			@TempDir final Path tempDir
	) throws IOException {
		final Path pathToFeatureZip = tempDir.resolve("test-file");
		final String testData = "test data";
		Files.writeString(pathToFeatureZip, testData);
		assumeThat(pathToFeatureZip.toFile())
				.isNotNull()
				.exists();
		// GIVEN
		when(_subjectUnderTest.getPathToFeatureZip()).thenReturn(pathToFeatureZip.toString());
		when(_subjectUnderTest.ensureOutputFileExists()).thenCallRealMethod();
		// WHEN
		final File result = _subjectUnderTest.ensureOutputFileExists();
		// THEN
		assertThat(result)
				.isNotNull()
				.as("file has not been deleted")
				.exists();
		assertThat(Files.readString(pathToFeatureZip))
				.isNotNull()
				.as("file has not been tampered with")
				.isEqualTo(testData);
	}

	@Test
	void ensureOutputFileExists_if_directory_is_given_exception_is_thrown(
			@TempDir final Path tempDir
	) throws IOException {
		assumeThat(tempDir)
				.isNotNull()
				.exists();
		assumeThat(tempDir)
				.isNotNull()
				.isDirectory();
		// GIVEN
		when(_subjectUnderTest.getPathToFeatureZip()).thenReturn(tempDir.toString());
		when(_subjectUnderTest.ensureOutputFileExists()).thenCallRealMethod();
		// WHEN
		Assertions.assertThatThrownBy(() -> _subjectUnderTest.ensureOutputFileExists())
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageEndingWith("is a directory");
		// THEN
	}

	@Test
	void ensureOutputFileExists_should_throw_IOException_it_should_not_try_to_come_up_with_alternative_file_location(
			@TempDir final Path tempDir
	) throws IOException {
		final Path pathToParentDir = tempDir.resolve("test-parent-dir");
		final File parentDir = pathToParentDir.toFile();
		assumeThat(parentDir.mkdir())
				.isTrue();
		assumeThat(parentDir.setReadOnly())
				.isTrue();
		assumeThat(pathToParentDir)
				.exists();
		// GIVEN
		final String pathToZip = pathToParentDir.resolve("feature-file.zip").toString();
		when(_subjectUnderTest.getPathToFeatureZip()).thenReturn(pathToZip);
		when(_subjectUnderTest.ensureOutputFileExists()).thenCallRealMethod();
		// WHEN
		Assertions.assertThatThrownBy(() -> _subjectUnderTest.ensureOutputFileExists())
				.isInstanceOf(IOException.class)
				.hasMessage("Permission denied");
		// THEN
	}

	@Test
	void getFeatureAgent_delegates_to_featureHelper() {
		// GIVEN
		when(_subjectUnderTest.getFeatureHelper()).thenReturn(_featureHelper);
		when(_subjectUnderTest.getFeatureAgent(any(), any())).thenCallRealMethod();
		// WHEN
		_subjectUnderTest.getFeatureAgent(_connection, _project);
		// THEN
		verify(_featureHelper).getFeatureAgent(_connection, _project);
	}

	@Test
	void getOriginalFeatureDescriptor_throws_if_feature_does_not_exist() {
		// GIVEN
		when(_subjectUnderTest.getFeatureName()).thenReturn("my cool feature");
		when(_subjectUnderTest.getFeatureHelper()).thenReturn(_featureHelper);
		final List<FeatureDescriptor> featureDescriptors = List.of(
				mockFeatureDescriptor("test feature 1"),
				mockFeatureDescriptor("test feature 2"),
				mockFeatureDescriptor("test feature 3")
		);
		when(_featureHelper.getFeatureDescriptors(any())).thenReturn(featureDescriptors);
		when(_subjectUnderTest.getOriginalFeatureDescriptor(any())).thenCallRealMethod();
		// WHEN
		Assertions.assertThatThrownBy(() -> _subjectUnderTest.getOriginalFeatureDescriptor(_featureAgent))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Feature 'my cool feature' not found!");
		// THEN
		verify(_subjectUnderTest).getFeatureName();
		verify(_subjectUnderTest).getFeatureHelper();
		verify(_featureHelper).getFeatureDescriptors(_featureAgent);
	}

	@Test
	void getOriginalFeatureDescriptor_returns_feature_if_it_exists() {
		// GIVEN
		final String featureName = "test feature 3";
		when(_subjectUnderTest.getFeatureName()).thenReturn(featureName);
		when(_subjectUnderTest.getFeatureHelper()).thenReturn(_featureHelper);
		final FeatureDescriptor featureDescriptor = mockFeatureDescriptor(featureName);
		final List<FeatureDescriptor> featureDescriptors = List.of(
				mockFeatureDescriptor("test feature 1"),
				mockFeatureDescriptor("test feature 2"),
				featureDescriptor,
				// just to make Mockito happy ("Please remove unnecessary stubbings")
				mock(FeatureDescriptor.class),
				mock(FeatureDescriptor.class)
		);
		when(_featureHelper.getFeatureDescriptors(any())).thenReturn(featureDescriptors);
		when(_subjectUnderTest.getOriginalFeatureDescriptor(any())).thenCallRealMethod();
		// WHEN
		final FeatureDescriptor result = _subjectUnderTest.getOriginalFeatureDescriptor(_featureAgent);
		// THEN
		assertThat(result)
				.isNotNull()
				.isSameAs(featureDescriptor);
		verify(_subjectUnderTest).getFeatureName();
		verify(_subjectUnderTest).getFeatureHelper();
		verify(_featureHelper).getFeatureDescriptors(_featureAgent);
	}

	@Test
	void getFeatureDescriptor_uses_original_feature_descriptor_if_no_options_are_set() {
		// GIVEN
		when(_subjectUnderTest.getFeatureDescriptor(any(), any())).thenCallRealMethod();
		// WHEN
		final FeatureDescriptor result = _subjectUnderTest.getFeatureDescriptor(_featureAgent, _originalFeatureDescriptor);
		// THEN
		assertThat(result)
				.isNotNull()
				.isSameAs(_originalFeatureDescriptor);
		verifyNoInteractions(_featureAgent);
	}

	@Test
	void getFeatureDescriptor_uses_original_feature_descriptor_if_options_correspond_to_original_feature_descriptor() {
		// GIVEN
		when(_subjectUnderTest.useRelease()).thenReturn(true);
		when(_originalFeatureDescriptor.isRelease()).thenReturn(true);
		when(_subjectUnderTest.getFeatureDescriptor(any(), any())).thenCallRealMethod();
		// WHEN
		final FeatureDescriptor result = _subjectUnderTest.getFeatureDescriptor(_featureAgent, _originalFeatureDescriptor);
		// THEN
		assertThat(result)
				.isNotNull()
				.isSameAs(_originalFeatureDescriptor);
		verifyNoInteractions(_featureAgent);
	}

	@Test
	void getFeatureDescriptor_use_latest_revision_case() {
		// GIVEN
		when(_subjectUnderTest.useLatestRevision()).thenReturn(true);
		when(_featureAgent.createFeatureBuilder(any())).thenReturn(_featureBuilder);
		when(_featureBuilder.create()).thenReturn(_featureDescriptor);
		when(_subjectUnderTest.getFeatureDescriptor(any(), any())).thenCallRealMethod();
		// WHEN
		final FeatureDescriptor result = _subjectUnderTest.getFeatureDescriptor(_featureAgent, _originalFeatureDescriptor);
		// THEN
		assertThat(result)
				.isNotNull()
				.isSameAs(_featureDescriptor);
		verify(_featureAgent).createFeatureBuilder(_originalFeatureDescriptor);
		verify(_featureBuilder).useLatestRevision();
	}

	@Test
	void getFeatureDescriptor_use_release_case() {
		// GIVEN
		when(_originalFeatureDescriptor.isRelease()).thenReturn(true);
		when(_featureAgent.createFeatureBuilder(any())).thenReturn(_featureBuilder);
		when(_featureBuilder.create()).thenReturn(_featureDescriptor);
		when(_subjectUnderTest.useRelease()).thenReturn(false);
		when(_subjectUnderTest.getFeatureDescriptor(any(), any())).thenCallRealMethod();
		// WHEN
		final FeatureDescriptor result = _subjectUnderTest.getFeatureDescriptor(_featureAgent, _originalFeatureDescriptor);
		// THEN
		assertThat(result)
				.isNotNull()
				.isSameAs(_featureDescriptor);
		verify(_featureAgent).createFeatureBuilder(_originalFeatureDescriptor);
		verify(_featureBuilder).useRelease(false);
	}

	@Test
	void downloadFeature_server_fails_to_create_archive() throws Exception {
		// GIVEN
		Mockito.<ServerActionHandle<? extends FeatureProgress, Boolean>>when(_featureAgent.createFeatureTransportFile(any()))
				.thenReturn(_serverActionHandle);
		when(_serverActionHandle.getResult(anyBoolean())).thenReturn(Boolean.FALSE);
		when(_featureDescriptor.getFeatureName()).thenReturn("foo");
		doCallRealMethod().when(_subjectUnderTest).downloadFeature(any(), any(), any());
		// WHEN
		Assertions.assertThatThrownBy(() -> _subjectUnderTest.downloadFeature(_file, _featureAgent, _featureDescriptor))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("Error downloading feature 'foo' to file 'null'!");
		// THEN
		verify(_featureAgent).createFeatureTransportFile(_featureDescriptor);
		verify(_serverActionHandle).getResult(false);
		verify(_featureDescriptor, times(2)).getFeatureName();
	}

	@Test
	void downloadFeature_delegates_to_writeInputStreamIntoFile() throws Exception {
		// GIVEN
		Mockito.<ServerActionHandle<? extends FeatureProgress, Boolean>>when(_featureAgent.createFeatureTransportFile(any()))
				.thenReturn(_serverActionHandle);
		when(_serverActionHandle.getResult(anyBoolean())).thenReturn(Boolean.TRUE);
		Mockito.<FeatureProgress>when(_serverActionHandle.getProgress(anyBoolean())).thenReturn(_featureProgress);
		when(_featureProgress.getFeatureFile()).thenReturn(_featureFile);
		final InputStream inputStream = mock(InputStream.class);
		when(_featureAgent.downloadFeatureFile(any())).thenReturn(inputStream);
		doCallRealMethod().when(_subjectUnderTest).downloadFeature(any(), any(), any());
		// WHEN
		_subjectUnderTest.downloadFeature(_file, _featureAgent, _featureDescriptor);
		// THEN
		verify(_featureAgent).createFeatureTransportFile(_featureDescriptor);
		verify(_serverActionHandle).getResult(false);
		verify(_serverActionHandle).getProgress(true);
		verify(_featureProgress).getFeatureFile();
		verify(_featureAgent).downloadFeatureFile(_featureFile);
		verify(_subjectUnderTest).writeInputStreamIntoFile(inputStream, _file);
	}

	@NotNull
	private static FeatureDescriptor mockFeatureDescriptor(@NotNull final String name) {
		final FeatureDescriptor featureDescriptor = mock(FeatureDescriptor.class);
		when(featureDescriptor.getFeatureName()).thenReturn(name);
		return featureDescriptor;
	}
}