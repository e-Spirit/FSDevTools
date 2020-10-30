/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2020 e-Spirit AG
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

package com.espirit.moddev.cli.exception;

import com.espirit.moddev.cli.api.configuration.Config;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Errors that can occur in the cli application.
 *
 * @author e-Spirit AG
 */
public enum CliError {

    /**
     * Indicates that the maximum number of sessions has been exceeded.
     * This error has a {@link com.espirit.moddev.cli.exception.CliErrorSevereness#MINOR} severity.
     */
    SESSIONS(CliErrorSevereness.MINOR),

    /**
     * Indicates that the authentication failed.
     * This error has a {@link com.espirit.moddev.cli.exception.CliErrorSevereness#MAJOR} severity.
     */
    AUTHENTICATION(CliErrorSevereness.MAJOR),

    /**
     * Indicates a general I/O error.
     * This error has a {@link com.espirit.moddev.cli.exception.CliErrorSevereness#MINOR} severity.
     */
    GENERAL_IO(CliErrorSevereness.MINOR),

    /**
     * Indicates a configuration error.
     * This error has a {@link com.espirit.moddev.cli.exception.CliErrorSevereness#MINOR} severity.
     */
    CONFIGURATION(CliErrorSevereness.MINOR),

    /**
     * Indicates an unexpected error.
     *This error has a {@link com.espirit.moddev.cli.exception.CliErrorSevereness#FATAL} severity.
     */
    UNEXPECTED(CliErrorSevereness.FATAL);

    private static ResourceBundle bundle = ResourceBundle.getBundle(CliError.class.getSimpleName());

    private final CliErrorSevereness severeness;

    CliError(CliErrorSevereness severeness) {
        this.severeness = severeness;
    }

    /**
     * Get the error message.
     *
     * @param config the configuration values to be contained in the error message
     * @return the error message
     */
    public String getMessage(Config config) {
        final String bundleString = bundle.getString(name());
        if (config == null) {
            return toString() + ": " + bundleString;
        }
        final Object[] args = {config.getHost(), config.getPort(), config.getConnectionMode(), config.getUser(), config.getPassword()};
        final String formattedMessage = MessageFormat.format(bundleString, args);
        return toString() + ": " + formattedMessage;
    }

    /**
     * Get the error code.
     *
     * @return the error code
     */
    public int getErrorCode() {
        return severeness.getErrorCode() + (ordinal() + 1);
    }

    @Override
    public String toString() {
        return severeness.toString() + " error (code " + getErrorCode() + ")";
    }
}
