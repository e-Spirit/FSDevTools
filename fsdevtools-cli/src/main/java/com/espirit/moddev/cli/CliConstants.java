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

/**
 * Constants that are used in the context of FirstSpirit and the cli classes.
 *
 * @author e-Spirit AG
 */
public enum CliConstants {

    /**
     * Sync dir cli constant.
     */
    SYNC_DIR("syncDir"),

    /**
     * Commands cli constant.
     */
    COMMANDS("commands"),

    /**
     * Create sync dir if missing cli constant.
     */
    CREATE_SYNC_DIR_IF_MISSING("create_sync_dir"),

    /**
     * The import command error message constant.
     */
    IMPORT_COMMAND_ERROR_MESSAGE("ImportCommand does not handle export!"),

    /**
     * Import comment cli constant.
     */
    IMPORT_COMMENT("import_comment"),

    /**
     * Import create entities cli constant.
     */
    IMPORT_CREATE_ENTITIES("import_create_entities"),

    /**
     * Delete obsolete files cli constant.
     */
    DELETE_OBSOLETE_FILES("delete_obsolete_files"),

    /**
     * Export child elements cli constant.
     */
    EXPORT_CHILD_ELEMENTS("export_child_elements"),

    /**
     * Export parent elements cli constant.
     */
    EXPORT_PARENT_ELEMENTS("export_parent_elements"),

    /**
     * Export release entities cli constant.
     */
    EXPORT_RELEASE_ENTITIES("export_release_entities"),

    /**
     * Export full templatestore cli constant.
     */
    EXPORT_FULL_TEMPLATESTORE("export_full_templatestore"),

    /**
     * True cli constant.
     */
    TRUE("true");

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
     * Value string.
     *
     * @return the string
     */
    public String value() {
        return configValue;
    }

    @Override
    public String toString() {
        return configValue;
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
}
