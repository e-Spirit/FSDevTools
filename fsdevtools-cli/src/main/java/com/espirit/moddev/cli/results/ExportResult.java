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

import de.espirit.firstspirit.io.FileHandle;
import de.espirit.firstspirit.store.access.nexport.operations.ExportOperation;

import java.util.Set;

/**
 * Specialization of {@link com.espirit.moddev.cli.results.SimpleResult} that can be used in conjunction with export commands.
 * @author e-Spirit AG
 */
public class ExportResult extends SimpleResult<ExportOperation.Result> {

    /**
     * Creates a new instance using the given command result.
     *
     * @param result Result produced by the command
     * @see com.espirit.moddev.cli.results.SimpleResult#SimpleResult(Object)
     */
    public ExportResult(ExportOperation.Result result) {
        super(result);
    }

    /**
     * Creates a new error result using the given exception.
     * @param exception Exception produced by the command
     * @see com.espirit.moddev.cli.results.SimpleResult#SimpleResult(Exception)
     */
    public ExportResult(Exception exception) {
        super(exception);
    }

    @Override
    public void log() {
        if(isError()) {
            LOGGER.error("Export operation not successful", exception);
        } else {
            LOGGER.info("Export operation successful");

                logFileChanges(get().getUpdatedFiles(), "updated files");
                logFileChanges(get().getCreatedFiles(), "created files");
                logFileChanges(get().getDeletedFiles(), "deleted files");
                Object[] args = {Integer.valueOf(get().getUpdatedFiles().size()),
                        Integer.valueOf(get().getCreatedFiles().size()),
                        Integer.valueOf(get().getDeletedFiles().size())};

                LOGGER.info("Export done.\n\t"
                        + "updated files: {}\n\t"
                        + "created files: {}\n\t"
                        + "deleted files: {}", args);
        }
    }

    /**
     * Log info messages.
     *
     * @param handle represents the current element that was exported
     * @param state  is used for the log message ("updated", "created" and "deleted")
     */
    private void logFileChanges(final Set<FileHandle> handle, final String state) {
        LOGGER.info("{}: {}", state, handle.size());
        for (FileHandle _handle : handle) {
            LOGGER.debug("fileName: " + _handle.getName() + " filePath: " + _handle.getPath());
        }
    }
}
