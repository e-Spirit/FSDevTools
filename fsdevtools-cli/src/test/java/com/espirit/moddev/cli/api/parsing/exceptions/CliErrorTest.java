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

package com.espirit.moddev.cli.api.parsing.exceptions;

import com.espirit.moddev.cli.exception.CliError;
import com.espirit.moddev.cli.api.configuration.Config;
import com.espirit.moddev.cli.api.FsConnectionMode;
import de.espirit.firstspirit.io.FileHandle;
import de.espirit.firstspirit.io.FileSystem;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author e-Spirit AG
 */
@RunWith(Theories.class)
public class CliErrorTest {

    @DataPoints
    public static CliError[] testCases = CliError.values();

    private ResourceBundle bundle = ResourceBundle.getBundle(CliError.class.getSimpleName());


    @Test
    public void testToString() throws Exception {
        assertThat("Exception a specific value", CliError.AUTHENTICATION.toString(),
                   containsString("code " + CliError.AUTHENTICATION.getErrorCode()));

    }

    @Theory
    public void testGetMessageNullConfig(CliError testCase) throws Exception {
        assertThat("Expect non-null value", testCase.getMessage(null), is(notNullValue()));
    }

    @Theory
    public void testGetMessageConfig(CliError testCase) throws Exception {
        final Config config = new Config() {
            @Override
            public String getHost() {
                return "localhost";
            }

            @Override
            public Integer getPort() {
                return 8000;
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

        };
        final Object[] args = {config.getHost(), config.getPort(), config.getConnectionMode(), config.getUser(), config.getPassword()};
        assertThat("Expect non-null value", testCase.getMessage(config),
                   is(testCase.toString() + ": " + MessageFormat.format(bundle.getString(testCase.name()), args)));
    }
}
