package com.espirit.moddev.moduleinstaller;

import com.espirit.moddev.moduleinstaller.webapp.WebAppIdentifierParser;
import com.espirit.moddev.shared.StringUtils;

import de.espirit.common.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

import static de.espirit.firstspirit.module.WebEnvironment.WebScope.valueOf;
import static java.util.stream.Collectors.toMap;

/**
 * This class provides utility methods to parseMultiple String parameters
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

            File projectAppConfigFile = createAndValidateOptionalProjectAppConfigurationFile(this.projectAppConfigurationFile);
            Map<String, File> configurationFileForServiceName = getAndValidateStringFilesMap(this.serviceConfigurationFile);
            List<WebAppIdentifier> splittedWebAppScopes = extractWebScopes(webAppScopes);
            Map<WebAppIdentifier, File> webAppConfigurationFilesForWebScopes = getAndValidateWebScopeFileMap(this.webAppConfigurationFiles);
            return new ModuleInstallationParameters(projectName, firstSpiritModule, configurationFileForServiceName, projectAppConfigFile, splittedWebAppScopes, webAppConfigurationFilesForWebScopes);
        }

        @VisibleForTesting
        List<WebAppIdentifier> extractWebScopes(String webAppScopes) {
            if (StringUtils.isNullOrEmpty(webAppScopes)) {
                return new ArrayList<>();
            }
            try {
                WebAppIdentifierParser webAppIdentifierParser = new WebAppIdentifierParser();
                return webAppIdentifierParser.parseMultiple(webAppScopes);
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

        private Map<WebAppIdentifier, File> getAndValidateWebScopeFileMap(String webAppConfigurationFiles) {
            Map<WebAppIdentifier, File> webScopeFileMap = getWebScopeFileMap(webAppConfigurationFiles);
            validateWebScopeFileMap(webScopeFileMap);
            return webScopeFileMap;
        }

        private void validateWebScopeFileMap(Map<WebAppIdentifier, File> webScopeFileMap) {
            for(Map.Entry<WebAppIdentifier, File> entry : webScopeFileMap.entrySet()) {
                if(!entry.getValue().isFile() || !entry.getValue().exists()) {
                    throw new IllegalArgumentException("File for webapp configuration with scope " + entry.getKey() + " doesn't exist or is not a file.");
                }
            }
        }

        @VisibleForTesting
        Map<WebAppIdentifier, File> getWebScopeFileMap(String webAppConfigurationFiles) {
            Set<Map.Entry<String, File>> entries = getStringFilesMap(webAppConfigurationFiles).entrySet();
            return entries.stream().collect(
                toMap(entry -> new WebAppIdentifierParser().parseSingle(entry.getKey()), Map.Entry::getValue));
        }

        @VisibleForTesting
        Map<String, File> getStringFilesMap(String webAppConfigurations) {
            if(StringUtils.isNullOrEmpty(webAppConfigurations)) {
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
