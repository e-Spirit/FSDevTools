package com.espirit.moddev.moduleinstaller;

import com.espirit.moddev.moduleinstaller.ModuleInstallationRawParameters.ModuleInstallationRawParametersBuilder;
import com.espirit.moddev.shared.webapp.WebAppIdentifier;

import java.io.File;
import java.util.*;

public class ModuleInstallationParameters {
    private final String projectName;
    private final File fsm;
    private final Map<String, File> serviceConfigurations;
    private final File projectAppConfiguration;
    private final List<WebAppIdentifier> webAppScopes;
    private final Map<WebAppIdentifier, File> webAppConfigurations;

    /**
     * Instantiates a parameters object and uses empty configurations for services, project apps and webapps.
     *
     * @param projectName the name of the FirstSpirit project the module's components should be installed to
     * @param fsm         the module file (fsm)
     * @deprecated please use the {@link ModuleInstallationRawParametersBuilder} instead.
     */
    @Deprecated
    public ModuleInstallationParameters(String projectName, File fsm) {
        this(projectName, fsm, new HashMap(), null, new ArrayList<>(), new HashMap<>());
    }

    /**
     * @param projectName              the optional name of the FirstSpirit project the module's components should be installed to
     * @param fsm                      the module file (fsm)
     * @param serviceConfigurations    configurations for the module's services
     * @param projectAppConfiguration  configuration file for the module's project app
     * @param webAppScopeDefinitions   scope configurations for the module's webapp
     * @param webAppConfigurations     configurations for the module's webapps per scope
     */
    public ModuleInstallationParameters(String projectName, File fsm, Map<String, File> serviceConfigurations, File projectAppConfiguration, List<WebAppIdentifier> webAppScopeDefinitions, Map<WebAppIdentifier, File> webAppConfigurations) {
        this.projectName = projectName;
        this.fsm = fsm;
        this.serviceConfigurations = serviceConfigurations != null ? serviceConfigurations : new HashMap<>();
        this.projectAppConfiguration = projectAppConfiguration;
        this.webAppScopes = webAppScopeDefinitions != null ? webAppScopeDefinitions : new ArrayList<>();
        this.webAppConfigurations = webAppConfigurations != null ? webAppConfigurations : new HashMap<>();
    }

    public static ModuleInstallationParametersBuilder builder() {
        return new ModuleInstallationParametersBuilder();
    }

    public String getProjectName() {
        return projectName;
    }

    public File getFsm() {
        return fsm;
    }

    public Map<String, File> getServiceConfigurations() {
        return serviceConfigurations;
    }

    public Optional<File> getProjectAppConfiguration() {
        return Optional.ofNullable(projectAppConfiguration);
    }

    public List<WebAppIdentifier> getWebAppScopes() {
        return Collections.unmodifiableList(webAppScopes);
    }

    public Map<WebAppIdentifier, File> getWebAppConfigurations() {
        return webAppConfigurations;
    }

    public static class ModuleInstallationParametersBuilder {
        private String projectName;
        private File fsm;
        private Map<String, File> serviceConfigurations;
        private File projectAppConfiguration;
        private List<WebAppIdentifier> webAppScopeDefinitions;
        private Map<WebAppIdentifier, File> webAppConfigurations;

        ModuleInstallationParametersBuilder() {
        }

        public ModuleInstallationParametersBuilder projectName(String projectName) {
            this.projectName = projectName;
            return this;
        }

        public ModuleInstallationParametersBuilder fsm(File fsm) {
            this.fsm = fsm;
            return this;
        }

        public ModuleInstallationParametersBuilder serviceConfigurations(Map<String, File> serviceConfigurations) {
            this.serviceConfigurations = serviceConfigurations;
            return this;
        }

        public ModuleInstallationParametersBuilder projectAppConfiguration(File projectAppConfiguration) {
            this.projectAppConfiguration = projectAppConfiguration;
            return this;
        }
        
        @SuppressWarnings("squid:S2384")
        public ModuleInstallationParametersBuilder webAppScopes(List<WebAppIdentifier> webAppScopes) {
            this.webAppScopeDefinitions = webAppScopes;
            return this;
        }

        public ModuleInstallationParametersBuilder webAppConfigurations(Map<WebAppIdentifier, File> webAppConfigurations) {
            this.webAppConfigurations = webAppConfigurations;
            return this;
        }

        public ModuleInstallationParameters build() {
            return new ModuleInstallationParameters(projectName, fsm, serviceConfigurations, projectAppConfiguration, webAppScopeDefinitions, webAppConfigurations);
        }

        public String toString() {
            return "ModuleInstallationParameters.ModuleInstallationParametersBuilder(projectName=" + this.projectName + ", fsm=" + this.fsm + ", serviceConfigurations=" + this.serviceConfigurations + ", projectAppConfiguration=" + this.projectAppConfiguration + ", webAppScopeDefinitions=" + this.webAppScopeDefinitions + ", webAppConfigurations=" + this.webAppConfigurations + ")";
        }
    }
}
