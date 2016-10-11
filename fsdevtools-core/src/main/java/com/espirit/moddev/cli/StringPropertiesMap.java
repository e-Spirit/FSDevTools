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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Properties;

public class StringPropertiesMap extends HashMap<String, String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(StringPropertiesMap.class);

    public StringPropertiesMap() {
        super();
    }

    public StringPropertiesMap(String source) {
        this();
        String propertiesFormat = source.replaceAll(",", "\n");
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(propertiesFormat));
        } catch (IOException e) {
            String errorString = "Error converting string '" + source + "' to map!"
                                 + " Please pass options like this: \"key=value,abc=123\"";
            LOGGER.error(errorString, e);
        }

        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            put(key, String.valueOf(value));
        }

    }

}
