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
import com.espirit.moddev.serverrunner.ServerProperties.ServerPropertiesBuilder;
import com.espirit.moddev.serverrunner.ServerRunner;
import com.espirit.moddev.serverrunner.ServerType;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.help.Examples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static java.time.temporal.ChronoUnit.SECONDS;

/**
 *  This Command can start a FirstSpirit server. Uses ServerRunner implementations to achieve this.
 *  It makes use of its command arguments to decide which server to start.
 *
 * @author e-Spirit AG
 */
@Command(name = "start", groupNames = "server", description = "Starts a FirstSpirit server. You have to provide at least the fs-server.jar / fs-isolated-server.jar and the wrapper jar, in order to boot a server."+
        "WARNING: If you execute commands asynchronously, you may end up in unpredictable behavior.")
@Examples(examples =
        {
                "server start -sid \"D:\\FirstSpirit5.2.717\"",
                "server start -sid \"D:\\FirstSpirit5.2.717\" -h localhost -p 9000",
                "server start -sj \"D:\\FirstSpirit5.2.717\\server\\lib\\fs-server.jar\" -wj \"D:\\FirstSpirit5.2.717\\server\\lib\\wrapper.jar\" -sr \"D:\\temp\\FirstSpirit\""
        },
        descriptions = {
                "Simply starts the server in the given path - uses the installation dir as working dir and to search for necessary artifacts.",
                "Simply starts the server in the given path - uses the installation dir as working dir and configures the server to use port 9000.",
                "Starts a server with temp as the working directory. Uses artifacts from the specified installation folder."
        })
@SuppressWarnings("squid:S1200")
public class ServerStartCommand extends AbstractServerCommand implements com.espirit.moddev.cli.api.command.Command<SimpleResult<String>> {

    public static final int DEFAULT_POLLING_INTERVALL = 2;
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerStartCommand.class);

    @Option(name = {"-sid", "--server-installation-directory"}, description = "A FirstSpirit server's installation directory. " +
            "If configured, serverJar and wrapperJar are being searched in this directory and their options are ignored. " +
            "The installation directory will also be used as the working directory, and the serverRoot option is ignored. WARNING: Don't " +
            "use this property in conjunction with a server, that has been bootstrapped with -sj and -wj properties, because " +
            "those jar files would then be searched in this installation directory (where the files aren't placed).")
    private String serverInstallationDirectory;

    @Option(name = {"-sj", "--server-jar"}, description = "The path to a FirstSpirit server's fs-server.jar.")
    private String serverJar;

    @Option(name = {"-wj", "--wrapper-jar"}, description = "The path to a FirstSpirit server's wrapper.jar.")
    private String wrapperJar;

    @Option(name = {"-lf", "--license-file"}, description = "The path to a FirstSpirit server license file")
    private String licenseFilePath;

    @Option(name = {"-wt", "--wait-time"}, description = "The time in seconds to wait for a successful connection." +
            "The default is 120 seconds if the working directory exists already, otherwise 60 seconds.")
    private long waitTimeInSeconds = -1;

    // 60 seconds is experiential the time a FirstSpirit server needs for a boot process if the working dir is already created
    private static final long WAIT_IN_MS_IF_NO_INSTALL_NEEDED = 60;
    // 120 seconds is experiential the time a FirstSpirit server needs for a completely new boot process
    private static final long WAIT_IN_MS_IF_NEEDS_INSTALL = 120;

    @Override
    public SimpleResult<String> call() throws Exception {
        final ServerProperties serverProperties = getServerProperties();
        final ServerRunner runner = createRunner(serverProperties);

        if(LOGGER.isInfoEnabled()) {
            LOGGER.info(String.format("Starting server on %s:%d with working dir %s and waiting %d seconds for startup...", getHost(), getPort(), getServerRoot(), getActualWaitTimeInSeconds(serverProperties.isServerInstall())));
        }

        final boolean started = runner.start();
        if(started) {
            return new SimpleResult<>("The server has been started.");
        } else {
            return new SimpleResult(new IllegalStateException("The server couldn't be started or it takes some more time (use --wait-time parameter)."));
        }
    }

    protected ServerRunner createRunner(final ServerProperties serverProperties) {
        return new NativeServerRunner(serverProperties);
    }

    protected ServerProperties getServerProperties() {
        final File serverRootFile = figureOutServerRootDirectory();

        final File[] filesInRootFolder = serverRootFile.listFiles();
        final boolean rootFolderIsEmpty = filesInRootFolder == null || filesInRootFolder.length == 0;
        final boolean needServerInstall = !serverRootFile.exists() || rootFolderIsEmpty;
        
        final long actualWaitTimeInSeconds = getActualWaitTimeInSeconds(needServerInstall);
        
        final ServerPropertiesBuilder serverPropertiesBuilder = ServerProperties.builder()
                .serverHost(getHost())
                .httpPort(getPort())
                .serverAdminPw(getPassword())
                .serverRoot(serverRootFile.toPath())
                // don't install if directory exists
                .serverInstall(needServerInstall)
                .retryCount((int) (actualWaitTimeInSeconds / DEFAULT_POLLING_INTERVALL))
                .threadWait(Duration.of(DEFAULT_POLLING_INTERVALL, SECONDS));

        if(licenseFilePath != null) {
            serverPropertiesBuilder.licenseFileSupplier(getLicenseFileSupplier());
        }

        addServerJarsToBuilder(serverPropertiesBuilder);

        return serverPropertiesBuilder.build();
    }

    private long getActualWaitTimeInSeconds(boolean needServerInstall) {
        long targetDefaultWaitTimeInSeconds = needServerInstall ? WAIT_IN_MS_IF_NEEDS_INSTALL : WAIT_IN_MS_IF_NO_INSTALL_NEEDED;
        boolean waitTimeSpecifiedExplicitely = waitTimeInSeconds > 0;
        return waitTimeSpecifiedExplicitely ? waitTimeInSeconds : targetDefaultWaitTimeInSeconds;
    }

    private File figureOutServerRootDirectory() {
        if(useServerInstallationDirectory()) {
            LOGGER.info("Server installation directory given, so it is used as the servers root directory.");
            return new File(serverInstallationDirectory);
        } else {
            return new File(getServerRoot());
        }
    }

    private void addServerJarsToBuilder(ServerPropertiesBuilder serverPropertiesBuilder) {
        if(useServerInstallationDirectory()) {
            LOGGER.info("Server installation directory given: {}", serverInstallationDirectory);
            Path serverInstallationDir = Paths.get(serverInstallationDirectory);

            Optional<List<File>> jars = Arrays.stream(ServerType.values())
                .map(serverType -> serverType.resolveJars(serverInstallationDir))
                .filter(jarsByServerType -> jarsByServerType.stream().allMatch(File::exists))
                .findFirst();

            if (jars.isPresent()) {
                LOGGER.info("Server and wrapper jar found in server installation directory.");
                serverPropertiesBuilder.firstSpiritJars(jars.get());
            } else {
                LOGGER.warn("Server and/or wrapper jar couldn't be retrieved from the given server installation directory. Fallback to jar parameters.");
                addServerJarsFromOptionsOrClasspath(serverPropertiesBuilder);
            }
        } else {
            addServerJarsFromOptionsOrClasspath(serverPropertiesBuilder);
        }
    }

    private void addServerJarsFromOptionsOrClasspath(ServerPropertiesBuilder serverPropertiesBuilder) {
        if(this.serverJar != null && this.wrapperJar != null) {
            serverPropertiesBuilder.firstSpiritJar(new File(this.serverJar)).firstSpiritJar(new File(this.wrapperJar));
        } else {
            List<File> jars = ServerProperties.getFirstSpiritJarsFromClasspath();
            if(!jars.isEmpty()) {
                serverPropertiesBuilder.firstSpiritJars(jars);
            } else {
                throw new IllegalStateException("Server and/or wrapper jar couldn't be retrieved from classpath.");
            }
        }
    }

    private boolean useServerInstallationDirectory() {
        return serverInstallationDirectory != null;
    }

    private Supplier<Optional<InputStream>> getLicenseFileSupplier() {
        return () -> {
            File licenseFile = new File(licenseFilePath);
            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(licenseFile);
            } catch (FileNotFoundException e) {
                LOGGER.error("License file couldn't be found", e);
            }
            return Optional.ofNullable(inputStream);
        };
    }

    public String getServerJar() {
        return serverJar;
    }

    public void setServerJar(String serverJar) {
        this.serverJar = serverJar;
    }

    public String getWrapperJar() {
        return wrapperJar;
    }

    public void setWrapperJar(String wrapperJar) {
        this.wrapperJar = wrapperJar;
    }

    public long getWaitTimeInSeconds() {
        return waitTimeInSeconds;
    }

    public void setWaitTimeInSeconds(long waitTimeInSeconds) {
        this.waitTimeInSeconds = waitTimeInSeconds;
    }
}
