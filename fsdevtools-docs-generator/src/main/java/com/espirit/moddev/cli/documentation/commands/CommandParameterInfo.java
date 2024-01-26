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

package com.espirit.moddev.cli.documentation.commands;

import com.espirit.moddev.cli.commands.PermissionsMode;
import com.espirit.moddev.cli.documentation.pojos.JsonElement;
import com.espirit.moddev.shared.StringUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class CommandParameterInfo implements JsonElement {

	private static final Map<Class<?>, Supplier<String[]>> POSSIBLE_VALUES_SUPPLIER = new HashMap<>();

	static {
		registerPossibleValuesSupplier(boolean.class, () -> new String[]{"true", "false"});
		registerPossibleValuesSupplier(Boolean.class, () -> new String[]{"true", "false"});
		registerPossibleValuesSupplier(int.class, () -> new String[]{"any integer"});
		registerPossibleValuesSupplier(Integer.class, () -> new String[]{"any integer"});
		registerPossibleValuesSupplier(long.class, () -> new String[]{"any long"});
		registerPossibleValuesSupplier(Long.class, () -> new String[]{"any long"});
		registerPossibleValuesSupplier(float.class, () -> new String[]{"any number"});
		registerPossibleValuesSupplier(Float.class, () -> new String[]{"any number"});
		registerPossibleValuesSupplier(double.class, () -> new String[]{"any number"});
		registerPossibleValuesSupplier(Double.class, () -> new String[]{"any number"});
		registerPossibleValuesSupplier(PermissionsMode.class, () -> {
			final String[] result = new String[PermissionsMode.values().length];
			for (final PermissionsMode value : PermissionsMode.values()) {
				result[value.ordinal()] = value.name();
			}
			return result;
		});
	}

	private static void registerPossibleValuesSupplier(@NotNull final Class<?> clazz, @NotNull final Supplier<String[]> supplier) {
		POSSIBLE_VALUES_SUPPLIER.put(clazz, supplier);
	}

	@Nullable
	private static Supplier<String[]> getPossibleValuesSupplier(@NotNull final Class<?> clazz) {
		return POSSIBLE_VALUES_SUPPLIER.get(clazz);
	}

	private final Class<?> _class;
	@JsonProperty("type") // needed in frontend
	private final String _type = "PARAMETER"; // needed in frontend
	@JsonProperty("className")
	private String _className;
	@JsonProperty("names")
	private final List<String> _names;
	@JsonProperty("global")
	private final boolean _isGlobal;
	@JsonProperty("examples")
	private final List<ExamplesInfo> _examples;
	@JsonProperty("description")
	private final String _description;
	@JsonProperty("required")
	private final boolean _required;
	@JsonProperty("defaultValue")
	private String _defaultValue;
	@JsonProperty("possibleValues")
	private String _possibleValues;

	public CommandParameterInfo(@NotNull final Class<?> clazz, @NotNull final List<String> names, final boolean isGlobal, @NotNull final List<ExamplesInfo> examples, @NotNull final String description, final boolean required) {
		_class = clazz;
		_className = clazz.getSimpleName();
		_names = names;
		_isGlobal = isGlobal;
		_examples = examples;
		_description = description.replaceAll("\n", " ").replaceAll("\\|", ",");
		_required = required;
		final Supplier<String[]> supplier = getPossibleValuesSupplier(_class);
		if (supplier == null) {
			_possibleValues = getAnyPossibleValueFallback();
		} else {
			_possibleValues = "[ " + String.join(" , ", supplier.get()) + " ]";
		}
	}

	@NotNull
	private String getAnyPossibleValueFallback() {
		return "<any " + _className + ">";
	}

	public void setPossibleValues(@Nullable final String[] possibleValues) {
		_possibleValues = possibleValues == null ? getAnyPossibleValueFallback() : "[ " + String.join(" , ", possibleValues) + " ]";
	}

	public void setClassName(@Nullable final String className) {
		_className = StringUtils.isNullOrEmpty(className) ? _class.getSimpleName() : className;
		final Supplier<String[]> supplier = getPossibleValuesSupplier(_class);
		if (supplier == null) {
			_possibleValues = getAnyPossibleValueFallback();
		} else {
			_possibleValues = "[ " + String.join(" , ", supplier.get()) + " ]";
		}
	}

	@NotNull
	public String getClassName() {
		return _className;
	}

	@NotNull
	public List<String> getNames() {
		return _names;
	}

	@NotNull
	public String getDescription() {
		return _description;
	}

	public boolean isGlobal() {
		return _isGlobal;
	}

	public boolean isRequired() {
		return _required;
	}

	public void setDefaultValue(@Nullable final String defaultValue) {
		_defaultValue = defaultValue;
	}

	@NotNull
	public List<ExamplesInfo> getExamples() {
		return _examples;
	}

	@NotNull
	public Optional<String> getDefaultValue() {
		return Optional.ofNullable(_defaultValue);
	}

	@NotNull
	public String getPossibleValues() {
		return _possibleValues;
	}

}
