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

package com.espirit.moddev.cli.testcommands;

import com.espirit.moddev.cli.CliConstants;
import com.espirit.moddev.cli.CliContextImpl;
import com.espirit.moddev.cli.commands.SimpleCommand;
import com.espirit.moddev.cli.configuration.GlobalConfig;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;

import static com.espirit.moddev.IntegrationTest.PROJECT_NAME;

/**
 * Can be used as a base for integration tests that need a connection to a fs server. Uses connection and temporary folder rules to isolate filesync
 * tests from each other as far as possible.
 *
 * @author e-Spirit AG
 */
public abstract class AbstractIntegrationTest {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    protected void initializeTestSpecificConfiguration(final SimpleCommand command) {
        command.setProject(PROJECT_NAME);
        command.setHost(CliConstants.DEFAULT_HOST.value());
        command.setUser(CliConstants.DEFAULT_USER.value());
        command.setPassword(CliConstants.DEFAULT_USER.value());
        command.setPort(command.getConnectionMode().getDefaultPort());

        command.setSynchronizationDirectory(testFolder.getRoot().getAbsolutePath());

        initializeContext(command);
    }

    protected void initializeTestSpecificConfiguration(final GlobalConfig config) {
        config.setProject(PROJECT_NAME);
        config.setHost(CliConstants.DEFAULT_HOST.value());
        config.setUser(CliConstants.DEFAULT_USER.value());
        config.setPassword(CliConstants.DEFAULT_USER.value());
        config.setPort(config.getConnectionMode().getDefaultPort());

        config.setSynchronizationDirectory(testFolder.getRoot().getAbsolutePath());
    }

    public void initializeContext(final SimpleCommand command) {
        command.setContext(new CliContextImpl(command));
    }

    protected File getFirstSpiritFileSyncFolder(final File directory) throws FileNotFoundException {
        checkIsDirectory(directory);
        final Collection<File>
            directories =
            FileUtils.listFilesAndDirs(testFolder.getRoot(), new NotFileFilter(TrueFileFilter.INSTANCE), DirectoryFileFilter.DIRECTORY);
        for (final File candidate : directories) {
            if (candidate.getName().equals(".FirstSpirit")) {
                return candidate;
            }
        }
        throw new FileNotFoundException("Cannot find .FirstSpirit folder in directory " + directory.getAbsolutePath());
    }

    protected boolean containsSubDirectory(final File directory, final String subDirectoryName) {
        checkIsDirectory(directory);
        final Collection<File>
            directories =
            FileUtils.listFilesAndDirs(testFolder.getRoot(), new NotFileFilter(TrueFileFilter.INSTANCE), DirectoryFileFilter.DIRECTORY);
        for (final File candidate : directories) {
            if (candidate.getName().equals(subDirectoryName)) {
                return true;
            }
        }
        return false;
    }

    protected void checkIsDirectory(final File directory) {
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Can only search for .FirstSpirit folder in a directory! Given: " + directory.getAbsolutePath());
        }
    }
}
