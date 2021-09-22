/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2021 e-Spirit AG
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

package com.espirit.moddev.cli.documentation.commands;

import com.espirit.moddev.cli.api.annotations.ParameterExamples;
import com.espirit.moddev.cli.api.annotations.ParameterType;
import com.espirit.moddev.cli.commands.help.DefaultCommand;
import com.espirit.moddev.cli.documentation.DocumentationGenerator;
import com.espirit.moddev.cli.documentation.pojos.JsonCommand;
import com.espirit.moddev.cli.documentation.pojos.JsonCommandGroup;
import com.espirit.moddev.cli.documentation.pojos.JsonElement;
import com.espirit.moddev.cli.documentation.pojos.JsonElementHolder;
import com.espirit.moddev.cli.documentation.utils.ArgumentUtils;
import com.espirit.moddev.cli.documentation.utils.ScanUtils;
import com.espirit.moddev.shared.annotation.VisibleForTesting;
import com.espirit.moddev.util.JacksonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.Examples;
import com.github.rvesse.airline.annotations.restrictions.AllowedRawValues;
import com.github.rvesse.airline.annotations.restrictions.Required;
import io.github.classgraph.AnnotationEnumValue;
import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.FieldInfo;
import io.github.classgraph.FieldInfoList;
import io.github.classgraph.ScanResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandDocumentationGenerator implements DocumentationGenerator<CommandDocumentationInfo> {

	public static void main(@Nullable final String[] args) throws IOException {
		final Optional<Path> outputFile = ArgumentUtils.findPath(args == null || args.length < 2 ? new String[]{"--file", Paths.get("data.json").toAbsolutePath().toString()} : args);
		if (!outputFile.isPresent()) {
			throw new IllegalStateException(String.format("Output file is not given, use '%s path/to/file.json' as an argument.", ArgumentUtils.ARG_FILE));
		}
		final CommandDocumentationGenerator generator = new CommandDocumentationGenerator(CommandTextRenderer::new);
		final List<JsonCommandGroup> jsonCommandGroups = generator.generate(generator.getDocumentationInfos());
		writeJsonFile(outputFile.get(), jsonCommandGroups);
	}

	private static final List<String> GROUP_BLACKLIST = Collections.singletonList("server");

	@VisibleForTesting
	static final String ARG_NAME = "name";
	@VisibleForTesting
	static final String ARG_DESCRIPTION = "description";
	@VisibleForTesting
	static final String ARG_COMMAND_GROUP_NAMES = "groupNames";
	@VisibleForTesting
	static final String ARG_TYPE = "type";
	@VisibleForTesting
	static final String ARG_EXAMPLES = "examples";
	@VisibleForTesting
	static final String ARG_DESCRIPTIONS = "descriptions";
	@VisibleForTesting
	static final String ARG_ALLOWED_VALUES = "allowedValues";

	private final Function<CommandDocumentationInfo, CommandRenderer> _commandRendererFunction;

	public CommandDocumentationGenerator(@NotNull final Function<CommandDocumentationInfo, CommandRenderer> commandRendererFunction) {
		_commandRendererFunction = commandRendererFunction;
	}

	@NotNull
	@Override
	public List<JsonCommandGroup> generate(@NotNull final Collection<CommandDocumentationInfo> documentationInfos) throws IOException {
		final Path buildTargetRoot = Paths.get("build");
		final Path docsTargetRoot = buildTargetRoot.resolve("docs");
		final Path commandsTargetRoot = docsTargetRoot.resolve("commands");
		final List<String> allGroupNames = documentationInfos
				.stream()
				.map(CommandDocumentationInfo::getGroupNames)
				.flatMap((Function<List<String>, Stream<String>>) Collection::stream)
				.distinct()
				.filter(groupName -> !GROUP_BLACKLIST.contains(groupName))
				.sorted(String::compareTo)
				.collect(Collectors.toList());

		final List<CommandGroup> commandGroups = new ArrayList<>();
		for (final String groupName : allGroupNames) {
			final CommandGroup commandGroup = new CommandGroup(groupName);
			documentationInfos
					.stream()
					.filter(commandDocumentationInfo -> commandDocumentationInfo.getGroupNames().contains(groupName))
					.forEach(commandGroup::addCommand);
			commandGroups.add(commandGroup);
		}

		// finally build the POJOs for each group & command
		final List<JsonCommandGroup> jsonCommandGroups = new ArrayList<>();
		for (final CommandGroup currentGroup : commandGroups) {
			final String groupName = currentGroup.getName();
			final JsonCommandGroup jsonCommandGroup = new JsonCommandGroup(groupName);
			jsonCommandGroups.add(jsonCommandGroup);
			for (final CommandDocumentationInfo command : currentGroup.getCommands()) {
				jsonCommandGroup.addCommand(new JsonCommand(command.getName(), groupName, generateCommandContent(commandsTargetRoot, currentGroup, command)));
			}
		}
		return jsonCommandGroups;
	}

	@NotNull
	@Override
	public Collection<CommandDocumentationInfo> getDocumentationInfos() {
		final String[] packagesToScan = {"com.espirit.moddev.cli.commands"};
		final String[] blacklistedClasses = {DefaultCommand.class.getName()};
		return scanClasses(packagesToScan, blacklistedClasses);
	}

	@NotNull
	private Collection<JsonElement> generateCommandContent(@NotNull final Path root, @NotNull final CommandGroup currentGroup, @NotNull final CommandDocumentationInfo command) throws IOException {
		Path groupPath = root.resolve(currentGroup.getName());
		final File groupPathfile = groupPath.toFile();
		if (!groupPathfile.exists()) {
			if (!groupPathfile.mkdirs()) {
				throw new IllegalStateException(String.format("Error creating directory '%s'!", groupPath));
			}
		}

		final CommandRenderer commandRenderer = _commandRendererFunction.apply(command);

		commandRenderer.renderName(currentGroup.getName());
		if (!command.getDescription().trim().isEmpty()) {
			commandRenderer.renderDescription();
		}

		// global parameters
		final List<CommandParameterInfo> globalParameters = command.getParameters(true);
		if (!globalParameters.isEmpty()) {
			commandRenderer.renderParametersTopic(true);
			globalParameters.forEach(commandRenderer::renderParameter);
		}

		// command parameters
		final List<CommandParameterInfo> parameters = command.getParameters(false);
		if (!parameters.isEmpty()) {
			commandRenderer.renderParametersTopic(false);
			parameters.forEach(commandRenderer::renderParameter);
		}

		// command examples
		if (!command.getExamples().isEmpty()) {
			commandRenderer.renderExamples(currentGroup.getName());
		}

		if (commandRenderer instanceof JsonElementHolder) {
			return ((JsonElementHolder) commandRenderer).getJsonElements();
		}
		return Collections.emptyList();
	}

	@VisibleForTesting
	@NotNull
	List<CommandDocumentationInfo> scanClasses(@NotNull final String[] packagesToScan, @NotNull final String[] blacklistedClasses) {
		final List<CommandDocumentationInfo> infos = new ArrayList<>();
		try (final ScanResult scan = new ClassGraph()
				.enableClassInfo()
				.enableAnnotationInfo()
				.enableFieldInfo()
				.ignoreFieldVisibility()
				.whitelistPackages(packagesToScan)
				.blacklistClasses(blacklistedClasses)
				.scan()) {
			final ClassInfoList commandInfoList = scan.getClassesWithAnnotation(Command.class.getName());
			System.out.printf("Commands found: %s%n", commandInfoList.size());
			for (final ClassInfo commandInfo : commandInfoList) {
				if (commandInfo.isInterface() || commandInfo.isAbstract()) {
					System.out.printf("Ignoring command: %s%n", commandInfo.getName());
					continue;
				}
				final Optional<CommandDocumentationInfo> optionalDocumentationInfo = createDocumentationInfo(commandInfo);
				if (!optionalDocumentationInfo.isPresent()) {
					continue;
				}
				infos.add(optionalDocumentationInfo.get());
			}
		}
		return infos;
	}

	@VisibleForTesting
	@NotNull
	Optional<CommandDocumentationInfo> createDocumentationInfo(@NotNull final ClassInfo commandInfo) {
		System.out.printf("Analyzing command: %s%n", commandInfo.getName());
		final AnnotationInfo commandAnnotationInfo = commandInfo.getAnnotationInfo().get(Command.class.getName());
		if (commandAnnotationInfo == null) {
			System.out.printf("Annotation '%s' on class '%s' not found!%n", Command.class.getName(), commandInfo.getName());
			return Optional.empty();
		}

		final Optional<String> name = ScanUtils.getValueAsString(commandAnnotationInfo, ARG_NAME);
		if (!name.isPresent()) {
			System.out.printf("Name undefined in annotation '%s': %s%n", Command.class.getName(), commandInfo.getName());
			return Optional.empty();
		}

		final Optional<String> description = ScanUtils.getValueAsString(commandAnnotationInfo, ARG_DESCRIPTION);
		final Optional<String[]> groupNames = ScanUtils.getValueAsArray(commandAnnotationInfo, ARG_COMMAND_GROUP_NAMES);
		final List<CommandParameterInfo> commandParameters = fetchParameters(commandInfo);
		final List<ExamplesInfo> examples = fetchExamples(commandInfo.getAnnotationInfo(Examples.class.getName()));
		return Optional.of(new CommandDocumentationInfo(name.get(), description.orElse(""), Arrays.asList(groupNames.orElse(new String[0])), examples, commandParameters));
	}

	@VisibleForTesting
	@NotNull
	List<CommandParameterInfo> fetchParameters(@NotNull final ClassInfo classInfo) {
		final List<CommandParameterInfo> result = new ArrayList<>();
		try {
			final Object commandInstance = classInfo.loadClass().newInstance();
			result.addAll(fetchParameterInfos(commandInstance, classInfo.getFieldInfo()));
		} catch (final InstantiationException | IllegalAccessException ignore) {
		}
		return result;
	}

	@VisibleForTesting
	@NotNull
	List<CommandParameterInfo> fetchParameterInfos(@NotNull final Object commandInstance, @NotNull final FieldInfoList fieldInfos) {
		final List<CommandParameterInfo> result = new ArrayList<>();
		for (final FieldInfo fieldInfo : fieldInfos) {
			fetchParameterInfo(commandInstance, fieldInfo).ifPresent(result::add);
		}
		return result;
	}

	@VisibleForTesting
	@NotNull
	Optional<CommandParameterInfo> fetchParameterInfo(@NotNull final Object commandInstance, @NotNull final FieldInfo fieldInfo) {
		final Optional<Field> optionalField = ScanUtils.getField(commandInstance.getClass(), fieldInfo.getName());
		if (!optionalField.isPresent()) {
			return Optional.empty();
		}
		final AnnotationInfo optionInfo = fieldInfo.getAnnotationInfo(Option.class.getName());
		if (optionInfo == null) {
			return Optional.empty();
		}
		final AnnotationEnumValue annotationEnumValue = (AnnotationEnumValue) optionInfo.getParameterValues().getValue(ARG_TYPE);
		OptionType optionType = OptionType.COMMAND;
		if (annotationEnumValue != null) {
			final String valueName = annotationEnumValue.getValueName();
			optionType = (OptionType.valueOf(valueName));
		}
		if (optionType != OptionType.GLOBAL && optionType != OptionType.COMMAND) {
			return Optional.empty();
		}
		final Optional<String[]> names = ScanUtils.getValueAsArray(optionInfo, ARG_NAME);
		final Optional<String> description = ScanUtils.getValueAsString(optionInfo, ARG_DESCRIPTION);
		if (!names.isPresent()) {
			return Optional.empty();
		}
		final String[] namesArray = names.get();
		if (namesArray.length < 1) {
			return Optional.empty();
		}
		final List<ExamplesInfo> examples = fetchExamples(fieldInfo.getAnnotationInfo(ParameterExamples.class.getName()));
		final CommandParameterInfo commandParameterInfo = new CommandParameterInfo(optionalField.get().getType(), Arrays.asList(namesArray), optionType == OptionType.GLOBAL, examples, description.orElse(""), fieldInfo.getAnnotationInfo(Required.class.getName()) != null);
		commandParameterInfo.setDefaultValue(ScanUtils.getFieldValueAsString(commandInstance, fieldInfo.getName()));
		final Optional<String> parameterType = fetchParameterType(fieldInfo.getAnnotationInfo(ParameterType.class.getName()));
		parameterType.ifPresent(commandParameterInfo::setClassName);
		final AnnotationInfo rawValuesInfo = fieldInfo.getAnnotationInfo(AllowedRawValues.class.getName());
		if (rawValuesInfo != null) {
			final Optional<Object[]> allowedValues = ScanUtils.getValueAsArray(rawValuesInfo, ARG_ALLOWED_VALUES);
			allowedValues.ifPresent(values -> commandParameterInfo.setPossibleValues((String[]) values));
		}
		return Optional.of(commandParameterInfo);
	}

	@VisibleForTesting
	@NotNull
	Optional<String> fetchParameterType(@Nullable final AnnotationInfo annotation) {
		if (annotation == null) {
			return Optional.empty();
		}
		return ScanUtils.getValueAsString(annotation, ARG_NAME);
	}

	@VisibleForTesting
	@NotNull
	List<ExamplesInfo> fetchExamples(@Nullable final AnnotationInfo annotation) {
		if (annotation == null) {
			return Collections.emptyList();
		}
		final List<ExamplesInfo> result = new ArrayList<>();
		final Optional<String[]> optionalExamples = ScanUtils.getValueAsArray(annotation, ARG_EXAMPLES);
		final Optional<String[]> optionalDescriptions = ScanUtils.getValueAsArray(annotation, ARG_DESCRIPTIONS);
		if (optionalExamples.isPresent() && optionalDescriptions.isPresent()) {
			final String[] examples = optionalExamples.get();
			final String[] descriptions = optionalDescriptions.get();
			for (int index = 0; index < examples.length && index < descriptions.length; index++) {
				result.add(new ExamplesInfo(descriptions[index], examples[index].replaceAll("\t", "  ")));
			}
		}
		return result;
	}

	@VisibleForTesting
	static void writeJsonFile(@NotNull final Path outputFilePath, @NotNull final List<JsonCommandGroup> jsonCommandGroups) throws IOException {
		final Path parent = outputFilePath.getParent();
		if (!parent.toFile().exists()) {
			if (!parent.toFile().mkdirs()) {
				throw new IOException(String.format("Directory '%s' does not exist! Directory creation failed.", parent.toAbsolutePath()));
			}
		}
		final ObjectMapper outputMapper = JacksonUtil.createOutputMapper();
		final File outputFile = outputFilePath.toFile();
		if (outputFile.exists()) {
			if (!outputFilePath.toFile().delete()) {
				throw new IOException(String.format("File '%s' could not be deleted!", outputFilePath.toAbsolutePath()));
			}
		}
		final String json = outputMapper.writeValueAsString(jsonCommandGroups);
		System.out.printf("Writing json to file '%s': %s%n", outputFilePath, json);
		Files.write(outputFilePath, json.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW);
	}

}
