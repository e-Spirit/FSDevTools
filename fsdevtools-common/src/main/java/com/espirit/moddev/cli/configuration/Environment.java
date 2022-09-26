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

package com.espirit.moddev.cli.configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * The type Environment encapsulates the system environment variables to make them testable.
 *
 * @author e-Spirit GmbH
 */
public class Environment {

	private final Map<String, String> environmentVariables;

	/**
	 * Instantiates a new Environment.
	 */
	public Environment() {
		this.environmentVariables = new HashMap<>();
		environmentVariables.putAll(System.getenv());
	}

	/**
	 * Checks if a value exists for a key.
	 *
	 * @param key the key
	 * @return the boolean
	 */
	public boolean containsKey(final Object key) {
		return environmentVariables.containsKey(key);
	}

	/**
	 * Get value for key.
	 *
	 * @param key the key
	 * @return the string
	 */
	public String get(final Object key) {
		return environmentVariables.get(key);
	}

	/**
	 * Add key value pair.
	 *
	 * @param key   the key
	 * @param value the value
	 * @return the string
	 */
	public String put(final String key, final String value) {
		return environmentVariables.put(key, value);
	}

	/**
	 * Clears all environment variables of this instance.
	 */
	public void clear() {
		environmentVariables.clear();
	}
}
