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

import de.espirit.firstspirit.access.database.BasicEntityInfo;
import de.espirit.firstspirit.access.store.BasicElementInfo;
import de.espirit.firstspirit.access.store.Store;
import de.espirit.firstspirit.access.store.StoreElement;
import de.espirit.firstspirit.feature.FeatureAnalyseResult;
import de.espirit.firstspirit.feature.FeatureDescriptor;
import de.espirit.firstspirit.storage.Revision;
import de.espirit.firstspirit.store.access.feature.ElementReference;
import de.espirit.firstspirit.store.access.feature.FeatureError;
import de.espirit.firstspirit.transport.PropertiesTransportOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FsObjectsLoggingFormatHelperTest {

	@Mock
	private FsObjectsLoggingFormatHelper _subjectUnderTest;
	@Mock
	private ExtendedFeatureAnalyseResult _extendedFeatureAnalyseResult;
	@Mock
	private FeatureAnalyseResult _featureAnalyseResult;

	@Test
	void logFeatureAnalyseResult_no_errors_no_warnings() {
		// GIVEN
		when(_extendedFeatureAnalyseResult.getFeatureAnalyseResult()).thenReturn(_featureAnalyseResult);
		doCallRealMethod().when(_subjectUnderTest).logFeatureAnalyseResult(any());
		// WHEN
		final boolean result = _subjectUnderTest.logFeatureAnalyseResult(_extendedFeatureAnalyseResult);
		// THEN
		assertThat(result).isFalse();
	}

	@Test
	void logFeatureAnalyseResult_no_errors_some_warnings() {
		// GIVEN
		final FeatureError testWarning1 = mock(FeatureError.class);
		final FeatureError testWarning2 = mock(FeatureError.class);
		final FeatureError testWarning3 = mock(FeatureError.class);
		final FeatureError testWarning4 = mock(FeatureError.class);
		final FeatureError testWarning5 = mock(FeatureError.class);
		Stream.of(
						testWarning1,
						testWarning2,
						testWarning3,
						testWarning4,
						testWarning5
				)
				.forEach(featureErrorMock -> when(featureErrorMock.getErrorLevel()).thenReturn(FeatureError.Level.WARNING));
		when(_featureAnalyseResult.getGlobalErrors()).thenReturn(List.of(testWarning1, testWarning2));
		when(_featureAnalyseResult.getEntryErrors()).thenReturn(Map.of(
				123L, List.of(testWarning3, testWarning4),
				456L, List.of(testWarning5)
		));
		when(_extendedFeatureAnalyseResult.getFeatureAnalyseResult()).thenReturn(_featureAnalyseResult);
		doCallRealMethod().when(_subjectUnderTest).logFeatureAnalyseResult(any());
		// WHEN
		final boolean result = _subjectUnderTest.logFeatureAnalyseResult(_extendedFeatureAnalyseResult);
		// THEN
		assertThat(result).isFalse();
		verify(_subjectUnderTest).formatFeatureErrors(argThat(arg -> {
			assertThat(arg)
					.isNotNull()
					.containsOnly(testWarning1, testWarning2, testWarning3, testWarning4, testWarning5);
			return true;
		}));
	}

	@Test
	void logFeatureAnalyseResult_some_errors_no_warnings() {
		// GIVEN
		final FeatureError testError1 = mock(FeatureError.class);
		final FeatureError testError2 = mock(FeatureError.class);
		final FeatureError testError3 = mock(FeatureError.class);
		final FeatureError testError4 = mock(FeatureError.class);
		final FeatureError testError5 = mock(FeatureError.class);
		Stream.of(
						testError1,
						testError2,
						testError3,
						testError4,
						testError5
				)
				.forEach(featureErrorMock -> when(featureErrorMock.getErrorLevel()).thenReturn(FeatureError.Level.ERROR));
		when(_featureAnalyseResult.getGlobalErrors()).thenReturn(List.of(testError1, testError2));
		when(_featureAnalyseResult.getEntryErrors()).thenReturn(Map.of(
				123L, List.of(testError3, testError4),
				456L, List.of(testError5)
		));
		when(_extendedFeatureAnalyseResult.getFeatureAnalyseResult()).thenReturn(_featureAnalyseResult);
		doCallRealMethod().when(_subjectUnderTest).logFeatureAnalyseResult(any());
		// WHEN
		final boolean result = _subjectUnderTest.logFeatureAnalyseResult(_extendedFeatureAnalyseResult);
		// THEN
		assertThat(result).isTrue();
		verify(_subjectUnderTest).formatFeatureErrors(argThat(arg -> {
			assertThat(arg)
					.isNotNull()
					.containsOnly(testError1, testError2, testError3, testError4, testError5);
			return true;
		}));
	}

	@Test
	void logFeatureAnalyseResult_some_errors_some_warnings() {
		// GIVEN
		final FeatureError testError1 = mock(FeatureError.class);
		final FeatureError testError2 = mock(FeatureError.class);
		final FeatureError testError3 = mock(FeatureError.class);
		final FeatureError testError4 = mock(FeatureError.class);
		final FeatureError testError5 = mock(FeatureError.class);
		Stream.of(
						testError1,
						testError2,
						testError3,
						testError4,
						testError5
				)
				.forEach(featureErrorMock -> when(featureErrorMock.getErrorLevel()).thenReturn(FeatureError.Level.ERROR));
		final FeatureError testWarning1 = mock(FeatureError.class);
		final FeatureError testWarning2 = mock(FeatureError.class);
		final FeatureError testWarning3 = mock(FeatureError.class);
		final FeatureError testWarning4 = mock(FeatureError.class);
		final FeatureError testWarning5 = mock(FeatureError.class);
		Stream.of(
						testWarning1,
						testWarning2,
						testWarning3,
						testWarning4,
						testWarning5
				)
				.forEach(featureErrorMock -> when(featureErrorMock.getErrorLevel()).thenReturn(FeatureError.Level.WARNING));
		when(_featureAnalyseResult.getGlobalErrors()).thenReturn(List.of(testError1, testError2, testWarning1));
		when(_featureAnalyseResult.getEntryErrors()).thenReturn(Map.of(
				123L, List.of(testError3, testError4, testWarning2),
				456L, List.of(testError5),
				789L, List.of(testWarning3, testWarning4),
				91011L, List.of(testWarning5)
		));
		final Set<FeatureError> processedErrors = new HashSet<>();
		final Set<FeatureError> processedWarnings = new HashSet<>();
		when(_subjectUnderTest.formatFeatureErrors(anyList())).thenAnswer(invocation -> {
			@SuppressWarnings("unchecked") final List<FeatureError> featureErrors = invocation.getArgument(0, List.class);
			for (final FeatureError featureError : featureErrors) {
				if (FeatureError.Level.ERROR == featureError.getErrorLevel()) {
					processedErrors.add(featureError);
				} else {
					processedWarnings.add(featureError);
				}
			}
			return "";
		});
		when(_extendedFeatureAnalyseResult.getFeatureAnalyseResult()).thenReturn(_featureAnalyseResult);
		doCallRealMethod().when(_subjectUnderTest).logFeatureAnalyseResult(any());
		// WHEN
		final boolean result = _subjectUnderTest.logFeatureAnalyseResult(_extendedFeatureAnalyseResult);
		// THEN
		assertThat(result).isTrue();
		assertThat(processedErrors).isEqualTo(Set.of(testError1, testError2, testError3, testError4, testError5));
		assertThat(processedWarnings).isEqualTo(Set.of(testWarning1, testWarning2, testWarning3, testWarning4, testWarning5));
	}

	@Test
	void formatFeatureDescriptor() {
		// GIVEN
		final FeatureDescriptor featureDescriptor = mock(FeatureDescriptor.class);
		final Revision revision = mock(Revision.class);
		when(featureDescriptor.getRevision()).thenReturn(revision);
		when(featureDescriptor.getFeatureName()).thenReturn("test");
		when(featureDescriptor.isRelease()).thenReturn(true);
		when(featureDescriptor.getElementCount()).thenReturn(509);
		when(featureDescriptor.getDatarecordsCount()).thenReturn(Map.of("test", 771));
		when(_subjectUnderTest.formatRevision(any())).thenReturn("FormattedRevision");
		when(_subjectUnderTest.formatFeatureDescriptor(any())).thenCallRealMethod();
		// WHEN
		final String result = _subjectUnderTest.formatFeatureDescriptor(featureDescriptor);
		// THEN
		assertThat(result)
				.isNotNull()
				.isEqualTo("Feature 'test':\n    Revision: FormattedRevision\n    Release: true\n    Elements: 509\n    Datasets: 771\n");
		verify(_subjectUnderTest).formatRevision(revision);
	}

	@Test
	void formatRevision() {
		// GIVEN
		final Revision revision = mock(Revision.class);
		when(revision.getId()).thenReturn(207L);
		when(revision.getComment()).thenReturn("test comment");
		when(revision.getEditor()).thenReturn("test editor");
		when(revision.getCommitOrCreationTime()).thenReturn(3002L);
		when(_subjectUnderTest.formatTimestamp(anyLong())).thenReturn("FormattedTimestamp");
		when(_subjectUnderTest.formatRevision(any())).thenCallRealMethod();
		// WHEN
		final String result = _subjectUnderTest.formatRevision(revision);
		// THEN
		assertThat(result)
				.isNotNull()
				.isEqualTo("ID = 207, Comment = test comment, Editor = test editor, Time = FormattedTimestamp");
		verify(_subjectUnderTest).formatTimestamp(3002L);
	}

	@Test
	void formatFeatureError() {
		// GIVEN
		final FeatureError obj = mock(FeatureError.class);
		when(obj.getErrorCode()).thenReturn(FeatureError.Code.MISSING_USER);
		when(obj.getMessage()).thenReturn("test message 07");
		when(_subjectUnderTest.formatFeatureError(any())).thenCallRealMethod();
		// WHEN
		final String result = _subjectUnderTest.formatFeatureError(obj);
		// THEN
		assertThat(result)
				.isNotNull()
				.isEqualTo("test message 07 (MISSING_USER)");
	}

	@Test
	void formatProjectProperties() {
		// GIVEN
		final EnumSet<PropertiesTransportOptions.ProjectPropertyType> obj = EnumSet.of(
				PropertiesTransportOptions.ProjectPropertyType.COMMON,
				PropertiesTransportOptions.ProjectPropertyType.RESOLUTIONS
		);
		when(_subjectUnderTest.formatProjectProperties(any())).thenCallRealMethod();
		// WHEN
		final String result = _subjectUnderTest.formatProjectProperties(obj);
		// THEN
		assertThat(result)
				.isNotNull()
				.isEqualTo("COMMON,RESOLUTIONS");
	}

	@Test
	void formatElementsMap() {
		// GIVEN
		final Map<BasicElementInfo, ElementReference> obj = new HashMap<>();
		final BasicElementInfo basicElementInfo = mock(BasicElementInfo.class);
		final ElementReference elementReference = mock(ElementReference.class);
		obj.put(basicElementInfo, elementReference);
		when(_subjectUnderTest.formatBasicElementInfo(any())).thenReturn("FormattedBasicElementInfo");
		when(_subjectUnderTest.formatElementReference(any())).thenReturn("FormattedElementReference");
		when(_subjectUnderTest.formatElementsMap(any(), anyString(), anyString())).thenCallRealMethod();
		// WHEN
		final String result = _subjectUnderTest.formatElementsMap(obj, "SOURCE: ", "\nTARGET: ");
		// THEN
		assertThat(result)
				.isNotNull()
				.isEqualTo("SOURCE: FormattedBasicElementInfo\nTARGET: FormattedElementReference");
		verify(_subjectUnderTest).formatBasicElementInfo(basicElementInfo);
		verify(_subjectUnderTest).formatElementReference(elementReference);
	}

	@Test
	void formatStoreNodesMap() {
		// GIVEN
		final EnumMap<Store.Type, List<BasicElementInfo>> obj = new EnumMap<>(Store.Type.class);
		final BasicElementInfo basicElementInfo = mock(BasicElementInfo.class);
		obj.put(Store.Type.SITESTORE, List.of(basicElementInfo));
		when(_subjectUnderTest.formatBasicElementInfo(any())).thenReturn("FormattedBasicElementInfo");
		when(_subjectUnderTest.formatStoreNodesMap(any())).thenCallRealMethod();
		// WHEN
		final String result = _subjectUnderTest.formatStoreNodesMap(obj);
		// THEN
		assertThat(result)
				.isNotNull()
				.isEqualTo("Store SITESTORE:\n    Element: FormattedBasicElementInfo\n");
		verify(_subjectUnderTest).formatBasicElementInfo(basicElementInfo);
	}

	@Test
	void formatBasicElementInfo() {
		// GIVEN
		final BasicElementInfo obj = mock(BasicElementInfo.class);
		when(obj.getStoreType()).thenReturn(Store.Type.SITESTORE);
		when(obj.getNodeTag()).thenReturn("test01");
		when(obj.getNodeId()).thenReturn(768L);
		when(obj.getUid()).thenReturn("testUid01");
		when(obj.getRevisionId()).thenReturn(123L);
		when(_subjectUnderTest.formatBasicElementInfo(any())).thenCallRealMethod();
		// WHEN
		final String result = _subjectUnderTest.formatBasicElementInfo(obj);
		// THEN
		assertThat(result)
				.isNotNull()
				.isEqualTo("StoreType = SITESTORE, NodeTag = test01, NodeId = 768, Uid = testUid01, RevisionId = 123");
	}

	@Test
	void formatElementReference() {
		// GIVEN
		final ElementReference obj = mock(ElementReference.class);
		when(obj.getId()).thenReturn(705L);
		when(obj.getUid()).thenReturn("test-uid-101010111");
		Mockito.doReturn(StoreElement.class).when(obj).getElementClass();
		when(obj.getProjectId()).thenReturn(913L);
		when(_subjectUnderTest.formatElementReference(any())).thenCallRealMethod();
		// WHEN
		final String result = _subjectUnderTest.formatElementReference(obj);
		// THEN
		assertThat(result)
				.isNotNull()
				.isEqualTo("ID = 705, UID = test-uid-101010111, ElementClass = interface de.espirit.firstspirit.access.store.StoreElement, ProjectId = 913");
	}

	@Test
	void formatBasicEntityInfo() {
		// GIVEN
		final BasicEntityInfo obj = mock(BasicEntityInfo.class);
		when(obj.getGid()).thenReturn(UUID.fromString("c694a910-0c5e-4ea7-bd74-8ac72a38918f"));
		when(obj.getEntityType()).thenReturn("test-e-type-03");
		when(obj.getSchemaUid()).thenReturn("testSchemaUid03");
		when(_subjectUnderTest.formatBasicEntityInfo(any())).thenCallRealMethod();
		// WHEN
		final String result = _subjectUnderTest.formatBasicEntityInfo(obj);
		// THEN
		assertThat(result)
				.isNotNull()
				.isEqualTo("Gid = c694a910-0c5e-4ea7-bd74-8ac72a38918f, EntityType = test-e-type-03, SchemaUid = testSchemaUid03");
	}

	@ParameterizedTest
	@MethodSource("testDataFor_formatTimestamp")
	void formatTimestamp(
			// GIVEN
			final long timestampMillis,
			final String expectedResult
	) {
		when(_subjectUnderTest.formatTimestamp(anyLong())).thenCallRealMethod();
		// WHEN
		final String result = _subjectUnderTest.formatTimestamp(timestampMillis);
		// THEN
		assertThat(result)
				.isNotNull()
				.isEqualTo(expectedResult);
	}

	static Stream<Arguments> testDataFor_formatTimestamp() {
		final Arguments args01 = Arguments.of(-23L, "<unknown>");
		final Arguments args02 = Arguments.of(-1L, "<unknown>");
		final Arguments args03 = Arguments.of(0L, "<unknown>");
		final Arguments args04 = Arguments.of(1665108307478L, "2022-10-07T02:05:07.478Z");
		final Arguments args05 = Arguments.of(1665408307478L, "2022-10-10T13:25:07.478Z");
		return Stream.of(
				args01,
				args02,
				args03,
				args04,
				args05
		);
	}
}
