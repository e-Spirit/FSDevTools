package com.espirit.moddev.moduleinstaller;

import de.espirit.firstspirit.module.WebEnvironment.WebScope;

import java.io.File;
import java.util.*;

public class ModuleInstallationParameters {
    private final String projectName;
    private final File fsm;
    private final Map<String, File> serviceConfigurations;
    private final File projectAppConfiguration;
    private final List<WebScope> webAppScopes;
    private final Map<WebScope, File> webAppConfigurations;

    /**
     * Instantiates a parameters object and uses empty configurations for services, project apps and webapps.
     *
     * @param projectName the name of the FirstSpirit project the module's components should be installed to
     * @param fsm         the module file (fsm)
     */
    public ModuleInstallationParameters(String projectName, File fsm) {
        this(projectName, fsm, new HashMap(), null, new ArrayList<>(), new HashMap<>());
    }

    /**
     * @param projectName              the name of the FirstSpirit project the module's components should be installed to
     * @param fsm                      the module file (fsm)
     * @param serviceConfigurations    configurations for the module's services
     * @param projectAppConfiguration  configuration file for the module's project app
     * @param webAppScopes             scope configurations for the module's webapp
     * @param webAppConfigurations     configurations for the module's webapps per scope
     */
    public ModuleInstallationParameters(String projectName, File fsm, Map<String, File> serviceConfigurations, File projectAppConfiguration, List<WebScope> webAppScopes, Map<WebScope, File> webAppConfigurations) {
        this.projectName = projectName;
        this.fsm = fsm;
        this.serviceConfigurations = serviceConfigurations != null ? serviceConfigurations : new HashMap<>();
        this.projectAppConfiguration = projectAppConfiguration != null ? projectAppConfiguration : null;
        this.webAppScopes = webAppScopes != null ? webAppScopes : new ArrayList<>();
        this.webAppConfigurations = webAppConfigurations != null ? webAppConfigurations : new HashMap<>();
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

    public List<WebScope> getWebAppScopes() {
        return webAppScopes;
    }

    public Map<WebScope, File> getWebAppConfigurations() {
        return webAppConfigurations;
    }
}
