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

package com.espirit.moddev.cli.api.parsing.parser;

import com.espirit.moddev.cli.api.parsing.identifier.SchemaIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

public class SchemaIdentifierParser implements Parser<SchemaIdentifier> {

	protected static final Logger LOGGER = LoggerFactory.getLogger(SchemaIdentifierParser.class);

	private static final String OPTION_DELIMITER = "[";
	private static final Pattern DELIMITER = Pattern.compile("\\s*:\\s*");
	public static final String CUSTOM_PREFIX_SCHEMA_OPTION = "schema";

	@Override
	@NotNull
	public List<SchemaIdentifier> parse(@Nullable final List<String> input) {
		if (input == null) {
			throw new IllegalArgumentException("input is null!");
		}
		if (input.isEmpty()) {
			return Collections.emptyList();
		}

		final List<SchemaIdentifier> list = new ArrayList<>(input.size());
		for (final String identifier : input) {
			try (final Scanner uidScanner = new Scanner(identifier)) {
				uidScanner.useDelimiter(DELIMITER);
				if (uidScanner.hasNext()) {
					uidScanner.next(); // skip the first part, which should be "schema"
					if (uidScanner.hasNext()) {
						list.add(getSchemaIdentifier(uidScanner.next()));
					} else {
						throw new IllegalArgumentException("Wrong input format for input string " + identifier);
					}
				}
			}
		}
		return list;
	}

	@NotNull
	static SchemaIdentifier getSchemaIdentifier(@NotNull final String SchemaID) {
		if (SchemaID.contains(OPTION_DELIMITER)) {
			final int splitPos = SchemaID.indexOf(OPTION_DELIMITER);
			return new SchemaIdentifier(SchemaID.substring(0, splitPos), parseSchemaOptions(SchemaID.substring(splitPos)));
		}
		return new SchemaIdentifier(SchemaID, Collections.emptyMap());
	}

	@Override
	public boolean appliesTo(@NotNull final String input) {
		final String[] split = input.split(DELIMITER.pattern());
		return split.length == 2 && split[0].toLowerCase(Locale.UK).trim().equals(CUSTOM_PREFIX_SCHEMA_OPTION);
	}

	@NotNull
	static Map<String, String> parseSchemaOptions(@NotNull String schemaOptions) {
		if (!schemaOptions.startsWith("[") || !schemaOptions.endsWith("]")) {
			throw new IllegalArgumentException("Invalid SchemaOptions format.");
		}

		// cut first and last char
		schemaOptions = schemaOptions.substring(1, schemaOptions.length() - 1);

		// split options by '|'
		final HashMap<String, String> result = new HashMap<>();
		final String[] split = schemaOptions.split("\\|");
		for (final String text : split) {
			// split single option by '='
			final String[] textSplit = text.split("=");
			if (textSplit.length == 2) {
				// trim & add option + value
				final String optionName = textSplit[0].trim();
				if (!SchemaIdentifier.isSchemaOptionValid(optionName)) {
					throw new IllegalArgumentException("SchemaOption '" + optionName + "' is unknown! Valid options: " + String.join(", ", SchemaIdentifier.VALID_SCHEMA_OPTIONS));
				}
				final String optionValue = textSplit[1].trim();
				result.put(optionName, optionValue);
			}
		}
		return result;
	}

}
