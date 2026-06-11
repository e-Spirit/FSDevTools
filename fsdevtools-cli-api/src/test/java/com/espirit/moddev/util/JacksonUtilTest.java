/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2026 Crownpeak Technology GmbH
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

package com.espirit.moddev.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class JacksonUtilTest {

	@Test
	public void createInputMapper() {
		final JsonMapper mapper = JacksonUtil.createInputMapper();
		Assertions.assertThat(mapper.deserializationConfig().hasDeserializationFeatures(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES.getMask())).isTrue();
		Assertions.assertThat(mapper.deserializationConfig().hasDeserializationFeatures(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES.getMask())).isFalse();
	}

	@Test
	public void createOutputMapper() throws JacksonException {
		final JsonMapper mapper = JacksonUtil.createOutputMapper();
		// Auto-detection is disabled — a plain POJO without @JsonProperty annotations must serialize to {}
		final String plainJson = mapper.writeValueAsString(new PlainPojoAutoDetectFixture());
		Assertions.assertThat(plainJson).doesNotContain("\"plainField\"", "\"plainGetter\"");
		Assertions.assertThat(mapper.serializationConfig().hasSerializationFeatures(SerializationFeature.INDENT_OUTPUT.getMask())).isTrue();
		Assertions.assertThat(mapper.serializationConfig().hasSerializationFeatures(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS.getMask())).isTrue();
	}

	@Test
	public void test_exception_serialization() throws JacksonException {
		// setup
		final JsonMapper mapper = JacksonUtil.createOutputMapper();
		final IOException innerCause = new IOException("innerCause");
		final IOException cause = new IOException("testCause", innerCause);
		final IOException exception = new IOException("errorMessage", cause);
		// test
		final String json = mapper.writeValueAsString(exception);
		// verify — structural assertions (Jackson 3 may vary whitespace cosmetically)
		assertThat(json).contains("\"class\"");
		assertThat(json).contains("java.io.IOException");
		assertThat(json).contains("\"message\"");
		assertThat(json).contains("errorMessage");
		assertThat(json).contains("testCause");
		assertThat(json).contains("innerCause");
		assertThat(json).contains("\"cause\"");
	}

	@Test
	public void hasJsonSerializeAnnotation_annotatedClass_returnsTrue() {
		assertThat(JacksonUtil.hasJsonSerializeAnnotation(AnnotatedClass.class)).isTrue();
	}

	@Test
	public void hasJsonSerializeAnnotation_unannotatedClass_returnsFalse() {
		assertThat(JacksonUtil.hasJsonSerializeAnnotation(UnannotatedClass.class)).isFalse();
	}

	@Test
	public void hasJsonSerializeAnnotation_unrelatedAnnotation_returnsFalse() {
		assertThat(JacksonUtil.hasJsonSerializeAnnotation(UnrelatedAnnotationClass.class)).isFalse();
	}

	// --- test fixtures ---

	@SuppressWarnings("unused")
	private static class PlainPojoAutoDetectFixture {
		public String plainField = "fieldValue";
		public String getPlainGetter() { return "getterValue"; }
	}

	@JsonSerialize
	private static class AnnotatedClass {
	}

	private static class UnannotatedClass {
	}

	@SuppressWarnings("unused")
	@Deprecated
	private static class UnrelatedAnnotationClass {
	}

}
