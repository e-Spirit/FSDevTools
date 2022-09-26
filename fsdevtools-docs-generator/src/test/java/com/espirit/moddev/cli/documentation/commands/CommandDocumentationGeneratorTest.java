/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2022 Crownpeak Technology GmbH
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

import com.espirit.moddev.cli.documentation.testclasses.NonCommandClass;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.restrictions.AllowedRawValues;
import io.github.classgraph.AnnotationEnumValue;
import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.FieldInfo;
import io.github.classgraph.FieldInfoList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.espirit.moddev.cli.documentation.commands.CommandDocumentationGenerator.ARG_ALLOWED_VALUES;
import static com.espirit.moddev.cli.documentation.commands.CommandDocumentationGenerator.ARG_COMMAND_GROUP_NAMES;
import static com.espirit.moddev.cli.documentation.commands.CommandDocumentationGenerator.ARG_DESCRIPTION;
import static com.espirit.moddev.cli.documentation.commands.CommandDocumentationGenerator.ARG_DESCRIPTIONS;
import static com.espirit.moddev.cli.documentation.commands.CommandDocumentationGenerator.ARG_EXAMPLES;
import static com.espirit.moddev.cli.documentation.commands.CommandDocumentationGenerator.ARG_NAME;
import static com.espirit.moddev.cli.documentation.commands.CommandDocumentationGenerator.ARG_TYPE;
import static com.espirit.moddev.cli.documentation.commands.CommandDocumentationInfo.GLOBAL_GROUP_NAME;
import static com.espirit.moddev.cli.documentation.testclasses.NonCommandClass.FIELD_VALUE;
import static com.espirit.moddev.cli.documentation.testclasses.NonCommandClass.VALUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class CommandDocumentationGeneratorTest {

	private CommandDocumentationGenerator _generator;

	@BeforeEach
	public void setup() {
		_generator = new CommandDocumentationGenerator(CommandTextRenderer::new);
	}

	@Test
	public void fetchParameterType() {
		// setup
		final AnnotationInfo info = mock(AnnotationInfo.class, RETURNS_DEEP_STUBS);
		when(info.getParameterValues(true).getValue(ARG_NAME)).thenReturn("myType");
		// test & verify
		assertThat(_generator.fetchParameterType(info)).contains("myType");
		assertThat(_generator.fetchParameterType(mock(AnnotationInfo.class, RETURNS_DEEP_STUBS))).isNotPresent();
		assertThat(_generator.fetchParameterType(null)).isNotPresent();
	}

	@Test
	public void fetchExamples() {
		// setup
		final String[] examples = {"Example#1", "Example#2"};
		final String[] descriptions = {"Description#1", "Description#2"};
		final AnnotationInfo info = mock(AnnotationInfo.class, RETURNS_DEEP_STUBS);
		when(info.getParameterValues(true).getValue(ARG_EXAMPLES)).thenReturn(examples);
		when(info.getParameterValues(true).getValue(ARG_DESCRIPTIONS)).thenReturn(descriptions);
		// test
		final List<ExamplesInfo> result = _generator.fetchExamples(info);
		// verify
		assertThat(result).hasSize(2);
		assertThat(result.get(0).getExample()).isEqualTo("Example#1");
		assertThat(result.get(0).getDescription()).isEqualTo("Description#1");
		assertThat(result.get(1).getExample()).isEqualTo("Example#2");
		assertThat(result.get(1).getDescription()).isEqualTo("Description#2");
	}

	@Test
	public void fetchExamples_emptyArrays() {
		// setup
		final AnnotationInfo info = mock(AnnotationInfo.class, RETURNS_DEEP_STUBS);
		when(info.getParameterValues(true).getValue(ARG_EXAMPLES)).thenReturn(new String[]{});
		when(info.getParameterValues(true).getValue(ARG_DESCRIPTIONS)).thenReturn(new String[]{});
		// test
		final List<ExamplesInfo> result = _generator.fetchExamples(info);
		// verify
		assertThat(result).isEmpty();
	}

	@Test
	public void fetchExamples_tooManyExamples() {
		// setup
		final String[] examples = {"Example#1", "Example#2"};
		final String[] descriptions = {"Description#1"};
		final AnnotationInfo info = mock(AnnotationInfo.class, RETURNS_DEEP_STUBS);
		when(info.getParameterValues(true).getValue(ARG_EXAMPLES)).thenReturn(examples);
		when(info.getParameterValues(true).getValue(ARG_DESCRIPTIONS)).thenReturn(descriptions);
		// test
		final List<ExamplesInfo> result = _generator.fetchExamples(info);
		// verify
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getExample()).isEqualTo("Example#1");
		assertThat(result.get(0).getDescription()).isEqualTo("Description#1");
	}

	@Test
	public void fetchExamples_tooManyDescriptions() {
		// setup
		final String[] examples = {"Example#1"};
		final String[] descriptions = {"Description#1", "Description#2"};
		final AnnotationInfo info = mock(AnnotationInfo.class, RETURNS_DEEP_STUBS);
		when(info.getParameterValues(true).getValue(ARG_EXAMPLES)).thenReturn(examples);
		when(info.getParameterValues(true).getValue(ARG_DESCRIPTIONS)).thenReturn(descriptions);
		// test
		final List<ExamplesInfo> result = _generator.fetchExamples(info);
		// verify
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getExample()).isEqualTo("Example#1");
		assertThat(result.get(0).getDescription()).isEqualTo("Description#1");
	}

	@Test
	public void fetchExamples_nullAnnotationInfo() {
		assertThat(_generator.fetchExamples(null)).isEmpty();
	}

	@Test
	public void fetchParameterInfo_forCommandType() {
		// setup
		final FieldInfo fieldInfo = mock(FieldInfo.class, RETURNS_DEEP_STUBS);
		when(fieldInfo.getName()).thenReturn(FIELD_VALUE);

		// Option
		final AnnotationInfo optionInfo = mock(AnnotationInfo.class, RETURNS_DEEP_STUBS);
		when(fieldInfo.getAnnotationInfo(Option.class.getName())).thenReturn(optionInfo);
		final AnnotationEnumValue typeEnumValue = mock(AnnotationEnumValue.class);
		when(typeEnumValue.getValueName()).thenReturn(OptionType.COMMAND.name());
		when(optionInfo.getParameterValues().getValue(ARG_TYPE)).thenReturn(typeEnumValue);
		when(optionInfo.getParameterValues(true).getValue(ARG_NAME)).thenReturn(new String[]{"MyName", "MySecondName"});
		when(optionInfo.getParameterValues(true).getValue(ARG_DESCRIPTION)).thenReturn("MyDescription");

		// enhance generator
		final CommandDocumentationGenerator generator = spy(_generator);
		final List<ExamplesInfo> examplesInfos = new ArrayList<>();
		examplesInfos.add(new ExamplesInfo("MyExampleDescription", "MyExample"));
		when(generator.fetchExamples(any())).thenReturn(examplesInfos);

		// test
		final Optional<CommandParameterInfo> optionalCommandParameterInfo = generator.fetchParameterInfo(new NonCommandClass(VALUE), fieldInfo);

		// verify
		assertThat(optionalCommandParameterInfo).isPresent();
		final CommandParameterInfo commandParameterInfo = optionalCommandParameterInfo.get();
		assertThat(commandParameterInfo.getClassName()).isEqualTo(String.class.getSimpleName());
		assertThat(commandParameterInfo.getDescription()).isEqualTo("MyDescription");
		assertThat(commandParameterInfo.getPossibleValues()).isEqualTo("<any " + String.class.getSimpleName() + ">");
		assertThat(commandParameterInfo.getDefaultValue()).contains(VALUE);
		assertThat(commandParameterInfo.getNames()).containsExactly("MyName", "MySecondName");
		assertThat(commandParameterInfo.getExamples()).hasSize(1);
		assertThat(commandParameterInfo.getExamples().get(0).getDescription()).isEqualTo("MyExampleDescription");
		assertThat(commandParameterInfo.getExamples().get(0).getExample()).isEqualTo("MyExample");
	}

	@Test
	public void fetchParameterInfo_forCommandType_withPossibleValues() {
		// setup
		final FieldInfo fieldInfo = mock(FieldInfo.class, RETURNS_DEEP_STUBS);
		when(fieldInfo.getName()).thenReturn(FIELD_VALUE);

		// Option
		final AnnotationInfo optionInfo = mock(AnnotationInfo.class, RETURNS_DEEP_STUBS);
		when(fieldInfo.getAnnotationInfo(Option.class.getName())).thenReturn(optionInfo);
		final AnnotationEnumValue typeEnumValue = mock(AnnotationEnumValue.class);
		when(typeEnumValue.getValueName()).thenReturn(OptionType.COMMAND.name());
		when(optionInfo.getParameterValues().getValue(ARG_TYPE)).thenReturn(typeEnumValue);
		when(optionInfo.getParameterValues(true).getValue(ARG_NAME)).thenReturn(new String[]{"MyName", "MySecondName"});
		when(optionInfo.getParameterValues(true).getValue(ARG_DESCRIPTION)).thenReturn("MyDescription");

		// AllowedRawValues
		final AnnotationInfo rawValuesInfo = mock(AnnotationInfo.class, RETURNS_DEEP_STUBS);
		when(fieldInfo.getAnnotationInfo(AllowedRawValues.class.getName())).thenReturn(rawValuesInfo);
		when(rawValuesInfo.getParameterValues(true).getValue(ARG_ALLOWED_VALUES)).thenReturn(new String[]{"first", "second"});

		// enhance generator
		final CommandDocumentationGenerator generator = spy(_generator);
		final List<ExamplesInfo> examplesInfos = new ArrayList<>();
		examplesInfos.add(new ExamplesInfo("MyExampleDescription", "MyExample"));
		when(generator.fetchExamples(any())).thenReturn(examplesInfos);

		// test
		final Optional<CommandParameterInfo> optionalCommandParameterInfo = generator.fetchParameterInfo(new NonCommandClass(VALUE), fieldInfo);

		// verify
		assertThat(optionalCommandParameterInfo).isPresent();
		final CommandParameterInfo commandParameterInfo = optionalCommandParameterInfo.get();
		assertThat(commandParameterInfo.getClassName()).isEqualTo(String.class.getSimpleName());
		assertThat(commandParameterInfo.getDescription()).isEqualTo("MyDescription");
		assertThat(commandParameterInfo.getPossibleValues()).isEqualTo("[ first , second ]");
		assertThat(commandParameterInfo.getDefaultValue()).contains(VALUE);
		assertThat(commandParameterInfo.isGlobal()).isFalse();
		assertThat(commandParameterInfo.getNames()).containsExactly("MyName", "MySecondName");
		assertThat(commandParameterInfo.getExamples()).hasSize(1);
		assertThat(commandParameterInfo.getExamples().get(0).getDescription()).isEqualTo("MyExampleDescription");
		assertThat(commandParameterInfo.getExamples().get(0).getExample()).isEqualTo("MyExample");
	}

	@Test
	public void fetchParameterInfo_forGlobalType_withPossibleValues() {
		// setup
		final FieldInfo fieldInfo = mock(FieldInfo.class, RETURNS_DEEP_STUBS);
		when(fieldInfo.getName()).thenReturn(FIELD_VALUE);

		// Option
		final AnnotationInfo optionInfo = mock(AnnotationInfo.class, RETURNS_DEEP_STUBS);
		when(fieldInfo.getAnnotationInfo(Option.class.getName())).thenReturn(optionInfo);
		final AnnotationEnumValue typeEnumValue = mock(AnnotationEnumValue.class);
		when(typeEnumValue.getValueName()).thenReturn(OptionType.GLOBAL.name());
		when(optionInfo.getParameterValues().getValue(ARG_TYPE)).thenReturn(typeEnumValue);
		when(optionInfo.getParameterValues(true).getValue(ARG_NAME)).thenReturn(new String[]{"MyName", "MySecondName"});
		when(optionInfo.getParameterValues(true).getValue(ARG_DESCRIPTION)).thenReturn("MyDescription");

		// AllowedRawValues
		final AnnotationInfo rawValuesInfo = mock(AnnotationInfo.class, RETURNS_DEEP_STUBS);
		when(fieldInfo.getAnnotationInfo(AllowedRawValues.class.getName())).thenReturn(rawValuesInfo);
		when(rawValuesInfo.getParameterValues(true).getValue(ARG_ALLOWED_VALUES)).thenReturn(new String[]{"first", "second"});

		// enhance generator
		final CommandDocumentationGenerator generator = spy(_generator);
		final List<ExamplesInfo> examplesInfos = new ArrayList<>();
		examplesInfos.add(new ExamplesInfo("MyExampleDescription", "MyExample"));
		when(generator.fetchExamples(any())).thenReturn(examplesInfos);

		// test
		final Optional<CommandParameterInfo> optionalCommandParameterInfo = generator.fetchParameterInfo(new NonCommandClass(VALUE), fieldInfo);

		// verify
		assertThat(optionalCommandParameterInfo).isPresent();
		final CommandParameterInfo commandParameterInfo = optionalCommandParameterInfo.get();
		assertThat(commandParameterInfo.getClassName()).isEqualTo(String.class.getSimpleName());
		assertThat(commandParameterInfo.getDescription()).isEqualTo("MyDescription");
		assertThat(commandParameterInfo.getPossibleValues()).isEqualTo("[ first , second ]");
		assertThat(commandParameterInfo.getDefaultValue()).contains(VALUE);
		assertThat(commandParameterInfo.isGlobal()).isTrue();
		assertThat(commandParameterInfo.getNames()).containsExactly("MyName", "MySecondName");
		assertThat(commandParameterInfo.getExamples()).hasSize(1);
		assertThat(commandParameterInfo.getExamples().get(0).getDescription()).isEqualTo("MyExampleDescription");
		assertThat(commandParameterInfo.getExamples().get(0).getExample()).isEqualTo("MyExample");
	}

	@Test
	public void fetchParameterInfo_noOptionAnnotation() {
		// setup
		final FieldInfo fieldInfo = mock(FieldInfo.class, RETURNS_DEEP_STUBS);
		when(fieldInfo.getName()).thenReturn(FIELD_VALUE);

		// Option
		when(fieldInfo.getAnnotationInfo(Option.class.getName())).thenReturn(null);

		// test
		final Optional<CommandParameterInfo> optionalCommandParameterInfo = _generator.fetchParameterInfo(new NonCommandClass(VALUE), fieldInfo);

		// verify
		assertThat(optionalCommandParameterInfo).isNotPresent();
	}

	@Test
	public void fetchParameterInfo_noNamesPresent() {
		// setup
		final FieldInfo fieldInfo = mock(FieldInfo.class, RETURNS_DEEP_STUBS);
		when(fieldInfo.getName()).thenReturn(FIELD_VALUE);

		// Option
		final AnnotationInfo optionInfo = mock(AnnotationInfo.class, RETURNS_DEEP_STUBS);
		when(fieldInfo.getAnnotationInfo(Option.class.getName())).thenReturn(optionInfo);
		final AnnotationEnumValue typeEnumValue = mock(AnnotationEnumValue.class);
		when(typeEnumValue.getValueName()).thenReturn(OptionType.COMMAND.name());
		when(optionInfo.getParameterValues().getValue(ARG_TYPE)).thenReturn(typeEnumValue);
		when(optionInfo.getParameterValues(true).getValue(ARG_NAME)).thenReturn(null);

		// test
		final Optional<CommandParameterInfo> optionalCommandParameterInfo = _generator.fetchParameterInfo(new NonCommandClass(VALUE), fieldInfo);

		// verify
		assertThat(optionalCommandParameterInfo).isNotPresent();
	}

	@Test
	public void fetchParameterInfo_emptyNames() {
		// setup
		final FieldInfo fieldInfo = mock(FieldInfo.class, RETURNS_DEEP_STUBS);
		when(fieldInfo.getName()).thenReturn(FIELD_VALUE);

		// Option
		final AnnotationInfo optionInfo = mock(AnnotationInfo.class, RETURNS_DEEP_STUBS);
		when(fieldInfo.getAnnotationInfo(Option.class.getName())).thenReturn(optionInfo);
		final AnnotationEnumValue typeEnumValue = mock(AnnotationEnumValue.class);
		when(typeEnumValue.getValueName()).thenReturn(OptionType.COMMAND.name());
		when(optionInfo.getParameterValues().getValue(ARG_TYPE)).thenReturn(typeEnumValue);
		when(optionInfo.getParameterValues(true).getValue(ARG_NAME)).thenReturn(new String[0]);

		// test
		final Optional<CommandParameterInfo> optionalCommandParameterInfo = _generator.fetchParameterInfo(new NonCommandClass(VALUE), fieldInfo);

		// verify
		assertThat(optionalCommandParameterInfo).isNotPresent();
	}

	@Test
	public void fetchParameterInfo_fieldNotFound() {
		// setup
		final FieldInfo fieldInfo = mock(FieldInfo.class, RETURNS_DEEP_STUBS);
		when(fieldInfo.getName()).thenReturn("UNKNOWN FIELD");

		// test
		final Optional<CommandParameterInfo> optionalCommandParameterInfo = _generator.fetchParameterInfo(new NonCommandClass(VALUE), fieldInfo);

		// verify
		assertThat(optionalCommandParameterInfo).isNotPresent();
	}

	@Test
	public void fetchParameterInfo_forIllegalType() {
		// setup
		final FieldInfo fieldInfo = mock(FieldInfo.class, RETURNS_DEEP_STUBS);
		when(fieldInfo.getName()).thenReturn(FIELD_VALUE);

		// Option
		final AnnotationInfo optionInfo = mock(AnnotationInfo.class, RETURNS_DEEP_STUBS);
		when(fieldInfo.getAnnotationInfo(Option.class.getName())).thenReturn(optionInfo);
		final AnnotationEnumValue typeEnumValue = mock(AnnotationEnumValue.class);
		when(typeEnumValue.getValueName()).thenReturn(OptionType.GROUP.name());
		when(optionInfo.getParameterValues().getValue(ARG_TYPE)).thenReturn(typeEnumValue);

		// test
		final Optional<CommandParameterInfo> optionalCommandParameterInfo = _generator.fetchParameterInfo(new NonCommandClass(VALUE), fieldInfo);

		// verify
		assertThat(optionalCommandParameterInfo).isNotPresent();
	}

	@Test
	public void fetchParameterInfos() {
		// setup
		final FieldInfo firstFieldInfo = mock(FieldInfo.class);
		final FieldInfo secondFieldInfo = mock(FieldInfo.class);
		final FieldInfo thirdFieldInfo = mock(FieldInfo.class);
		final CommandParameterInfo firstParameterInfo = mock(CommandParameterInfo.class);
		final CommandParameterInfo thirdParameterInfo = mock(CommandParameterInfo.class);

		final Command command = mock(Command.class);
		final CommandDocumentationGenerator generator = spy(_generator);

		doReturn(Optional.of(firstParameterInfo)).when(generator).fetchParameterInfo(command, firstFieldInfo);
		doReturn(Optional.empty()).when(generator).fetchParameterInfo(command, secondFieldInfo);
		doReturn(Optional.of(thirdParameterInfo)).when(generator).fetchParameterInfo(command, thirdFieldInfo);

		final FieldInfoList fieldInfos = new FieldInfoList();
		fieldInfos.add(firstFieldInfo);
		fieldInfos.add(secondFieldInfo);
		fieldInfos.add(thirdFieldInfo);

		// test
		final List<CommandParameterInfo> result = generator.fetchParameterInfos(command, fieldInfos);

		// verify
		assertThat(result).containsExactly(firstParameterInfo, thirdParameterInfo);
	}

	@Test
	public void fetchParameters() {
		// setup
		final ClassInfo classInfo = mock(ClassInfo.class);
		final FieldInfoList fieldInfoList = new FieldInfoList();
		when(classInfo.loadClass()).thenReturn((Class) NonCommandClass.class);
		when(classInfo.getFieldInfo()).thenReturn(fieldInfoList);

		final ArrayList<CommandParameterInfo> parameterInfos = new ArrayList<>();
		final CommandParameterInfo firstInfo = mock(CommandParameterInfo.class);
		final CommandParameterInfo secondInfo = mock(CommandParameterInfo.class);
		parameterInfos.add(firstInfo);
		parameterInfos.add(secondInfo);

		final CommandDocumentationGenerator generator = spy(_generator);
		doReturn(parameterInfos).when(generator).fetchParameterInfos(any(), any());

		// test
		final List<CommandParameterInfo> parameters = generator.fetchParameters(classInfo);

		// verify
		assertThat(parameters).containsExactly(firstInfo, secondInfo);
	}

	@Test
	public void createDocumentationInfo_withGroups() {
		// setup
		final ClassInfo classInfo = mock(ClassInfo.class, RETURNS_DEEP_STUBS);
		final AnnotationInfo commandAnnotationInfo = mock(AnnotationInfo.class, RETURNS_DEEP_STUBS);
		when(classInfo.getAnnotationInfo().get(Command.class.getName())).thenReturn(commandAnnotationInfo);
		when(commandAnnotationInfo.getParameterValues(true).getValue(ARG_NAME)).thenReturn("MyName");
		when(commandAnnotationInfo.getParameterValues(true).getValue(ARG_DESCRIPTION)).thenReturn("MyDescription");
		when(commandAnnotationInfo.getParameterValues(true).getValue(ARG_COMMAND_GROUP_NAMES)).thenReturn(new String[]{"Group#1", "Group#2"});

		final CommandDocumentationGenerator generator = spy(_generator);
		final ArrayList<ExamplesInfo> examples = new ArrayList<>();
		doReturn(new ArrayList<>()).when(generator).fetchParameters(classInfo);
		doReturn(examples).when(generator).fetchExamples(any());

		// test
		final Optional<CommandDocumentationInfo> optionalDocumentationInfo = generator.createDocumentationInfo(classInfo);

		// verify
		assertThat(optionalDocumentationInfo).isPresent();
		final CommandDocumentationInfo documentationInfo = optionalDocumentationInfo.get();
		assertThat(documentationInfo.getName()).isEqualTo("MyName");
		assertThat(documentationInfo.getDescription()).isEqualTo("MyDescription");
		assertThat(documentationInfo.getGroupNames()).containsExactly("Group#1", "Group#2");
		assertThat(documentationInfo.getExamples()).isSameAs(examples);
	}

	@Test
	public void createDocumentationInfo_withoutGroups() {
		// setup
		final ClassInfo classInfo = mock(ClassInfo.class, RETURNS_DEEP_STUBS);
		final AnnotationInfo commandAnnotationInfo = mock(AnnotationInfo.class, RETURNS_DEEP_STUBS);
		when(classInfo.getAnnotationInfo().get(Command.class.getName())).thenReturn(commandAnnotationInfo);
		when(commandAnnotationInfo.getParameterValues(true).getValue(ARG_NAME)).thenReturn("MyName");
		when(commandAnnotationInfo.getParameterValues(true).getValue(ARG_DESCRIPTION)).thenReturn("MyDescription");
		when(commandAnnotationInfo.getParameterValues(true).getValue(ARG_COMMAND_GROUP_NAMES)).thenReturn(null);

		final CommandDocumentationGenerator generator = spy(_generator);
		final ArrayList<ExamplesInfo> examples = new ArrayList<>();
		doReturn(new ArrayList<>()).when(generator).fetchParameters(classInfo);
		doReturn(examples).when(generator).fetchExamples(any());

		// test
		final Optional<CommandDocumentationInfo> optionalDocumentationInfo = generator.createDocumentationInfo(classInfo);

		// verify
		assertThat(optionalDocumentationInfo).isPresent();
		final CommandDocumentationInfo documentationInfo = optionalDocumentationInfo.get();
		assertThat(documentationInfo.getName()).isEqualTo("MyName");
		assertThat(documentationInfo.getDescription()).isEqualTo("MyDescription");
		assertThat(documentationInfo.getGroupNames()).containsExactly(GLOBAL_GROUP_NAME);
		assertThat(documentationInfo.getExamples()).isSameAs(examples);
	}

	@Test
	public void createDocumentationInfo_commandAnnotationNotPresent() {
		// setup
		final ClassInfo classInfo = mock(ClassInfo.class, RETURNS_DEEP_STUBS);
		when(classInfo.getAnnotationInfo().get(Command.class.getName())).thenReturn(null);
		// test
		final Optional<CommandDocumentationInfo> optionalDocumentationInfo = _generator.createDocumentationInfo(classInfo);
		// verify
		assertThat(optionalDocumentationInfo).isNotPresent();
	}

	@Test
	public void createDocumentationInfo_noNameDefined() {
		// setup
		final ClassInfo classInfo = mock(ClassInfo.class, RETURNS_DEEP_STUBS);
		final AnnotationInfo commandAnnotationInfo = mock(AnnotationInfo.class, RETURNS_DEEP_STUBS);
		when(classInfo.getAnnotationInfo().get(Command.class.getName())).thenReturn(commandAnnotationInfo);
		when(commandAnnotationInfo.getParameterValues(true).getValue(ARG_NAME)).thenReturn(null);
		// test
		final Optional<CommandDocumentationInfo> optionalDocumentationInfo = _generator.createDocumentationInfo(classInfo);
		// verify
		assertThat(optionalDocumentationInfo).isNotPresent();
	}

	@Test
	public void scanClasses() {
		// setup
		final String[] packagesToScan = new String[]{"com.espirit.moddev.cli.documentation.testclasses"};
		final String[] blacklistedClasses = new String[]{"com.espirit.moddev.cli.documentation.testclasses.BlacklistedCommandClass"};

		// test
		final List<CommandDocumentationInfo> documentationInfos = _generator.scanClasses(packagesToScan, blacklistedClasses);

		// verify
		assertThat(documentationInfos).hasSize(2);
		{
			final Optional<CommandDocumentationInfo> optionalInfo = documentationInfos.stream().filter(info -> info.getName().equals("ValidCommandClass")).findFirst();
			assertThat(optionalInfo).isPresent();
			final CommandDocumentationInfo info = optionalInfo.get();
			assertThat(info.getName()).isEqualTo("ValidCommandClass");
			assertThat(info.getDescription()).isEqualTo("");
			assertThat(info.getGroupNames()).containsExactly(GLOBAL_GROUP_NAME);
			assertThat(info.getParameters(true)).isEmpty();
			assertThat(info.getParameters(false)).isEmpty();
			assertThat(info.getExamples()).isEmpty();
		}
		{
			final Optional<CommandDocumentationInfo> optionalInfo = documentationInfos.stream().filter(name -> name.getName().equals("ValidCommandClassFullExample")).findFirst();
			assertThat(optionalInfo).isPresent();
			final CommandDocumentationInfo info = optionalInfo.get();
			assertThat(info.getName()).isEqualTo("ValidCommandClassFullExample");
			assertThat(info.getDescription()).isEqualTo("ValidCommandClassFullExampleDescription");
			assertThat(info.getGroupNames()).containsExactly("first", "second");
			{
				assertThat(info.getExamples()).hasSize(2);
				assertThat(info.getExamples().get(0).getExample()).isEqualTo("example#1");
				assertThat(info.getExamples().get(0).getDescription()).isEqualTo("description#1");
				assertThat(info.getExamples().get(1).getExample()).isEqualTo("example#2");
				assertThat(info.getExamples().get(1).getDescription()).isEqualTo("description#2");
			}
			{
				final List<CommandParameterInfo> parameters = info.getParameters(true);
				assertThat(parameters).hasSize(2);
				verifyParameter(parameters, String.class, "--globalParameterFromAbstractClass");
				verifyParameter(parameters, String.class, "--globalParameter");
			}
			{
				final List<CommandParameterInfo> parameters = info.getParameters(false);
				assertThat(parameters).hasSize(8);
				verifyParameter(parameters, String.class, "--parameterFromAbstractClass");
				verifyParameter(parameters, String.class, "--parameterWithTitle", null, "Title of parameter", null, null, false);
				verifyParameter(parameters, String.class, "--parameterWithDescription", "Description", null, null, null, false);
				verifyParameter(parameters, String.class, "--parameterWithRequired", null, null, null, null, true);
				verifyParameter(parameters, String.class, "--parameterWithDefaultValue", null, null, "defaultValue", null, false);
				verifyParameter(parameters, Integer.class, new String[]{"--parameterWithMultipleNames", "-pwmn"}, null, null, null, null, false);
				verifyParameter(parameters, String.class, "--parameterWithRawValues", null, null, null, new String[]{"VALUE#1", "VALUE#2"}, false);
				{
					final CommandParameterInfo parameter = verifyParameter(parameters, Integer.class, "--parameterWithExamples");
					assertThat(parameter.getExamples()).hasSize(2);
					assertThat(parameter.getExamples().get(0).getExample()).isEqualTo("example#1");
					assertThat(parameter.getExamples().get(0).getDescription()).isEqualTo("description#1");
					assertThat(parameter.getExamples().get(1).getExample()).isEqualTo("example#2");
					assertThat(parameter.getExamples().get(1).getDescription()).isEqualTo("description#2");
				}
			}
		}
	}

	@Test
	public void getDocumentationInfos() {
		final Collection<CommandDocumentationInfo> documentationInfos = _generator.getDocumentationInfos();
		assertThat(documentationInfos).hasSize(25); // currently: 25 commands
	}

	@NotNull
	private CommandParameterInfo verifyParameter(@NotNull final List<CommandParameterInfo> parameters, @NotNull final Class<?> type, @NotNull final String name) {
		return verifyParameter(parameters, type, new String[]{name}, null, null, null, null, false);
	}

	@NotNull
	private CommandParameterInfo verifyParameter(@NotNull final List<CommandParameterInfo> parameters,
												 @NotNull final Class<?> type,
												 @NotNull final String name,
												 @Nullable final String description,
												 @Nullable final String title,
												 @Nullable final Object defaultValue,
												 @Nullable final String[] possibleValues,
												 final boolean required
	) {
		return verifyParameter(parameters, type, new String[]{name}, description, title, defaultValue, possibleValues, required);
	}

	@NotNull
	private CommandParameterInfo verifyParameter(@NotNull final List<CommandParameterInfo> parameters,
												 @NotNull final Class<?> type,
												 @NotNull final String[] names,
												 @Nullable final String description,
												 @Nullable final String title,
												 @Nullable final Object defaultValue,
												 @Nullable final String[] possibleValues,
												 final boolean required
	) {
		final Optional<CommandParameterInfo> optionalParameter = parameters.stream().filter(info -> info.getNames().get(0).equals(names[0])).findFirst();
		assertThat(optionalParameter).isPresent();
		final CommandParameterInfo parameter = optionalParameter.get();
		assertThat(parameter.getNames()).containsExactly(names);
		assertThat(parameter.getDescription()).isEqualTo(description == null ? "" : description);
		assertThat(parameter.getClassName()).isEqualTo(title == null ? type.getSimpleName() : title);
		assertThat(parameter.isRequired()).isEqualTo(required);
		if (defaultValue != null) {
			assertThat(parameter.getDefaultValue()).contains(defaultValue.toString());
		} else {
			assertThat(parameter.getDefaultValue()).isNotPresent();
		}
		if (possibleValues != null) {
			assertThat(parameter.getPossibleValues()).isEqualTo("[ " + String.join(" , ", possibleValues) + " ]");
		}
		return parameter;
	}

}