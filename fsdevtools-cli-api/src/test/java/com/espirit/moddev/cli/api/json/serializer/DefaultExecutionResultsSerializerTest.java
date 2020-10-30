/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2020 e-Spirit AG
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

import com.espirit.moddev.cli.api.result.ExecutionResults;
import com.espirit.moddev.cli.api.result.ExecutionResultsTest;
import com.espirit.moddev.util.JacksonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultExecutionResultsSerializerTest {

	@Test
	public void serialize_no_errorResults() throws JsonProcessingException {
		// setup
		final ObjectMapper objectMapper = JacksonUtil.createOutputMapper();
		final ExecutionResults results = new ExecutionResults();
		results.add(new ExecutionResultsTest.TestResult(42));
		results.add(new ExecutionResultsTest.TestResult(1337));

		// test
		final String json = objectMapper.writeValueAsString(results);

		// verify
		final String expectedResult = "{" +
				"  \"error\" : false," +
				"  \"results\" : [ {" +
				"    \"message\" : \"TestResult[value=42]\"" +
				"  }, {" +
				"    \"message\" : \"TestResult[value=1337]\"" +
				"  } ]" +
				"}";
		assertThat(json).isEqualToIgnoringNewLines(expectedResult);
	}

	@Test
	public void serialize_with_errorResults() throws JsonProcessingException {
		// setup
		final ObjectMapper objectMapper = JacksonUtil.createOutputMapper();
		final ExecutionResults results = new ExecutionResults();
		results.add(new ExecutionResultsTest.TestResult(42));
		results.add(new ExecutionResultsTest.TestErrorResult(1337));

		// test
		final String json = objectMapper.writeValueAsString(results);

		// verify
		final String expectedResult = "{\n" +
				"  \"error\" : true,\n" +
				"  \"results\" : [ {\n" +
				"    \"message\" : \"TestResult[value=42]\"\n" +
				"  }, {\n" +
				"    \"message\" : \"TestErrorResult[value=1337, exception=java.lang.IllegalStateException: 1337]\",\n" +
				"    \"exception\" : {\n" +
				"      \"class\" : \"java.lang.IllegalStateException\",\n" +
				"      \"message\" : \"1337\",\n" +
				"      \"localizedMessage\" : \"1337\"\n" +
				"    }\n" +
				"  } ]\n" +
				"}";
		assertThat(json.replaceAll("\r", "")).isEqualTo(expectedResult);
	}

}
