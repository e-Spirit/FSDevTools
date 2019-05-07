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

package com.espirit.moddev.cli.legacy;

import com.espirit.moddev.cli.api.FsConnectionMode;
import com.espirit.moddev.cli.api.configuration.Config;
import com.espirit.moddev.cli.SyncDirectoryFactory;

import de.espirit.firstspirit.io.FileHandle;
import de.espirit.firstspirit.io.FileSystem;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author e-Spirit AG
 */
public class SyncDirectoryFactoryTest {


    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private SyncDirectoryFactory testling;
    private File syncDir;

    @Before
    public void setUp() throws Exception {
        syncDir = temporaryFolder.newFolder();
        testling = new SyncDirectoryFactory(new Config() {
            @Override
            public String getHost() {
                return "localhost";
            }

            @Override
            public Integer getPort() {
                return 8000;
            }

            @Override
            public String getProxyHost() {
                return "";
            }

            @Override
            public Integer getProxyPort() {
                return 80;
            }

            @Override
            public FsConnectionMode getConnectionMode() {
                return null;
            }

            @Override
            public String getUser() {
                return "Admin";
            }

            @Override
            public String getPassword() {
                return "Admin";
            }

            @Override
            public String getProject() {
                return null;
            }

            @Override
            public String getSynchronizationDirectoryString() {
                return "test";
            }

            @Override
            public <F extends FileHandle> FileSystem<F> getSynchronizationDirectory() {
                return null;
            }

            @Override
            public boolean isActivateProjectIfDeactivated() {
                return false;
            }

        });
    }

    @Test
    public void testCheckAndCreateSyncDirIfNeeded() throws Exception {
        testling.checkAndCreateSyncDirIfNeeded(syncDir.getAbsolutePath());

        assertTrue("Expect sync dir exists", syncDir.exists());
    }

    @Test
    public void testCheckAndCreateSyncDir() throws Exception {
        syncDir = new File(temporaryFolder.newFolder(), "mySyncDir");

        assertFalse("Expect sync dir is missing", syncDir.exists());

        testling.checkAndCreateSyncDirIfNeeded(syncDir.getAbsolutePath());

        assertTrue("Expect sync dir exists", syncDir.exists());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckAndCreateSyncDirIfNeededException() throws Exception {
        syncDir = temporaryFolder.newFile();

        testling.checkAndCreateSyncDirIfNeeded(syncDir.getAbsolutePath());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckAndCreateSyncDirIfNeededEmpty() throws Exception {
        testling.checkAndCreateSyncDirIfNeeded("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckAndCreateSyncDirIfNeededNull() throws Exception {
        testling.checkAndCreateSyncDirIfNeeded(null);
    }
}
