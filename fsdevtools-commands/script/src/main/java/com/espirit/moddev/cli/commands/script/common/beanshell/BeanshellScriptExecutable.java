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

package com.espirit.moddev.cli.commands.script.common.beanshell;

import bsh.EvalError;
import bsh.PreparsedScript;
import bsh.Primitive;
import bsh.TargetError;
import com.espirit.moddev.cli.api.script.ScriptExecutable;
import com.espirit.moddev.cli.api.script.exception.ScriptExecutionException;
import com.espirit.moddev.cli.api.script.exception.ScriptParsingException;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link ScriptExecutable} implementation for Beanshell scripts.
 */
class BeanshellScriptExecutable implements ScriptExecutable {

	private final ClassLoader _classLoader;
	private final String _source;

	BeanshellScriptExecutable(@NotNull final ClassLoader classLoader, @NotNull final String source) {
		_classLoader = classLoader;
		_source = source;
	}

	@Override
	public void parse(@NotNull final Map<String, Object> context) throws ScriptParsingException {
		try {
			new PreparsedScript(_source, _classLoader);
		} catch (final EvalError e) {
			try {
				throw new ScriptParsingException(e, e.getErrorLineNumber(), -1);
			} catch (final RuntimeException re) {
				// EvalError#getErrorLineNumber sometimes throws NPE
				throw new ScriptParsingException(e);
			}
		}
	}

	@SuppressWarnings({"UseOfSystemOutOrSystemErr"})
	@Override
	public Object execute(@NotNull final Map<String, Object> context) throws ScriptExecutionException {
		final HashMap<String, Object> localContext = new HashMap<>(context.size());
		for (final Map.Entry<String, Object> entry : context.entrySet()) {
			final Object value = entry.getValue() != null ? entry.getValue() : Primitive.NULL;
			localContext.put(entry.getKey(), value);
		}
		try {
			final PreparsedScript script = new PreparsedScript(_source, _classLoader);
			return script.invoke(localContext);
		} catch (final TargetError e) {
			throw new ScriptExecutionException(e.getMessage(), e.getTarget(), e.getErrorLineNumber(), -1);
		} catch (final EvalError e) {
			throw new ScriptExecutionException(e.getMessage(), e, e.getErrorLineNumber(), -1);
		} catch (final Exception | Error e) {
			throw new ScriptExecutionException(e);
		}
	}

}
