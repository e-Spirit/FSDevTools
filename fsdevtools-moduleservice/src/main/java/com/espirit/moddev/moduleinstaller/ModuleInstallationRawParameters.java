package com.espirit.moddev.moduleinstaller;

import com.google.common.annotations.VisibleForTesting;
import de.espirit.firstspirit.module.WebEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

import static de.espirit.firstspirit.module.WebEnvironment.WebScope.valueOf;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * This class provides utility methods to parse String parameters
 * to correctly typed @see com.espirit.moddev.moduleinstaller.ModuleInstallationParameters.
 */
public class ModuleInstallationRawParameters {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleInstallationRawParameters.class);
    private String webAppConfigurationFiles;
    private String webAppScopes;
    private String projectAppConfigurationFile;
    private String serviceConfigurationFile;
    private String fsm;
    private String projectName;

    public ModuleInstallationRawParameters() {
        // I'm just sitting here, doing nothing but wasting lines.
    }

    public static ModuleInstallationRawParametersBuilder builder() {
        return new ModuleInstallationRawParametersBuilder();
    }

    public String getWebAppConfigurationFiles() {
        return this.webAppConfigurationFiles;
    }

    public String getWebAppScopes() {
        return this.webAppScopes;
    }

    public String getProjectAppConfigurationFile() {
        return this.projectAppConfigurationFile;
    }

    public String getServiceConfigurationFile() {
        return this.serviceConfigurationFile;
    }

    public String getFsm() {
        return this.fsm;
    }

    public String getProjectName() {
        return this.projectName;
    }

    public static class ModuleInstallationRawParametersBuilder {
        private String webAppConfigurationFiles;
        private String webAppScopes;
        private String projectAppConfigurationFile;
        private String serviceConfigurationFile;
        private String fsm;
        private String projectName;

        ModuleInstallationRawParametersBuilder() {
        }

        /**
         * This method creates a new instance of @see com.espirit.moddev.moduleinstaller.ModuleInstallationParameters
         * based on all setted parameters.
         * @return an instance of @see com.espirit.moddev.moduleinstaller.ModuleInstallationParameters
         */
        public ModuleInstallationParameters build() {
            File firstSpiritModule = new File(this.fsm);
            if (!firstSpiritModule.isFile() || !firstSpiritModule.exists()) {
                throw new IllegalArgumentException("Could not open .fsm file: " + firstSpiritModule.getPath());
            }
            if (projectName == null || projectName.trim().length() <= 0) {
                throw new IllegalArgumentException("Project name must be set");
            }

            File projectAppConfigFile = createAndValidateOptionalProjectAppConfigurationFile(this.projectAppConfigurationFile);
            Map<String, File> configurationFileForServiceName = getAndValidateStringFilesMap(this.serviceConfigurationFile);
            List<WebEnvironment.WebScope> splittedWebAppScopes = extractWebScopes(webAppScopes);
            Map<WebEnvironment.WebScope, File> webAppConfigurationFilesForWebScopes = getAndValidateWebScopeFileMap(this.webAppConfigurationFiles);
            return new ModuleInstallationParameters(projectName, firstSpiritModule, configurationFileForServiceName, projectAppConfigFile, splittedWebAppScopes, webAppConfigurationFilesForWebScopes);
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
            Set<Map.Entry<String, File>> entries = getStringFilesMap(webAppConfigurationFiles).entrySet();
            return entries.stream().collect(
                toMap(entry -> valueOf(entry.getKey().trim().toUpperCase(Locale.getDefault())), Map.Entry::getValue));
        }

        @VisibleForTesting
        Map<String, File> getStringFilesMap(String webAppConfigurations) {
            if(webAppConfigurations == null) {
                return new HashMap<>();
            }
            return Arrays.stream(webAppConfigurations.split(","))
                .map(propertyString -> propertyString.split("="))
                .collect(toMap(entry -> entry[0], entry -> new File(entry[1])));
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

        public ModuleInstallationRawParametersBuilder webAppConfigurationFiles(String webAppConfigurationFiles) {
            this.webAppConfigurationFiles = webAppConfigurationFiles;
            return this;
        }

        public ModuleInstallationRawParametersBuilder webAppScopes(String webAppScopes) {
            this.webAppScopes = webAppScopes;
            return this;
        }

        public ModuleInstallationRawParametersBuilder projectAppConfigurationFile(String projectAppConfigurationFile) {
            this.projectAppConfigurationFile = projectAppConfigurationFile;
            return this;
        }

        public ModuleInstallationRawParametersBuilder serviceConfigurationFile(String serviceConfigurationFile) {
            this.serviceConfigurationFile = serviceConfigurationFile;
            return this;
        }

        public ModuleInstallationRawParametersBuilder fsm(String fsm) {
            this.fsm = fsm;
            return this;
        }

        public ModuleInstallationRawParametersBuilder projectName(String projectName) {
            this.projectName = projectName;
            return this;
        }

        public String toString() {
            return "ModuleInstallationRawParameters.ModuleInstallationRawParametersBuilder(webAppConfigurationFiles=" + this.webAppConfigurationFiles + ", webAppScopes=" + this.webAppScopes + ", projectAppConfigurationFile=" + this.projectAppConfigurationFile + ", serviceConfigurationFile=" + this.serviceConfigurationFile + ", fsm=" + this.fsm + ", projectName=" + this.projectName + ")";
        }
    }
}
