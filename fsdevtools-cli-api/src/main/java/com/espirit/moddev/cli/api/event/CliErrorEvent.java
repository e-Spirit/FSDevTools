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

package com.espirit.moddev.cli.api.event;


/**
 * Event used in the cli application to notify about an error.
 *
 * @author e-Spirit AG
 */
public class CliErrorEvent {

    private final Object source;
    private final Throwable error;

    /**
     * Instantiates a new instance.
     *
     * @param source the source of the event
     * @param error the error that occurred
     */
    public CliErrorEvent(Object source, Throwable error) {
        this.source = source;
        this.error = error;
    }

    /**
     * Get source of the event.
     *
     * @return the source
     */
    public Object getSource() {
        return source;
    }

    /**
     * Get error that occurred.
     *
     * @return the error
     */
    public Throwable getError() {
        return error;
    }
}
