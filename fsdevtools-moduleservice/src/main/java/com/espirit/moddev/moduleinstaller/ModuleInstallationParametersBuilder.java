package com.espirit.moddev.moduleinstaller;

import com.google.common.annotations.VisibleForTesting;

import de.espirit.firstspirit.module.WebEnvironment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * This class provides utility methods to parse String parameters
 * to correctly typed @see com.espirit.moddev.moduleinstaller.ModuleInstallationParameters.
 */
public class ModuleInstallationParametersBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleInstallationParametersBuilder.class);
    private Map<WebEnvironment.WebScope, File> webAppConfigurationFilesForWebScopes;
    private List<WebEnvironment.WebScope> splittedWebAppScopes;
    private File projectAppConfigurationFile;
    private Map<String, File> configurationFileForServiceName;
    private File fsm;
    private String projectName;

    public ModuleInstallationParametersBuilder() {
        // I'm just sitting here, doing nothing but wasting lines.
    }

    public ModuleInstallationParametersBuilder setProjectName(String projectName) {
        if (isNotEmpty(projectName)) {
            this.projectName = projectName;
        } else {
            throw new IllegalArgumentException("String 'projectName' is null or emptry.");
        }
        return this;
    }

    public ModuleInstallationParametersBuilder setFirstSpiritModule(String firstSpiritModule) {
        if (isNotEmpty(firstSpiritModule)) {
            this.fsm = new File(firstSpiritModule);
        } else {
            throw new IllegalArgumentException("String 'firstSpiritModule' is null or emptry.");
        }
        return this;
    }

    public ModuleInstallationParametersBuilder setServiceConfigurationFiles(String serviceConfigurationFiles) {
        if (isNotEmpty(serviceConfigurationFiles)) {
            this.configurationFileForServiceName = getAndValidateStringFilesMap(serviceConfigurationFiles);
        } else {
            throw new IllegalArgumentException("String 'serviceConfigurationFiles' is null or empty.");
        }
        return this;
    }

    public ModuleInstallationParametersBuilder setProjectAppConfigurationFile(String projectAppConfigurationFile) {
        if (isNotEmpty(projectAppConfigurationFile)) {
            this.projectAppConfigurationFile = createAndValidateOptionalProjectAppConfigurationFile(projectAppConfigurationFile);
        } else {
            throw new IllegalArgumentException("String 'projectAppConfigurationFile' is null or empty.");
        }
        return this;
    }

    public ModuleInstallationParametersBuilder setWebAppScopes(List<String> webAppScopes) {
        List<WebEnvironment.WebScope> scopes = new ArrayList<>();
        webAppScopes.forEach(scope -> {
            if (isNotEmpty(scope)) {
                scopes.add(WebEnvironment.WebScope.valueOf(scope));
            } else {
                throw new IllegalArgumentException("String 'scope' is null or empty.");
            }
            splittedWebAppScopes = scopes;
        });
        return this;
    }

    public ModuleInstallationParametersBuilder setWebAppScopes(String webAppScopes) {
        if (isNotEmpty(webAppScopes)) {
            this.splittedWebAppScopes = extractWebScopes(webAppScopes);
        } else {
            throw new IllegalArgumentException("String 'webAppScopes' is null or empty.");
        }
        return this;
    }

    public ModuleInstallationParametersBuilder setWebAppConfigurationFiles(Map<WebEnvironment.WebScope, File> webAppConfigurationFiles) {
        validateWebScopeFileMap(webAppConfigurationFiles);
        return this;
    }

    public ModuleInstallationParametersBuilder setWebAppConfigurationFiles(String webAppConfigurationFiles) {
        if (isNotEmpty(webAppConfigurationFiles)) {
            this.webAppConfigurationFilesForWebScopes = getAndValidateWebScopeFileMap(webAppConfigurationFiles);
        } else {
            throw new IllegalArgumentException("String 'webAppConfigurationFiles' is null or empty.");
        }
        return this;
    }

    /**
     * This method creates a new instance of @see com.espirit.moddev.moduleinstaller.ModuleInstallationParameters
     * based on all setted parameters.
     * @return an instance of @see com.espirit.moddev.moduleinstaller.ModuleInstallationParameters
     */
    public ModuleInstallationParameters build() {
        return new ModuleInstallationParameters(projectName, fsm, configurationFileForServiceName, projectAppConfigurationFile, splittedWebAppScopes, webAppConfigurationFilesForWebScopes);
    }

    private static boolean isNotEmpty(String string) {
        return string != null && string.trim().length() > 0;
    }

    @VisibleForTesting
    List<WebEnvironment.WebScope> extractWebScopes(String webAppScopes) {
        if (webAppScopes == null) {
            return new ArrayList<>();
        }
        try {
            return Arrays.stream(webAppScopes.split(","))
                .map(scope -> scope.trim().toUpperCase(Locale.getDefault()))
                .map(WebEnvironment.WebScope::valueOf)
                .collect(toList());
        } catch (IllegalArgumentException e) {
            LOGGER.error("You passed an illegal argument as webapp scope", e);
        }
        return new ArrayList<>();
    }

    @VisibleForTesting
    File createAndValidateOptionalProjectAppConfigurationFile(String projectAppConfigurationFile) {
        File result = createOptionalProjectAppConfigurationFile(projectAppConfigurationFile);
        if(result != null && (!result.isFile() || !result.exists())) {
            throw new IllegalArgumentException("Project app configuration file doesn't exist or is not a file!");
        }
        return result;
    }

    @VisibleForTesting
    File createOptionalProjectAppConfigurationFile(String projectAppConfigurationFile) {
        return Optional.ofNullable(projectAppConfigurationFile)
            .map(File::new)
            .orElse(null);
    }

    private Map<WebEnvironment.WebScope, File> getAndValidateWebScopeFileMap(String webAppConfigurationFiles) {
        Map<WebEnvironment.WebScope, File> webScopeFileMap = getWebScopeFileMap(webAppConfigurationFiles);
        validateWebScopeFileMap(webScopeFileMap);
        return webScopeFileMap;
    }

    private void validateWebScopeFileMap(Map<WebEnvironment.WebScope, File> webScopeFileMap) {
        for(Map.Entry<WebEnvironment.WebScope, File> entry : webScopeFileMap.entrySet()) {
            if(!entry.getValue().isFile() || !entry.getValue().exists()) {
                throw new IllegalArgumentException("File for webapp configuration with scope " + entry.getKey() + " doesn't exist or is not a file.");
            }
        }
    }

    @VisibleForTesting
    Map<WebEnvironment.WebScope, File> getWebScopeFileMap(String webAppConfigurationFiles) {
        return getStringFilesMap(webAppConfigurationFiles).entrySet().stream().collect(
            Collectors.toMap(entry -> WebEnvironment.WebScope.valueOf(entry.getKey().trim().toUpperCase(Locale.getDefault())), Map.Entry::getValue));
    }

    @VisibleForTesting
    Map<String, File> getStringFilesMap(String webAppConfigurations) {
        if(webAppConfigurations == null) {
            return new HashMap<>();
        }
        return Arrays.stream(webAppConfigurations.split(","))
            .map(propertyString -> propertyString.split("="))
            .collect(Collectors.toMap(entry -> entry[0], entry -> new File(entry[1])));
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
}
