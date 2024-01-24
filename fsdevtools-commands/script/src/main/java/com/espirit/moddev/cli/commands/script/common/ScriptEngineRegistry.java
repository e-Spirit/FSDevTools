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
import com.espirit.moddev.cli.commands.script.common.beanshell.BeanshellScriptEngine;
import com.espirit.moddev.cli.commands.script.parseCommand.ParseScriptCommand;
import com.espirit.moddev.cli.reflection.ReflectionUtils;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Registry class for managing and initializing {@link ScriptEngine} instances.
 * This class scans the classpath for implementations of {@link ScriptEngine}
 * and allows the registration, retrieval, and creation of  {@link ScriptEngine}s.
 */
public class ScriptEngineRegistry {

	private static final Logger LOGGER = LoggerFactory.getLogger(ParseScriptCommand.class);
	private static final String JAR_EXTENSION = ".jar";

	private final Map<String, ScriptEngine> _engines = new HashMap<>();

	/**
	 * Initializes the registry by scanning the classpath for implementations of {@link ScriptEngine}.
	 * It calls {@link #registerEngine(ScriptEngine)} to register the default script engine.
	 */
	public void initialize() {
		registerEngine(new BeanshellScriptEngine());
		getPluginsPath().ifPresent(this::initializeExternalEngines);
	}

	/**
	 * Retrieves a collection of all currently registered {@link ScriptEngine} instances in the registry.
	 * This method is intended for testing purposes and provides access to the internal map of registered engines.
	 *
	 * @return A collection of {@link ScriptEngine} instances currently registered in the registry.
	 */
	@VisibleForTesting
	@NotNull
	Collection<ScriptEngine> getRegisteredEngines() {
		return _engines.values();
	}

	/**
	 * Gets the path to the directory containing external plugin JARs.
	 *
	 * @return An {@link Optional} containing the path to the directory, or empty if an error occurs.
	 */
	@VisibleForTesting
	@NotNull
	Optional<Path> getPluginsPath() {
		try {
			final Path cliJar = getJarPath();
			final Path libDir = cliJar.getParent();
			final Path mainDir = libDir.getParent();
			final Path pluginsDir = mainDir.resolve("plugins");
			return Optional.of(pluginsDir);
		} catch (final URISyntaxException e) {
			LOGGER.error("Error retrieving plugins directory.", e);
			return Optional.empty();
		}
	}

	/**
	 * Retrieves the file path of the JAR containing the current class.
	 * This method is marked as visible for testing to allow easier testing of scenarios
	 * involving the location of the JAR file.
	 *
	 * @return The {@link Path} object representing the file path of the JAR containing the current class.
	 * @throws URISyntaxException If a syntax error occurs during URI construction.
	 */
	@NotNull
	@VisibleForTesting
	Path getJarPath() throws URISyntaxException {
		return new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).toPath();
	}

	/**
	 * Scans for external {@link ScriptEngine} JARs in the specified directory and registers the found engines.
	 *
	 * @param path The path to the directory containing external {@link ScriptEngine} JARs.
	 */
	@VisibleForTesting
	void initializeExternalEngines(@NotNull final Path path) {
		// load external jars
		final File dir = path.toFile();
		LOGGER.debug("Scanning script engine jars in '{}'...", path.toAbsolutePath());
		if (!dir.exists() || !dir.isDirectory()) {
			LOGGER.debug("Path '{}' does not exist or is not a directory.", path.toAbsolutePath());
			return;
		}
		final File[] jarFiles = dir.listFiles(file -> file.isFile() && file.getName().endsWith(JAR_EXTENSION));
		if (jarFiles != null) {
			final Collection<ScriptEngine> externalEngines = registerEngines(scanExternalJars(jarFiles));
			LOGGER.info("Registered {} external script engines. Available engines: [{}]", externalEngines.size(), _engines.values().stream().map(ScriptEngine::getName).collect(Collectors.joining(", ")));
		} else {
			LOGGER.info("No external script engine jars found.");
		}
	}

	/**
	 * Scans the provided array of JAR files and returns a list of {@link ScriptEngine} classes found in those JARs.
	 *
	 * @param jarFiles An array of JAR files to scan for {@link ScriptEngine} classes.
	 * @return A list of {@link ScriptEngine} classes found in the JAR files.
	 */
	@VisibleForTesting
	@NotNull
	ArrayList<Class<? extends ScriptEngine>> scanExternalJars(@NotNull final File[] jarFiles) {
		final ArrayList<Class<? extends ScriptEngine>> scriptEngineClasses = new ArrayList<>();
		for (final File jarFile : jarFiles) {
			try {
				final URLClassLoader classLoader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()}, ScriptEngineRegistry.class.getClassLoader());
				final Collection<Class<? extends ScriptEngine>> scanResult = scanClassLoader(classLoader);
				scriptEngineClasses.addAll(scanResult);
			} catch (final MalformedURLException e) {
				LOGGER.error(String.format("Error analyzing script engine jar '%s'!", jarFile.getAbsolutePath()), e);
				throw new RuntimeException(e);
			}
		}
		return scriptEngineClasses;
	}

	/**
	 * Scans the provided external JAR file using the specified class loader and returns a collection of {@link ScriptEngine} classes.
	 *
	 * @param classLoader The class loader to use for scanning the JAR file.
	 * @return A collection of {@link ScriptEngine} classes found in the JAR file.
	 */
	@VisibleForTesting
	@NotNull
	Collection<Class<? extends ScriptEngine>> scanClassLoader(@NotNull final ClassLoader classLoader) {
		final ArrayList<Class<? extends ScriptEngine>> result = new ArrayList<>();
		final ClassGraph classGraph = new ClassGraph().enableClassInfo().overrideClassLoaders(classLoader);
		try (final ScanResult scanResult = classGraph.scan()) {
			final ClassInfoList classInfoList = scanResult.getClassesImplementing(ScriptEngine.class);
			for (final ClassInfo classInfo : classInfoList) {
				final String className = classInfo.getName();
				if (classInfo.isAbstract() || classInfo.isInterface()) {
					LOGGER.debug("ScriptEngine '{}' is abstract or interface ", className);
					continue;
				}
				try {
					//noinspection unchecked
					result.add((Class<? extends ScriptEngine>) classInfo.loadClass());
				} catch (final Exception e) {
					LOGGER.error(String.format("Error loading ScriptEngine class '%s'.", className), e);
				}
			}

		}
		return result;
	}

	/**
	 * Registers the provided external {@link ScriptEngine} classes in the registry.
	 *
	 * @param scriptEngineClasses A list of external {@link ScriptEngine} classes to register.
	 * @return A collection of the successfully registered {@link ScriptEngine} instances.
	 */
	@VisibleForTesting
	@NotNull
	Collection<ScriptEngine> registerEngines(@NotNull final Collection<Class<? extends ScriptEngine>> scriptEngineClasses) {
		// register
		final ArrayList<ScriptEngine> result = new ArrayList<>();
		scriptEngineClasses.forEach(clazz -> {
			final String className = clazz.getName();
			LOGGER.debug("Registering ScriptEngine '{}'...", className);
			final Optional<ScriptEngine> optionalEngine = createEngine(clazz);
			optionalEngine.ifPresent(engineToRegister -> {
				final Optional<ScriptEngine> optionalRegistration = registerEngine(engineToRegister);
				if (optionalRegistration.isEmpty()) {
					result.add(engineToRegister);
					LOGGER.debug("ScriptEngine registered: '{}'", className);
				} else {
					LOGGER.error("Script engine '{}' is already registered by class '{}'", engineToRegister.getName(), optionalRegistration.get().getClass().getName());
				}
			});
		});
		return result;
	}

	/**
	 * Creates a {@link ScriptEngine} instance from the provided class.
	 *
	 * @param scriptEngineClass The class of the {@link ScriptEngine} implementation.
	 * @param <T>               The type of the {@link ScriptEngine}.
	 * @return An {@link Optional} containing the created {@link ScriptEngine}, or empty if an error occurs.
	 */
	@SuppressWarnings("unchecked")
	@VisibleForTesting
	@NotNull <T extends ScriptEngine> Optional<T> createEngine(@NotNull final Class<? extends ScriptEngine> scriptEngineClass) {
		// load class
		final String className = scriptEngineClass.getName();
		try {
			return Optional.of((T) ReflectionUtils.createInstance(scriptEngineClass));
		} catch (final NoSuchMethodException e) {
			LOGGER.error(String.format("Empty default constructor for ScriptEngine '%s' is missing.", className), e);
			return Optional.empty();
		} catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
			LOGGER.error(String.format("Error registering ScriptEngine '%s': %s", className, e.getMessage()), e);
			return Optional.empty();
		}
	}

	/**
	 * Requests a registered {@link ScriptEngine} by name.
	 *
	 * @param name The name of the requested {@link ScriptEngine}.
	 * @return An {@link Optional} containing the requested {@link ScriptEngine}, or empty if not found.
	 */
	@NotNull
	public Optional<ScriptEngine> requestEngine(@NotNull final String name) {
		final String engineName = name.toLowerCase(Locale.ROOT);
		return Optional.ofNullable(_engines.get(engineName));
	}

	/**
	 * Requires a registered {@link ScriptEngine} by name, throwing an exception if not found.
	 *
	 * @param name The name of the required {@link ScriptEngine}.
	 * @return The requested {@link ScriptEngine}.
	 * @throws IllegalStateException If the requested {@link ScriptEngine} is not registered.
	 */
	@NotNull
	public ScriptEngine requireEngine(@NotNull final String name) {
		final Optional<ScriptEngine> optionalEngine = requestEngine(name);
		if (optionalEngine.isEmpty()) {
			throw new IllegalStateException(String.format("Script engine '%s' is not registered.", name));
		}
		return optionalEngine.get();
	}

	/**
	 * Registers a {@link ScriptEngine} in the registry.
	 *
	 * @param engine The {@link ScriptEngine} to register.
	 * @return an {@link Optional} containing the currently registered script engine if there is one, otherwise an empty {@link Optional}.
	 * @throws IllegalArgumentException If the {@link ScriptEngine} is already registered.
	 */
	@VisibleForTesting
	@NotNull
	Optional<ScriptEngine> registerEngine(@NotNull final ScriptEngine engine) {
		final String engineName = engine.getName().toLowerCase(Locale.ROOT);
		final ScriptEngine currentEngine = _engines.get(engineName);
		if (currentEngine != null) {
			return Optional.of(currentEngine);
		}
		_engines.put(engineName, engine);
		return Optional.empty();
	}

}
