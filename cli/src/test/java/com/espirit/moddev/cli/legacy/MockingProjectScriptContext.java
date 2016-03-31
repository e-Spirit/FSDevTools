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

package com.espirit.moddev.cli.legacy;

import com.espirit.moddev.cli.CliConstants;
import com.espirit.moddev.test.tests.MockingContext;

import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.UserService;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.access.project.ProjectScriptContext;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * The type Mocking project script context.
 */
public class MockingProjectScriptContext extends MockingContext implements ProjectScriptContext {

    private Project project;
    private UserService userService;
    private Connection connection;
    private final String projectName;
    private final Map<String, Object> properties;

    /**
     * Instantiates a new Mocking project script context.
     *
     * @param projectName           the project name
     * @param locale                the locale
     * @param syncDir               the sync dir
     * @param supportedEnvironments the supported environments
     */
    public MockingProjectScriptContext(String projectName, Locale locale, File syncDir, Env... supportedEnvironments) {
        super(locale, supportedEnvironments);
        this.projectName = projectName;
        properties = new HashMap<>();
        properties.put(CliConstants.SYNC_DIR.value(), syncDir.getAbsolutePath());
        properties.put(CliConstants.COMMANDS.value(), "templatestore");
        properties.put(CliConstants.CREATE_SYNC_DIR_IF_MISSING.value(), "true");
        properties.put(CliConstants.DELETE_OBSOLETE_FILES.value(), "true");
        properties.put(CliConstants.EXPORT_CHILD_ELEMENTS.value(), "true");
        properties.put(CliConstants.EXPORT_FULL_TEMPLATESTORE.value(), "true");
        properties.put(CliConstants.EXPORT_PARENT_ELEMENTS.value(), "true");
        properties.put(CliConstants.EXPORT_RELEASE_ENTITIES.value(), "true");
        mockFields();
    }

    private void mockFields() {
        project = mock(Project.class);
        userService = mock(UserService.class);
        connection = mock(Connection.class);
        when(connection.getProjectByName(projectName)).thenReturn(project);
        when(project.getUserService()).thenReturn(userService);
    }

    @Override
    public UserService getUserService() {
        return userService;
    }

    @Override
    public Project getProject() {
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
}
