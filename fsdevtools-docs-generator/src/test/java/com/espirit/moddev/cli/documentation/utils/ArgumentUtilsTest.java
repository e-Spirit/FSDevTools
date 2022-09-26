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

import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class ArgumentUtilsTest {

	@Test
	public void findPath() {
		assertThat(ArgumentUtils.findPath(new String[]{"--file"})).isEmpty();
		assertThat(ArgumentUtils.findPath(new String[]{"--file", "path/to/file.json"})).contains(Paths.get("path/to/file.json"));
		assertThat(ArgumentUtils.findPath(new String[]{"firstArgument", "--file", "path/to/file.json"})).contains(Paths.get("path/to/file.json"));
		assertThat(ArgumentUtils.findPath(new String[]{"--file", "path/to/file.json", "lastArgument"})).contains(Paths.get("path/to/file.json"));
	}

}