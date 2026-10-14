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

package com.espirit.moddev.cli.classloading;

import com.espirit.moddev.cli.classloading.TestDistribution.Run;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Runs the shaded CLI in real JVMs with the layout and classpath of the distribution and loads commands and script
 * engines like a user does. Each test works in its own temp directory with its own copy of the distribution.
 * <p>
 * Cases C1, C2, C3, C6, C7 and C8b fail against the 4.8.9 jar (run with {@code -PbaselineJar}); C4, C5, C9 and C10 pass on
 * both. The cases named "characterization" pin current behaviour on purpose, so that a later change turns them red
 * deliberately.
 */
class ClassloadingTest {

	private static final String EXAMPLES_SECTION = "(?m)^EXAMPLES\\s*$";

	@TempDir
	Path _workDir;

	private TestDistribution _dist;

	@BeforeEach
	void createDistribution() throws Exception {
		_dist = TestDistribution.create(_workDir);
		if (!_dist.hasFirstSpiritRuntime()) {
			final String message = "fs-isolated-runtime could not be resolved (internal repository), the CLI cannot start";
			if (Boolean.getBoolean("fscli.requireRuntime")) {
				fail(message);
			}
			assumeTrue(false, message);
		}
	}

	/**
	 * C1: Airline finds its help section factories through META-INF/services.
	 */
	@Test
	void helpOfBuiltInCommand_printsExamplesSection() throws Exception {
		final Run run = _dist.cli("help", "import");

		assertThat(run.exitCode()).isZero();
		assertThat(run.output()).containsPattern(EXAMPLES_SECTION);
	}

	/**
	 * C2: option restrictions come from the same service files.
	 */
	@Test
	void invalidValueOfRestrictedOption_isRejectedByAirline() throws Exception {
		final Run run = _dist.cli("-c", "BOGUS", "import");

		assertThat(run.exitCode()).isNotZero();
		assertThat(run.output()).contains("which is not in the list of allowed values");
	}

	/**
	 * C3: a custom command in lib/ is listed, has help with examples and can be executed.
	 */
	@Test
	void customCommandInLib_isListedDocumentedAndExecutable() throws Exception {
		_dist.install("exampleCommand", "lib");

		assertThat(_dist.cli("help").output()).contains("example-custom-group");
		final Run help = _dist.cli("help", "example-custom-group", "example-custom-command");
		assertThat(help.exitCode()).isZero();
		assertThat(help.output()).containsPattern(EXAMPLES_SECTION);
		final Run run = _dist.cli("example-custom-group", "example-custom-command");
		assertThat(run.exitCode()).isZero();
		assertThat(run.output()).contains("Executing ExampleCustomCommand");
	}

	/**
	 * C4: characterization. Only script engines are loaded from plugins/; commands are found on the classpath (lib/).
	 */
	@Test
	void characterization_customCommandInPluginsDir_isNotDiscovered() throws Exception {
		_dist.install("exampleCommand", "plugins");

		final Run run = _dist.cli("example-custom-group", "example-custom-command");

		assertThat(run.exitCode()).isNotZero();
		assertThat(run.output()).contains("UnknownCommandException");
	}

	/**
	 * C5: the shipped script engines are registered and a script runs. Green on 4.8.9 as well.
	 * The Javascript engine is only checked for registration: running a script fails with an
	 * UnsupportedOperationException on the JDK used here, with the 4.8.9 jar as well.
	 */
	@Test
	void shippedScriptEngines_areRegisteredAndGroovyRuns() throws Exception {
		_dist.install("groovyEngine", "plugins");
		_dist.install("javascriptEngine", "plugins");

		final Run engines = _dist.probe("engines", "BEANSHELL", "GROOVY", "JAVASCRIPT");
		assertThat(engines.output()).contains("BEANSHELL registered", "GROOVY registered", "JAVASCRIPT registered");
		final Run groovy = _dist.probe("run", "GROOVY", "1+1");
		assertThat(groovy.exitCode()).isZero();
		assertThat(groovy.output()).containsPattern("RESULT_BEGIN\\s+2\\s+RESULT_END");
	}

	/**
	 * C6: characterization. A plugin shares Airline with the CLI, but sees none of the relocated libraries
	 * under their original names, so it has to bundle its own.
	 */
	@Test
	void characterization_pluginSeesOnlyAirlineOfTheCli() throws Exception {
		_dist.install("probeEngine", "plugins");

		final Run run = _dist.probe("run", "PROBE", "x");

		assertThat(run.exitCode()).isZero();
		assertThat(run.output()).contains(
				"com.github.rvesse.airline.annotations.Command OK fsdevtools-cli",
				"tools.jackson.databind.ObjectMapper FAIL ClassNotFoundException",
				"com.fasterxml.jackson.annotation.JsonProperty FAIL ClassNotFoundException",
				"com.google.common.collect.ImmutableList FAIL ClassNotFoundException",
				"org.apache.commons.lang3.StringUtils FAIL ClassNotFoundException",
				"org.apache.commons.compress.archivers.ArchiveEntry FAIL ClassNotFoundException",
				"io.github.classgraph.ClassGraph FAIL ClassNotFoundException");
	}

	/**
	 * C7: a plugin that bundles its own Airline and jackson-annotations (upload-hook scenario of DEVEX-706) neither
	 * breaks the CLI nor replaces its classes: the parent-first loader hands out the CLI's Airline.
	 */
	@Test
	void pluginBundlingOwnLibraries_doesNotBreakOrReplaceTheCli() throws Exception {
		_dist.install("bundlingEngine", "plugins");

		final Run run = _dist.probe("run", "PROBE", "x");

		assertThat(run.exitCode()).isZero();
		assertThat(run.output()).doesNotContain("NoSuchFieldError", "NoSuchMethodError", "LinkageError");
		assertThat(run.output()).contains(
				"com.github.rvesse.airline.annotations.Command OK fsdevtools-cli",
				"com.fasterxml.jackson.annotation.JsonProperty OK bundling-engine");
		assertThat(_dist.cli("help", "import").output()).containsPattern(EXAMPLES_SECTION);
	}

	/**
	 * C8b: characterization of a known limit. A plugin result class annotated with the unrelocated Jackson annotations
	 * is not written: the shaded jar does not contain those classes (Jackson must stay invisible to plugins), so the
	 * JVM drops the annotations when the class loads. The CLI logs a warning that names the class.
	 */
	@Test
	void characterization_pluginResultWithJacksonAnnotations_writesNoResultFile() throws Exception {
		_dist.install("probeEngine", "lib");

		final Run run = _dist.cli("-rf", "annotated.json", "probe-annotated-result");

		assertThat(run.exitCode()).isZero();
		assertThat(run.output()).contains("Executing ProbeAnnotatedResultCommand");
		assertThat(_dist.workDir().resolve("annotated.json")).doesNotExist();
		assertThat(run.output()).contains("uses Jackson annotations").contains("ProbeAnnotatedResultCommand$ProbeValue");
	}

	/**
	 * C8c: same limit as C8b, but the plugin brings its own Jackson, so its annotations can be loaded. They are still not
	 * supported (the CLI's relocated Jackson does not know them): no result file is written (not even one with an empty
	 * result object) and the CLI logs the same warning.
	 */
	@Test
	void characterization_pluginWithOwnJacksonAndAnnotatedResult_writesNoResultFile() throws Exception {
		_dist.install("ownJacksonEngine", "lib");

		final Run run = _dist.cli("-rf", "annotated.json", "probe-annotated-result");

		assertThat(run.exitCode()).as(run.output()).isZero();
		assertThat(run.output()).contains("Executing ProbeAnnotatedResultCommand");
		assertThat(_dist.workDir().resolve("annotated.json")).doesNotExist();
		assertThat(run.output()).contains("uses Jackson annotations").contains("ProbeAnnotatedResultCommand$ProbeValue");
	}

	/**
	 * C10: the CLI must not put Jackson (or any other relocated library) on the classpath of commands. A plugin cannot
	 * load it under its original name, not even the annotations.
	 */
	@Test
	void cliJar_containsNoUnrelocatedJackson() throws Exception {
		final Run run = _dist.probe("classes", "tools.jackson.databind.ObjectMapper", "tools.jackson.databind.annotation.JsonSerialize",
				"com.fasterxml.jackson.annotation.JsonProperty");

		assertThat(run.exitCode()).isZero();
		assertThat(run.output()).contains(
				"tools.jackson.databind.ObjectMapper missing",
				"tools.jackson.databind.annotation.JsonSerialize missing",
				"com.fasterxml.jackson.annotation.JsonProperty missing");
	}

	/**
	 * C9: the CLI's own, relocated Jackson writes the result file (needs its service files). A refused connection
	 * makes a built-in command fail without a server.
	 */
	@Test
	void failingBuiltInCommand_writesJsonResultFile() throws Exception {
		final Run run = _dist.cli("-h", "localhost", "-port", "1", "-u", "a", "-pwd", "b", "-p", "x", "-rf", "result.json", "test", "connection");

		assertThat(run.exitCode()).isNotZero();
		final Path resultFile = _dist.workDir().resolve("result.json");
		assertThat(resultFile).exists();
		final JsonNode root = readJson("result.json");
		assertThat(root.get("command").asString()).isEqualTo("test connection");
		assertThat(root.get("error").asBoolean()).isTrue();
		assertThat(root.get("exception").get("class").asString()).isNotEmpty();
	}

	private JsonNode readJson(final String fileName) throws Exception {
		final Path file = _dist.workDir().resolve(fileName);
		assertThat(file).exists();
		return JsonMapper.builder().build().readTree(Files.readString(file));
	}

}
