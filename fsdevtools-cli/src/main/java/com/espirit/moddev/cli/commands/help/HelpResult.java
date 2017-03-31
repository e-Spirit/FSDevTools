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

package com.espirit.moddev.cli.commands.help;

import com.espirit.moddev.cli.results.SimpleResult;
import com.github.rvesse.airline.model.GlobalMetadata;

/**
 * Specialization of {@link com.espirit.moddev.cli.results.SimpleResult} that can be used in conjunction with help commands.
 */
public class HelpResult extends SimpleResult<GlobalMetadata<Object>> {

    /**
     * Creates a new instance using the given command result.
     *
     * @param metadata Result produced by the command
     * @see com.espirit.moddev.cli.results.SimpleResult#SimpleResult(Object)
     */
    public HelpResult(GlobalMetadata metadata) {
        super(metadata);
    }

    /**
     * Creates a new error result using the given exception.
     *
     * @param exception Exception produced by the command
     * @see com.espirit.moddev.cli.results.SimpleResult#SimpleResult(Exception)
     */
    public HelpResult(Exception exception) {
        super(exception);
    }
}
