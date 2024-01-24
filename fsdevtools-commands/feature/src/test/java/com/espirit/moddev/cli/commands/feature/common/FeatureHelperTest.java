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

package com.espirit.moddev.cli.commands.feature.common;

import de.espirit.common.util.Pair;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.ServerActionHandle;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.agency.BrokerAgent;
import de.espirit.firstspirit.agency.SpecialistsBroker;
import de.espirit.firstspirit.feature.FeatureAgent;
import de.espirit.firstspirit.feature.FeatureAnalyseResult;
import de.espirit.firstspirit.feature.FeatureDescriptor;
import de.espirit.firstspirit.feature.FeatureFile;
import de.espirit.firstspirit.feature.FeatureInstallAgent;
import de.espirit.firstspirit.feature.FeatureInstallOptions;
import de.espirit.firstspirit.feature.FeatureInstallResult;
import de.espirit.firstspirit.feature.FeatureProgress;
import de.espirit.firstspirit.storage.Revision;
import de.espirit.firstspirit.store.access.feature.FeatureInstallResultImpl;
import de.espirit.firstspirit.transport.LayerMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeatureHelperTest {

	private static final String PATH_TO_TEST_FEATURE_ARCHIVE = "test-feature-file.zip";

	@Mock
	private FeatureHelper _subjectUnderTest;
	@Mock
	private Connection _connection;
	@Mock
	private Project _project;
	@Mock
	private ServerActionHandle<? extends FeatureProgress, FeatureInstallResult> _serverActionHandleWithFeatureInstallResult;
	@Mock
	private ServerActionHandle<? extends FeatureProgress, FeatureAnalyseResult> _serverActionHandleWithFeatureAnalyseResult;
	@Mock
	private FeatureAnalyseResult _featureAnalyseResult;
	@Mock
	private SpecialistsBroker _specialistsBroker;
	@Mock
	private BrokerAgent _brokerAgent;
	@Mock
	private SpecialistsBroker _projectSpecialistsBroker;
	@Mock
	private FeatureAgent _featureAgent;
	@Mock
	private FeatureInstallAgent _featureInstallAgent;
	@Mock
	private LayerMapper _layerMapper;
	@Mock
	private FeatureFile _featureFile;
	@Mock
	private FeatureInstallResultImpl _featureInstallResult;

	@Test
	void getFeatureInstallResult_throws_when_server_returns_error() throws Exception {
		// GIVEN
		when(_subjectUnderTest.uploadFeatureFile(any(), any(File.class))).thenReturn(_featureFile);
		Mockito.doReturn(_serverActionHandleWithFeatureInstallResult)
				.when(_subjectUnderTest)
				.getServerActionHandleForFeatureInstallation(any(), any(), any(), anyBoolean());
		when(_serverActionHandleWithFeatureInstallResult.getResult(anyBoolean())).thenReturn(_featureInstallResult);
		when(_featureInstallResult.hasInstallException()).thenReturn(true);
		final Exception testError = new Exception("test");
		when(_featureInstallResult.getInstallException()).thenReturn(new Pair<>(testError, null));
		when(_subjectUnderTest.getFeatureInstallResult(any(), any(File.class), any(), anyBoolean())).thenCallRealMethod();
		// WHEN
		assertThatThrownBy(() -> _subjectUnderTest.getFeatureInstallResult(_featureInstallAgent, new File(PATH_TO_TEST_FEATURE_ARCHIVE), _layerMapper, false))
				.isSameAs(testError);
		// THEN
		verify(_subjectUnderTest).uploadFeatureFile(_featureInstallAgent, new File(PATH_TO_TEST_FEATURE_ARCHIVE));
		verify(_subjectUnderTest).getServerActionHandleForFeatureInstallation(_featureInstallAgent, _featureFile, _layerMapper, false);
		verify(_serverActionHandleWithFeatureInstallResult).getResult(true);
		verify(_featureInstallResult).hasInstallException();
		verify(_featureInstallResult).getInstallException();
	}

	@Test
	void getFeatureInstallResult_returns_without_exception_when_no_server_error_happened() throws Exception {
		// GIVEN
		when(_subjectUnderTest.uploadFeatureFile(any(), any(File.class))).thenReturn(_featureFile);
		Mockito.doReturn(_serverActionHandleWithFeatureInstallResult)
				.when(_subjectUnderTest)
				.getServerActionHandleForFeatureInstallation(any(), any(), any(), anyBoolean());
		when(_serverActionHandleWithFeatureInstallResult.getResult(anyBoolean())).thenReturn(_featureInstallResult);
		when(_featureInstallResult.hasInstallException()).thenReturn(false);
		when(_subjectUnderTest.getFeatureInstallResult(any(), any(File.class), any(), anyBoolean())).thenCallRealMethod();
		// WHEN
		final FeatureInstallResultImpl result = _subjectUnderTest.getFeatureInstallResult(_featureInstallAgent, new File(PATH_TO_TEST_FEATURE_ARCHIVE), _layerMapper, false);
		// THEN
		assertThat(result)
				.isNotNull()
				.isSameAs(_featureInstallResult);
		verify(_subjectUnderTest).uploadFeatureFile(_featureInstallAgent, new File(PATH_TO_TEST_FEATURE_ARCHIVE));
		verify(_subjectUnderTest).getServerActionHandleForFeatureInstallation(_featureInstallAgent, _featureFile, _layerMapper, false);
		verify(_serverActionHandleWithFeatureInstallResult).getResult(true);
		verify(_featureInstallResult).hasInstallException();
	}

	@Test
	void getFeatureInstallResult_include_model() throws Exception {
		// GIVEN
		when(_subjectUnderTest.uploadFeatureFile(any(), any(File.class)))
				.thenReturn(_featureFile);
		Mockito.doReturn(_serverActionHandleWithFeatureInstallResult)
				.when(_subjectUnderTest)
				.getServerActionHandleForFeatureInstallation(any(), any(), any(), anyBoolean());
		when(_serverActionHandleWithFeatureInstallResult.getResult(anyBoolean()))
				.thenReturn(_featureInstallResult);
		when(_featureInstallResult.hasInstallException())
				.thenReturn(false);
		when(_subjectUnderTest.getFeatureInstallResult(any(), any(File.class), any(), anyBoolean()))
				.thenCallRealMethod();
		// WHEN
		final FeatureInstallResultImpl result = _subjectUnderTest.getFeatureInstallResult(_featureInstallAgent, new File(PATH_TO_TEST_FEATURE_ARCHIVE), _layerMapper, true);
		// THEN
		assertThat(result)
				.isNotNull()
				.isSameAs(_featureInstallResult);
		verify(_subjectUnderTest).uploadFeatureFile(_featureInstallAgent, new File(PATH_TO_TEST_FEATURE_ARCHIVE));
		verify(_subjectUnderTest).getServerActionHandleForFeatureInstallation(_featureInstallAgent, _featureFile, _layerMapper, true);
		verify(_serverActionHandleWithFeatureInstallResult).getResult(true);
		verify(_featureInstallResult).hasInstallException();
	}

	@Test
	void getServerActionHandleForFeatureInstallation_uses_older_api_if_model_does_not_have_to_be_installed() throws Exception {
		// GIVEN
		Mockito.doReturn(_serverActionHandleWithFeatureInstallResult)
				.when(_featureInstallAgent)
				.installFeature(any(FeatureFile.class), any(LayerMapper.class));
		when(_subjectUnderTest.getServerActionHandleForFeatureInstallation(any(), any(), any(), anyBoolean()))
				.thenCallRealMethod();
		// WHEN
		final ServerActionHandle<? extends FeatureProgress, FeatureInstallResult> result = _subjectUnderTest.getServerActionHandleForFeatureInstallation(_featureInstallAgent, _featureFile, _layerMapper, false);
		// THEN
		assertThat(result)
				.isNotNull()
				.isSameAs(_serverActionHandleWithFeatureInstallResult);
		verify(_featureInstallAgent, never()).installFeature(any());
		verify(_featureInstallAgent).installFeature(_featureFile, _layerMapper);
	}

	@Test
	void getServerActionHandleForFeatureInstallation_uses_newer_api_for_model_installation() throws Exception {
		// GIVEN
		Mockito.doReturn(_serverActionHandleWithFeatureInstallResult)
				.when(_featureInstallAgent)
				.installFeature(any());
		when(_subjectUnderTest.getServerActionHandleForFeatureInstallation(any(), any(), any(), anyBoolean()))
				.thenCallRealMethod();
		// WHEN
		final ServerActionHandle<? extends FeatureProgress, FeatureInstallResult> result = _subjectUnderTest.getServerActionHandleForFeatureInstallation(_featureInstallAgent, _featureFile, _layerMapper, true);
		// THEN
		assertThat(result)
				.isNotNull()
				.isSameAs(_serverActionHandleWithFeatureInstallResult);
		verify(_featureInstallAgent).installFeature(Mockito.argThat(options -> {
			assertThat(options).isNotNull();
			assertThat(options.getFeatureFile()).isSameAs(_featureFile);
			assertThat(options.getLayerMapper()).isSameAs(_layerMapper);
			assertThat(options.installFeatureModel()).isTrue();
			return true;
		}));
	}

	@ParameterizedTest
	@MethodSource("data_for_getServerActionHandleForFeatureInstallation_falls_back_to_old_api_if_new_api_not_available")
	void getServerActionHandleForFeatureInstallation_falls_back_to_older_api_if_new_api_not_available(
			final Class<? extends Throwable> testErrorClass
	) throws Exception {
		// GIVEN
		Mockito.doThrow(testErrorClass)
				.when(_featureInstallAgent)
				.installFeature(any(FeatureInstallOptions.class));
		Mockito.doReturn(_serverActionHandleWithFeatureInstallResult)
				.when(_featureInstallAgent)
				.installFeature(any(FeatureFile.class), any(LayerMapper.class));
		when(_subjectUnderTest.getServerActionHandleForFeatureInstallation(any(), any(), any(), anyBoolean()))
				.thenCallRealMethod();
		// WHEN
		final ServerActionHandle<? extends FeatureProgress, FeatureInstallResult> result = _subjectUnderTest.getServerActionHandleForFeatureInstallation(_featureInstallAgent, _featureFile, _layerMapper, true);
		// THEN
		assertThat(result)
				.isNotNull()
				.isSameAs(_serverActionHandleWithFeatureInstallResult);
		verify(_featureInstallAgent).installFeature(_featureFile, _layerMapper);
	}

	@Test
	void getFeatureDescriptorsDeduplicatedAndSortedByName() {
		// GIVEN
		final Revision revision1 = mock(Revision.class);
		final Revision revision2 = mock(Revision.class);
		final Revision revision3 = mock(Revision.class);
		when(revision1.compareTo(revision2)).thenReturn(-1);
		when(revision2.compareTo(revision3)).thenReturn(-1);
		when(revision3.compareTo(revision2)).thenReturn(1);
		final FeatureDescriptor featureDescriptor1 = mockFeatureDescriptor("Feature A", revision1);
		final FeatureDescriptor featureDescriptor2 = mockFeatureDescriptor("Feature B", revision3);
		final FeatureDescriptor featureDescriptor3 = mockFeatureDescriptor("Feature A", revision2);
		final FeatureDescriptor featureDescriptor4 = mockFeatureDescriptor("Feature B", revision2);
		final FeatureDescriptor featureDescriptor5 = mockFeatureDescriptor("Feature A", revision3);
		final FeatureDescriptor featureDescriptor6 = mockFeatureDescriptor("Feature B", revision1);
		final FeatureDescriptor featureDescriptor7 = mockFeatureDescriptor("Feature C", null);
		final List<FeatureDescriptor> featureDescriptors = List.of(
				featureDescriptor1,
				featureDescriptor2,
				featureDescriptor3,
				featureDescriptor4,
				featureDescriptor5,
				featureDescriptor6,
				featureDescriptor7
		);
		when(_featureAgent.getFeatureDescriptors()).thenReturn(featureDescriptors);
		when(_subjectUnderTest.getFeatureDescriptors(any())).thenCallRealMethod();
		// WHEN
		final List<FeatureDescriptor> result = _subjectUnderTest.getFeatureDescriptors(_featureAgent);
		// THEN
		assertThat(result)
				.isNotNull()
				.isEqualTo(List.of(
						featureDescriptor5,
						featureDescriptor2,
						featureDescriptor7
				));
	}

	@Test
	void getFeatureDescriptor() {
		// GIVEN
		final String featureName = "Feature A";
		final Revision revision1 = mock(Revision.class);
		final Revision revision2 = mock(Revision.class);
		final FeatureDescriptor featureDescriptor1 = mockFeatureDescriptor(featureName, revision1);
		final FeatureDescriptor featureDescriptor2 = mockFeatureDescriptor(featureName, revision2);
		when(revision1.compareTo(revision2)).thenReturn(-1);
		final List<FeatureDescriptor> featureDescriptors = List.of(
				featureDescriptor1,
				featureDescriptor2
		);
		final FeatureHelper featureHelper = spy(new FeatureHelper());
		when(featureHelper.getFeatureDescriptors(_featureAgent)).thenReturn(featureDescriptors);
		// WHEN
		final FeatureDescriptor result = featureHelper.getFeatureDescriptor(_featureAgent, featureName);
		// THEN
		assertThat(result.getRevision()).isSameAs(revision2);
	}

	@Test
	void getFeatureDescriptor_throws_if_feature_does_not_exist() {
		// GIVEN
		final String featureName = "unknownFeature";
		final FeatureHelper featureHelper = spy(new FeatureHelper());
		when(featureHelper.getFeatureDescriptors(_featureAgent)).thenReturn(Collections.emptyList());
		// WHEN & THEN
		assertThatThrownBy(() -> featureHelper.getFeatureDescriptor(_featureAgent, featureName))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage(String.format("Feature '%s' not found!", featureName));
	}

	@Test
	void getFeatureAnalyseResult_throws_if_server_fails_to_provide_result() throws Exception {
		// GIVEN
		when(_subjectUnderTest.getFeatureInstallAgent(any(), any())).thenReturn(_featureInstallAgent);
		when(_subjectUnderTest.uploadFeatureFile(any(), any(File.class))).thenReturn(_featureFile);
		when(_featureFile.getFeatureName()).thenReturn("MY TEST FEATURE 90");
		when(_subjectUnderTest.getFeatureAnalyseResult(any(), any())).thenReturn(Optional.empty());
		when(_project.getName()).thenReturn("test proj");
		when(_subjectUnderTest.getFeatureAnalyseResult(any(), any(), any(File.class))).thenCallRealMethod();
		// WHEN
		final File file = new File(PATH_TO_TEST_FEATURE_ARCHIVE);
		assertThatThrownBy(() -> _subjectUnderTest.getFeatureAnalyseResult(_connection, _project, file))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("Error analyzing feature 'MY TEST FEATURE 90' (file: '" + file.getAbsolutePath() + "', project: 'test proj').");
		// THEN
		verify(_subjectUnderTest).getFeatureInstallAgent(_connection, _project);
		verify(_subjectUnderTest).uploadFeatureFile(_featureInstallAgent, file);
		verify(_subjectUnderTest).getFeatureAnalyseResult(_featureInstallAgent, _featureFile);
	}

	@Test
	void getFeatureAnalyseResult_returns_result_from_server() throws Exception {
		// GIVEN
		when(_subjectUnderTest.getFeatureInstallAgent(any(), any())).thenReturn(_featureInstallAgent);
		when(_subjectUnderTest.uploadFeatureFile(any(), any(File.class))).thenReturn(_featureFile);
		when(_featureFile.getFeatureName()).thenReturn("MY TEST FEATURE 71");
		when(_subjectUnderTest.getFeatureAnalyseResult(any(), any())).thenReturn(Optional.of(_featureAnalyseResult));
		when(_project.getName()).thenReturn("MY TEST PROJECT 71");
		when(_subjectUnderTest.getFeatureAnalyseResult(any(), any(), any(File.class))).thenCallRealMethod();
		final File file = new File(PATH_TO_TEST_FEATURE_ARCHIVE);
		// WHEN
		final ExtendedFeatureAnalyseResult result = _subjectUnderTest.getFeatureAnalyseResult(_connection, _project, file);
		// THEN
		assertThat(result).isNotNull();
		assertThat(result.getFeatureAnalyseResult())
				.isNotNull()
				.isSameAs(_featureAnalyseResult);
		assertThat(result.getFeatureName())
				.isNotNull()
				.isEqualTo("MY TEST FEATURE 71");
		assertThat(result.getAbsolutePathToFeatureFile())
				.isNotNull()
				.isEqualTo(file.getAbsolutePath());
		assertThat(result.getProjectName())
				.isNotNull()
				.isEqualTo("MY TEST PROJECT 71");
		verify(_subjectUnderTest).getFeatureInstallAgent(_connection, _project);
		verify(_subjectUnderTest).uploadFeatureFile(_featureInstallAgent, file);
		verify(_subjectUnderTest).getFeatureAnalyseResult(_featureInstallAgent, _featureFile);
	}

	@Test
	void getFeatureAgent_delegates_to_broker() {
		// GIVEN
		when(_subjectUnderTest.getProjectBroker(any(), any())).thenReturn(_projectSpecialistsBroker);
		when(_projectSpecialistsBroker.requireSpecialist(any())).thenReturn(_featureAgent);
		when(_subjectUnderTest.getFeatureAgent(any(), any())).thenCallRealMethod();
		// WHEN
		final FeatureAgent featureAgent = _subjectUnderTest.getFeatureAgent(_connection, _project);
		// THEN
		assertThat(featureAgent)
				.isNotNull()
				.isSameAs(_featureAgent);
		Mockito.verify(_subjectUnderTest).getProjectBroker(_connection, _project);
		Mockito.verify(_projectSpecialistsBroker).requireSpecialist(FeatureAgent.TYPE);
	}

	@Test
	void getFeatureInstallAgent_delegates_to_broker() {
		// GIVEN
		when(_subjectUnderTest.getProjectBroker(any(), any())).thenReturn(_projectSpecialistsBroker);
		when(_projectSpecialistsBroker.requireSpecialist(any())).thenReturn(_featureInstallAgent);
		when(_subjectUnderTest.getFeatureInstallAgent(any(), any())).thenCallRealMethod();
		// WHEN
		final FeatureInstallAgent featureInstallAgent = _subjectUnderTest.getFeatureInstallAgent(_connection, _project);
		// THEN
		assertThat(featureInstallAgent)
				.isNotNull()
				.isSameAs(_featureInstallAgent);
		Mockito.verify(_subjectUnderTest).getProjectBroker(_connection, _project);
		Mockito.verify(_projectSpecialistsBroker).requireSpecialist(FeatureInstallAgent.TYPE);
	}

	@Test
	void uploadFeatureFile_delegates_to_agent(
			@TempDir @NotNull final Path tempDir
	) throws IOException {
		// GIVEN
		final Path tempFile = tempDir.resolve(PATH_TO_TEST_FEATURE_ARCHIVE);
		Files.writeString(tempFile, "my test content");
		when(_featureInstallAgent.uploadFeatureFile(Mockito.any())).thenAnswer(args -> {
			final FileInputStream fileInputStream = args.getArgument(0, FileInputStream.class);
			final String fileContent = new String(fileInputStream.readAllBytes(), StandardCharsets.UTF_8);
			// THEN
			assertThat(fileContent)
					.isNotNull()
					.isEqualTo("my test content");
			return _featureFile;
		});
		when(_subjectUnderTest.uploadFeatureFile(any(), any(File.class))).thenCallRealMethod();
		// WHEN
		final FeatureFile result = _subjectUnderTest.uploadFeatureFile(_featureInstallAgent, tempFile.toFile());
		// THEN
		assertThat(result)
				.isNotNull()
				.isSameAs(_featureFile);
	}

	@Test
	void getFeatureAnalyseResult_delegates_to_agent() throws Exception {
		// GIVEN
		Mockito.<ServerActionHandle<? extends FeatureProgress, FeatureAnalyseResult>>when(_featureInstallAgent.analyzeFeature(any())).thenReturn(_serverActionHandleWithFeatureAnalyseResult);
		when(_serverActionHandleWithFeatureAnalyseResult.getResult(anyBoolean())).thenReturn(_featureAnalyseResult);
		when(_subjectUnderTest.getFeatureAnalyseResult(any(), any())).thenCallRealMethod();
		// WHEN
		final Optional<FeatureAnalyseResult> result = _subjectUnderTest.getFeatureAnalyseResult(_featureInstallAgent, _featureFile);
		// THEN
		assertThat(result)
				.isNotNull()
				.isPresent();
		assertThat(result.get())
				.isNotNull()
				.isSameAs(_featureAnalyseResult);
		verify(_featureInstallAgent).analyzeFeature(_featureFile);
		verify(_serverActionHandleWithFeatureAnalyseResult).getResult(true);
	}

	@Test
	void getProjectBroker_throws_when_no_project_broker_is_available() {
		// GIVEN
		when(_connection.getBroker()).thenReturn(_specialistsBroker);
		when(_specialistsBroker.requireSpecialist(any())).thenReturn(_brokerAgent);
		final String projectName = "my test proj 03";
		when(_project.getName()).thenReturn(projectName);
		when(_subjectUnderTest.getProjectBroker(any(), any())).thenCallRealMethod();
		// THEN
		assertThatThrownBy(() -> _subjectUnderTest.getProjectBroker(_connection, _project))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("Project broker for project '" + projectName + "' not found.");
		// WHEN
		verify(_connection).getBroker();
		verify(_specialistsBroker).requireSpecialist(BrokerAgent.TYPE);
		verify(_brokerAgent).getBrokerByProjectName(projectName);
	}

	@Test
	void getProjectBroker_happyDayScenario() {
		// GIVEN
		when(_connection.getBroker()).thenReturn(_specialistsBroker);
		when(_specialistsBroker.requireSpecialist(any())).thenReturn(_brokerAgent);
		when(_brokerAgent.getBrokerByProjectName(anyString())).thenReturn(_projectSpecialistsBroker);
		final String projectName = "my test proj 04";
		when(_project.getName()).thenReturn(projectName);
		when(_subjectUnderTest.getProjectBroker(any(), any())).thenCallRealMethod();
		// THEN
		final SpecialistsBroker result = _subjectUnderTest.getProjectBroker(_connection, _project);
		// WHEN
		assertThat(result)
				.isNotNull()
				.isSameAs(_projectSpecialistsBroker);
		verify(_connection).getBroker();
		verify(_specialistsBroker).requireSpecialist(BrokerAgent.TYPE);
		verify(_brokerAgent).getBrokerByProjectName(projectName);
	}

	@NotNull
	private static FeatureDescriptor mockFeatureDescriptor(
			@NotNull final String featureName,
			@Nullable final Revision revision
	) {
		final FeatureDescriptor featureDescriptor = mock(FeatureDescriptor.class);
		when(featureDescriptor.getFeatureName()).thenReturn(featureName);
		if (revision != null) {
			when(featureDescriptor.getRevision()).thenReturn(revision);
		}
		return featureDescriptor;
	}

	@NotNull
	private static Stream<Arguments> data_for_getServerActionHandleForFeatureInstallation_falls_back_to_old_api_if_new_api_not_available() {
		return Stream.of(
				Arguments.of(IncompatibleClassChangeError.class),
				Arguments.of(NoClassDefFoundError.class)
		);
	}
}
