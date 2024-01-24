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

package com.espirit.moddev.cli.commands.script.common;

import com.espirit.moddev.cli.api.script.ScriptEngine;
import com.espirit.moddev.cli.api.script.ScriptExecutable;
import com.espirit.moddev.cli.commands.script.common.beanshell.BeanshellScriptEngine;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

class ScriptEngineRegistryTest {

	@Test
	void initialize() {
		// setup
		final ScriptEngineRegistry registry = new ScriptEngineRegistry();

		// test & verify
		assertThat(registry.getRegisteredEngines()).hasSize(0);
		registry.initialize();
		assertThat(registry.getRegisteredEngines()).hasSize(1);
		assertThat(registry.getRegisteredEngines()).first().isInstanceOf(BeanshellScriptEngine.class);
	}

	@Test
	void getPluginsPath() {
		// setup
		final ScriptEngineRegistry registry = new ScriptEngineRegistry();

		// test
		final Optional<Path> path = registry.getPluginsPath();

		// verify
		assertThat(path).isNotEmpty();
		assertThat(path.get().toString().replaceAll("\\\\", "/")).endsWith("plugins");
	}

	@Test
	void getPluginsPath_withError() throws URISyntaxException {
		// setup
		final ScriptEngineRegistry registry = spy(new ScriptEngineRegistry());
		doAnswer(invocation -> {
			throw new URISyntaxException("message", "reason");
		}).when(registry).getJarPath();

		// test
		final Optional<Path> path = registry.getPluginsPath();

		// verify
		assertThat(path).isEmpty();
	}

	@Test
	void initializeExternalEngines() {
		// setup
		final ScriptEngineRegistry registry = new ScriptEngineRegistry();

		// test
		final Path resourceDir = new File("").toPath().resolve("src").resolve("test").resolve("resources");
		registry.initializeExternalEngines(resourceDir);

		// verify
		assertThat(registry.getRegisteredEngines()).hasSize(2);
		assertThat(registry.getRegisteredEngines()).extracting(ScriptEngine::getName).containsExactlyInAnyOrder("GROOVY", "JAVASCRIPT");
	}

	@Test
	void scanExternalJars() {
		// setup
		final ScriptEngineRegistry registry = new ScriptEngineRegistry();

		// test
		final Path resourceDir = new File("").toPath().resolve("src").resolve("test").resolve("resources");
		final File[] files = resourceDir.toFile().listFiles(pathname -> pathname.getName().endsWith(".jar"));
		final ArrayList<Class<? extends ScriptEngine>> result = registry.scanExternalJars(files);

		// verify
		assertThat(result).hasSize(2);
		assertThat(result).extracting(Class::getSimpleName).containsExactlyInAnyOrder("GroovyScriptEngine", "JavascriptScriptEngine");
	}

	@Test
	void scanClassLoader() {
		// setup
		final ScriptEngineRegistry registry = new ScriptEngineRegistry();

		// test
		final Collection<Class<? extends ScriptEngine>> classes = registry.scanClassLoader(getClass().getClassLoader());

		// verify
		assertThat(classes).hasSize(4);
		assertThat(classes).contains(BeanshellScriptEngine.class);
		assertThat(classes).contains(TestEngineWithDefaultConstructor.class);
		assertThat(classes).contains(TestEngineWithoutThrowingInstantiationException.class);
		assertThat(classes).contains(TestEngineWithoutThrowingInstantiationException.class);
	}

	@Test
	void registerEngines() {
		// setup
		final ScriptEngineRegistry registry = new ScriptEngineRegistry();
		registry.initialize();

		// test
		final List<Class<? extends ScriptEngine>> enginesToRegister = List.of(BeanshellScriptEngine.class, TestEngineWithDefaultConstructor.class, TestEngineWithoutDefaultConstructor.class, TestEngineWithoutThrowingInstantiationException.class);
		final Collection<ScriptEngine> result = registry.registerEngines(enginesToRegister);

		// verify
		assertThat(result).hasSize(1);
		assertThat(result).first().isInstanceOf(TestEngineWithDefaultConstructor.class);
		assertThat(registry.getRegisteredEngines()).hasSize(2);
		assertThat(registry.getRegisteredEngines()).allMatch(engine -> engine instanceof BeanshellScriptEngine || engine instanceof TestEngineWithDefaultConstructor);
	}

	@Test
	void createEngine() {
		// setup
		final ScriptEngineRegistry registry = new ScriptEngineRegistry();

		// test
		final Optional<ScriptEngine> optional = registry.createEngine(BeanshellScriptEngine.class);

		// verify
		assertThat(optional).isNotEmpty();
	}

	@Test
	void createEngine_noDefaultConstructor() {
		// setup
		final ScriptEngineRegistry registry = new ScriptEngineRegistry();

		// test
		final Optional<ScriptEngine> optional = registry.createEngine(TestEngineWithoutDefaultConstructor.class);

		// verify
		assertThat(optional).isEmpty();
	}

	@Test
	void createEngine_instantiationException() {
		// setup
		final ScriptEngineRegistry registry = new ScriptEngineRegistry();

		// test
		final Optional<ScriptEngine> optional = registry.createEngine(TestEngineWithoutThrowingInstantiationException.class);

		// verify
		assertThat(optional).isEmpty();
	}

	@Test
	void requestEngine() {
		// setup
		final ScriptEngineRegistry registry = new ScriptEngineRegistry();
		registry.initialize();

		// test
		final Optional<ScriptEngine> optionalEngine = registry.requestEngine(BeanshellScriptEngine.NAME);

		// verify
		assertThat(optionalEngine).isNotEmpty();
		assertThat(optionalEngine).containsInstanceOf(BeanshellScriptEngine.class);
	}

	@Test
	void requestEngine_empty() {
		// setup
		final ScriptEngineRegistry registry = new ScriptEngineRegistry();

		// test
		final Optional<ScriptEngine> optionalEngine = registry.requestEngine(BeanshellScriptEngine.NAME);

		// verify
		assertThat(optionalEngine).isEmpty();
	}

	@Test
	void requireEngine() {
		// setup
		final ScriptEngineRegistry registry = new ScriptEngineRegistry();
		registry.initialize();

		// test
		final ScriptEngine engine = registry.requireEngine(BeanshellScriptEngine.NAME);

		// verify
		assertThat(engine).isInstanceOf(BeanshellScriptEngine.class);
	}

	@Test
	void requireEngine_notFound() {
		// setup
		final ScriptEngineRegistry registry = new ScriptEngineRegistry();

		// test & verify
		assertThatThrownBy(() -> registry.requireEngine(BeanshellScriptEngine.NAME)).isInstanceOf(IllegalStateException.class);
	}

	@Test
	void registerEngine() {
		// setup
		final ScriptEngineRegistry registry = new ScriptEngineRegistry();

		// test
		final Optional<ScriptEngine> result = registry.registerEngine(new BeanshellScriptEngine());

		// verify
		assertThat(result).isEmpty();
		assertThat(registry.getRegisteredEngines()).hasSize(1);
		assertThat(registry.getRegisteredEngines()).first().isInstanceOf(BeanshellScriptEngine.class);
	}

	@Test
	void registerEngine_alreadyRegistered() {
		// setup
		final ScriptEngineRegistry registry = new ScriptEngineRegistry();
		registry.initialize();
		final ScriptEngine firstEngine = registry.requireEngine(BeanshellScriptEngine.NAME);

		// test
		final BeanshellScriptEngine secondEngine = new BeanshellScriptEngine();
		final Optional<ScriptEngine> result = registry.registerEngine(secondEngine);

		// verify
		assertThat(result).isNotEmpty();
		assertThat(result).containsSame(firstEngine);
		assertThat(secondEngine).isNotSameAs(registry.requireEngine(BeanshellScriptEngine.NAME));
		assertThat(registry.getRegisteredEngines()).hasSize(1);
		assertThat(registry.getRegisteredEngines()).first().isInstanceOf(BeanshellScriptEngine.class);
	}

	public interface InterfaceTestEngine extends ScriptEngine {
	}

	public static abstract class AbstractTestEngine implements ScriptEngine {
		public AbstractTestEngine() {
		}

		@NotNull
		@Override
		public String getName() {
			return getClass().getName();
		}

		@NotNull
		@Override
		public ScriptExecutable getExecutable(@NotNull final String scriptSource) {
			return null;
		}
	}

	public static class TestEngineWithDefaultConstructor extends AbstractTestEngine {
		public TestEngineWithDefaultConstructor() {
		}
	}

	public static class TestEngineWithoutDefaultConstructor extends AbstractTestEngine {
		public TestEngineWithoutDefaultConstructor(@NotNull final Object parameter) {
		}
	}

	public static class TestEngineWithoutThrowingInstantiationException extends AbstractTestEngine {
		public TestEngineWithoutThrowingInstantiationException() throws InstantiationException {
			throw new InstantiationException("message");
		}
	}

}