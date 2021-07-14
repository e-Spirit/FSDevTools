/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2021 e-Spirit AG
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultExecutionResultSerializerTest {

	@Test
	public void serialize() throws JsonProcessingException {
		// setup
		final ObjectMapper objectMapper = JacksonUtil.createOutputMapper();
		final ExecutionResultsTest.TestResult testResult = new ExecutionResultsTest.TestResult(42);

		// test
		final String json = objectMapper.writeValueAsString(testResult);

		// verify
		final String expectedResult = "{" +
				"  \"message\" : \"TestResult[value=42]\"" +
				"}";
		assertThat(json).isEqualToIgnoringNewLines(expectedResult);
	}

}
