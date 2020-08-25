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

import com.espirit.moddev.cli.CliConstants;
import com.espirit.moddev.cli.api.configuration.Config;
import com.espirit.moddev.cli.SyncDirectoryFactory;

import com.espirit.moddev.connection.FsConnectionType;
import com.espirit.moddev.util.FsUtil;
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
    public TemporaryFolder _temporaryFolder = new TemporaryFolder();

    private SyncDirectoryFactory _testling;
    private File _syncDir;

    @Before
    public void setUp() throws Exception {
        _syncDir = _temporaryFolder.newFolder();
        _testling = new SyncDirectoryFactory(new Config() {
            @Override
            public String getHost() {
                return FsUtil.VALUE_DEFAULT_HOST;
            }

            @Override
            public Integer getPort() {
                return FsConnectionType.HTTP.getDefaultPort();
            }

            @Override
            public String getHttpProxyHost() {
                return "";
            }

            @Override
            public Integer getHttpProxyPort() {
                return 8080;
            }

            @Override
            public String getServletZone() {
                return CliConstants.DEFAULT_SERVLET_ZONE.value();
            }

            @Override
            public FsConnectionType getConnectionMode() {
                return null;
            }

            @Override
            public String getUser() {
                return FsUtil.VALUE_DEFAULT_USER;
            }

            @Override
            public String getPassword() {
                return FsUtil.VALUE_DEFAULT_USER;
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
        _testling.checkAndCreateSyncDirIfNeeded(_syncDir.getAbsolutePath());

        assertTrue("Expect sync dir exists", _syncDir.exists());
    }

    @Test
    public void testCheckAndCreateSyncDir() throws Exception {
        _syncDir = new File(_temporaryFolder.newFolder(), "mySyncDir");

        assertFalse("Expect sync dir is missing", _syncDir.exists());

        _testling.checkAndCreateSyncDirIfNeeded(_syncDir.getAbsolutePath());

        assertTrue("Expect sync dir exists", _syncDir.exists());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckAndCreateSyncDirIfNeededException() throws Exception {
        _syncDir = _temporaryFolder.newFile();

        _testling.checkAndCreateSyncDirIfNeeded(_syncDir.getAbsolutePath());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckAndCreateSyncDirIfNeededEmpty() throws Exception {
        _testling.checkAndCreateSyncDirIfNeeded("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckAndCreateSyncDirIfNeededNull() throws Exception {
        _testling.checkAndCreateSyncDirIfNeeded(null);
    }
}
