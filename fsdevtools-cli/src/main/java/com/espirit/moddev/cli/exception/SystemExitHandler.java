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

package com.espirit.moddev.cli.exception;

import com.espirit.moddev.cli.api.event.CliEventHandler;
import org.slf4j.LoggerFactory;

/**
 * Listener that will exit the cli application with {@link System#exit(int)} if an error occurs.
 *
 * @author e-Spirit AG
 */
public final class SystemExitHandler implements CliEventHandler {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SystemExitHandler.class);

    @Override
    @SuppressWarnings("squid:S1147")
    public final void afterExceptionalTermination(Throwable e) {
        LOGGER.error("An unexpected error occurred during command execution", e);
        System.exit(1);
    }

    @Override
    @SuppressWarnings("squid:S1147")
    public final void afterTermination() {
        LOGGER.trace("Execution terminated without exception. Calling System.exit(0).");
        System.exit(0);
    }

}
