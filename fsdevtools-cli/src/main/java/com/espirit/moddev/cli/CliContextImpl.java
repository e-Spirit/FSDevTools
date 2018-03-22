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

import com.espirit.moddev.cli.api.CliContext;
import com.espirit.moddev.cli.api.configuration.Config;
import com.espirit.moddev.cli.exception.CliError;
import com.espirit.moddev.cli.exception.CliException;

import de.espirit.firstspirit.access.AdminService;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.UserService;
import de.espirit.firstspirit.access.admin.ProjectStorage;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.agency.BrokerAgent;
import de.espirit.firstspirit.agency.ServerInformationAgent;
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
    private Project project;

    /**
     * Create a new instance that uses the given {@link com.espirit.moddev.cli.api.configuration.Config}
     * and established a FirstSpirit connection. Afterwards, a ProjectSpecificBroker is required if a project is given.
     *
     * @param clientConfig the configuration to be used
     * @throws IllegalArgumentException if clientConfig is null
     */
    public CliContextImpl(final Config clientConfig) {
        if (clientConfig == null) {
            throw new IllegalArgumentException("Config is null!");
        }
        this.clientConfig = clientConfig;
        properties = new HashMap<>();
        openConnection();
        requireProjectSpecificBroker();
    }

    protected void openConnection() {
        try {
            connection = obtainConnection();
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new CliException(CliError.CONFIGURATION, clientConfig, e);
        } catch (RuntimeException e) {
            throw new CliException(CliError.UNEXPECTED, clientConfig, e);
        }
        try {
            connection.connect();
            final ServerInformationAgent serverInformationAgent = connection.getBroker().requestSpecialist(ServerInformationAgent.TYPE);
            if (serverInformationAgent != null) {
                final ServerInformationAgent.VersionInfo serverVersion = serverInformationAgent.getServerVersion();
                LOGGER.info("Connected to FirstSpirit server at {} of version {}",
                            new Object[]{clientConfig.getHost(), serverVersion.getFullVersionString()});
            }
        } catch (MaximumNumberOfSessionsExceededException e) {
            throw new CliException(CliError.SESSIONS, clientConfig, e);
        } catch (AuthenticationException e) {
            throw new CliException(CliError.AUTHENTICATION, clientConfig, e);
        } catch (IOException e) {
            throw new CliException(CliError.GENERAL_IO, clientConfig, e);
        } catch (IOError e) {
            throw new CliException(e);
        } catch (RuntimeException e) {
            throw new CliException(CliError.UNEXPECTED, clientConfig, e);
        }
    }

    protected Connection obtainConnection() {
        return ConnectionBuilder.with(clientConfig).build();
    }

    private void requireProjectSpecificBroker() {
        String projectName = clientConfig.getProject();
        if(StringUtils.isEmpty(projectName)) {
            LOGGER.info("No project name given, so no project specific broker is required");
        } else {
            LOGGER.debug("Require project specific specialist broker for project '{}'...", projectName);

            try {
                loadProject(projectName);
            } catch (Exception e) { //NOSONAR
                LOGGER.info("Can't load project {}. Not going to require a broker.", projectName);
                LOGGER.debug("Exception while loading project", e);
            }

            if(project != null) {
                final SpecialistsBroker broker = connection.getBroker();
                final BrokerAgent brokerAgent = broker.requireSpecialist(BrokerAgent.TYPE);
                projectBroker = brokerAgent.getBrokerByProjectName(project.getName());
            } else {
                LOGGER.info("Project not available, so no project specific broker is required");
            }
        }
    }

    @Override
    public UserService getUserService() {
        return getProject().getUserService();
    }

    @Override
    public Project getProject() {
        return project;
    }

    private void loadProject(String projectName) {
        if (!StringUtils.isBlank(projectName)) {
            Project project = connection.getProjectByName(projectName);
            if (project == null && clientConfig.isCreatingProjectIfMissing()) {
                project = createProject(projectName);
            }
            LOGGER.debug("activate project if deactivated: " + clientConfig.isActivateProjectIfDeactivated(), projectName);
            if (clientConfig.isActivateProjectIfDeactivated()) {
                activateProject(projectName, project);
            }
            LOGGER.info("project is '{}'", project);
            this.project = project;
        }
    }

    private static void activateProject(String projectName, Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Project for activation is null");
        }
        if (!project.isActive()) {
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
        if(getSpecialistsBroker() == null) {
            LOGGER.warn("Project broker is null, probably because no project name was configured. Going to return null.");
            return null;

        }
        return getSpecialistsBroker().requestSpecialist(type);
    }

    @Override
    public <S> S requireSpecialist(SpecialistType<S> type) {
        if (getSpecialistsBroker() == null) {
            throw new IllegalStateException("No ProjectBroker initialized! Probably because no project name was configured.");
        }
        return getSpecialistsBroker().requireSpecialist(type);
    }

    @Override
    public void close() throws Exception {
        LOGGER.debug("Closing connection to FirstSpirit ...");
        connection.close();
        LOGGER.info("Connection to FirstSpirit closed!");
    }

    @Override
    public SpecialistsBroker getSpecialistsBroker() {
        return projectBroker;
    }
}
