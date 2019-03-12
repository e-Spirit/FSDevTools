package com.espirit.moddev.cli;

/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2019 e-Spirit AG
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License"),
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

import com.espirit.moddev.cli.api.FsConnectionMode;
import com.espirit.moddev.cli.api.configuration.Config;
import de.espirit.firstspirit.access.Connection;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Theories.class)
public class ConnectionBuilderTest {

    @DataPoints
    public static FsConnectionMode[] testCases = FsConnectionMode.values();

    private ConnectionBuilder testling;
    private Config config;

    @Before
    public void setUp() throws Exception {
        config = mock(Config.class);
        testling = ConnectionBuilder.with(config);
    }

    @Theory
    public void testBuild(final FsConnectionMode mode) throws Exception {

        when(config.getHost()).thenReturn("localhost");
        when(config.getPort()).thenReturn(mode.getDefaultPort());
        when(config.getConnectionMode()).thenReturn(mode);
        when(config.getUser()).thenReturn("Admin");
        when(config.getPassword()).thenReturn("Admin");

        final Connection connection = testling.build();

        assertThat(connection.getHost(), is("localhost"));
        assertThat(connection.getPort(), is(mode.getDefaultPort()));
    }

    @Test(expected = NullPointerException.class)
    public void testWithNull() throws Exception {
        ConnectionBuilder.with(null);
    }
}
