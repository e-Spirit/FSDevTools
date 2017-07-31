package com.espirit.moddev.moduleinstaller;

import de.espirit.firstspirit.access.AdminService;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.admin.ProjectStorage;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.access.store.LockException;
import de.espirit.firstspirit.agency.ModuleAdminAgent;
import de.espirit.firstspirit.agency.ModuleAdminAgent.ModuleResult;
import de.espirit.firstspirit.agency.WebAppId;
import de.espirit.firstspirit.io.FileHandle;
import de.espirit.firstspirit.io.FileSystem;
import de.espirit.firstspirit.io.ServerConnection;
import de.espirit.firstspirit.manager.*;
import de.espirit.firstspirit.module.*;
import de.espirit.firstspirit.module.WebEnvironment.WebScope;
import de.espirit.firstspirit.module.descriptor.*;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

import static de.espirit.firstspirit.module.WebEnvironment.WebScope.PREVIEW;
import static de.espirit.firstspirit.module.WebEnvironment.WebScope.STAGING;
import static de.espirit.firstspirit.module.WebEnvironment.WebScope.WEBEDIT;
import static de.espirit.firstspirit.module.descriptor.ComponentDescriptor.Type.SERVICE;

/**
 * This class can install modules and module configurations.
 */
public class ModuleInstaller {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ModuleInstaller.class);


    /**
     * Instantiates a {@link ModuleInstaller}. Doesn't do anything else.
     */
    public ModuleInstaller() {
        // Nothing to do here
    }

    /**
     * Method for installing a given FirstSpirit module (only the module itself will be installed, no components will be added to any project).
     *
     * @param fsm        The path to the FirstSpirit module file (fsm) to be installed
     * @param connection A {@link Connection} to the server the module shall be installed to
     * @return An optional ModuleResult. Result might be absent when there's an exception with the fsm file stream.
     */
    private static Optional<ModuleResult> installModule(final File fsm, final Connection connection) {
        LOGGER.info("Starting module installation");
        ModuleAdminAgent moduleAdminAgent = connection.getBroker().requireSpecialist(ModuleAdminAgent.TYPE);
        boolean updateUsages = true;

        try (FileInputStream fsmStream = new FileInputStream(fsm)) {
            ModuleResult result = moduleAdminAgent.install(fsmStream, updateUsages);
            moduleAdminAgent.setTrusted(result.getDescriptor().getName(), true);
            return Optional.of(result);
        } catch (IOException e) {
            LOGGER.error("Exception during module installation!", e);
        }
        return Optional.empty();
    }

    /**
     * Method for activating auto start of services of a given module
     * @param connection A {@link Connection} to the server
     * @param parameters
     * @param descriptor the module descriptor
     */
    private static void activateServices(final Connection connection, ModuleInstallationParameters parameters, ModuleDescriptor descriptor) {
        ProjectStorage projectStorage = connection.getService(AdminService.class).getProjectStorage();

        Project project = projectStorage.getProject(parameters.getProjectName());
        if(project == null) {
            throw new IllegalArgumentException("Project " + parameters.getProjectName() + " not found!");
        }
        long projectId = project.getId();
        LOGGER.info("ModuleInstaller activateServices ...");

        ModuleAdminAgent moduleAdminAgent = connection.getBroker().requireSpecialist(ModuleAdminAgent.TYPE);
        String moduleName = descriptor.getModuleName();
        Optional<ModuleDescriptor> moduleDescriptor = moduleAdminAgent.getModules().stream().filter(it -> it.getName().equals(moduleName)).findFirst();

        if(!moduleDescriptor.isPresent()) {
            LOGGER.info("ModuleDescriptor not present!");
        }

        final ComponentDescriptor[] componentDescriptors = descriptor.getComponents();
        if (componentDescriptors == null) {
            LOGGER.error("No components found for module: {}", moduleName);
        } else {
            Arrays.stream(componentDescriptors).filter(it -> it.getType().equals(SERVICE)).forEach(serviceDescriptor -> {
                LOGGER.info("Found service " + serviceDescriptor.getName());
                File configuration = parameters.getServiceConfigurations().get(serviceDescriptor.getName());
                if(configuration != null) {
                    createConfigurationFile(SERVICE, connection, serviceDescriptor, configuration, moduleName, projectId, null);
                    setAutostartAndRestartService(moduleAdminAgent, serviceDescriptor);
                } else {
                    LOGGER.info("No configuration found for service " + serviceDescriptor.getName());
                }
            });
        }
    }

    private static void setAutostartAndRestartService(ModuleAdminAgent moduleAdminAgent,
                                                      ComponentDescriptor componentDescriptor) {
        String componentDescriptorName = componentDescriptor.getName();
        moduleAdminAgent.setAutostart(componentDescriptorName, true);
        LOGGER.info("Stopping service {}", componentDescriptorName);
        moduleAdminAgent.stopService(componentDescriptorName);
        LOGGER.info("Starting service {}", componentDescriptorName);
        moduleAdminAgent.startService(componentDescriptorName);
        LOGGER.info("Service {} running: {}", componentDescriptorName, moduleAdminAgent.isRunning(componentDescriptorName));
    }

    /**
     * Convenience method for copying the configuration files from the module to the server-dirs
     * @param type                  Type of the module whose configuration should be written e.g. Service, ProjectApp
     * @param connection            A {@link Connection} to the server
     * @param componentDescriptor   The component from the module.xml to use
     * @param configurationFile The map from the pom.xml that includes the configuration files
     * @param moduleName            The name of the module whose configuration should be written (nullable)
     * @param projectId             The id of the project the project applications shall be installed to
     * @param webAppId              The webAppId to use - only used by webapp configurations
     */
    private static void createConfigurationFile(ComponentDescriptor.Type type,
                                                Connection connection,
                                                ComponentDescriptor componentDescriptor,
                                                File configurationFile,
                                                String moduleName,
                                                long projectId, WebAppId webAppId) {
        LOGGER.info("Config created, preparing for saving");
        Optional<FileSystem<?>> fsOptional = getFileSystemForConfigurationType(type, connection, componentDescriptor, moduleName, projectId, webAppId);
        fsOptional.ifPresent(fs -> {
            LOGGER.info("Obtaining handle");
            FileHandle handle = null;
            try {
                handle = fs.obtain(getConfigFileName(componentDescriptor) + ".ini");
                LOGGER.info("Saving handle to " + handle.getPath());
                handle.save(new FileInputStream(configurationFile));
            } catch (IOException e) {
                LOGGER.error("Cannot obtain and save file handle!", e);
            }
        });

        LOGGER.info("Configuration files created");
    }

    private static Optional<FileSystem<?>> getFileSystemForConfigurationType(ComponentDescriptor.Type type, Connection connection, ComponentDescriptor componentDescriptor, String moduleName, long projectId, WebAppId webAppId) {
        ModuleAdminAgent moduleAdminAgent = connection.getBroker().requestSpecialist(ModuleAdminAgent.TYPE);
        ProjectStorage projectStorage = connection.getService(AdminService.class).getProjectStorage();
        FileSystem<?> fs = null;

        if (type.equals(SERVICE)) {
//          TODO: replace exception with uncommented function - uncomment when available
            throw new IllegalStateException("Service configuration not yet implemented.");
//            fs = moduleAdminAgent.getServiceConfig(componentDescriptor.getName());

        } else if (type.equals(ComponentDescriptor.Type.PROJECTAPP)) {
            fs = moduleAdminAgent.getProjectAppConfig(moduleName, componentDescriptor.getName(), projectStorage.getProject(projectId));
        } else if (type.equals(ComponentDescriptor.Type.WEBAPP)) {
            LOGGER.info("ComponentDescriptor: " + componentDescriptor.getName());
            fs = moduleAdminAgent.getWebAppConfig(moduleName, componentDescriptor.getName(),  webAppId);
        }
        return Optional.ofNullable(fs);
    }

    /**
     * Method for installing the project applications of a given module into a given project
     *
     * @param connection A {@link Connection} to the server
     * @param moduleName The name of the module whose project applications shall be installed
     * @param parameters
     */
    private static void installProjectApps(final Connection connection, final String moduleName, final ModuleInstallationParameters parameters) {
        Project project = connection.getProjectByName(parameters.getProjectName());
        LOGGER.info("Installing project apps for {} project {}", moduleName, project.getName());
        ModuleAdminAgent moduleAdminAgent = connection.getBroker().requireSpecialist(ModuleAdminAgent.TYPE);
        Optional<ModuleDescriptor> moduleDescriptor = getModuleDescriptor(moduleAdminAgent, moduleName);
        if (moduleDescriptor.isPresent()) {
            Arrays.asList(moduleDescriptor.get().getComponents()).stream().filter(it -> it instanceof ProjectAppDescriptor).forEach(projectAppDescriptor -> {
                LOGGER.info("ProjectDescriptor {} is processed", projectAppDescriptor.getName());

                FileSystem<?> projectAppConfig = null;
                try {
                    projectAppConfig = getFileSystemForConfigurationType(projectAppDescriptor.getType(), connection, projectAppDescriptor, moduleName, project.getId(), null).get();
                } catch (IllegalArgumentException e) {
                    LOGGER.info("projectAppConfig can not be obtained so it is created");
                    LOGGER.debug("", e);
                }
                if (projectAppConfig != null) {
                    LOGGER.info("Existing project: {} app config - updating with the given configuration!", project.getName(), moduleName);
                }
                LOGGER.info("Install ProjectApp");
                moduleAdminAgent.installProjectApp(moduleName, projectAppDescriptor.getName(), project);
                LOGGER.info("Create configuration files");
                parameters.getProjectAppConfiguration().ifPresent(projectAppFile -> {
                    createConfigurationFile(ComponentDescriptor.Type.PROJECTAPP, connection, projectAppDescriptor, projectAppFile, moduleName, project.getId(), null);
                });
            });
        } else {
            LOGGER.error("No descriptor for {} found!", moduleName);
        }
        LOGGER.info("Installing project apps finished");
    }

    protected static Optional<ModuleDescriptor> getModuleDescriptor(ModuleAdminAgent moduleAdminAgent, String moduleName) {
        for (ModuleDescriptor moduleDescriptor : moduleAdminAgent.getModules()) {
            if (moduleDescriptor.getModuleName().equals(moduleName)) {
                return Optional.of(moduleDescriptor);
            }
        }
        return Optional.empty();
    }

    /**
     * Method for installing the web applications of a given module into a given project
     *  @param connection A {@link Connection} to the server
     * @param parameters parameters containing the specific entries from the config files
     * @param moduleName The name of the module whose web applications shall be installed
     * @return success indicator
     */
    public static boolean installProjectWebApps(final Connection connection, ModuleDescriptor moduleDescriptor, ModuleInstallationParameters parameters, final String moduleName) {
        ProjectStorage projectStorage = connection.getService(AdminService.class).getProjectStorage();
        ModuleAdminAgent moduleAdminAgent = connection.getBroker().requestSpecialist(ModuleAdminAgent.TYPE);

        long projectId = projectStorage.getProject(parameters.getProjectName()).getId();
        installWebAppsAndCreateConfig(connection, moduleName, projectId, moduleAdminAgent, moduleDescriptor.getComponents(), parameters.getWebAppScopes(), parameters.getWebAppConfigurations());

        return deployWebAppsForScopes(connection, moduleName, projectId, moduleAdminAgent, moduleDescriptor.getComponents());
    }

    private static boolean deployWebAppsForScopes(Connection connection, String moduleName, long projectId, ModuleAdminAgent moduleAdminAgent, ComponentDescriptor[] componentDescriptors) {
        LOGGER.info("Installing Project WebApps");
        WebScope[] webScopes = {PREVIEW, STAGING, WEBEDIT};
        for (ComponentDescriptor componentDescriptor : componentDescriptors) {
            if (ComponentDescriptor.Type.WEBAPP.equals(componentDescriptor.getType())
                && !deployWebApps(connection, moduleName, projectId, moduleAdminAgent, webScopes, componentDescriptor)) {

                return false;
            }
        }
        return true;
    }

    private static boolean deployWebApps(Connection connection, String moduleName, long projectId, ModuleAdminAgent moduleAdminAgent,
                                         WebScope[] webScopes, ComponentDescriptor componentDescriptor) {
        for (WebAppId webAppId: moduleAdminAgent.getWebAppUsages(moduleName, componentDescriptor.getName())) {
            Optional<Boolean>
                failed = Arrays.stream(webScopes).map(scope -> installWebAppAndActivateWebServer(scope, projectId, connection, webAppId)).filter(it -> !it).findAny();
            if (!failed.isPresent()) {
                LOGGER.error("Cannot install WebApp for specific scope!  ID: {}", webAppId );
                return false;
            }
        }
        return true;
    }

    private static boolean installWebAppAndActivateWebServer(final WebScope webScope, final long projectId,
                                                             final Connection connection, final WebAppId webAppId) {

        final Project project = connection.getProjectById(projectId);

        try {
            ModuleAdminAgent moduleAdminAgent = connection.getBroker().requireSpecialist(ModuleAdminAgent.TYPE);
            boolean successfullyDeployed = moduleAdminAgent.deployWebApp(webAppId);
            if (!setActiveWebServerForProject(webScope, project) || !successfullyDeployed){
                return false;
            }

        } catch (IllegalStateException ise) {
            LOGGER.error("Cannot deploy war file!", ise);
            return false;
        }
        return true;
    }

    private static void installWebAppsAndCreateConfig(Connection connection, String moduleName, long projectId, ModuleAdminAgent moduleAdminAgent, ComponentDescriptor[] componentDescriptors, List<WebScope> webAppScopes, Map<WebScope, File> webAppConfigurations) {
        LOGGER.info("Creating WebApp configuration files");
        Arrays.stream(componentDescriptors).filter(it -> ComponentDescriptor.Type.WEBAPP.equals(it.getType())).forEach(componentDescriptor -> {
            for (WebScope scope : webAppScopes) {
                if (webAppConfigurations.containsKey(scope) && !scope.equals(WebScope.GLOBAL)) {
                    try {
                        WebAppId id = WebAppId.Factory.create(connection.getProjectById(projectId), scope);
                        moduleAdminAgent.installWebApp(moduleName, componentDescriptor.getName(), id);
                        createConfigurationFile(ComponentDescriptor.Type.WEBAPP,
                                                connection,
                                                componentDescriptor,
                                                webAppConfigurations.get(scope),
                                                moduleName, projectId,
                                                id);

                        LOGGER.info("WebAppScope: " + scope);
                    } catch (IllegalArgumentException e) {
                        LOGGER.error("Invalid Scope " + scope, e);
                    }
                }
            }
        });
    }

    private static String getConfigFileName(ComponentDescriptor componentDescriptor) {
        String componentDescriptorName = null;
        if (componentDescriptor.getName() != null) {
            componentDescriptorName = componentDescriptor.getName().replace(" ", "_").replaceFirst("-fsm-projectApp", "");
        }
        return componentDescriptorName;
    }

    private static boolean setActiveWebServerForProject(WebScope webScope, Project project) {
        try {
            project.lock();
            project.setActiveWebServer(webScope.toString(), project.getSelectedWebServer(webScope.toString()));
            project.save();
            project.unlock();
            return true;
        } catch (LockException e) {
            LOGGER.error("Cannot lock and save project!", e);
            return false;
        }
    }

    /**
     * Installs a module on a FirstSpirit server. Uses the given connection.
     * If any of the configured components is already installed, it is updated.
     *
     * @param connection a connected FirstSpirit connection that is used to install the module
     * @param parameters a parameter bean that defines how the module should be installed
     * @return a boolean to as success indicator
     */
    public boolean install(Connection connection, ModuleInstallationParameters parameters) {
        if (connection == null || !connection.isConnected()) {
            throw new IllegalStateException("Connection is null or not connected!");
        }
        if (parameters.getProjectName() == null || parameters.getProjectName().isEmpty()) {
            throw new IllegalArgumentException("Project name is null or empty!");
        }

        Optional<ModuleResult> moduleResultOption = installModule(parameters.getFsm(), connection);
        if(moduleResultOption.isPresent()) {
            activateServices(connection, parameters, moduleResultOption.get().getDescriptor());

            String moduleName = moduleResultOption.get().getDescriptor().getName();
            LOGGER.info("Finished module installation for {}", moduleName);

            installProjectApps(connection, moduleName, parameters);
            ModuleAdminAgent moduleAdminAgent = connection.getBroker().requestSpecialist(ModuleAdminAgent.TYPE);
            Optional<ModuleDescriptor> moduleDescriptor = moduleAdminAgent.getModules().stream().filter(it -> it.getName().equals(moduleName)).findFirst();
            if(!moduleDescriptor.isPresent()) {
                LOGGER.info("ModuleDescriptor not present!");
                return false;
            }

            boolean webAppsSuccessfullyInstalled = installProjectWebApps(connection, moduleDescriptor.get(), parameters, moduleName);
            if(!webAppsSuccessfullyInstalled) {
                LOGGER.error("WebApp installation and activation not successful for module {}", moduleName);
                return false;
            }
            return true;
        }
        return false;
    }
}
