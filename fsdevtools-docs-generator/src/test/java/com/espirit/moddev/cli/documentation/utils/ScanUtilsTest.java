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

package com.espirit.moddev.cli.documentation.utils;

import com.espirit.moddev.cli.documentation.testclasses.NonCommandClass;
import io.github.classgraph.AnnotationInfo;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Optional;

import static com.espirit.moddev.cli.documentation.testclasses.NonCommandClass.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScanUtilsTest {

	@Test
	public void getValueAsString() {
		// setup
		final AnnotationInfo info = mock(AnnotationInfo.class, RETURNS_DEEP_STUBS);
		when(info.getParameterValues(true).getValue(any())).thenReturn(VALUE);

		// test
		final Optional<String> value = ScanUtils.getValueAsString(info, FIELD_VALUE, DEFAULT_VALUE);

		// verify
		assertThat(value).contains(VALUE);
	}

	@Test
	public void getValueAsString_fromArray() {
		// setup
		final AnnotationInfo info = mock(AnnotationInfo.class, RETURNS_DEEP_STUBS);
		when(info.getParameterValues(true).getValue(any())).thenReturn(new String[]{VALUE});

		// test
		final Optional<String> value = ScanUtils.getValueAsString(info, FIELD_VALUE, DEFAULT_VALUE);

		// verify
		assertThat(value).contains(VALUE);
	}

	@Test
	public void getValueAsString_fromEmptyArray_defaultValue() {
		// setup
		final AnnotationInfo info = mock(AnnotationInfo.class, RETURNS_DEEP_STUBS);
		when(info.getParameterValues(true).getValue(any())).thenReturn(new String[]{});

		// test
		final Optional<String> value = ScanUtils.getValueAsString(info, FIELD_VALUE, DEFAULT_VALUE);

		// verify
		assertThat(value).contains(DEFAULT_VALUE);
	}

	@Test
	public void getValueAsString_fromArray_nullValueFallbackToDefault() {
		// setup
		final AnnotationInfo info = mock(AnnotationInfo.class, RETURNS_DEEP_STUBS);
		when(info.getParameterValues(true).getValue(any())).thenReturn(new String[]{null});

		// test
		final Optional<String> value = ScanUtils.getValueAsString(info, FIELD_VALUE, DEFAULT_VALUE);

		// verify
		assertThat(value).contains(DEFAULT_VALUE);
	}

	@Test
	public void getValueAsString_defaultValue() {
		// setup
		final AnnotationInfo info = mock(AnnotationInfo.class, RETURNS_DEEP_STUBS);
		when(info.getParameterValues(true).getValue(any())).thenReturn(null);

		// test
		final Optional<String> value = ScanUtils.getValueAsString(info, FIELD_VALUE, DEFAULT_VALUE);

		// verify
		assertThat(value).contains(DEFAULT_VALUE);
	}

	@Test
	public void getValueAsString_nullValue() {
		// setup
		final AnnotationInfo info = mock(AnnotationInfo.class, RETURNS_DEEP_STUBS);
		when(info.getParameterValues(true).getValue(any())).thenReturn(null);

		// test
		final Optional<String> value = ScanUtils.getValueAsString(info, FIELD_VALUE);

		// verify
		assertThat(value).isNotPresent();
	}

	@Test
	public void getValueAsArray() {
		// setup
		final String[] array = {"value#1"};
		final AnnotationInfo info = mock(AnnotationInfo.class, RETURNS_DEEP_STUBS);
		when(info.getParameterValues(true).getValue(any())).thenReturn(array);

		// test
		final Optional<String[]> value = ScanUtils.getValueAsArray(info, FIELD_VALUE);

		// verify
		assertThat(value).containsSame(array);
	}

	@Test
	public void getValueAsArray_nullValue() {
		// setup
		final AnnotationInfo info = mock(AnnotationInfo.class, RETURNS_DEEP_STUBS);
		when(info.getParameterValues(true).getValue(any())).thenReturn(null);

		// test
		final Optional<String[]> value = ScanUtils.getValueAsArray(info, FIELD_VALUE);

		// verify
		assertThat(value).isNotPresent();
	}

	@Test
	public void getValueAsArray_nonArrayValue() {
		// setup
		final AnnotationInfo info = mock(AnnotationInfo.class, RETURNS_DEEP_STUBS);
		when(info.getParameterValues(true).getValue(any())).thenReturn("NON_ARRAY_VALUE");

		// test
		final Optional<String[]> value = ScanUtils.getValueAsArray(info, FIELD_VALUE);

		// verify
		assertThat(value).isNotPresent();
	}

	@Test
	public void getField() {
		assertThat(ScanUtils.getField(NonCommandClass.class, FIELD_VALUE)).isPresent();
		assertThat(ScanUtils.getField(NonCommandClass.class, "NON EXISTING FIELD")).isNotPresent();
	}

	@Test
	public void getFieldValue() {
		final Optional<Field> existingField = ScanUtils.getField(NonCommandClass.class, FIELD_VALUE);
		final Optional<Field> nonExistingField = ScanUtils.getField(NonCommandClass.class, "NON EXISTING FIELD");
		assertThat(existingField).isPresent();
		assertThat(nonExistingField).isNotPresent();
		assertThat(ScanUtils.getFieldValue(new NonCommandClass(VALUE), existingField.get())).isEqualTo(VALUE);
	}

	@Test
	public void getFieldValueAsString() {
		assertThat(ScanUtils.getFieldValueAsString(new NonCommandClass(null), FIELD_VALUE)).isNull();
		assertThat(ScanUtils.getFieldValueAsString(new NonCommandClass(VALUE), FIELD_VALUE)).isNotNull();
		assertThat(ScanUtils.getFieldValueAsString(new NonCommandClass(VALUE), "NON EXISTING FIELD")).isNull();
	}

}