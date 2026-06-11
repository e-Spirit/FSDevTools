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

package com.espirit.moddev.cli.api.json.serializer;

import com.espirit.moddev.cli.api.result.ExecutionResultsTest;
import com.espirit.moddev.util.JacksonUtil;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultExecutionResultSerializerTest {

	@Test
	public void serialize() throws JacksonException {
		// setup
		final JsonMapper jsonMapper = JacksonUtil.createOutputMapper();
		final ExecutionResultsTest.TestResult testResult = new ExecutionResultsTest.TestResult(42);

		// test
		final String json = jsonMapper.writeValueAsString(testResult);
		final JsonNode root = jsonMapper.readTree(json);

		// verify structural contract
		assertThat(root.has("message")).isTrue();
		assertThat(root.get("message").asString()).isEqualTo("TestResult[value=42]");
	}

}
