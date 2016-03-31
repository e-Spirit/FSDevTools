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

package com.espirit.moddev.cli.results;

import com.espirit.moddev.cli.api.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param <CUSTOM_RESULT_TYPE>
 * @author e-Spirit AG
 */
public class SimpleResult<CUSTOM_RESULT_TYPE> implements Result<CUSTOM_RESULT_TYPE> {

    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    protected final CUSTOM_RESULT_TYPE result;
    protected Exception exception = null;

    public SimpleResult() {
        this((CUSTOM_RESULT_TYPE) null);
    }
    public SimpleResult(CUSTOM_RESULT_TYPE result) {
        this.result = result;
    }
    public SimpleResult(Exception exception) {
        this.result = null;
        this.exception = exception;
    }

    @Override
    public boolean isError() {
        return exception != null;
    }

    @Override
    public Exception getError() {
        return exception;
    }

    @Override
    public void log() {
        if(isError()) {
            LOGGER.error("Exception occurred while executing command", exception);
        } else {
            if(result != null) {
                LOGGER.info("Result available: " +  result.getClass());
            } else {
                LOGGER.info("Result available");
            }
        }
    }

    @Override
    public CUSTOM_RESULT_TYPE get() {
        return result;
    }
}
