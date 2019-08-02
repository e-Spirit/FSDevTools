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

package com.espirit.moddev.cli.api.configuration;

import com.espirit.moddev.cli.api.FsConnectionMode;

import de.espirit.firstspirit.access.project.ProjectScriptContext;
import de.espirit.firstspirit.io.FileHandle;
import de.espirit.firstspirit.io.FileSystem;

/**
 * Interface that defines means to access the configuration values.
 *
 * @author e-Spirit AG
 */
public interface Config {

    /**
     * Get the FirstSpirit server host.
     *
     * @return the FirstSpirit server host
     */
    String getHost();

    /**
     * Get the FirstSpirit server port.
     *
     * @return the FirstSpirit server port
     */
    Integer getPort();

    /**
     * Get the proxy host for http/https connections.
     *
     * @return the proxy host for http/https connections
     */
    String getHttpProxyHost();

    /**
     * Get the proxy server port for http/https connections.
     *
     * @return the proxy server port for http/https connections
     */
    Integer getHttpProxyPort();

    /**
     * Get the connection mode used to connect to the FirstSpirit server.
     *
     * @return a {@link com.espirit.moddev.cli.api.FsConnectionMode} object that specifies the connection mode used to connect to the FirstSpirit server
     */
    FsConnectionMode getConnectionMode();

    /**
     * Get the username used to authenticate against the FirstSpirit server.
     *
     * @return the username used to authenticate against the FirstSpirit server
     */
    String getUser();

    /**
     * Get the password used to authenticate against the FirstSpirit server.
     *
     * @return the password used to authenticate against the FirstSpirit server.
     */
    String getPassword();

    /**
     * Get the name of the project that will be synchronized.
     *
     * @return the name of the project that will be synchronized
     */
    String getProject();

    /**
     * Gets the synchronization directory as a {@link java.lang.String} identifier.
     * Can be used to retrieve an actual file handle to a directory.
     *
     * @return the synchronization directory
     */
    String getSynchronizationDirectoryString();

    /**
     * Indicates whether or not a directory for file synchronization should be created if absent.
     * The default value is true.
     *
     * @return true if a directory should be created if absent, false if not
     */
    default boolean createSynchronizationDirectoryIfMissing() { return true; }

    /**
     * Retrieves a directory for file synchronization.
     *
     * @param <F> the type of a FileHandle subclass
     * @return a FileSystem handle
     */
    <F extends FileHandle> FileSystem<F> getSynchronizationDirectory();

    /**
     * Indicates if this type of configuration needs a context to be initialized.
     * An execution environment can evaluate this information to properly initialize commands, for example.
     * The default value is true.
     *
     * @return a boolean value that indicates whether or not a context is required for this config.
     */
    default boolean needsContext() { return true; }

    /**
     * Set the context used by this configuration.
     * Since not all configurations need a context, the default implementation is empty.
     *
     * @param context the context this configuration should use
     */
    default void setContext(ProjectScriptContext context) {}

    /**
     * Indicates if the synchronized project specified by {@link #getProject()} should be activated if it is currently deactivated.
     * The default value is true.
     *
     * @return a boolean value indicating if the synchronized project should be activated if it is currently deactivated
     */
    default boolean isActivateProjectIfDeactivated() { return true; }

    /**
     * Indicates if the project specified by {@link #getProject()} should be created if it does not exist. This
     * default implementation returns false, because otherwise it would be difficult to detect possible errors
     * due to missing projects.
     *
     * @return a boolean value that indicates if the synchronized project should be created if it does not exist.
     */
    default boolean isCreatingProjectIfMissing() { return false; }
}
