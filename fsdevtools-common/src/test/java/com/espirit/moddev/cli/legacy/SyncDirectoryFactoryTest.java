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

package com.espirit.moddev.cli.legacy;

import com.espirit.moddev.cli.CliConstants;
import com.espirit.moddev.cli.api.configuration.Config;
import com.espirit.moddev.cli.extsync.SyncDirectoryFactory;
import com.espirit.moddev.connection.FsConnectionType;
import com.espirit.moddev.util.FsUtil;
import de.espirit.firstspirit.io.FileHandle;
import de.espirit.firstspirit.io.FileSystem;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SyncDirectoryFactoryTest {

	@TempDir
	public File _temporaryFolder;

	private SyncDirectoryFactory _testling;
	private File _syncDir;

	@BeforeEach
	public void setUp() {
		_syncDir = _temporaryFolder.toPath().resolve(UUID.randomUUID().toString()).toFile();
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

			@NotNull
			@Override
			public String getResultFile() {
				return FsUtil.VALUE_DEFAULT_RESULT_FILE;
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
	public void testCheckAndCreateSyncDirIfNeeded() {
		_testling.checkAndCreateSyncDirIfNeeded(_syncDir.getAbsolutePath());

		assertTrue(_syncDir.exists(), "Expect sync dir exists");
	}

	@Test
	public void testCheckAndCreateSyncDir() {
		_syncDir = new File(_temporaryFolder, "mySyncDir");

		assertFalse(_syncDir.exists(), "Expect sync dir is missing");

		_testling.checkAndCreateSyncDirIfNeeded(_syncDir.getAbsolutePath());

		assertTrue(_syncDir.exists(), "Expect sync dir exists");
	}

	@Test
	public void testCheckAndCreateSyncDirIfNeededEmpty() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			_testling.checkAndCreateSyncDirIfNeeded("");
		});
	}

	@Test
	public void testCheckAndCreateSyncDirIfNeededNull() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			_testling.checkAndCreateSyncDirIfNeeded(null);
		});
	}
}
