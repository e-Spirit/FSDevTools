/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2021 e-Spirit GmbH
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

import com.espirit.moddev.cli.results.logging.AdvancedLogger;
import de.espirit.firstspirit.agency.StoreAgent;
import de.espirit.firstspirit.store.access.nexport.operations.ExportOperation;


/**
 * Specialization of {@link com.espirit.moddev.cli.results.SimpleResult} that can be used in conjunction with export commands.
 *
 * @author e-Spirit GmbH
 */
public class ExportResult extends SimpleResult<ExportOperation.Result> {

    private final StoreAgent _storeAgent;

    /**
     * Creates a new instance using the given command result.
     *
     * @param storeAgent used to request related FirstSpirit elements
     * @param result Result produced by the command
     * @see com.espirit.moddev.cli.results.SimpleResult#SimpleResult(Object)
     */
    public ExportResult(final StoreAgent storeAgent, ExportOperation.Result result) {
        super(result);
        _storeAgent = storeAgent;
    }

    /**
     * Creates a new error result using the given exception.
     *
     * @param exception Exception produced by the command
     * @see com.espirit.moddev.cli.results.SimpleResult#SimpleResult(Exception)
     */
    public ExportResult(Exception exception) {
        super(exception);
        _storeAgent = null;
    }

    @Override
    public void log() {
        if (isError()) {
            LOGGER.error("Export operation not successful", exception);
        } else {
            LOGGER.info("Export operation successful");
            // new logging, based on elements
            logElementBasedResult(get());
        }
    }

    /**
     * Logs a result based on the element based export operation api result.
     *
     * @see de.espirit.firstspirit.store.access.nexport.ExportInfo
     */
    private void logElementBasedResult(final ExportOperation.Result exportResult) {
        AdvancedLogger.logExportResult(LOGGER, _storeAgent, exportResult);
    }


}
