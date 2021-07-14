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

package com.espirit.moddev.cli.commands.module.configureCommand.json.components.common;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ProjectNotFoundResultTest {

	private static final String PROJECT_NAME = "testProject";

	@Test
	public void getException() {
		final ProjectNotFoundResult result = new ProjectNotFoundResult(PROJECT_NAME);
		final IllegalStateException exception = result.getException();
		assertThat(exception.getMessage()).isEqualTo(String.format(ProjectNotFoundResult.MESSAGE, PROJECT_NAME));
	}

	@Test
	public void _toString() {
		final ProjectNotFoundResult result = new ProjectNotFoundResult(PROJECT_NAME);
		assertThat(result.toString()).isEqualTo(String.format(ProjectNotFoundResult.MESSAGE, PROJECT_NAME));
	}

}
