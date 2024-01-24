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

package com.espirit.moddev.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class JacksonUtilTest {

	@Test
	public void createInputMapper() {
		final ObjectMapper mapper = JacksonUtil.createInputMapper();
		Assertions.assertThat(mapper.getDeserializationConfig().hasDeserializationFeatures(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES.getMask())).isTrue();
		Assertions.assertThat(mapper.getDeserializationConfig().hasDeserializationFeatures(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES.getMask())).isFalse();
	}

	@Test
	public void createOutputMapper() {
		final ObjectMapper mapper = JacksonUtil.createOutputMapper();
		Assertions.assertThat(mapper.isEnabled(MapperFeature.AUTO_DETECT_CREATORS)).isFalse();
		Assertions.assertThat(mapper.isEnabled(MapperFeature.AUTO_DETECT_FIELDS)).isFalse();
		Assertions.assertThat(mapper.isEnabled(MapperFeature.AUTO_DETECT_GETTERS)).isFalse();
		Assertions.assertThat(mapper.isEnabled(MapperFeature.AUTO_DETECT_IS_GETTERS)).isFalse();
		Assertions.assertThat(mapper.getSerializationConfig().hasSerializationFeatures(SerializationFeature.INDENT_OUTPUT.getMask())).isTrue();
		Assertions.assertThat(mapper.getSerializationConfig().hasSerializationFeatures(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS.getMask())).isTrue();
	}

	@Test
	public void test_exception_serialization() throws JsonProcessingException {
		// setup
		final ObjectMapper mapper = JacksonUtil.createOutputMapper();
		final IOException innerCause = new IOException("innerCause");
		final IOException cause = new IOException("testCause", innerCause);
		final IOException exception = new IOException("errorMessage", cause);
		// test
		final String json = mapper.writeValueAsString(exception);
		// verify
		final String expectedResult = "{\n" +
				"  \"class\" : \"java.io.IOException\",\n" +
				"  \"message\" : \"errorMessage\",\n" +
				"  \"localizedMessage\" : \"errorMessage\",\n" +
				"  \"cause\" : {\n" +
				"    \"class\" : \"java.io.IOException\",\n" +
				"    \"message\" : \"testCause\",\n" +
				"    \"localizedMessage\" : \"testCause\",\n" +
				"    \"cause\" : {\n" +
				"      \"class\" : \"java.io.IOException\",\n" +
				"      \"message\" : \"innerCause\",\n" +
				"      \"localizedMessage\" : \"innerCause\"\n" +
				"    }\n" +
				"  }\n" +
				"}";
		assertThat(json.replaceAll("\r", "")).isEqualTo(expectedResult);
	}

}
