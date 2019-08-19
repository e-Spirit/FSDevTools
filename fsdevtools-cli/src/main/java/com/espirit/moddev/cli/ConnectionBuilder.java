package com.espirit.moddev.cli;

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
import com.espirit.moddev.cli.api.validation.DefaultConnectionConfigValidator;
import com.espirit.moddev.cli.api.validation.Violation;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.ConnectionManager;
import de.espirit.firstspirit.access.Proxy;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;

/**
 * Default builder for FirstSpirit {@link Connection}s.
 *
 * @author e -Spirit AG
 */
public class ConnectionBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionBuilder.class);

    private final Config _config;

    private ConnectionBuilder(@NotNull final Config config){
        _config = Objects.requireNonNull(config, "Config is null!");
    }

    /**
     * Creates a builder with a config.
     *
     * @param config the config
     * @return the connection builder
     */
    @NotNull
    public static ConnectionBuilder with(@NotNull final Config config){
        return new ConnectionBuilder(config);
    }

    /**
     * Build a FirstSpirit connection based on the initial config which is checked first.
     *
     * @return the FirstSpirit connection
     */
    @NotNull
    public Connection build(){
        // validate configuration
        validateConfiguration();

        // use https, if needed
        final FsConnectionMode connectionMode = _config.getConnectionMode();
        if(FsConnectionMode.HTTPS == connectionMode) {
            ConnectionManager.setUseHttps(true);
        } else {
            ConnectionManager.setUseHttps(false);
        }

        // if set: use proxy for http / https
        if (!_config.getHttpProxyHost().isEmpty()) {
            if (_config.getConnectionMode() == FsConnectionMode.HTTP || _config.getConnectionMode() == FsConnectionMode.HTTPS) {
                LOGGER.info("Using http proxy '{}:{}'", _config.getHttpProxyHost(), _config.getHttpProxyPort());
                ConnectionManager.setProxy(new Proxy(_config.getHttpProxyHost(), _config.getHttpProxyPort()));
            }
        }

        // setup user, host & port
        final String user = _config.getUser();
        final Integer port = _config.getPort();
        final String host = _config.getHost();

        // logging
        Object[] args = {host, port, user};
        LOGGER.debug("Create connection for FirstSpirit server at '{}:{}' with user '{}'...", args);

        // create connection
        return ConnectionManager.getConnection(host, port, connectionMode.getCode(), user, _config.getPassword());
    }

    private void validateConfiguration() throws IllegalStateException {
        // validate configuration
        final DefaultConnectionConfigValidator validator = new DefaultConnectionConfigValidator();
        final Set<Violation> violations = validator.validate(_config);

        // violations found --> build error message
        if (!violations.isEmpty()) {
            // build message
            final StringBuilder errorMessage = new StringBuilder("The configuration is invalid:");
            errorMessage.append(System.lineSeparator());
            for (final Violation violation : violations) {
                errorMessage.append(violation.toString());
                errorMessage.append(System.lineSeparator());
            }

            // finally throw exception
            throw new IllegalStateException(errorMessage.toString());
        }
    }

}
