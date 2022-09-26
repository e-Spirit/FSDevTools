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

import com.espirit.moddev.cli.api.parsing.identifier.Identifier;

import java.util.List;

/**
 * Interface for a parser. A parser can parse a given string into a
 * generic identifier.
 *
 * @param <T> the identifier subclass that the parser implementation can return from a string
 */
public interface Parser<T extends Identifier> {

	/**
	 * Parses a list of strings to a list of generic identifier instances.
	 *
	 * @param input a list of strings to parse
	 * @return a list of parsed identifiers
	 */
	List<T> parse(List<String> input);

	/**
	 * Indicates if the parser implementation can handle the given input string.
	 *
	 * @param input the string to test applicability for
	 * @return true if the input string can be handled somehow
	 */
	boolean appliesTo(String input);
}
