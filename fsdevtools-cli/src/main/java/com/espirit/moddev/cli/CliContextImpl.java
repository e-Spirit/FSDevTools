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

package com.espirit.moddev.cli;

import com.espirit.moddev.cli.exception.CliError;
import com.espirit.moddev.cli.exception.CliException;
import com.espirit.moddev.cli.api.configuration.Config;
import com.espirit.moddev.cli.api.configuration.ImportConfig;
import com.espirit.moddev.cli.api.CliContext;
import de.espirit.firstspirit.access.AdminService;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.ConnectionManager;
import de.espirit.firstspirit.access.UserService;
import de.espirit.firstspirit.access.admin.ProjectStorage;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.agency.BrokerAgent;
import de.espirit.firstspirit.agency.SpecialistType;
import de.espirit.firstspirit.agency.SpecialistsBroker;
import de.espirit.firstspirit.common.IOError;
import de.espirit.firstspirit.common.MaximumNumberOfSessionsExceededException;
import de.espirit.firstspirit.server.authentication.AuthenticationException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of {@link com.espirit.moddev.cli.api.CliContext}.
 *
 * @author e-Spirit AG
 */
public class CliContextImpl implements CliContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(CliContextImpl.class);

    private final Map<String, Object> properties;
    private final Config clientConfig;
    private Connection connection;
    private SpecialistsBroker projectBroker;

    /**
     * Create a new instance that uses the given {@link com.espirit.moddev.cli.api.configuration.Config}.
     *
     * @param clientConfig the configuration to be used
     * @throws java.lang.IllegalArgumentException if clientConfig is null
     */
    public CliContextImpl(final Config clientConfig) {
        if (clientConfig == null) {
            throw new IllegalArgumentException("Config is null!");
        }
        this.clientConfig = clientConfig;
        properties = new HashMap<>();
        initializeFirstSpiritConnection();
    }

    private void initializeFirstSpiritConnection() {
        openConnection();
        requireProjectSpecificBroker();
    }

    private void openConnection() {
        try {
            connection = obtainConnection();
            Object[] args = {clientConfig.getHost(), clientConfig.getPort(), clientConfig.getUser()};
            LOGGER
                .debug("Connect to fs-server '{}:{}' with user '{}'...", args);
            connection.connect();
        } catch (MaximumNumberOfSessionsExceededException e) {
            throw new CliException(CliError.SESSIONS, clientConfig, e);
        } catch (AuthenticationException e) {
            throw new CliException(CliError.AUTHENTICATION, clientConfig, e);
        } catch (IOException e) {
            throw new CliException(CliError.GENERAL_IO, clientConfig, e);
        } catch (IOError e) {
            throw new CliException(e);
        } catch (Exception e) { //NOSONAR
            throw new CliException(CliError.UNEXPECTED, clientConfig, e);
        }
    }

    /**
     * Connect to the FirstSpirit server using the configuration of this instance.
     *
     * @return a connection to a FirstSpirit server
     */
    protected Connection obtainConnection() {
        return ConnectionManager
            .getConnection(clientConfig.getHost(), clientConfig.getPort(), clientConfig.getConnectionMode().getCode(), clientConfig.getUser(),
                           clientConfig.getPassword());
    }

    private void requireProjectSpecificBroker() {
        LOGGER.debug("Require project specific specialist broker for project '{}'...", clientConfig.getProject());

        String name;
        try {
            final Project project = getProject();
            name = project != null ? project.getName() : null;
        } catch (Exception e) { //NOSONAR
            throw new IllegalStateException(
                "Project '" + clientConfig.getProject() + "' not found on server. Correct project name or omit --dont-create-project option.", e);
        }

        if (StringUtils.isNotBlank(name)) {
            final SpecialistsBroker broker = connection.getBroker();
            final BrokerAgent brokerAgent = broker.requireSpecialist(BrokerAgent.TYPE);
            projectBroker = brokerAgent.getBrokerByProjectName(name);
        }
        if(projectBroker == null) {
            throw new IllegalStateException("ProjectBroker cannot be retrieved for project " + name);
        }
    }

    @Override
    public UserService getUserService() {
        return getProject().getUserService();
    }

    @Override
    public Project getProject() {
        final String projectName = clientConfig.getProject();
        Project project = connection.getProjectByName(projectName);
        if (project == null && clientConfig.isCreatingProjectIfMissing()) {
            project = createProject(projectName);
        }
        LOGGER.debug("activate project if deactivated: " + clientConfig.isActivateProjectIfDeactivated(), projectName);
        if (clientConfig.isActivateProjectIfDeactivated()) {
            activateProject(projectName, project);
        }
        LOGGER.debug("project is '{}'", project);
        return project;
    }

    private void activateProject(String projectName, Project project) {
        if(project != null) {
            throw new IllegalArgumentException("Project for activation is null");
        }
        if(!project.isActive()) {
            LOGGER.warn("Project '{}' is not active! Try to activate...", projectName);
            UserService userService = project.getUserService();
            AdminService adminService = userService.getConnection().getService(AdminService.class);
            adminService.getProjectStorage().activateProject(project);
        } else {
            LOGGER.debug("Project '{}' is already active! No need to activate...", projectName);
        }
    }

    private Project createProject(String projectName) {
        Project project;
        LOGGER.info("Creating missing project '{}' on server...", projectName);
        AdminService ac = connection.getService(AdminService.class);
        final ProjectStorage projectStorage = ac.getProjectStorage();
        project = projectStorage.createProject(projectName, projectName + " created by fs-cli");
        return project;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public Object getProperty(String name) {
        return properties.get(name);
    }

    @Override
    public void setProperty(String name, Object value) {
        properties.put(name, value);
    }

    @Override
    public void removeProperty(String name) {
        properties.remove(name);
    }

    @Override
    public String[] getProperties() {
        return properties.values().toArray(new String[properties.size()]);
    }

    @Override
    public void logError(String s) {
        LOGGER.error(s);
    }

    @Override
    public void logError(String s, Throwable throwable) {
        LOGGER.error(s, throwable);
    }

    @Override
    public void logWarning(String s) {
        LOGGER.warn(s);
    }

    @Override
    public void logDebug(String s) {
        LOGGER.debug(s);
    }

    @Override
    public void logInfo(String s) {
        LOGGER.info(s);
    }

    @Override
    public boolean is(Env env) {
        return Env.HEADLESS == env;
    }

    @Override
    public <S> S requestSpecialist(SpecialistType<S> type) {
        return projectBroker.requestSpecialist(type);
    }

    @Override
    public <S> S requireSpecialist(SpecialistType<S> type) {
        return projectBroker.requireSpecialist(type);
    }

    @Override
    public void close() throws Exception {
        LOGGER.debug("Closing connection to FirstSpirit ...");
        connection.close();
        LOGGER.info("Connection to FirstSpirit closed!");
    }
}
