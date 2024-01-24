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

import com.espirit.moddev.cli.api.script.ScriptEngine;
import com.espirit.moddev.cli.api.script.ScriptExecutable;
import org.jetbrains.annotations.NotNull;

/**
 * {@link ScriptEngine} implementation for Groovy scripts.
 */
public class GroovyScriptEngine implements ScriptEngine {

	/**
	 * The name of the Groovy script engine.
	 */
	public static final String NAME = "GROOVY";

	private final ClassLoader _classLoader;

	public GroovyScriptEngine() {
		_classLoader = GroovyScriptEngine.class.getClassLoader();
	}

	@NotNull
	@Override
	public String getName() {
		return NAME;
	}

	@NotNull
	@Override
	public ScriptExecutable getExecutable(@NotNull final String scriptSource) {
		return new GroovyScriptExecutable(_classLoader, scriptSource);
	}

}
