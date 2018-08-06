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

import com.espirit.moddev.serverrunner.ServerProperties;
import com.github.rvesse.airline.annotations.Option;
import java.nio.file.Paths;

public abstract class AbstractServerCommand {
    @Option(name = {"-h", "--host"}, description = "The hostname to use for the FirstSpirit server. The default is 'localhost'.")
    private String host = "localhost";
    @Option(name = {"-p", "-port", "--port"}, description = "The HTTP port to use for the FirstSpirit server. The default is '8000'.")
    private Integer port = 8000;
    @Option(name = {"-pw", "-password"}, description = "The admin password to be used. The default is 'Admin'.")
    @SuppressWarnings("squid:S2068")
    private String password = "Admin";
    @Option(
        name = {"-sr", "--server-root"}, 
        description = "The FirstSpirit server's working directory root. The default is 'user.home/opt/FirstSpirit'. "+
            "Caution: If you use this property in conjunction with a different server, that has been connected via -h or -p properties, "+
            "you may have to wait for events form the wrong server."
    )
    private String serverRoot = Paths.get(System.getProperty("user.home"), "opt", "FirstSpirit").toString();

    public String getServerRoot() {
        return serverRoot;
    }
    
    public void setServerRoot(final String serverRoot) {
        this.serverRoot = serverRoot;
    }
    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(final Integer port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * Initializes this class's fields based on the given {@link ServerProperties}
     * @param serverProperties the configuration to source from
     */
    public void initializeFromProperties(final ServerProperties serverProperties) {
        setHost(serverProperties.getServerHost());
        setPort(serverProperties.getHttpPort());
        setPassword(serverProperties.getServerAdminPw());
    }

}
