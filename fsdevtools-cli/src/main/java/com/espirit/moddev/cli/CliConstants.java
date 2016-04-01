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
     * The SYNC_DIR.
     */
    SYNC_DIR("syncDir"),

    /**
     * The COMMANDS.
     */
    COMMANDS("commands"),

    /**
     * The CREATE_SYNC_DIR_IF_MISSING.
     */
    CREATE_SYNC_DIR_IF_MISSING("create_sync_dir"),

    /**
     * The IMPORT_COMMAND_ERROR_MESSAGE.
     */
    IMPORT_COMMAND_ERROR_MESSAGE("ImportCommand does not handle export!"),

    IMPORT_COMMENT("import_comment"),

    IMPORT_CREATE_ENTITIES("import_create_entities"),

    DELETE_OBSOLETE_FILES("delete_obsolete_files"),

    EXPORT_CHILD_ELEMENTS("export_child_elements"),

    EXPORT_PARENT_ELEMENTS("export_parent_elements"),

    EXPORT_RELEASE_ENTITIES("export_release_entities"),

    EXPORT_FULL_TEMPLATESTORE("export_full_templatestore"),

    TRUE("true");

    private final String configValue;

    CliConstants(String configValue) {
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
    public boolean equalsValue(Object value) {
        return configValue.equals(value);
    }
}
