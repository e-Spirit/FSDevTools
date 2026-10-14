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

package com.espirit.moddev.cli.classloading.probe;

import com.espirit.moddev.cli.api.script.ScriptEngine;
import com.espirit.moddev.cli.api.script.ScriptExecutable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Fixture plugin: reports, for one class of every library the CLI relocates, whether a plugin can load it
 * and from which jar. The classes are looked up by their original (unrelocated) names, as a plugin compiled
 * against cli-api would reference them.
 */
public class ProbeScriptEngine implements ScriptEngine {

	public static final String NAME = "PROBE";

	private static final String[] PROBED_CLASSES = {
			"com.github.rvesse.airline.annotations.Command",
			"tools.jackson.databind.ObjectMapper",
			"com.fasterxml.jackson.annotation.JsonProperty",
			"com.google.common.collect.ImmutableList",
			"org.apache.commons.lang3.StringUtils",
			"org.apache.commons.compress.archivers.ArchiveEntry",
			"io.github.classgraph.ClassGraph"
	};

	@NotNull
	@Override
	public String getName() {
		return NAME;
	}

	@NotNull
	@Override
	public ScriptExecutable getExecutable(@NotNull final String scriptSource) {
		return new ScriptExecutable() {
			@Override
			public void parse(@NotNull final Map<String, Object> context) {
				// nothing to parse
			}

			@Override
			public Object execute(@NotNull final Map<String, Object> context) {
				final StringBuilder report = new StringBuilder();
				for (final String className : PROBED_CLASSES) {
					report.append(className).append(' ').append(probe(className)).append('\n');
				}
				return report.toString();
			}
		};
	}

	private static String probe(final String className) {
		try {
			final Class<?> clazz = Class.forName(className, true, ProbeScriptEngine.class.getClassLoader());
			final java.security.CodeSource source = clazz.getProtectionDomain().getCodeSource();
			final String location = source == null ? "unknown" : source.getLocation().getPath();
			return "OK " + location.substring(location.lastIndexOf('/') + 1);
		} catch (final Throwable t) {
			return "FAIL " + t.getClass().getSimpleName();
		}
	}

}
