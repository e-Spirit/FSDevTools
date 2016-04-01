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

package com.espirit.moddev.cli.configuration;

import com.espirit.moddev.cli.Environment;
import com.espirit.moddev.cli.SyncDirectoryFactory;
import com.espirit.moddev.cli.api.FsConnectionMode;
import com.espirit.moddev.cli.api.FullQualifiedUid;
import com.espirit.moddev.cli.api.configuration.Config;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.restrictions.AllowedRawValues;
import com.github.rvesse.airline.annotations.restrictions.Port;
import com.github.rvesse.airline.annotations.restrictions.PortType;
import de.espirit.firstspirit.access.project.ProjectScriptContext;
import de.espirit.firstspirit.io.FileHandle;
import de.espirit.firstspirit.io.FileSystem;
import de.espirit.firstspirit.io.FileSystemsAgent;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A configuration class for a common configuration in a FirstSpirit environment.
 * Some getters return the value of a corresponding environment variable, if the provided
 * value is empty or no value is provided at all.
 *
 * @author e-Spirit AG
 */
public class GlobalConfig implements Config {

    private final Environment environment = new Environment();

    @Option(type = OptionType.GLOBAL, name = "-e", description = "Error mode. Shows error stacktraces.")
    private boolean error;

    @Option(type = OptionType.GLOBAL, name = {"-h", "--host"}, description = "FirstSpirit host. Default is localhost.")
    private String host;

    @Option(type = OptionType.GLOBAL, name = {"-c", "--conn-mode"}, description = "FirstSpirit connection mode. Default is HTTP.")
    @AllowedRawValues(allowedValues = {"HTTP", "SOCKET"})
    private FsConnectionMode fsMode;

    @Option(type = OptionType.GLOBAL, name = {"-port"}, description = "FirstSpirit host's port. Default is 8000.")
    @Port(acceptablePorts = {PortType.SYSTEM, PortType.USER})
    private Integer port;

    @Option(type = OptionType.GLOBAL, name = {"-u", "--user"}, description = "FirstSpirit user. Default is Admin.")
    private String user;

    @Option(type = OptionType.GLOBAL, name = {"-pwd", "--password"}, description = "FirstSpirit user's password. Default is Admin.")
    private String password;

    @Option(type = OptionType.GLOBAL, name = {"-p", "--project"}, description = "Name of FirstSpirit project")
    private String project;

    @Option(type = OptionType.GLOBAL, name = {"-a", "--activateProjectIfDeactivated"}, description = "Activates a project if deactivated for any reason")
    private boolean activateProjectIfDeactivated;

    @Option(type = OptionType.GLOBAL, name = {"-sd", "--syncDir"}, description = "The synchronization directory that is used for im- and export. Default is current directory")
    private String synchronizationDirectory = ".";

    @Option(type = OptionType.GLOBAL, name = {"--dont-create-sync-dir"}, description = "Do not create synchronisation directory if it is missing")
    protected boolean dontCreateSynchronizationDirexctoryIfMissing;

    @Arguments(title = "arguments", description = "An arbitrary number of arguments. Each command can have its own, special arguments.")
    private List<String> args = new LinkedList<>();

    protected ProjectScriptContext context;
    @Override
    public final void setContext(ProjectScriptContext context) {
        if(context == null) {
            throw new IllegalArgumentException("Context should not be null");
        }
        this.context = context;
    }

    public ProjectScriptContext getContext() {
        return context;
    }

    /**
     * Shows if it is in error mode.
     *
     * @return the boolean
     */
    public boolean isError() {
        return error;
    }

    /**
     * Gets environment. Needs to be protected so it can be accessed in tests.
     *
     * @return the environment
     */
    public Environment getEnvironment() {
        return environment;
    }

    @Override
    public String getHost() {
        if(host == null || host.isEmpty()) {
            boolean environmentContainsHost = getEnvironment().containsKey(CliConstants.KEY_FS_HOST.value());
            if(environmentContainsHost) {
                return getEnvironment().get(CliConstants.KEY_FS_HOST.value()).trim();
            }
            return CliConstants.DEFAULT_HOST.value();
        }
        return host;
    }

    @Override
    public Integer getPort() {
        if(port == null) {
            boolean environmentContainsPort = getEnvironment().containsKey(CliConstants.KEY_FS_PORT.value());
            if(environmentContainsPort) {
                return Integer.valueOf(getEnvironment().get(CliConstants.KEY_FS_PORT.value()).trim());
            }
            return getConnectionMode().getDefaultPort();
        }
        return port;
    }

    @Override
    public FsConnectionMode getConnectionMode() {
        if(fsMode == null) {
            boolean environmentContainsPort = getEnvironment().containsKey(CliConstants.KEY_FS_MODE.value());
            if(environmentContainsPort) {
                return FsConnectionMode.valueOf(getEnvironment().get(CliConstants.KEY_FS_MODE.value()).trim().toUpperCase());
            }
            return FsConnectionMode.valueOf(CliConstants.DEFAULT_CONNECTION_MODE.value());
        }
        return fsMode;
    }

    @Override
    public String getUser() {
        if(user == null || user.isEmpty()) {
            boolean environmentContainsUser = getEnvironment().containsKey(CliConstants.KEY_FS_USER.value());
            if(environmentContainsUser) {
                return getEnvironment().get(CliConstants.KEY_FS_USER.value()).trim();
            }
            return CliConstants.DEFAULT_USER.value();
        }
        return user;
    }

    @Override
    public String getPassword() {
        if(password == null || password.isEmpty()) {
            boolean environmentContainsPassword = getEnvironment().containsKey(CliConstants.KEY_FS_PASSWORD.value());
            if(environmentContainsPassword) {
                return getEnvironment().get(CliConstants.KEY_FS_PASSWORD.value()).trim();
            }
            return CliConstants.DEFAULT_USER.value();
        }
        return password;
    }

    @Override
    public String getProject() {
        if(project == null || project.isEmpty()) {
            boolean environmentContainsProject = getEnvironment().containsKey(CliConstants.KEY_FS_PROJECT.value());
            if(environmentContainsProject) {
                return getEnvironment().get(CliConstants.KEY_FS_PROJECT.value()).trim();
            }
            return CliConstants.DEFAULT_PROJECT.value();
        }
        return project;
    }

    @Override
    public boolean isActivateProjectIfDeactivated() {
        return activateProjectIfDeactivated;
    }

    @Override
    public String getSynchronizationDirectoryString() {
        return synchronizationDirectory;
    }

    @Override
    public <F extends FileHandle> FileSystem<F> getSynchronizationDirectory() {
        final String syncDirStr = getSynchronizationDirectoryString();

        SyncDirectoryFactory syncDirectoryFactory = new SyncDirectoryFactory(this);
        syncDirectoryFactory.checkAndCreateSyncDirIfNeeded(syncDirStr);

        final FileSystemsAgent fileSystemsAgent = context.requireSpecialist(FileSystemsAgent.TYPE);
        return (FileSystem<F>) fileSystemsAgent.getOSFileSystem(syncDirStr);
    }

    @Override
    public boolean createSynchronizationDirectoryIfMissing() {
        return !dontCreateSynchronizationDirexctoryIfMissing;
    }

    @Override
    public List<FullQualifiedUid> getFullQualifiedUids() {
        if (args.size() > 0) {
            return FullQualifiedUid.parse(args);
        }
        return Collections.emptyList();
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public FsConnectionMode getFsMode() {
        return fsMode;
    }

    public void setFsMode(FsConnectionMode fsMode) {
        this.fsMode = fsMode;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public void setActivateProjectIfDeactivated(boolean activateProjectIfDeactivated) {
        this.activateProjectIfDeactivated = activateProjectIfDeactivated;
    }

    public void setSynchronizationDirectory(String synchronizationDirectory) {
        this.synchronizationDirectory = synchronizationDirectory;
    }
    public List<String> getArgs() {
        return args; //NOSONAR
    }

    public void setArgs(List<String> args) {
        this.args = args; //NOSONAR
    }
}
