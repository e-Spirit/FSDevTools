/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2016 e-Spirit AG
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

package com.espirit.moddev.cli;

import java.util.HashMap;
import java.util.Map;


/**
 * The type Environment encapsulates the System environment variables to make them testable.
 *
 * @author e-Spirit AG
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
     * Contains key.
     *
     * @param key the key
     * @return the boolean
     */
    public boolean containsKey(Object key) {
        return environmentVariables.containsKey(key);
    }

    /**
     * Get string.
     *
     * @param key the key
     * @return the string
     */
    public String get(Object key) {
        return environmentVariables.get(key);
    }

    /**
     * Put string.
     *
     * @param key   the key
     * @param value the value
     * @return the string
     */
    public String put(String key, String value) {
        return environmentVariables.put(key, value);
    }

    /**
     * Clears all environment variables of this instance
     */
    public void clear() {
        environmentVariables.clear();
    }
}
