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

package com.espirit.moddev.cli;

import com.espirit.moddev.cli.api.result.ExecutionResult;
import com.espirit.moddev.cli.api.result.ExecutionResults;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.fail;


public class LicenseTest {

	private static final String[] EXTENSIONS_TO_CHECK = new String[]{".java", ".gradle", ".groovy", ".properties"};
	private static final String[] DIRS_TO_IGNORE = new String[]{".idea", ".git", ".gradle", "build", "gradle", "out"};
	private static final String[] FILES_TO_IGNORE = new String[]{"/gradle.properties"};

	private static final String LICENSE_YEAR = "2024";
	private static final String LICENSE_VENDOR = "Crownpeak Technology GmbH";

	@Test
	public void testLicenses() throws IOException {
		final ExecutionResults executionResults = testPath(Paths.get("").resolve("../"));
		if (!executionResults.isEmpty()) {
			final ArrayList<String> errors = new ArrayList<>();
			errors.add("The following files have mismatched license headers:");
			executionResults.stream().forEach(executionResult -> errors.add(" - " + executionResult.toString()));
			fail(String.join("\n", errors));
		}
	}

	@NotNull
	private ExecutionResults testPath(@NotNull final Path path) throws IOException {
		final ExecutionResults results = new ExecutionResults();
		final File dir = path.toFile();
		final File[] files = dir.listFiles(file -> {
			final String fileName = file.getName();
			if (file.isDirectory()) {
				for (final String dirToIgnore : DIRS_TO_IGNORE) {
					if (fileName.equalsIgnoreCase(dirToIgnore)) {
						return false;
					}
				}
				return true;
			}
			if (file.isFile()) {
				for (final String fileToIgnore : FILES_TO_IGNORE) {
					final String filePath = file.toPath().toString().replaceAll("\\\\", "/").replaceAll("\\.\\./", "/");
					if (filePath.equals(fileToIgnore)) {
						return false;
					}
				}
				for (final String extension : EXTENSIONS_TO_CHECK) {
					if (fileName.toLowerCase(Locale.ROOT).endsWith(extension)) {
						return true;
					}
				}
			}
			return false;
		});

		if (files != null) {
			for (final File file : files) {
				if (file.isDirectory()) {
					results.add(testPath(file.toPath()));
				} else if (file.isFile()) {
					final String fileContent = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
					if (!fileContent.contains("Copyright (C) " + LICENSE_YEAR + " " + LICENSE_VENDOR) || !fileContent.contains("http://www.apache.org/licenses/LICENSE-2.0")) {
						results.add(new ExecutionResult() {
							@Override
							public String toString() {
								return file.toPath().toString().replaceAll("\\\\", "/").replaceAll("\\.\\./", "/");
							}
						});
					}
				}
			}
		}
		return results;
	}
}
