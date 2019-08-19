package com.espirit.moddev.cli.api.validation;

/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2016 e-Spirit AG
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

import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultConnectionConfigValidatorTest {

    private DefaultConnectionConfigValidator _testling;
    private Config _config;

    @Before
    public void setUp() throws Exception {
        _testling = new DefaultConnectionConfigValidator();
        _config = mock(Config.class);
    }

    @Test
    public void validate() throws Exception {
        when(_config.getHost()).thenReturn("Localhost");
        when(_config.getUser()).thenReturn("Admin");
        when(_config.getPassword()).thenReturn("Admin");
        when(_config.getPort()).thenReturn(FsConnectionMode.HTTP.getDefaultPort());
        when(_config.getConnectionMode()).thenReturn(FsConnectionMode.HTTP);
        final Set<Violation> violation = _testling.validate(_config);

        assertThat("Expect no violation", violation, is(empty()));
    }

    @Test
    public void validateHost() throws Exception {
        when(_config.getHost()).thenReturn("");
        when(_config.getUser()).thenReturn("Admin");
        when(_config.getPassword()).thenReturn("Admin");
        when(_config.getPort()).thenReturn(FsConnectionMode.HTTP.getDefaultPort());
        when(_config.getConnectionMode()).thenReturn(FsConnectionMode.HTTP);

        final Set<Violation> violation = _testling.validate(_config);

        assertThat("Expect one voilation", violation, hasSize(1));
        assertThat("Expect a specific voilation", violation, contains(new Violation("host", "is null or blank")));
    }

    @Test
    public void validateUser() throws Exception {
        when(_config.getHost()).thenReturn("Localhost");
        when(_config.getUser()).thenReturn(" ");
        when(_config.getPassword()).thenReturn("Admin");
        when(_config.getPort()).thenReturn(FsConnectionMode.HTTP.getDefaultPort());
        when(_config.getConnectionMode()).thenReturn(FsConnectionMode.HTTP);

        final Set<Violation> violation = _testling.validate(_config);

        assertThat("Expect one voilation", violation, hasSize(1));
        assertThat("Expect a specific voilation", violation, contains(new Violation("user", "is null or blank")));
    }

    @Test
    public void validatePassword() throws Exception {
        when(_config.getHost()).thenReturn("Localhost");
        when(_config.getUser()).thenReturn("Admin");
        when(_config.getPassword()).thenReturn("\t");
        when(_config.getPort()).thenReturn(FsConnectionMode.HTTP.getDefaultPort());
        when(_config.getConnectionMode()).thenReturn(FsConnectionMode.HTTP);

        final Set<Violation> violation = _testling.validate(_config);

        assertThat("Expect one voilation", violation, hasSize(1));
        assertThat("Expect a specific voilation", violation, contains(new Violation("password", "is null or blank")));
    }

    @Test
    public void validatePort() throws Exception {
        when(_config.getHost()).thenReturn("Localhost");
        when(_config.getUser()).thenReturn("Admin");
        when(_config.getPassword()).thenReturn("Admin");
        when(_config.getPort()).thenReturn(null);
        when(_config.getConnectionMode()).thenReturn(FsConnectionMode.HTTP);

        final Set<Violation> violation = _testling.validate(_config);

        assertThat("Expect one voilation", violation, hasSize(1));
        assertThat("Expect a specific voilation", violation, contains(new Violation("port", "is null")));
    }

    @Test
    public void validateConnectionMode() throws Exception {
        when(_config.getHost()).thenReturn("Localhost");
        when(_config.getUser()).thenReturn("Admin");
        when(_config.getPassword()).thenReturn("Admin");
        when(_config.getPort()).thenReturn(FsConnectionMode.HTTP.getDefaultPort());
        when(_config.getConnectionMode()).thenReturn(null);

        final Set<Violation> violation = _testling.validate(_config);

        assertThat("Expect one voilation", violation, hasSize(1));
        assertThat("Expect a specific voilation", violation, contains(new Violation("connectionMode", "is null")));
    }

}
