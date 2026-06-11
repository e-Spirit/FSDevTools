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

package com.espirit.moddev.cli;

import com.espirit.moddev.cli.api.result.ExecutionResults;
import com.espirit.moddev.util.JacksonUtil;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization test for the result-file serialization flow.
 * Pins the structural contract of {@link Cli.WrappedCommandResult} and
 * {@link Cli.WrappedExceptionResult} so that the Jackson 3 migration
 * preserves the output shape consumed by CI/CD tooling.
 */
public class CliResultFileCharacterizationTest {

	@Test
	public void wrappedCommandResult_hasExpectedStructure() {
		final JsonMapper mapper = JacksonUtil.createOutputMapper();
		final ExecutionResults results = new ExecutionResults();
		final Cli.WrappedCommandResult wrapped = new Cli.WrappedCommandResult("test command", false, results);

		final String json = mapper.writeValueAsString(wrapped);
		final JsonNode root = mapper.readTree(json);

		assertThat(root.has("command")).isTrue();
		assertThat(root.get("command").asString()).isEqualTo("test command");
		assertThat(root.has("error")).isTrue();
		assertThat(root.get("error").asBoolean()).isFalse();
		assertThat(root.has("result")).isTrue();
	}

	@Test
	public void wrappedExceptionResult_hasExpectedStructure() {
		final JsonMapper mapper = JacksonUtil.createOutputMapper();
		final Cli.WrappedExceptionResult wrapped = new Cli.WrappedExceptionResult("test command", new RuntimeException("boom"));

		final String json = mapper.writeValueAsString(wrapped);
		final JsonNode root = mapper.readTree(json);

		assertThat(root.has("command")).isTrue();
		assertThat(root.get("command").asString()).isEqualTo("test command");
		assertThat(root.has("error")).isTrue();
		assertThat(root.get("error").asBoolean()).isTrue();
		assertThat(root.has("exception")).isTrue();
	}

}
