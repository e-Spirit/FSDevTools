package com.espirit.moddev.cli.commands.module;

import com.espirit.moddev.cli.ConnectionBuilder;
import com.espirit.moddev.cli.commands.SimpleCommand;
import com.espirit.moddev.cli.results.SimpleResult;
import com.espirit.moddev.moduleinstaller.ModuleInstallationParameters;
import com.espirit.moddev.moduleinstaller.ModuleInstaller;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.Examples;
import com.github.rvesse.airline.annotations.restrictions.Required;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.common.MaximumNumberOfSessionsExceededException;
import de.espirit.firstspirit.module.WebEnvironment.WebScope;
import de.espirit.firstspirit.server.authentication.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Installs a module on a FirstSpirit server. Provides mechanisms to configure project apps, webapps
 * and corresponding scopes. If a given component is already installed, it is updated.
 */
@Command(name = "install", groupNames = {"module"}, description = "Installs a FirstSpirit module into a FirstSpirit Server. If a given component is already installed, it is updated.")
@Examples(examples = "module install -mpn \"Mithras Energy\" -fsm \"folder\\videomanagementpro.fsm\" -pacf \"resources\\projectApp.ini\" " +
        "-wacf \"preview=resources\\previewAppConfig.ini\"",
        descriptions = "module install -mpn \"Mithras Energy\" -fsm \"module.fsm\" -pacf \"resources\\projectApp.ini\"")
public class InstallModuleCommand extends SimpleCommand<SimpleResult<Boolean>> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(InstallModuleCommand.class);

    @Option(type = OptionType.COMMAND, name = {"-fsm", "--fsm"}, description = "Path to the module fsm file file that should be installed")
    @Required
    private String fsm;

    @Option(type = OptionType.COMMAND, name = {"-mpn", "--moduleProjectName"}, description = "Name of the FirstSpirit target project where the application's components should be installed to")
    @Required
    private String projectName;

    //FIXME: will be fixed in DEVEX-109
    //@Option(type = OptionType.COMMAND, name = {"-scf", "--serviceConfigurationFiles"}, description = "Define a map-like configuration file for services of the given module - comma-separated value paris with service name and configuration path file.")
    private String serviceConfigurationsFiles;
    @Option(type = OptionType.COMMAND, name = {"-pacf", "--projectAppConfigurationFile"}, description = "Configuration file path for project app")
    private String projectAppConfigurationFile;
    @Option(type = OptionType.COMMAND, name = {"-was", "--webAppScopes"}, description = "Define a map-like configuration for webapp scopes of the given module - comma-separated values from the FirstSpirit WebScope enum.")
    private String webAppScopes;
    @Option(type = OptionType.COMMAND, name = {"-wacf", "--webAppConfigurationFiles"}, description = "Define a map-like configuration for the webapps of the given module - with comma-separated key-values.")
    private String webAppConfigurationFiles;

    @Override
    public SimpleResult<Boolean> call() {
        try(Connection connection = create()) {
            connection.connect();
            return installModule(connection);
        } catch (IOException | AuthenticationException | MaximumNumberOfSessionsExceededException | IllegalArgumentException e) {
            return new SimpleResult<>(e);
        }
    }

    private SimpleResult<Boolean> installModule(Connection connection) {
        File projectAppConfigurationFile = getAndValidateOptionalProjectAppConfigurationFile();
        List<WebScope> splittedWebAppScopes = extractWebScopes();
        Map<WebScope, File> webappConfigurationFilesForWebScopes = getAndValidateWebScopeFileMap();
        Map<String, File> configurationFileForServiceName = getAndValidateStringFilesMap(serviceConfigurationsFiles);

        final ModuleInstallationParameters parameters = new ModuleInstallationParameters(projectName, new File(fsm), configurationFileForServiceName, projectAppConfigurationFile, splittedWebAppScopes, webappConfigurationFilesForWebScopes);
        boolean installed = new ModuleInstaller().install(connection, parameters);
        return new SimpleResult<>(installed);
    }

    private File getAndValidateOptionalProjectAppConfigurationFile() {
        File result = getOptionalProjectAppConfigurationFile();
        if(result != null && (!result.isFile() || !result.exists())) {
            throw new IllegalArgumentException("Project app configuration file doesn't exist or is not a file!");
        }
        return result;
    }

    private Map<String, File> getAndValidateStringFilesMap(String configurations) {
        Map<String, File> result = getStringFilesMap(configurations);
        for(Map.Entry<String, File> entry : result.entrySet()) {
            if(!entry.getValue().exists() || !entry.getValue().isFile()) {
                throw new IllegalArgumentException("File doesn't exist for key " + entry.getKey());
            }
        }
        return result;
    }

    private Map<WebScope, File> getAndValidateWebScopeFileMap() {
        Map<WebScope, File> webScopeFileMap = getWebScopeFileMap();
        for(Map.Entry<WebScope, File> entry : webScopeFileMap.entrySet()) {
            if(!entry.getValue().isFile() || !entry.getValue().exists()) {
                throw new IllegalArgumentException("File for webapp configuration with scope " + entry.getKey() + " doesn't exist or is not a file.");
            }
        }
        return webScopeFileMap;
    }

    protected Map<WebScope, File> getWebScopeFileMap() {
        return getStringFilesMap(webAppConfigurationFiles).entrySet().stream().collect(Collectors.toMap(entry -> WebScope.valueOf(entry.getKey().trim().toUpperCase()), Map.Entry::getValue));
    }

    protected File getOptionalProjectAppConfigurationFile() {
        File projectAppConfigurationFile = null;
        if(this.projectAppConfigurationFile != null) {
            projectAppConfigurationFile = new File(this.projectAppConfigurationFile);
        }
        return projectAppConfigurationFile;
    }

    protected Map<String, File> getStringFilesMap(String webAppConfigurations) {
        if(webAppConfigurations == null) {
            return new HashMap<>();
        }
        return Arrays.stream(webAppConfigurations.split(","))
                .map(propertyString -> propertyString.split("="))
                .collect(Collectors.toMap(entry -> entry[0], entry -> new File(entry[1])));
    }

    protected List<WebScope> extractWebScopes() {
        if (webAppScopes == null) {
            return new ArrayList<>();
        }
        try {
            return Arrays.stream(webAppScopes.split(","))
                .map(scope -> scope.trim().toUpperCase())
                .map(WebScope::valueOf)
                .collect(toList());
        } catch (IllegalArgumentException e) {
            LOGGER.error("You passed an illegal argument as webapp scope", e);
        }
        return new ArrayList<>();
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
