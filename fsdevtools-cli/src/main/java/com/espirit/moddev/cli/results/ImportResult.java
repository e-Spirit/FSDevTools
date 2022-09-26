/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2022 Crownpeak Technology GmbH
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

import de.espirit.firstspirit.agency.StoreAgent;
import de.espirit.firstspirit.store.access.nexport.operations.ImportOperation;

import com.espirit.moddev.cli.results.logging.AdvancedLogger;

/**
 * Specialization of {@link com.espirit.moddev.cli.results.SimpleResult} that can be used in conjunction with import commands.
 *
 * @author e-Spirit GmbH
 */
public class ImportResult extends SimpleResult<ImportOperation.Result> {

	private final StoreAgent storeAgent;

	/**
	 * Creates a new instance using the given command result.
	 *
	 * @param storeAgent a store agent
	 * @param result     Result produced by the command
	 */
	public ImportResult(final StoreAgent storeAgent, ImportOperation.Result result) {
		super(result);
		this.storeAgent = storeAgent;
	}

	/**
	 * Creates a new error result using the given exception.
	 *
	 * @param exception Exception produced by the command
	 */
	public ImportResult(Exception exception) {
		super(exception);
		storeAgent = null;
	}

	@Override
	public void log() {
		if (isError()) {
			LOGGER.error("Import operation not successful", exception);
		} else {
			LOGGER.info("Import operation successful");
			AdvancedLogger.logImportResult(LOGGER, storeAgent, get());
		}
	}
}
