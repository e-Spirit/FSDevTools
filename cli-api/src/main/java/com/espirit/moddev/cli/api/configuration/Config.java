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
import com.espirit.moddev.cli.api.FullQualifiedUid;
import de.espirit.firstspirit.access.project.ProjectScriptContext;
import de.espirit.firstspirit.io.FileHandle;
import de.espirit.firstspirit.io.FileSystem;

import java.util.List;

/**
 * The interface Client config.
 */
public interface Config {

    /**
     * Gets host.
     *
     * @return the host
     */
    String getHost();

    /**
     * Gets port.
     *
     * @return the port
     */
    Integer getPort();

    /**
     * Gets connection mode.
     *
     * @return the connection mode
     */
    FsConnectionMode getConnectionMode();

    /**
     * Gets user.
     *
     * @return the user
     */
    String getUser();

    /**
     * Gets password.
     *
     * @return the password
     */
    String getPassword();

    /**
     * Gets project.
     *
     * @return the project
     */
    String getProject();

    /**
     * Gets the synchronization directory as a String identifier. Can be used to retrieve
     * an actual file handle to a directory.
     *
     * @return the sync dir
     */
    String getSynchronizationDirectoryString();

    /**
     * Indicates whether or not a directory for file synchronization should be created if absent.
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
     * Indicates if this type of configuration needs a context to be initialized. An execution
     * environment can evaluate this information to properly initialize commands, for example.
     *
     * @return a boolean value that indicates whether or not a context is required for this config.
     */
    default boolean needsContext() { return true; }

    /**
     * Setter for a context. Since not all configurations need a context, the default
     * implementation is empty.
     *
     * @param context the context this command should use
     */
    default void setContext(ProjectScriptContext context) {}

    /**
     * Activate project if deactivated. Default is true.
     *
     * @return the boolean
     */
    default boolean isActivateProjectIfDeactivated() { return true; }

    /**
     * Retrieves a list of FullQualifiedUids
     *
     * @return a list of FullQualifiedUids
     */
    List<FullQualifiedUid> getFullQualifiedUids();
}
