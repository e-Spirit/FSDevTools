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

package com.espirit.moddev.cli.commands.script.common.groovy;

import com.espirit.moddev.cli.api.script.ScriptExecutable;
import com.espirit.moddev.cli.api.script.exception.ScriptExecutionException;
import com.espirit.moddev.cli.api.script.exception.ScriptParsingException;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilationFailedException;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * {@link ScriptExecutable} implementation for Groovy scripts.
 */
public class GroovyScriptExecutable implements ScriptExecutable {

	private final ClassLoader _classLoader;
	private final String _source;

	public GroovyScriptExecutable(@NotNull final ClassLoader classLoader, @NotNull final String source) {
		_classLoader = classLoader;
		_source = source;
	}

	@Override
	public void parse(@NotNull final Map<String, Object> context) throws ScriptParsingException {
		try {
			final GroovyShell groovyShell = new GroovyShell(_classLoader);
			groovyShell.parse(_source);
		} catch (final CompilationFailedException e) {
			throw new ScriptParsingException(e);
		}
	}

	@Override
	public Object execute(@NotNull final Map<String, Object> context) throws ScriptExecutionException {
		try {
			final GroovyShell groovyShell = new GroovyShell(_classLoader);
			final Script script = groovyShell.parse(_source);
			script.setBinding(new Binding(context));
			return script.run();
		} catch (final Exception | Error e) {
			throw new ScriptExecutionException(e);
		}
	}
}
