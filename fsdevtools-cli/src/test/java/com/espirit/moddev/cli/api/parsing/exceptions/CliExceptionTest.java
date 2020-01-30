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

import com.espirit.moddev.cli.api.configuration.Config;
import com.espirit.moddev.cli.exception.CliError;
import com.espirit.moddev.cli.exception.CliException;
import com.espirit.moddev.connection.FsConnectionType;
import com.espirit.moddev.util.FsUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author e-Spirit AG
 */
@RunWith(Parameterized.class)
public class CliExceptionTest {

    private Config config;
    private Exception cause;

    @Parameterized.Parameters
    public static Collection<CliError> provideErrors() {
        final Collection<CliError> list = Arrays.asList(CliError.values());
        return list;
    }

    private CliException testling;
    private CliError error;

    public CliExceptionTest(final CliError error) {
        this.error = error;
    }

    @Before
    public void setUp() throws Exception {
        config = mock(Config.class);
        when(config.getUser()).thenReturn(FsUtil.VALUE_DEFAULT_USER);
        when(config.getHost()).thenReturn(FsUtil.VALUE_DEFAULT_HOST);
        when(config.getPort()).thenReturn(FsConnectionType.HTTP.getDefaultPort());
        cause = new Exception("JUnit");
        testling = new CliException(error, config, cause);
    }

    @Test
    public void testToString() throws Exception {
        assertThat("Expected a specific value", testling.toString(), is(error.getMessage(config)));
    }

    @Test
    public void testToStringWithException() throws Exception {
        testling = new CliException(cause);
        assertThat("Expected a specific value", testling.toString(), is("JUnit"));
    }
}
