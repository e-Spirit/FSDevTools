/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2020 e-Spirit AG
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

package com.espirit.moddev.cli.extsync;

import com.espirit.moddev.cli.CliConstants;
import com.espirit.moddev.cli.api.configuration.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Helper class to create the synchronization directory if needed.
 *
 * @author e-Spirit AG
 */
public class SyncDirectoryFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SyncDirectoryFactory.class);

    private final Config config;

    /**
     * Creates a new instance.
     *
     * @param config the configuration values to use
     */
    public SyncDirectoryFactory(Config config) {
        this.config = config;
    }

    /**
     * Check and create sync dir if needed.
     *
     * @param syncDirStr the sync dir path
     * @throws java.lang.IllegalArgumentException if the sync dir ist not a directory or if the sync dir can not be read.
     */
    public void checkAndCreateSyncDirIfNeeded(String syncDirStr) {
        LOGGER.debug("Check sync dir: {}", syncDirStr);
        try {
            File syncDir = new File(syncDirStr);
            LOGGER.debug("Sync dir absolute path: " + syncDir.getAbsolutePath());
            makeSyncDirIfNeeded(syncDir);
            if (!syncDir.isDirectory()) {
                throw new IllegalArgumentException(
                    "The path '" + syncDirStr + "' is not a directory! Try to omit option '--dont-create-sync-dir' if added.");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) { //NOSONAR
            throw new IllegalArgumentException("Problem with reading synchronisation directory", e);
        }
    }

    private void makeSyncDirIfNeeded(File syncDir) {
        if (nonExistent(syncDir) && createSyncDirIfMissing()) {
            LOGGER.info("Creating synchronisation directory '" + syncDir.getAbsolutePath() + "' since it does not exists!");
            boolean result = syncDir.mkdirs();
            if (!result) {
                throw new IllegalArgumentException("Could not create '" + syncDir.getAbsolutePath() + "'!");
            }
        }
    }

    private static boolean nonExistent(File syncDir) {
        return !syncDir.exists();
    }

    private Boolean createSyncDirIfMissing() {
        final String key = CliConstants.CREATE_SYNC_DIR_IF_MISSING.value();
        final Boolean decision = config.createSynchronizationDirectoryIfMissing();
        LOGGER.debug("{}: {}", key, decision);
        return decision;
    }
}
