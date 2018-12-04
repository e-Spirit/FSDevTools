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

    private DefaultConnectionConfigValidator testling;
    private Config config;

    @Before
    public void setUp() throws Exception {
        testling = new DefaultConnectionConfigValidator();
        config = mock(Config.class);
    }

    @Test
    public void validate() throws Exception {
        when(config.getHost()).thenReturn("Localhost");
        when(config.getUser()).thenReturn("Admin");
        when(config.getPassword()).thenReturn("Admin");
        when(config.getPort()).thenReturn(FsConnectionMode.HTTP.getDefaultPort());
        when(config.getConnectionMode()).thenReturn(FsConnectionMode.HTTP);
        final Set<Voilation> voilations = testling.validate(config);

        assertThat("Expect no voilations", voilations, is(empty()));
    }

    @Test
    public void validateHost() throws Exception {
        when(config.getHost()).thenReturn("");
        when(config.getUser()).thenReturn("Admin");
        when(config.getPassword()).thenReturn("Admin");
        when(config.getPort()).thenReturn(FsConnectionMode.HTTP.getDefaultPort());
        when(config.getConnectionMode()).thenReturn(FsConnectionMode.HTTP);

        final Set<Voilation> voilations = testling.validate(config);

        assertThat("Expect one voilation", voilations, hasSize(1));
        assertThat("Expect a specific voilation", voilations, contains(new Voilation("host", "is null or blank")));
    }

    @Test
    public void validateUser() throws Exception {
        when(config.getHost()).thenReturn("Localhost");
        when(config.getUser()).thenReturn(" ");
        when(config.getPassword()).thenReturn("Admin");
        when(config.getPort()).thenReturn(FsConnectionMode.HTTP.getDefaultPort());
        when(config.getConnectionMode()).thenReturn(FsConnectionMode.HTTP);

        final Set<Voilation> voilations = testling.validate(config);

        assertThat("Expect one voilation", voilations, hasSize(1));
        assertThat("Expect a specific voilation", voilations, contains(new Voilation("user", "is null or blank")));
    }

    @Test
    public void validatePassword() throws Exception {
        when(config.getHost()).thenReturn("Localhost");
        when(config.getUser()).thenReturn("Admin");
        when(config.getPassword()).thenReturn("\t");
        when(config.getPort()).thenReturn(FsConnectionMode.HTTP.getDefaultPort());
        when(config.getConnectionMode()).thenReturn(FsConnectionMode.HTTP);

        final Set<Voilation> voilations = testling.validate(config);

        assertThat("Expect one voilation", voilations, hasSize(1));
        assertThat("Expect a specific voilation", voilations, contains(new Voilation("password", "is null or blank")));
    }

    @Test
    public void validatePort() throws Exception {
        when(config.getHost()).thenReturn("Localhost");
        when(config.getUser()).thenReturn("Admin");
        when(config.getPassword()).thenReturn("Admin");
        when(config.getPort()).thenReturn(null);
        when(config.getConnectionMode()).thenReturn(FsConnectionMode.HTTP);

        final Set<Voilation> voilations = testling.validate(config);

        assertThat("Expect one voilation", voilations, hasSize(1));
        assertThat("Expect a specific voilation", voilations, contains(new Voilation("port", "is null")));
    }

    @Test
    public void validateConnectionMode() throws Exception {
        when(config.getHost()).thenReturn("Localhost");
        when(config.getUser()).thenReturn("Admin");
        when(config.getPassword()).thenReturn("Admin");
        when(config.getPort()).thenReturn(FsConnectionMode.HTTP.getDefaultPort());
        when(config.getConnectionMode()).thenReturn(null);

        final Set<Voilation> voilations = testling.validate(config);

        assertThat("Expect one voilation", voilations, hasSize(1));
        assertThat("Expect a specific voilation", voilations, contains(new Voilation("connectionMode", "is null")));
    }

}
