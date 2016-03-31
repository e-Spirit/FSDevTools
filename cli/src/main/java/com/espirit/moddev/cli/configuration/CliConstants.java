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

package com.espirit.moddev.cli.configuration;

/**
 * The enum Vcs connect cli constants.
 */
public enum CliConstants {

    DEFAULT_USER("Admin"),
    DEFAULT_PROJECT("Mithras Energy"),
    DEFAULT_HOST("localhost"),
    DEFAULT_CONNECTION_MODE("HTTP"),

    KEY_FS_HOST("fshost"),
    KEY_FS_PORT("fsport"),
    KEY_FS_MODE("fsmode"),
    KEY_FS_USER("fsuser"),
    KEY_FS_PASSWORD("fspwd"),
    KEY_FS_PROJECT("fsproject"),
    KEY_FS_IMPORT_COMMENT("import_comment"),

    FS_CLI("fs-cli"),
    HELP("help"),
    LOG4J_DEBUG("log4j.debug"),
    FS_CLI_LOG_DIR("FS_CLI_LOG_DIR"),
    USER_HOME("user.home"),
    FS_CLI_DIR("/.fs-cli/"),
    ONE_SECOND_IN_MILLIS("1000"),

    STDOUT_APPENDER("stdout"),
    STDERR_APPENDER("stderr");

    private final String configValue;

    CliConstants(String configValue) {
        this.configValue = configValue;
    }

    /**
     * Checks if a value is the default value.
     *
     * @param value the value
     * @return the boolean
     */
    public boolean isDefault(String value) {
        return configValue.equals(value);
    }

    /**
     * Value string.
     *
     * @return the string
     */
    public String value() {
        return configValue;
    }

    /**
     * Value as integer.
     *
     * @return the integer
     */
    public Integer valueAsInt() {
        return Integer.valueOf(configValue);
    }

    @Override
    public String toString() {
        return configValue;
    }
}
