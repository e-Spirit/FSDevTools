/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2021 e-Spirit GmbH
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

package com.espirit.moddev.cli.commands.module.installCommand;

import com.espirit.moddev.shared.annotation.RequiredForSerialization;
import com.espirit.moddev.shared.exception.MultiException;
import com.espirit.moddev.shared.webapp.WebAppIdentifier;
import com.espirit.moddev.shared.webapp.WebAppIdentifierParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.agency.GlobalWebAppId;
import de.espirit.firstspirit.agency.ModuleAdminAgent;
import de.espirit.firstspirit.agency.WebAppId;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Elements of this class represent a single configuration for a module using the "module installBulk"-command. Its the
 * json-equivalent for {@link ModuleInstallationParameters} and can is ready to use
 * in scenarios using the json file format.
 */
public class ModuleInstallationConfiguration {

    /**
     * Returns a {@link List list} of configurations for the file at the given path.
     *
     * @param pathToFile the path of the configuration file
     * @return a {@link List list} of configurations
     * @throws IOException thrown if the file at the given path could not be de-serialized properly
     */
    public static List<ModuleInstallationConfiguration> fromFile(final String pathToFile) throws IOException {
        final Path path = Paths.get(pathToFile);
        if (!path.toFile().exists()) {
            throw new IllegalArgumentException("File '" + pathToFile + "' does not exist.");
        }

        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        final ModuleInstallationConfiguration[] configurations = objectMapper.readValue(Files.readAllBytes(path), ModuleInstallationConfiguration[].class);
        return Arrays.asList(configurations);
    }

    private String _fsm;
    private String _moduleProjectName;
    private final List<String> _webAppScopes;
    private String _deploy;
    private String _projectAppConfigurationFile;
    private final List<String> _serviceConfigurationFiles;
    private final List<String> _webAppConfigurationFiles;

    @RequiredForSerialization
    public ModuleInstallationConfiguration() {
        _webAppScopes = new ArrayList<>();
        _serviceConfigurationFiles = new ArrayList<>();
        _webAppConfigurationFiles = new ArrayList<>();
    }

    @RequiredForSerialization
    public String getFsm() {
        return _fsm;
    }

    @RequiredForSerialization
    public void setFsm(final String path) {
        _fsm = path;
    }

    @RequiredForSerialization
    public String getModuleProjectName() {
        return _moduleProjectName;
    }

    @RequiredForSerialization
    public void setModuleProjectName(final String moduleProjectName) {
        _moduleProjectName = moduleProjectName;
    }

    @RequiredForSerialization
    public List<String> getWebAppScopes() {
        return _webAppScopes;
    }

    @RequiredForSerialization
    public void setWebAppScopes(final List<String> scopes) {
        _webAppScopes.clear();
        _webAppScopes.addAll(scopes);
    }

    @RequiredForSerialization
    public String getDeploy() {
        return _deploy;
    }

    @RequiredForSerialization
    public void setDeploy(final String deploy) {
        _deploy = deploy;
    }

    @RequiredForSerialization
    public String getProjectAppConfigurationFile() {
        return _projectAppConfigurationFile;
    }

    @RequiredForSerialization
    public void setProjectAppConfigurationFile(final String projectAppConfigurationFile) {
        _projectAppConfigurationFile = projectAppConfigurationFile;
    }

    @RequiredForSerialization
    public List<String> getServiceConfigurationFiles() {
        return _serviceConfigurationFiles;
    }

    @RequiredForSerialization
    public void setServiceConfigurationFiles(final List<String> serviceConfigurationFiles) {
        _serviceConfigurationFiles.clear();
        _serviceConfigurationFiles.addAll(serviceConfigurationFiles);
    }

    @RequiredForSerialization
    public List<String> getWebAppConfigurationFiles() {
        return _webAppConfigurationFiles;
    }

    @RequiredForSerialization
    public void setWebAppConfigurationFiles(final List<String> webAppConfigurationFiles) {
        _webAppConfigurationFiles.clear();
        _webAppConfigurationFiles.addAll(webAppConfigurationFiles);
    }

    public void verify(final Connection connection) {
        final ModuleAdminAgent moduleAdminAgent = connection.getBroker().requestSpecialist(ModuleAdminAgent.TYPE);
        final List<Exception> exceptions = new ArrayList<>();
        verifyFsm(exceptions);
        verifyProject(exceptions, connection);
        verifyScopes(moduleAdminAgent, exceptions);
        verifyFile(exceptions, "Project app configuration file", _projectAppConfigurationFile);
        verifyFiles(exceptions, "Service configuration file", _serviceConfigurationFiles);
        verifyFiles(exceptions, "Web app configuration file", _webAppConfigurationFiles);
        if (!exceptions.isEmpty()) {
            throw new MultiException("Error verifying module configuration!", exceptions);
        }
    }

    private void verifyFiles(final List<Exception> exceptions, final String description, final List<String> combinedPaths) {
        for (final String path : combinedPaths) {
            if (path.contains("=")) {
                final String[] split = path.split("=");
                if (split.length != 2) {
                    exceptions.add(new IllegalStateException(_fsm + " - Path to " + description + " '" + path + "' has illegal format."));
                }
                verifyFile(exceptions, description, split[1]);
            } else {
                verifyFile(exceptions, description, path);
            }
        }
    }

    private void verifyFile(final List<Exception> exceptions, final String description, final String path) {
        if (path == null) {
            return;
        }
        final File file = new File(path);
        if (!file.exists()) {
            exceptions.add(new IllegalArgumentException(_fsm + " - " + description + " '" + path + "' does not exist!"));
            return;
        }
        if (!file.isFile()) {
            exceptions.add(new IllegalArgumentException(_fsm + " - " + description + " '" + path + "' is not a file!"));
        }
    }

    private void verifyFsm(final List<Exception> exceptions) {
        if (_fsm == null) {
            exceptions.add(new IllegalArgumentException("Path to FSM must be defined (\"fsm\": \"path\\\\to\\\\file.fsm\")!"));
            return;
        }
        verifyFile(exceptions, "FSM", _fsm);
    }

    private void verifyProject(final List<Exception> exceptions, final Connection connection) {
        if (_moduleProjectName != null && connection.getProjectByName(_moduleProjectName) == null) {
            exceptions.add(new IllegalArgumentException(_fsm + " - Project '" + _moduleProjectName + "' does not exist."));
        }
    }

    public void verifyScopes(final ModuleAdminAgent moduleAdminAgent, final List<Exception> exceptions) {
        final Collection<String> globalWebApps = moduleAdminAgent.getGlobalWebApps(true).stream().map(GlobalWebAppId::getGlobalId).collect(Collectors.toList());
        final WebAppIdentifierParser webAppIdentifierParser = new WebAppIdentifierParser();
        for (final String scope : _webAppScopes) {
            try {
                final WebAppIdentifier webAppIdentifier = webAppIdentifierParser.parseSingle(scope, false);
                if (webAppIdentifier.isGlobal()) {
                    final WebAppIdentifier.GlobalWebAppIdentifier globalWebAppIdentifier = (WebAppIdentifier.GlobalWebAppIdentifier) webAppIdentifier;
                    final GlobalWebAppId globalWebAppId = WebAppId.Factory.create(globalWebAppIdentifier.getGlobalWebAppId());
                    if (!globalWebApps.contains(globalWebAppId.getGlobalId())) {
                        exceptions.add(new IllegalArgumentException(_fsm + " - Unknown global scope '" + globalWebAppId.getGlobalId() + "'!"));
                    }
                }
            } catch (final Exception e) {
                exceptions.add(new IllegalArgumentException(_fsm + " - Unknown scope '" + scope + "'!"));
            }
        }
    }

}
