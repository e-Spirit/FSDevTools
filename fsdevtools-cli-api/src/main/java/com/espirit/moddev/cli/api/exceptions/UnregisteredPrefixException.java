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

package com.espirit.moddev.cli.api.exceptions;

/**
 * States, that a given prefix is not within the registered ones.
 * Prefixes are meant to identify FirstSpirit elements in specific namespaces.
 *
 * @author e-Spirit AG
 */
public class UnregisteredPrefixException extends RuntimeException {

    /**
     * Create a new instance of this exception with the given message.
     *
     * @param message the message of this exception
     * @see java.lang.Exception#Exception(String)
     */
    public UnregisteredPrefixException(String message) {
        super(message);
    }
}
