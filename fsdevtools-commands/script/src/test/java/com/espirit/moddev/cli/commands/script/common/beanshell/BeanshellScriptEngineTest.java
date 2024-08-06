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

import com.espirit.moddev.cli.api.script.exception.ScriptExecutionException;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class BeanshellScriptEngineTest {

	@Test
	void returnValue() throws ScriptExecutionException {
		final var engine = new BeanshellScriptEngine();
		final var executable = engine.getExecutable("return 42;");
		assertThat(executable.execute(Collections.emptyMap())).isEqualTo(42);
	}

	@Test
	void useContext() throws ScriptExecutionException {
		final var engine = new BeanshellScriptEngine();
		final var executable = engine.getExecutable("return myValue;");
		assertThat(executable.execute(Map.of("myValue", "23"))).isEqualTo("23");
	}

	@Test
	void lineNumber() {
		final var engine = new BeanshellScriptEngine();

		// Missing semicolon in second line
		final var executable = engine.getExecutable("""
				firstValue = 1;
				secondValue = 2
				thirdValue = 3;
				""");

		assertThatExceptionOfType(ScriptExecutionException.class)
				.isThrownBy(() -> executable.execute(Collections.emptyMap()))
				.withMessageContaining("Parse error at line 3, column 1");
	}

}