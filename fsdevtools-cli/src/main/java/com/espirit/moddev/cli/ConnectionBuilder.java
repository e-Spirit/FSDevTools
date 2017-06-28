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
import com.espirit.moddev.cli.api.validation.Voilation;

import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.ConnectionManager;

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

    private final Config config;

    private ConnectionBuilder(final Config config){
        this.config = Objects.requireNonNull(config, "Config is null!");
    }

    /**
     * Creates a builder with a config.
     *
     * @param config the config
     * @return the connection builder
     */
    public static ConnectionBuilder with(Config config){
        return new ConnectionBuilder(config);
    }

    /**
     * Build a FirstSpirit connection based on the initial config which is checked first.
     *
     * @return the FirstSpirit connection
     */
    public Connection build(){

        checkConfig();

        final FsConnectionMode connectionMode = config.getConnectionMode();
        if(FsConnectionMode.HTTPS == connectionMode) {
            ConnectionManager.setUseHttps(true);
        } else {
            ConnectionManager.setUseHttps(false);
        }

        final String user = config.getUser();
        final Integer port = config.getPort();
        final String host = config.getHost();

        Object[] args = {host, port, user};
        LOGGER.debug("Create connection for FirstSpirit server at '{}:{}' with user '{}'...", args);

        return ConnectionManager.getConnection(host, port, connectionMode.getCode(), user, config.getPassword());
    }

    private void checkConfig() {
        DefaultConnectionConfigValidator validator = new DefaultConnectionConfigValidator();
        final Set<Voilation> voilations = validator.validate(config);

        if(!voilations.isEmpty()){
            StringBuilder errorMessage = new StringBuilder("The configuration is invalid:");
            errorMessage.append(System.lineSeparator());
            for (Voilation voilation : voilations) {
                errorMessage.append(voilation.toString())
                    .append(System.lineSeparator());
            }
            final String message = errorMessage.toString();

            throw new IllegalStateException(message);
        }
    }

}
