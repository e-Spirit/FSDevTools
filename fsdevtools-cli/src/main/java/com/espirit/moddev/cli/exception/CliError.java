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

package com.espirit.moddev.cli.exception;

import com.espirit.moddev.cli.api.configuration.Config;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * The enum Fs file sync error.
 *
 * @author e-Spirit AG
 */
public enum CliError {

    /**
     * The SESSIONS.
     */
    SESSIONS(CliErrorSevereness.MINOR),

    /**
     * The AUTHENTICATION.
     */
    AUTHENTICATION(CliErrorSevereness.MAJOR),

    /**
     * The GENERAL_IO.
     */
    GENERAL_IO(
        CliErrorSevereness.MINOR),

    /**
     * The UNEXPECTED.
     */
    UNEXPECTED(CliErrorSevereness.FATAL);

    private static ResourceBundle bundle = ResourceBundle.getBundle(CliError.class.getSimpleName());

    private final CliErrorSevereness severeness;

    CliError(CliErrorSevereness severeness) {
        this.severeness = severeness;
    }

    /**
     * Gets message.
     *
     * @param config the config
     * @return the message
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
     * Gets error code.
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
