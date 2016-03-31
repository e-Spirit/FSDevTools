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

package com.espirit.moddev.cli.api.result;

/**
 * A generic Result interface for arbitrary types. Can be used to
 * distinguish between a successful and non-successful result
 * through exceptions.
 *
 * @param <CUSTOM_RESULT_TYPE>
 */
public interface Result<CUSTOM_RESULT_TYPE> {

    /**
     * Indicates whether a result is successful or not. Can tell a
     * surrounding environment that a Exception is available.
     */
    boolean isError();

    /**
     * Getter for an exception. Should return a exception if the result
     * is not successful.
     *
     * @return the exception that makes this a failed result
     */
    Exception getError();

    /**
     * Optional method for custom result logging.
     */
    default void log() {}

    /**
     * Getter for the custom result type.
     *
     * @return an instance of the custom result type or null
     */
    CUSTOM_RESULT_TYPE get();
}
