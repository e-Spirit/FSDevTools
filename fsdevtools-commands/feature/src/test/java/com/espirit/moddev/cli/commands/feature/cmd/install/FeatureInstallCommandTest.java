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

package com.espirit.moddev.cli.commands.feature.cmd.install;

import com.espirit.moddev.cli.commands.feature.common.ExtendedFeatureAnalyseResult;
import com.espirit.moddev.cli.commands.feature.common.FeatureHelper;
import com.espirit.moddev.cli.commands.feature.common.FsObjectsLoggingFormatHelper;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.database.BasicEntityInfo;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.access.store.BasicElementInfo;
import de.espirit.firstspirit.access.store.Store;
import de.espirit.firstspirit.feature.FeatureAnalyseResult;
import de.espirit.firstspirit.feature.FeatureInstallAgent;
import de.espirit.firstspirit.store.access.feature.ElementReference;
import de.espirit.firstspirit.store.access.feature.FeatureInstallResultImpl;
import de.espirit.firstspirit.transport.LayerMapper;
import de.espirit.firstspirit.transport.PropertiesTransportOptions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeatureInstallCommandTest {

	private static final String PATH_TO_FEATURE_ZIP = "test-feature.zip";
	private static final String TEST_FALLBACK_LAYER = "TestFallbackLayer";

	@Mock
	private FeatureInstallCommand _subjectUnderTest;
	@Mock
	private Connection _connection;
	@Mock
	private Project _project;
	@Mock
	private FeatureHelper _featureHelper;
	@Mock
	private LayerMapper _layerMapper;
	@Mock
	private FeatureAnalyseResult _featureAnalyseResult;
	@Mock
	private ExtendedFeatureAnalyseResult _extendedFeatureAnalyseResult;
	@Mock
	private FeatureInstallAgent _featureInstallAgent;
	@Mock
	private FeatureInstallResultImpl _featureInstallResult;
	@Mock
	private FsObjectsLoggingFormatHelper _fsObjectsLoggingFormatHelper;

	@Test
	void execute_delegates_to_sibling_methods() throws Exception {
		// GIVEN
		when(_subjectUnderTest.getFeatureHelper()).thenReturn(_featureHelper);
		when(_subjectUnderTest.getLayerMapper(any())).thenReturn(_layerMapper);
		when(_subjectUnderTest.getPathToFeatureZip()).thenReturn(PATH_TO_FEATURE_ZIP);
		when(_featureHelper.getFeatureAnalyseResult(any(), any(), any(File.class))).thenReturn(_extendedFeatureAnalyseResult);
		when(_extendedFeatureAnalyseResult.getFeatureAnalyseResult()).thenReturn(_featureAnalyseResult);
		when(_featureHelper.getFeatureInstallAgent(any(), any())).thenReturn(_featureInstallAgent);
		when(_featureHelper.getFeatureInstallResult(any(), any(File.class), any(), anyBoolean())).thenReturn(_featureInstallResult);
		doCallRealMethod().when(_subjectUnderTest).execute(any(), any());
		// WHEN
		_subjectUnderTest.execute(_connection, _project);
		// THEN
		verify(_subjectUnderTest).getFeatureHelper();
		verify(_subjectUnderTest).getPathToFeatureZip();
		verify(_featureHelper).getFeatureAnalyseResult(_connection, _project, new File(PATH_TO_FEATURE_ZIP));
		verify(_subjectUnderTest).checkForErrors(_extendedFeatureAnalyseResult);
		verify(_subjectUnderTest).getLayerMapper(_featureAnalyseResult);
		verify(_featureHelper).getFeatureInstallAgent(_connection, _project);
		verify(_featureHelper).getFeatureInstallResult(_featureInstallAgent, new File(PATH_TO_FEATURE_ZIP), _layerMapper, false);
		verify(_subjectUnderTest).logFeatureInstallResult(_featureInstallResult);
	}

	@Test
	void checkForErrors_no_errors_no_exception() {
		// GIVEN
		when(_subjectUnderTest.getFsObjectsLoggingFormatHelper()).thenReturn(_fsObjectsLoggingFormatHelper);
		doCallRealMethod().when(_subjectUnderTest).checkForErrors(any());
		// WHEN
		_subjectUnderTest.checkForErrors(_extendedFeatureAnalyseResult);
		// THEN
		verify(_subjectUnderTest).getFsObjectsLoggingFormatHelper();
		verify(_fsObjectsLoggingFormatHelper).logFeatureAnalyseResult(_extendedFeatureAnalyseResult);
	}

	@Test
	void checkForErrors_there_are_errors_but_no_exception_if_force_specified() {
		// GIVEN
		when(_subjectUnderTest.getFsObjectsLoggingFormatHelper()).thenReturn(_fsObjectsLoggingFormatHelper);
		when(_fsObjectsLoggingFormatHelper.logFeatureAnalyseResult(any())).thenReturn(true);
		when(_subjectUnderTest.isForceImport()).thenReturn(true);
		doCallRealMethod().when(_subjectUnderTest).checkForErrors(any());
		// WHEN
		_subjectUnderTest.checkForErrors(_extendedFeatureAnalyseResult);
		// THEN
		verify(_subjectUnderTest).getFsObjectsLoggingFormatHelper();
		verify(_fsObjectsLoggingFormatHelper).logFeatureAnalyseResult(_extendedFeatureAnalyseResult);
	}

	@Test
	void checkForErrors_exception_if_errors_and_force_not_specified() {
		// GIVEN
		when(_extendedFeatureAnalyseResult.getFeatureName()).thenReturn("MyFeatureName");
		when(_extendedFeatureAnalyseResult.getAbsolutePathToFeatureFile()).thenReturn("/my/absolute/path/to/file.zip");
		when(_extendedFeatureAnalyseResult.getProjectName()).thenReturn("MyProjectName");
		when(_subjectUnderTest.getFsObjectsLoggingFormatHelper()).thenReturn(_fsObjectsLoggingFormatHelper);
		when(_fsObjectsLoggingFormatHelper.logFeatureAnalyseResult(any())).thenReturn(true);
		doCallRealMethod().when(_subjectUnderTest).checkForErrors(any());
		// WHEN
		assertThatThrownBy(() -> _subjectUnderTest.checkForErrors(_extendedFeatureAnalyseResult))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("Errors have been found during analysis of feature 'MyFeatureName' (file: '/my/absolute/path/to/file.zip', project: 'MyProjectName').");
		// THEN
		verify(_subjectUnderTest).getFsObjectsLoggingFormatHelper();
		verify(_fsObjectsLoggingFormatHelper).logFeatureAnalyseResult(_extendedFeatureAnalyseResult);
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"", "   ", "  \t\n\t   "})
	void getLayerMapper_mimics_behavior_of_external_sync_import_command_when_layer_mapping_is_not_specified(
			// GIVEN
			final String layerMappingOption
	) {
		when(_subjectUnderTest.getLayerMapping()).thenReturn(Optional.ofNullable(layerMappingOption));
		final LayerMapper.LayerNameBasedLayerMapper layerNameBasedLayerMapper
				= LayerMapper.LayerNameBasedLayerMapper.from(Collections.emptyMap());
		when(_subjectUnderTest.mimicDefaultLayerMapperFromExternalSyncImportCommand()).thenReturn(layerNameBasedLayerMapper);
		when(_subjectUnderTest.getLayerMapper(any())).thenCallRealMethod();
		// WHEN
		final LayerMapper result = _subjectUnderTest.getLayerMapper(_featureAnalyseResult);
		// THEN
		assertThat(result)
				.isNotNull()
				.isSameAs(layerNameBasedLayerMapper);
		verify(_subjectUnderTest).getLayerMapping();
		verify(_subjectUnderTest).mimicDefaultLayerMapperFromExternalSyncImportCommand();
	}

	@Test
	void getLayerMapper_recognizes_create_new_layer_mapping_option() {
		// GIVEN
		when(_subjectUnderTest.getLayerMapping()).thenReturn(Optional.of(FeatureInstallCommand.WILDCARD + ':' + FeatureInstallCommand.CREATE_NEW));
		when(_subjectUnderTest.getLayerMapper(any())).thenCallRealMethod();
		// WHEN
		final LayerMapper result = _subjectUnderTest.getLayerMapper(_featureAnalyseResult);
		// THEN
		assertThat(result)
				.isNotNull()
				.isSameAs(LayerMapper.CREATE_NEW_DEFAULT_LAYER_MAPPER);
		verify(_subjectUnderTest).getLayerMapping();
	}

	@Test
	void getLayerMapper_delegates_to_sibling_methods() {
		// GIVEN
		final Map<String, String> mappedLayers = Map.of();
		when(_featureAnalyseResult.getMappedLayers()).thenReturn(mappedLayers);
		final List<String> unmappedLayers = List.of();
		when(_featureAnalyseResult.getUnmappedLayers()).thenReturn(unmappedLayers);
		when(_subjectUnderTest.getLayerMapping()).thenReturn(Optional.of("foo=bar"));
		final Map<String, String> nameBasedLayerMapping = Map.of();
		when(_subjectUnderTest.getNameBasedLayerMapping(anyMap(), anyMap(), anyList()))
				.thenReturn(nameBasedLayerMapping);
		when(_subjectUnderTest.getFirstSpiritLayerMapper(anyMap())).thenReturn(_layerMapper);
		when(_subjectUnderTest.getLayerMapper(any())).thenCallRealMethod();
		// WHEN
		final LayerMapper result = _subjectUnderTest.getLayerMapper(_featureAnalyseResult);
		// THEN
		assertThat(result)
				.isNotNull()
				.isSameAs(_layerMapper);
		verify(_subjectUnderTest).getLayerMapping();
		final ArgumentMatcher<Map<String, String>> matchUserDefinedLayerMapping = arg -> {
			assertThat(arg)
					.isNotNull()
					.isEqualTo(Map.of("foo", "bar"));
			return true;
		};
		verify(_subjectUnderTest).useSpecialNameForCreatingNewDestinationLayer(argThat(matchUserDefinedLayerMapping));
		verify(_subjectUnderTest).getNameBasedLayerMapping(
				argThat(matchUserDefinedLayerMapping),
				same(mappedLayers),
				same(unmappedLayers)
		);
		verify(_subjectUnderTest).getFirstSpiritLayerMapper(nameBasedLayerMapping);
	}

	@DisplayName("Test special name is used for creating new destination layer")
	@ParameterizedTest(
			name = ParameterizedTest.DISPLAY_NAME_PLACEHOLDER
					+ ' '
					+ ParameterizedTest.INDEX_PLACEHOLDER
					+ ": {0}"
	)
	@MethodSource("testDataFor_useSpecialNameForCreatingNewDestinationLayer")
	void useSpecialNameForCreatingNewDestinationLayer(
			// GIVEN
			@NotNull final String testCaseName,
			@NotNull final Map<String, String> layerMappingsFromOption,
			@NotNull final Map<String, String> expectedResult
	) {
		doCallRealMethod().when(_subjectUnderTest).useSpecialNameForCreatingNewDestinationLayer(anyMap());
		// WHEN
		_subjectUnderTest.useSpecialNameForCreatingNewDestinationLayer(layerMappingsFromOption);
		// THEN
		assertThat(layerMappingsFromOption)
				.as(testCaseName)
				.isEqualTo(expectedResult);
	}

	static Stream<Arguments> testDataFor_useSpecialNameForCreatingNewDestinationLayer() {
		final List<Arguments> result = new ArrayList<>();
		{
			final String testCaseName = String.format(
					"no \"%s\" constant, nothing to substitute",
					FeatureInstallCommand.CREATE_NEW
			);
			final Map<String, String> layerMappingsFromOption = new HashMap<>(Map.of(
					"A", "B",
					"C", "D"
			));
			final Map<String, String> expectedResult = Map.copyOf(layerMappingsFromOption);
			final Arguments args = Arguments.arguments(
					testCaseName,
					layerMappingsFromOption,
					expectedResult
			);
			result.add(args);
		}
		{
			final String testCaseName = String.format(
					"\"%s\" on RHS is substituted",
					FeatureInstallCommand.CREATE_NEW
			);
			final Map<String, String> layerMappingsFromOption = new HashMap<>(Map.of(
					"A", "B",
					"C", FeatureInstallCommand.CREATE_NEW,
					"E", "F"
			));
			final Map<String, String> expectedResult = Map.of(
					"A", "B",
					"C", LayerMapper.CREATE_NEW_DEFAULT_LAYER,
					"E", "F"
			);
			final Arguments args = Arguments.arguments(
					testCaseName,
					layerMappingsFromOption,
					expectedResult
			);
			result.add(args);
		}
		{
			final String testCaseName = String.format(
					"\"%s\" on LHS is not substituted",
					FeatureInstallCommand.CREATE_NEW
			);
			final Map<String, String> layerMappingsFromOption = new HashMap<>(Map.of(
					"A", "B",
					FeatureInstallCommand.CREATE_NEW, "C",
					"E", "F"
			));
			final Map<String, String> expectedResult = Map.copyOf(layerMappingsFromOption);
			final Arguments args = Arguments.arguments(
					testCaseName,
					layerMappingsFromOption,
					expectedResult
			);
			result.add(args);
		}
		return result.stream();
	}

	@Test
	void getNameBasedLayerMapping_wildcard_case() {
		// GIVEN
		final Map<String, String> layerMappingsFromOption = Map.of(
				FeatureInstallCommand.WILDCARD, ""
		);
		final Map<String, String> mappedLayers = Map.of();
		final List<String> unmappedLayers = List.of();
		final Map<String, String> expectedResult = Map.of();
		when(_subjectUnderTest.getNameBasedLayerMappingWithWildcardFallback(anyMap(), anyMap(), anyList())).thenReturn(expectedResult);
		when(_subjectUnderTest.getNameBasedLayerMapping(anyMap(), anyMap(), anyList())).thenCallRealMethod();
		// WHEN
		final Map<String, String> result = _subjectUnderTest.getNameBasedLayerMapping(
				layerMappingsFromOption,
				mappedLayers,
				unmappedLayers
		);
		// THEN
		assertThat(result)
				.isNotNull()
				.isSameAs(expectedResult);
		verify(_subjectUnderTest).getNameBasedLayerMappingWithWildcardFallback(
				layerMappingsFromOption,
				mappedLayers,
				unmappedLayers
		);
		verify(_subjectUnderTest, never()).getNameBasedLayerMappingWithoutWildcardFallback(
				anyMap(),
				anyMap(),
				anyList()
		);
	}

	@Test
	void getNameBasedLayerMapping_no_wildcard_case() {
		// GIVEN
		final Map<String, String> layerMappingsFromOption = Map.of(
				"foo", "bar"
		);
		final Map<String, String> mappedLayers = Map.of();
		final List<String> unmappedLayers = List.of();
		final Map<String, String> expectedResult = Map.of();
		when(_subjectUnderTest.getNameBasedLayerMappingWithoutWildcardFallback(anyMap(), anyMap(), anyList())).thenReturn(expectedResult);
		when(_subjectUnderTest.getNameBasedLayerMapping(anyMap(), anyMap(), anyList())).thenCallRealMethod();
		// WHEN
		final Map<String, String> result = _subjectUnderTest.getNameBasedLayerMapping(
				layerMappingsFromOption,
				mappedLayers,
				unmappedLayers
		);
		// THEN
		assertThat(result)
				.isNotNull()
				.isSameAs(expectedResult);
		verify(_subjectUnderTest).getNameBasedLayerMappingWithoutWildcardFallback(
				layerMappingsFromOption,
				mappedLayers,
				unmappedLayers
		);
		verify(_subjectUnderTest, never()).getNameBasedLayerMappingWithWildcardFallback(
				anyMap(),
				anyMap(),
				anyList()
		);
	}

	@Test
	void getFirstSpiritLayerMapper_returns_name_based_mapper() {
		// GIVEN
		final Map<String, String> layerMapping = Map.of(
				"foo", "bar",
				"bazz", "fizz"
		);
		when(_subjectUnderTest.getFirstSpiritLayerMapper(anyMap())).thenCallRealMethod();
		// WHEN
		final LayerMapper result = _subjectUnderTest.getFirstSpiritLayerMapper(
				layerMapping
		);
		// THEN
		assertThat(result)
				.isNotNull()
				.isInstanceOf(LayerMapper.LayerNameBasedLayerMapper.class);
		final String dstLayer1 = result.getLayer(mockMappingContext("foo"));
		assertThat(dstLayer1)
				.isNotNull()
				.isEqualTo("bar");
		final String dstLayer2 = result.getLayer(mockMappingContext("bazz"));
		assertThat(dstLayer2)
				.isNotNull()
				.isEqualTo("fizz");
	}

	@DisplayName("Test layer mapping with wildcard")
	@ParameterizedTest(
			name = ParameterizedTest.DISPLAY_NAME_PLACEHOLDER
					+ ' '
					+ ParameterizedTest.INDEX_PLACEHOLDER
					+ ": {0}"
	)
	@MethodSource("testDataFor_getNameBasedLayerMappingWithWildcardFallback")
	void getNameBasedLayerMappingWithWildcardFallback(
			// GIVEN
			@NotNull final String testCaseName,
			@NotNull final Map<String, String> layerMappingsFromOption,
			@NotNull final Map<String, String> mappedLayers,
			@NotNull final List<String> unmappedLayers,
			@NotNull final Map<String, String> expectedResult
	) {
		when(_subjectUnderTest.getFallbackLayer(anyMap())).thenReturn(TEST_FALLBACK_LAYER);
		when(_subjectUnderTest.getNameBasedLayerMappingWithWildcardFallback(anyMap(), anyMap(), anyList())).thenCallRealMethod();
		// WHEN
		final Map<String, String> result = _subjectUnderTest.getNameBasedLayerMappingWithWildcardFallback(
				layerMappingsFromOption,
				mappedLayers,
				unmappedLayers
		);
		// THEN
		assertThat(result)
				.as(testCaseName)
				.isNotNull()
				.isEqualTo(expectedResult);
		verify(_subjectUnderTest).getFallbackLayer(layerMappingsFromOption);
	}

	@NotNull
	static Stream<Arguments> testDataFor_getNameBasedLayerMappingWithWildcardFallback() {
		final List<Arguments> arguments = new ArrayList<>();
		{
			final String testCaseName = "Case 01." +
					" User supplied mappings have priority" +
					" over both mapped and unmapped layers.";
			final Map<String, String> layerMappingsFromOption = Map.of(
					FeatureInstallCommand.WILDCARD, "value does not matter",
					"srcLayerA", "overrideDstLayerA",
					"srcLayerB", "overrideDstLayerB"
			);
			final Map<String, String> mappedLayers = Map.of(
					"srcLayerA", "dstLayerA"
			);
			final List<String> unmappedLayers = List.of(
					"srcLayerB"
			);
			final Map<String, String> expectedResult = Map.of(
					"srcLayerA", "overrideDstLayerA",
					"srcLayerB", "overrideDstLayerB"
			);
			arguments.add(Arguments.of(
					testCaseName,
					layerMappingsFromOption,
					mappedLayers,
					unmappedLayers,
					expectedResult
			));
		}
		{
			final String testCaseName = "Case 02." +
					" Both mapped and unmapped layers" +
					" get mapped onto at least the fallback layer.";
			final Map<String, String> layerMappingsFromOption = Map.of(
					FeatureInstallCommand.WILDCARD, "value does not matter",
					"srcLayerA", "userSpecifiedDstLayerA",
					"srcLayerB", "userSpecifiedDstLayerB",
					"srcLayerC", "userSpecifiedDstLayerC",
					"srcLayerI", "userSpecifiedDstLayerI"
			);
			final Map<String, String> mappedLayers = Map.of(
					"srcLayerA", "dstLayerA",
					"srcLayerB", "dstLayerB",
					"srcLayerC", "dstLayerC",
					"srcLayerD", "dstLayerD",
					"srcLayerE", "dstLayerE"
			);
			final List<String> unmappedLayers = List.of(
					"srcLayerF",
					"srcLayerG",
					"srcLayerH",
					"srcLayerI",
					"srcLayerJ"
			);
			final Map<String, String> expectedResult = Map.of(
					"srcLayerA", "userSpecifiedDstLayerA",
					"srcLayerB", "userSpecifiedDstLayerB",
					"srcLayerC", "userSpecifiedDstLayerC",
					"srcLayerD", TEST_FALLBACK_LAYER,
					"srcLayerE", TEST_FALLBACK_LAYER,
					"srcLayerF", TEST_FALLBACK_LAYER,
					"srcLayerG", TEST_FALLBACK_LAYER,
					"srcLayerH", TEST_FALLBACK_LAYER,
					"srcLayerI", "userSpecifiedDstLayerI",
					"srcLayerJ", TEST_FALLBACK_LAYER
			);
			arguments.add(Arguments.of(
					testCaseName,
					layerMappingsFromOption,
					mappedLayers,
					unmappedLayers,
					expectedResult
			));
		}
		return arguments.stream();
	}

	@Test
	void getFallbackLayer_throws_if_no_wildcard() {
		// GIVEN
		final Map<String, String> layerMappingsFromOption = Map.of("foo", "bar");
		when(_subjectUnderTest.getFallbackLayer(anyMap())).thenCallRealMethod();
		// WHEN
		assertThatThrownBy(() -> _subjectUnderTest.getFallbackLayer(layerMappingsFromOption))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("no fallback layer");
		// THEN
	}

	@Test
	void getFallbackLayer() {
		// GIVEN
		final Map<String, String> layerMappingsFromOption = Map.of(
				FeatureInstallCommand.WILDCARD, TEST_FALLBACK_LAYER
		);
		when(_subjectUnderTest.getFallbackLayer(anyMap())).thenCallRealMethod();
		// WHEN
		final String result = _subjectUnderTest.getFallbackLayer(layerMappingsFromOption);
		// THEN
		assertThat(result)
				.isNotNull()
				.isEqualTo(TEST_FALLBACK_LAYER);
	}

	@Test
	void getNameBasedLayerMappingWithoutWildcardFallback_throws_if_wildcard_is_present() {
		// GIVEN
		final Map<String, String> layerMappingsFromOption = Map.of(
				"foo", "bar",
				FeatureInstallCommand.WILDCARD, "tada",
				"hey", "may"
		);
		when(_subjectUnderTest.getNameBasedLayerMappingWithoutWildcardFallback(anyMap(), anyMap(), anyList())).thenCallRealMethod();
		// WHEN
		assertThatThrownBy(
				() -> _subjectUnderTest.getNameBasedLayerMappingWithoutWildcardFallback(layerMappingsFromOption, Map.of(), List.of())
		)
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("wildcard must not be here");
		// THEN
	}

	@DisplayName("Test layer mapping without wildcard")
	@ParameterizedTest(
			name = ParameterizedTest.DISPLAY_NAME_PLACEHOLDER
					+ ' '
					+ ParameterizedTest.INDEX_PLACEHOLDER
					+ ": {0}"
	)
	@MethodSource("testDataFor_getNameBasedLayerMappingWithoutWildcardFallback")
	void getNameBasedLayerMappingWithoutWildcardFallback(
			// GIVEN
			@NotNull final String testCaseName,
			@NotNull final Map<String, String> layerMappingsFromOption,
			@NotNull final Map<String, String> mappedLayers,
			@NotNull final List<String> unmappedLayers,
			@NotNull final Map<String, String> expectedResult
	) {
		when(_subjectUnderTest.getNameBasedLayerMappingWithoutWildcardFallback(anyMap(), anyMap(), anyList())).thenCallRealMethod();
		// WHEN
		final Map<String, String> result = _subjectUnderTest.getNameBasedLayerMappingWithoutWildcardFallback(
				layerMappingsFromOption,
				mappedLayers,
				unmappedLayers
		);
		// THEN
		assertThat(result)
				.as(testCaseName)
				.isNotNull()
				.isEqualTo(expectedResult);
	}

	@NotNull
	static Stream<Arguments> testDataFor_getNameBasedLayerMappingWithoutWildcardFallback() {
		final List<Arguments> arguments = new ArrayList<>();
		{
			final String testCaseName = "Case 01." +
					" User supplied mappings have priority" +
					" over both mapped and unmapped layers.";
			final Map<String, String> layerMappingsFromOption = Map.of(
					"srcLayerA", "overrideDstLayerA",
					"srcLayerB", "overrideDstLayerB"
			);
			final Map<String, String> mappedLayers = Map.of(
					"srcLayerA", "dstLayerA"
			);
			final List<String> unmappedLayers = List.of(
					"srcLayerB"
			);
			final Map<String, String> expectedResult = Map.of(
					"srcLayerA", "overrideDstLayerA",
					"srcLayerB", "overrideDstLayerB"
			);
			arguments.add(Arguments.of(
					testCaseName,
					layerMappingsFromOption,
					mappedLayers,
					unmappedLayers,
					expectedResult
			));
		}
		{
			final String testCaseName = "Case 02." +
					" Unmapped layers get mapped onto themselves"
					+ " and mapped layers are taken as is.";
			final Map<String, String> layerMappingsFromOption = Map.of(
					"srcLayerA", "userSpecifiedDstLayerA",
					"srcLayerB", "userSpecifiedDstLayerB",
					"srcLayerC", "userSpecifiedDstLayerC",
					"srcLayerI", "userSpecifiedDstLayerI"
			);
			final Map<String, String> mappedLayers = Map.of(
					"srcLayerA", "dstLayerA",
					"srcLayerB", "dstLayerB",
					"srcLayerC", "dstLayerC",
					"srcLayerD", "dstLayerD",
					"srcLayerE", "dstLayerE"
			);
			final List<String> unmappedLayers = List.of(
					"srcLayerF",
					"srcLayerG",
					"srcLayerH",
					"srcLayerI",
					"srcLayerJ"
			);
			final Map<String, String> expectedResult = Map.of(
					"srcLayerA", "userSpecifiedDstLayerA",
					"srcLayerB", "userSpecifiedDstLayerB",
					"srcLayerC", "userSpecifiedDstLayerC",
					"srcLayerD", "dstLayerD",
					"srcLayerE", "dstLayerE",
					"srcLayerF", "srcLayerF",
					"srcLayerG", "srcLayerG",
					"srcLayerH", "srcLayerH",
					"srcLayerI", "userSpecifiedDstLayerI",
					"srcLayerJ", "srcLayerJ"
			);
			arguments.add(Arguments.of(
					testCaseName,
					layerMappingsFromOption,
					mappedLayers,
					unmappedLayers,
					expectedResult
			));
		}
		return arguments.stream();
	}

	@Test
	void logFeatureInstallResult_delegates_to_sibling_methods() {
		// GIVEN
		assumeThat(LoggerFactory.getLogger(FeatureInstallCommand.class).isDebugEnabled())
				.isTrue();
		final Map<BasicElementInfo, ElementReference> elementsMap = Map.of();
		when(_featureInstallResult.getNewElements()).thenReturn(elementsMap);
		when(_featureInstallResult.getUpdatedElementsMap()).thenReturn(elementsMap);
		final EnumSet<PropertiesTransportOptions.ProjectPropertyType> projectProperties
				= EnumSet.of(PropertiesTransportOptions.ProjectPropertyType.COMMON);
		when(_featureInstallResult.getModifiedProjectProperties()).thenReturn(projectProperties);
		when(_featureInstallResult.getMovedElementsMap()).thenReturn(elementsMap);
		when(_featureInstallResult.getModifiedSchemes()).thenReturn(elementsMap);
		final Set<BasicEntityInfo> setOfEntities = Set.of();
		when(_featureInstallResult.getNewEntities()).thenReturn(setOfEntities);
		when(_featureInstallResult.getUpdatedEntities()).thenReturn(setOfEntities);
		final EnumMap<Store.Type, List<BasicElementInfo>> storeNodes = new EnumMap<>(Store.Type.class);
		when(_featureInstallResult.getLostAndFoundStoreNodes()).thenReturn(storeNodes);
		when(_featureInstallResult.getDeletedStoreNodes()).thenReturn(storeNodes);
		when(_subjectUnderTest.getFsObjectsLoggingFormatHelper()).thenReturn(_fsObjectsLoggingFormatHelper);
		doCallRealMethod().when(_subjectUnderTest).logFeatureInstallResult(any());
		// WHEN
		_subjectUnderTest.logFeatureInstallResult(_featureInstallResult);
		// THEN
		verify(_featureInstallResult).getNewElements();
		verify(_featureInstallResult).getUpdatedElementsMap();
		verify(_featureInstallResult).getModifiedProjectProperties();
		verify(_featureInstallResult).getMovedElementsMap();
		verify(_featureInstallResult).getModifiedSchemes();
		verify(_featureInstallResult).getNewEntities();
		verify(_featureInstallResult).getUpdatedEntities();
		verify(_featureInstallResult).getLostAndFoundStoreNodes();
		verify(_featureInstallResult).getDeletedStoreNodes();
		verify(_subjectUnderTest).logElementsMap(elementsMap, "new elements");
		verify(_subjectUnderTest).logElementsMap(elementsMap, "updated elements");
		verify(_fsObjectsLoggingFormatHelper).formatProjectProperties(projectProperties);
		verify(_subjectUnderTest, atLeastOnce()).getFsObjectsLoggingFormatHelper();
		verify(_subjectUnderTest).logElementsMap(elementsMap, "moved elements");
		verify(_subjectUnderTest).logElementsMap(elementsMap, "modified schemes");
		verify(_subjectUnderTest).logEntities(setOfEntities, "new entities");
		verify(_subjectUnderTest).logEntities(setOfEntities, "updated entities");
		verify(_subjectUnderTest).logStoreNodes(storeNodes, "lost and found store nodes");
		verify(_subjectUnderTest).logStoreNodes(storeNodes, "deleted store nodes");
	}

	@Test
	void logElementsMap_delegates_to_fsObjectsLoggingFormatHelper() {
		// GIVEN
		assumeThat(LoggerFactory.getLogger(FeatureInstallCommand.class).isDebugEnabled())
				.isTrue();
		final Map<BasicElementInfo, ElementReference> elementsMap = Map.of(
				mock(BasicElementInfo.class), mock(ElementReference.class)
		);
		when(_subjectUnderTest.getFsObjectsLoggingFormatHelper()).thenReturn(_fsObjectsLoggingFormatHelper);
		doCallRealMethod().when(_subjectUnderTest).logElementsMap(anyMap(), anyString());
		// WHEN
		_subjectUnderTest.logElementsMap(elementsMap, "");
		// THEN
		verify(_fsObjectsLoggingFormatHelper).formatElementsMap(elementsMap, "SOURCE ELEMENT: ", "\n    TARGET ELEMENT: ");
	}

	@Test
	void logEntities_delegates_to_fsObjectsLoggingFormatHelper() {
		// GIVEN
		assumeThat(LoggerFactory.getLogger(FeatureInstallCommand.class).isDebugEnabled())
				.isTrue();
		final BasicEntityInfo basicEntityInfo = mock(BasicEntityInfo.class);
		final Set<BasicEntityInfo> entities = Set.of(basicEntityInfo);
		when(_subjectUnderTest.getFsObjectsLoggingFormatHelper()).thenReturn(_fsObjectsLoggingFormatHelper);
		doCallRealMethod().when(_subjectUnderTest).logEntities(anySet(), anyString());
		// WHEN
		_subjectUnderTest.logEntities(entities, "");
		// THEN
		verify(_fsObjectsLoggingFormatHelper).formatBasicEntityInfo(basicEntityInfo);
	}

	@Test
	void logStoreNodes_delegates_to_fsObjectsLoggingFormatHelper() {
		// GIVEN
		assumeThat(LoggerFactory.getLogger(FeatureInstallCommand.class).isDebugEnabled())
				.isTrue();
		final EnumMap<Store.Type, List<BasicElementInfo>> storeNodes = new EnumMap<>(Store.Type.class);
		storeNodes.put(Store.Type.SITESTORE, List.of());
		when(_subjectUnderTest.getFsObjectsLoggingFormatHelper()).thenReturn(_fsObjectsLoggingFormatHelper);
		doCallRealMethod().when(_subjectUnderTest).logStoreNodes(any(), anyString());
		// WHEN
		_subjectUnderTest.logStoreNodes(storeNodes, "");
		// THEN
		verify(_fsObjectsLoggingFormatHelper).formatStoreNodesMap(storeNodes);
	}

	@NotNull
	private static LayerMapper.MappingContext mockMappingContext(
			@NotNull final String sourceLayer
	) {
		final LayerMapper.MappingContext result = mock(LayerMapper.MappingContext.class);
		when(result.getSourceLayer()).thenReturn(sourceLayer);
		return result;
	}
}
