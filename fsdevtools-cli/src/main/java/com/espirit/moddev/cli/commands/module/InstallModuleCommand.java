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

package com.espirit.moddev.cli.commands.module;

import com.espirit.moddev.cli.ConnectionBuilder;
import com.espirit.moddev.cli.commands.SimpleCommand;
import com.espirit.moddev.cli.results.InstallModuleResult;
import com.espirit.moddev.moduleinstaller.ModuleInstallationParameters;
import com.espirit.moddev.moduleinstaller.ModuleInstallationRawParameters;
import com.espirit.moddev.moduleinstaller.ModuleInstaller;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.Examples;
import com.github.rvesse.airline.annotations.restrictions.Required;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.agency.ModuleAdminAgent;
import de.espirit.firstspirit.common.MaximumNumberOfSessionsExceededException;
import de.espirit.firstspirit.server.authentication.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

import static com.espirit.moddev.shared.StringUtils.isNullOrEmpty;

/**
 * Installs a module on a FirstSpirit server. Provides mechanisms to configure project apps, webapps
 * and corresponding scopes. If a given component is already installed, it is updated.
 */
@Command(name = "install", groupNames = {"module"}, description = "Installs a FirstSpirit module into a FirstSpirit Server. If a given component is already installed, it is updated.")
@Examples(examples = "module install -mpn \"Mithras Energy\" -fsm \"folder\\videomanagementpro.fsm\" -pacf \"resources\\projectApp.ini\" -scf\n" +
        "\"VideoManagementProService=folder\\videomanagementpro_service.ini\" -wacf \"preview=resources\\previewAppConfig.ini\"",
        descriptions = "Installs the videomanagementpro module with a given project app configuration and configures the VideoManagementProService with the given ini file.")
public class InstallModuleCommand extends SimpleCommand<InstallModuleResult> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(InstallModuleCommand.class);

    @Option(type = OptionType.COMMAND, name = {"-fsm", "--fsm"}, description = "Path to the module fsm file file that should be installed")
    @Required
    private String fsm;

    @Option(type = OptionType.COMMAND, name = {"-mpn", "--moduleProjectName"}, description = "Name of the FirstSpirit target project where the application's components should be installed to. Optional.")
    private String projectName;

    @Option(type = OptionType.COMMAND, name = {"-scf", "--serviceConfigurationFiles"}, description = "Define a map-like configuration file for services of the given module - comma-separated value pairs with service name and configuration path file.")
    private String serviceConfigurationsFiles;
    @Option(type = OptionType.COMMAND, name = {"-pacf", "--projectAppConfigurationFile"}, description = "Configuration file path for project app")
    private String projectAppConfigurationFile;
    @Option(type = OptionType.COMMAND, name = {"-was", "--webAppScopes"}, description = "Define a map-like configuration for webapp scopes of the given module - comma-separated values from the FirstSpirit WebScope enum."
                                                                                        + " The FS WebScope enum contains the following keys:\n"
                                                                                        + "'GLOBAL'\n"
                                                                                        + "'LIVE'\n"
                                                                                        + "'PREVIEW'\n"
                                                                                        + "'STAGING'\n"
                                                                                        + "'WEBEDIT'\n"
                                                                                        + " For global webapps, use 'global(WebAppId)'.")
    private String webAppScopes;
    @Option(type = OptionType.COMMAND, name = {"-wacf", "--webAppConfigurationFiles"}, description = "Define a map-like configuration for the webapps of the given module - with comma-separated key-values.")
    private String webAppConfigurationFiles;

    @Override
    public InstallModuleResult call() {
        try(Connection connection = create()) {
            connection.connect();
            return installModule(connection);
        } catch (IOException | AuthenticationException | MaximumNumberOfSessionsExceededException | IllegalArgumentException e) {
            return new InstallModuleResult(e);
        }
    }

    private InstallModuleResult installModule(Connection connection) {
        String projectName = retrieveProjectNameOrFallback();

        final ModuleInstallationParameters parameters = ModuleInstallationRawParameters.builder()
            .fsm(fsm)
            .projectAppConfigurationFile(projectAppConfigurationFile)
            .projectName(projectName)
            .webAppConfigurationFiles(webAppConfigurationFiles)
            .serviceConfigurationFile(serviceConfigurationsFiles)
            .webAppScopes(webAppScopes)
            .build();

        Optional<ModuleAdminAgent.ModuleResult> result = new ModuleInstaller().install(connection, parameters);
        return result
                .map(moduleResult -> new InstallModuleResult(moduleResult.getDescriptor().getModuleName()))
                .orElseGet(() -> new InstallModuleResult(new IllegalStateException("Cannot get installation result for module " + fsm)));
    }

    private String retrieveProjectNameOrFallback() {
        String projectName = this.projectName;
        if(isNullOrEmpty(projectName)) {
            LOGGER.warn("No --moduleProjectName parameter given for module installation.");
            if(!isNullOrEmpty(getProject())) {
                projectName = getProject();
                LOGGER.warn("Using global --project parameter of value \"" + getProject() +  "\"");
            } else {
                LOGGER.debug("No project name given as --moduleProjectName or --project parameter. " +
                        "Going on without project, so module installation with project specific components could fail.");
            }
        }
        return projectName;
    }


    protected Connection create() {
        return ConnectionBuilder.with(this).build();
    }

    @Override
    public boolean needsContext() {
        return false;
    }

    public String getFsm() {
        return fsm;
    }

    public void setFsm(String fsm) {
        this.fsm = fsm;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getServiceConfigurationsFiles() {
        return serviceConfigurationsFiles;
    }

    public void setServiceConfigurationsFiles(String serviceConfigurationsFiles) {
        this.serviceConfigurationsFiles = serviceConfigurationsFiles;
    }

    public String getProjectAppConfigurationFile() {
        return projectAppConfigurationFile;
    }

    public void setProjectAppConfigurationFile(String projectAppConfigurationFile) {
        this.projectAppConfigurationFile = projectAppConfigurationFile;
    }

    public String getWebAppScopes() {
        return webAppScopes;
    }

    public void setWebAppScopes(String webAppScopes) {
        this.webAppScopes = webAppScopes;
    }

    public String getWebAppConfigurationFiles() {
        return webAppConfigurationFiles;
    }

    public void setWebAppConfigurationFiles(String webAppConfigurationFiles) {
        this.webAppConfigurationFiles = webAppConfigurationFiles;
    }
}
