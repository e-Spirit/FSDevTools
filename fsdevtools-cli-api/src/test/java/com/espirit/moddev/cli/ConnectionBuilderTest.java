package com.espirit.moddev.cli;

/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2021 e-Spirit GmbH
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

import com.espirit.moddev.cli.api.configuration.Config;
import com.espirit.moddev.connection.FsConnectionType;
import com.espirit.moddev.util.FsUtil;
import de.espirit.firstspirit.access.Connection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConnectionBuilderTest {

    private ConnectionBuilder testling;
    private Config config;

    @BeforeEach
    public void setUp() throws Exception {
        config = mock(Config.class);
        testling = ConnectionBuilder.with(config);
    }

    @ParameterizedTest
    @EnumSource(FsConnectionType.class)
    public void testBuild(final FsConnectionType mode) {
        final String customServletZone = "/customServletZone";
        when(config.getHost()).thenReturn(FsUtil.VALUE_DEFAULT_HOST);
        when(config.getHttpProxyHost()).thenReturn("");
        when(config.getPort()).thenReturn(mode.getDefaultPort());
        when(config.getHttpProxyPort()).thenReturn(8080);
        when(config.getConnectionMode()).thenReturn(mode);
        when(config.getUser()).thenReturn(FsUtil.VALUE_DEFAULT_USER);
        when(config.getPassword()).thenReturn(FsUtil.VALUE_DEFAULT_USER);
        when(config.getPassword()).thenReturn(FsUtil.VALUE_DEFAULT_USER);
        when(config.getServletZone()).thenReturn(customServletZone);

        final Connection connection = testling.build();

        assertThat(connection.getHost(), is(FsUtil.VALUE_DEFAULT_HOST));
        assertThat(connection.getPort(), is(mode.getDefaultPort()));
        assertThat(connection.getServletZone(), is(customServletZone));
    }

    @Test
    public void testWithNull() {
        Assertions.assertThrows(NullPointerException.class, () -> ConnectionBuilder.with(null));
    }

}
