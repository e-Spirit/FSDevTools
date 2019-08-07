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

package com.espirit.moddev.cli.commands.server;

import com.espirit.moddev.cli.CliConstants;
import com.espirit.moddev.cli.api.FsConnectionMode;
import com.espirit.moddev.serverrunner.ServerProperties;
import com.espirit.moddev.shared.annotation.VisibleForTesting;
import com.github.rvesse.airline.annotations.Option;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Paths;

public abstract class AbstractServerCommand {
    @Option(name = {"-h", "--host"}, description = "The hostname to use for the FirstSpirit server. The default is 'localhost'.")
    private String _host = CliConstants.DEFAULT_HOST.value();

    @Option(name = {"-p", "-port", "--port"}, description = "The HTTP port to use for the FirstSpirit server. The default is '8000'.")
    private Integer _port = FsConnectionMode.Constants.DEFAULT_HTTP_PORT;

    @Option(name = {"-pw", "-password"}, description = "The admin password to be used. The default is 'Admin'.")
    @SuppressWarnings("squid:S2068")
    private String _password = CliConstants.DEFAULT_USER.value();

    @Option(
        name = {"-sr", "--server-root"},
        description = "The FirstSpirit server's working directory root. The default is 'user.home/opt/FirstSpirit'. "+
            "Caution: If you use this property in conjunction with a different server, that has been connected via -h or -p properties, "+
            "you may have to wait for events form the wrong server."
    )
    private String _serverRoot = Paths.get(System.getProperty("user.home"), "opt", "FirstSpirit").toString();
    private ServerProperties.ConnectionMode _connectionMode = ServerProperties.ConnectionMode.HTTP_MODE;

    public String getServerRoot() {
        return _serverRoot;
    }

    public void setServerRoot(final String serverRoot) {
        _serverRoot = serverRoot;
    }

    public String getHost() {
        return _host;
    }

    public void setHost(final String host) {
        _host = host;
    }

    public Integer getPort() {
        return _port;
    }

    public void setPort(final Integer port) {
        _port = port;
    }

    public String getPassword() {
        return _password;
    }

    public void setPassword(final String password) {
        _password = password;
    }

    /**
     * Initializes this class's fields based on the given {@link ServerProperties}
     *
     * @param serverProperties the configuration to source from
     */
    public void initializeFromProperties(@NotNull final ServerProperties serverProperties) {
        setServerRoot(serverProperties.getServerRoot().toFile().getAbsolutePath());
        setConnectionMode(serverProperties.getMode());
        setHost(serverProperties.getServerHost());
        setPort(serverProperties.getMode() == ServerProperties.ConnectionMode.SOCKET_MODE ? serverProperties.getSocketPort() : serverProperties.getHttpPort());
        setPassword(serverProperties.getServerAdminPw());
    }

    @NotNull
    public ServerProperties.ConnectionMode getConnectionMode() {
        return _connectionMode;
    }

    @VisibleForTesting
    public void setConnectionMode(@NotNull final ServerProperties.ConnectionMode mode) {
        _connectionMode = mode;
    }

}
