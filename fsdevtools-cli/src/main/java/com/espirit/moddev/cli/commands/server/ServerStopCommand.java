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

import com.espirit.moddev.cli.results.SimpleResult;
import com.espirit.moddev.serverrunner.NativeServerRunner;
import com.espirit.moddev.serverrunner.ServerProperties;
import com.espirit.moddev.serverrunner.ServerRunner;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.help.Examples;
import java.io.File;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(name = "stop", groupNames = "server", description = "Stops a FirstSpirit server. Needs an fs-access.jar on the classpath. "+
        "WARNING: If you execute commands asynchronously, you may end up in unpredictable behavior.")
@Examples(examples =
        {
                "server stop",
                "server stop -h localhost -p 9000",
                "server stop -sr \"D:\\FirstSpirit5.2.717\""
        },
        descriptions = {
                "Simply stops the server running on localhost:8000.",
                "Simply stops the server running on localhost:9000.",
                "Blocks the fs-cli until the server at the server-root has been shut down."
        })
/**
 * 
 */
public class ServerStopCommand extends AbstractServerCommand implements com.espirit.moddev.cli.api.command.Command<SimpleResult<Boolean>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerStopCommand.class);

    @Override
    public SimpleResult<Boolean> call() {
        try {
            final Path serverRootPath = getServerRootPath(new File(getServerRoot()));
        
            final ServerProperties serverProperties = ServerProperties.builder()
                .serverHost(getHost())
                .serverPort(getPort())
                .serverAdminPw(getPassword())
                .firstSpiritJar(ServerProperties.getAccessJarFileFromClasspath().get())
                .serverRoot(serverRootPath)
                    .build();
            final ServerRunner serverRunner = createRunner(serverProperties);
            final boolean stopped = serverRunner.stop();
            LOGGER.info(stopped ? "Server stopped" : "Server couldn't be stopped!");
            return new SimpleResult<>(stopped);
        } catch (final IllegalArgumentException e) {
            return new SimpleResult<>(e);
        }
    }
    
    protected ServerRunner createRunner(final ServerProperties serverProperties) {
        return new NativeServerRunner(serverProperties);
    }

    
    private Path getServerRootPath(File serverRootFile) throws IllegalArgumentException {
        if (serverRootFile.exists()) {
            LOGGER.info("Server root is at \""+ serverRootFile.getAbsolutePath() +"\"");
            return serverRootFile.toPath();
        } else {
            LOGGER.info("Server root is not set correctly. The server will stop but the \"isStopped\"-state may be displayed to early!");
            throw new IllegalArgumentException("Server root is not set correctly." + serverRootFile.toURI());
        }
    }

}
