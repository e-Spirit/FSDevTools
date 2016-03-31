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

/**
 * The type CliException.
 */
public class CliException extends RuntimeException {

    /**
     * Instantiates a new Fs file sync exception.
     *
     * @param error  the error
     * @param config the config
     * @param cause  the cause
     */
    public CliException(CliError error, Config config, Throwable cause) {
        super(error.getMessage(config), cause);
    }
    public CliException(Throwable cause) {
        super(cause.getMessage(), cause);
    }
    @Override
    public String toString() {
        return getMessage();
    }
}
