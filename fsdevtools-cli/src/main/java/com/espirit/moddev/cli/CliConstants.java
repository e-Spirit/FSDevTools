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

import com.espirit.moddev.util.FsUtil;

/**
 * Constants that are used in the context of FirstSpirit and the cli classes.
 *
 * @author e-Spirit AG
 */
public enum CliConstants {

    /**
     * Create sync dir if missing cli constant.
     */
    CREATE_SYNC_DIR_IF_MISSING("create_sync_dir"),

    /**
     * True cli constant.
     */
    TRUE("true"),

    /**
     * Default user cli constant.
     */
    DEFAULT_USER(FsUtil.VALUE_DEFAULT_USER),

    /**
     * Default host cli constant.
     */
    DEFAULT_HOST(FsUtil.VALUE_DEFAULT_HOST),

    /**
     * Default servlet zone cli constant.
     */
    DEFAULT_SERVLET_ZONE(null),

    /**
     * Default connection mode cli constant.
     */
    DEFAULT_CONNECTION_MODE("HTTP"),

    /**
     * Default import comment.
     */
    DEFAULT_IMPORT_COMMENT("Imported by fs-cli"),

    /**
     * Key http proxy host cli constant.
     */
    KEY_FS_HTTP_PROXYHOST("fshttpproxyhost"),

    /**
     * Key http proxy port cli constant.
     */
    KEY_FS_HTTP_PROXYPORT("fshttpproxyport"),

    /**
     * Key fs host cli constant.
     */
    KEY_FS_HOST("fshost"),

    /**
     * Key fs port cli constant.
     */
    KEY_FS_PORT("fsport"),

    /**
     * Key fs port cli constant.
     */
    KEY_FS_SERVLETZONE("fsservletzone"),

    /**
     * Key fs mode cli constant.
     */
    KEY_FS_MODE("fsmode"),

    /**
     * Key fs user cli constant.
     */
    KEY_FS_USER("fsuser"),

    /**
     * Key fs password cli constant.
     */
    KEY_FS_PASSWORD("fspwd"),

    /**
     * Key fs project cli constant.
     */
    KEY_FS_PROJECT("fsproject"),

    /**
     * Key fs import comment cli constant.
     */
    KEY_FS_IMPORT_COMMENT("import_comment"),

    /**
     * Fs cli cli constant.
     */
    FS_CLI("fs-cli"),

    /**
     * Help cli constant.
     */
    HELP("help"),

    /**
     * Log 4 j debug cli constant.
     */
    LOG4J_DEBUG("log4j.debug"),

    /**
     * Fs cli log dir cli constant.
     */
    FS_CLI_LOG_DIR("FS_CLI_LOG_DIR"),

    /**
     * User home cli constant.
     */
    USER_HOME("user.home"),

    /**
     * Fs cli dir cli constant.
     */
    FS_CLI_DIR("/.fs-cli/"),

    /**
     * One second in millis cli constant.
     */
    ONE_SECOND_IN_MILLIS("1000"),

    /**
     * Stdout appender cli constant.
     */
    STDOUT_APPENDER("stdout"),

    /**
     * Stderr appender cli constant.
     */
    STDERR_APPENDER("stderr");

    private final String configValue;

    /**
     * Instantiates a new Cli constants.
     *
     * @param configValue the config value
     */
    CliConstants(final String configValue) {
        this.configValue = configValue;
    }

    /**
     * Equals value.
     *
     * @param value the value
     * @return the boolean
     */
    public boolean equalsValue(final Object value) {
        return configValue.equals(value);
    }

    /**
     * Checks if a value is the default value.
     *
     * @param value the value
     * @return the boolean
     */
    public boolean isDefault(final String value) {
        return configValue.equals(value);
    }

    /**
     * Access value as String.
     *
     * @return the string
     */
    public String value() {
        return configValue;
    }

    /**
     * Access value as integer.
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
