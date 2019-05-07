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

import com.espirit.moddev.cli.CliConstants;
import com.espirit.moddev.cli.SyncDirectoryFactory;
import com.espirit.moddev.cli.api.FsConnectionMode;
import com.espirit.moddev.cli.api.configuration.Config;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.restrictions.AllowedRawValues;

import de.espirit.firstspirit.access.project.ProjectScriptContext;
import de.espirit.firstspirit.io.FileHandle;
import de.espirit.firstspirit.io.FileSystem;
import de.espirit.firstspirit.io.FileSystemsAgent;

import java.util.Locale;

/**
 * A configuration class for a common configuration in a FirstSpirit environment.
 * Some getters return the value of a corresponding environment variable, if the provided
 * value is empty or no value is provided at all.
 *
 * @author e-Spirit AG
 */
public class GlobalConfig implements Config {

    /**
     * {@link de.espirit.firstspirit.access.project.ProjectScriptContext} used by this configuration.
     */
    protected ProjectScriptContext context;

    /**
     * Boolean flag that indicates if the synchronization directory should be created if it does not exist.
     */
    @Option(type = OptionType.GLOBAL, name = {"--dont-create-sync-dir"}, description = "Do not create synchronisation directory if it is missing")
    protected boolean dontCreateSynchronizationDirectoryIfMissing;

    private final Environment environment = new Environment();

    @Option(type = OptionType.GLOBAL, name = "-e", description = "Error mode. Shows error stacktraces.")
    private boolean error;

    @Option(type = OptionType.GLOBAL, name = {"-h", "--host"}, description = "FirstSpirit host. Default is localhost.")
    private String host;

    @Option(type = OptionType.GLOBAL, name = {"-c", "--conn-mode"}, description = "FirstSpirit connection mode. Default is HTTP.")
    @AllowedRawValues(allowedValues = {"HTTP", "HTTPS", "SOCKET"})
    private FsConnectionMode fsMode;

    @Option(type = OptionType.GLOBAL, name = {"-port"}, description = "FirstSpirit host's port. Default is 8000.")
    private Integer port;

    @Option(type = OptionType.GLOBAL, name = {"-ph", "--proxyhost"}, description = "Proxy host.")
    private String proxyHost;

    @Option(type = OptionType.GLOBAL, name = {"-proxyport"}, description = "Proxy host's port. Default is 8080.")
    private Integer proxyPort;

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

    public GlobalConfig() {
    }

    @Override
    public final void setContext(ProjectScriptContext context) {
        if(context == null) {
            throw new IllegalArgumentException("Context should not be null");
        }
        this.context = context;
    }

    /**
     * Get the {@link de.espirit.firstspirit.access.project.ProjectScriptContext} used by this configuration.
     *
     * @return the context used by this configuration
     */
    public ProjectScriptContext getContext() {
        return context;
    }

    /**
     * Indicates if the error mode is enabled.
     * If in error mode, the cli application will log the full stack trace of an error.
     *
     * @return true if error mode is enabled, otherwise false
     */
    public boolean isError() {
        return error;
    }

    /**
     * Get the {@link Environment} used by this instance.
     *
     * @return the {@link Environment} used by this instance
     */
    public Environment getEnvironment() {
        return environment;
    }

    @Override
    public String getProxyHost() {
        if(proxyHost == null || proxyHost.isEmpty()) {
            boolean environmentContainsHost = getEnvironment().containsKey(CliConstants.KEY_FS_PROXYHOST.value());
            if(environmentContainsHost) {
                return getEnvironment().get(CliConstants.KEY_FS_PROXYHOST.value()).trim();
            }
            return "";
        }
        return proxyHost;
    }

    @Override
    public Integer getProxyPort() {
        if(proxyPort == null) {
            boolean environmentContainsPort = getEnvironment().containsKey(CliConstants.KEY_FS_PROXYPORT.value());
            if(environmentContainsPort) {
                return Integer.valueOf(getEnvironment().get(CliConstants.KEY_FS_PROXYPORT.value()).trim());
            }
            return 8080;
        }
        return proxyPort;
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
                return FsConnectionMode.valueOf(getEnvironment().get(CliConstants.KEY_FS_MODE.value()).trim().toUpperCase(Locale.UK));
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
        return getSynchronizationDirectory(getSynchronizationDirectoryString());
    }


    protected <F extends FileHandle> FileSystem<F> getSynchronizationDirectory(final String syncDirStr) {
        SyncDirectoryFactory syncDirectoryFactory = new SyncDirectoryFactory(this);
        syncDirectoryFactory.checkAndCreateSyncDirIfNeeded(syncDirStr);

        final FileSystemsAgent fileSystemsAgent = context.requireSpecialist(FileSystemsAgent.TYPE);
        return (FileSystem<F>) fileSystemsAgent.getOSFileSystem(syncDirStr);
    }

    @Override
    public boolean createSynchronizationDirectoryIfMissing() {
        return !dontCreateSynchronizationDirectoryIfMissing;
    }

    /**
     * Enable or disable the error mode.
     * If the error mode is enabled, the full stack trace of errors will be logged.
     * @param error boolean value indicating if the error code should be enabled or not
     */
    public void setError(boolean error) {
        this.error = error;
    }

    /**
     * Set the FirstSpirit server host that the cli application will connect to.
     * @param host FirstSpirit server host
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Get the {@link com.espirit.moddev.cli.api.FsConnectionMode} used to connect to FirstSpirit.
     * @return the {@link com.espirit.moddev.cli.api.FsConnectionMode} used to connect to FirstSpirit
     */
    public FsConnectionMode getFsMode() {
        return fsMode;
    }

    /**
     * Set the {@link com.espirit.moddev.cli.api.FsConnectionMode} used to connect to FirstSpirit.
     * @param fsMode the {@link com.espirit.moddev.cli.api.FsConnectionMode} used to connect to FirstSpirit
     */
    public void setFsMode(FsConnectionMode fsMode) {
        this.fsMode = fsMode;
    }

    /**
     * Set the FirstSpirit server port that the cli application will connect to.
     * @param port FirstSpirit server port
     */
    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * Set the user used to authenticate against FirstSpirit.
     * @param user the username used to authenticate against FirstSpirit
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * Set the password used to authenticate against FirstSpirit.
     * @param password the password used to authenticate against FirstSpirit
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Set the name of the project that will be synchronized.
     * @param project the name of the project that will be synchronized
     */
    public void setProject(String project) {
        this.project = project;
    }

    /**
     * Enable or disable the automatic activation of the synchronized project.
     * @param activateProjectIfDeactivated a boolean value indicating if the automatic activation of the synchronized project should be enabled or not
     */
    public void setActivateProjectIfDeactivated(boolean activateProjectIfDeactivated) {
        this.activateProjectIfDeactivated = activateProjectIfDeactivated;
    }

    /**
     * Set the synchronization directory.
     * @param synchronizationDirectory Path to the synchronization directory
     */
    public void setSynchronizationDirectory(String synchronizationDirectory) {
        this.synchronizationDirectory = synchronizationDirectory;
    }

}
