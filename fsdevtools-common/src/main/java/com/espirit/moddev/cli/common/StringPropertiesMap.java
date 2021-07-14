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

package com.espirit.moddev.cli.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * The Class StringPropertiesMap.
 */
public class StringPropertiesMap extends HashMap<String, String> {

    private static final long serialVersionUID = 3456922922496131342L;
    private static final Logger LOGGER = LoggerFactory.getLogger(StringPropertiesMap.class);
    private static final Pattern SEPARATOR_REGEX = Pattern.compile("\\s*,\\s*");

    /**
     * Instantiates a new string properties map.
     */
    public StringPropertiesMap() {
        // der super-Aufruf muss hier sein
        super();
    }

    /**
     * Instantiates a new string properties map.
     *
     * @param source the source
     */
    public StringPropertiesMap(String source) {
        this();
        if (source != null && !source.trim().isEmpty()) {
            Properties properties = parseSource(source);
            store(properties);
        }
    }

    private void store(Properties properties) {
        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            put(key, String.valueOf(value.trim()));
        }
    }

    private static Properties parseSource(String source) {
        // Aus Performance-Gründen nie String.replaceAll() machen!
        String propertiesFormat = SEPARATOR_REGEX.matcher(source).replaceAll(System.lineSeparator());
        Properties properties = new Properties();
        try (Reader reader = new StringReader(propertiesFormat)) {
            properties.load(reader);
        } catch (IOException e) {
            String errorString = "Error converting string '" + source + "' to map!"
                    + " Please pass options like this: \"key=value,abc=123\"";
            LOGGER.error(errorString, e);
        }
        return properties;
    }

}
