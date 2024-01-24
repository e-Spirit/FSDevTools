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

package com.espirit.moddev.cli.commands.script.common.javascript;

import com.espirit.moddev.cli.api.script.ScriptExecutable;
import com.espirit.moddev.cli.api.script.exception.ScriptExecutionException;
import com.espirit.moddev.cli.api.script.exception.ScriptParsingException;
import org.jetbrains.annotations.NotNull;
import org.openjdk.nashorn.api.scripting.NashornScriptEngine;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.CompiledScript;
import javax.script.SimpleBindings;
import java.util.Map;

/**
 * {@link ScriptExecutable} implementation for Javascript scripts.
 */
public class JavascriptScriptExecutable implements ScriptExecutable {

	private final ClassLoader _classLoader;
	private final String _source;

	public JavascriptScriptExecutable(@NotNull final ClassLoader classLoader, @NotNull final String source) {
		_classLoader = classLoader;
		final String consolePrefix = "var console = { \n" +
				"    debug: print,\n" +
				"    info: print,\n" +
				"    log: print,\n" +
				"    warn: print,\n" +
				"    error: print\n" +
				"};\n\n";
		_source = consolePrefix + source;
	}

	@Override
	public void parse(@NotNull final Map<String, Object> context) throws ScriptParsingException {
		try {
			final NashornScriptEngine scriptEngine = (NashornScriptEngine) new NashornScriptEngineFactory().getScriptEngine(_classLoader);
			scriptEngine.compile(_source);
		} catch (final Exception e) {
			throw new ScriptParsingException(e);
		}
	}

	@Override
	public Object execute(@NotNull final Map<String, Object> context) throws ScriptExecutionException {
		try {
			final NashornScriptEngine scriptEngine = (NashornScriptEngine) new NashornScriptEngineFactory().getScriptEngine(_classLoader);
			final CompiledScript script = scriptEngine.compile(_source);
			return script.eval(new SimpleBindings(context));
		} catch (final Exception e) {
			throw new ScriptExecutionException(e);
		}
	}

}
