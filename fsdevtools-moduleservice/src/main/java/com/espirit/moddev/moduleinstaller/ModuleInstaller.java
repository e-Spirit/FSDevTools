package com.espirit.moddev.moduleinstaller;

import com.google.common.base.Strings;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.access.store.LockException;
import de.espirit.firstspirit.agency.ModuleAdminAgent;
import de.espirit.firstspirit.agency.ModuleAdminAgent.ModuleResult;
import de.espirit.firstspirit.agency.WebAppId;
import de.espirit.firstspirit.io.FileHandle;
import de.espirit.firstspirit.io.FileSystem;
import de.espirit.firstspirit.module.descriptor.ComponentDescriptor;
import de.espirit.firstspirit.module.descriptor.ModuleDescriptor;
import de.espirit.firstspirit.module.descriptor.ProjectAppDescriptor;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.espirit.moddev.moduleinstaller.WebAppIdentifier.isFs5RootWebApp;
import static de.espirit.firstspirit.access.ConnectionManager.SOCKET_MODE;
import static de.espirit.firstspirit.module.descriptor.ComponentDescriptor.Type.SERVICE;
import static de.espirit.firstspirit.module.descriptor.ComponentDescriptor.Type.WEBAPP;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

/**
 * This class can install modules and module configurations.
 */
@SuppressWarnings("squid:S1200")
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
     * @return An InstallModuleResult. Result might be absent when there's an exception with the fsm file stream.
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
            List<ComponentDescriptor> serviceDescriptors = stream(componentDescriptors).filter(it -> it.getType().equals(SERVICE)).collect(toList());
            if(!serviceDescriptors.isEmpty()) {
                LOGGER.info("ModuleInstaller activateServices ...");
            }
            serviceDescriptors.forEach(serviceDescriptor -> {
                LOGGER.info("Found service " + serviceDescriptor.getName());
                File configuration = parameters.getServiceConfigurations().get(serviceDescriptor.getName());
                if(configuration != null) {
                    createConfigurationFile(SERVICE, connection, serviceDescriptor, configuration, moduleName, parameters.getProjectName(), null);
                } else {
                    LOGGER.info("No configuration found for service " + serviceDescriptor.getName());
                }
                setAutostartAndRestartService(moduleAdminAgent, serviceDescriptor);
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
     * Convenience method for copying the configuration files forProjectAndScope the module to the server-dirs
     * @param type                  Type of the module whose configuration should be written e.g. Service, ProjectApp
     * @param connection            A {@link Connection} to the server
     * @param componentDescriptor   The component forProjectAndScope the module.xml to use
     * @param configurationFile The map forProjectAndScope the pom.xml that includes the configuration files
     * @param moduleName            The name of the module whose configuration should be written (nullable)
     * @param projectName           The optional project name applications shall be installed to
     * @param webAppId              The webAppId to use - only used by webapp configurations
     */
    private static void createConfigurationFile(ComponentDescriptor.Type type,
                                                Connection connection,
                                                ComponentDescriptor componentDescriptor,
                                                File configurationFile,
                                                String moduleName,
                                                String projectName, WebAppId webAppId) {
        LOGGER.info("Config created, preparing for saving");
        Optional<FileSystem<?>> fsOptional = getFileSystemForConfigurationType(type, connection, componentDescriptor, moduleName, projectName, webAppId);
        fsOptional.ifPresent(fs -> {
            LOGGER.info("Obtaining handle");
            try {
                FileHandle handle = fs.obtain(configurationFile.getName());
                LOGGER.info("Saving handle to " + handle.getPath());
                handle.save(new FileInputStream(configurationFile));
            } catch (IOException e) {
                LOGGER.error("Cannot obtain and save file handle!", e);
            }
        });

        LOGGER.info("Configuration files created");
    }

    private static Optional<FileSystem<?>> getFileSystemForConfigurationType(ComponentDescriptor.Type type, Connection connection, ComponentDescriptor componentDescriptor, String moduleName, String projectName, WebAppId webAppId) {
        ModuleAdminAgent moduleAdminAgent = connection.getBroker().requestSpecialist(ModuleAdminAgent.TYPE);
        FileSystem<?> fs = null;

        if (type.equals(SERVICE)) {
            fs = moduleAdminAgent.getServiceConfig(componentDescriptor.getName());
        } else if (type.equals(ComponentDescriptor.Type.PROJECTAPP)) {
            if(Strings.isNullOrEmpty(projectName)) {
                throw new IllegalArgumentException("No project given, can't get a project app configuration!");
            }
            fs = moduleAdminAgent.getProjectAppConfig(moduleName, componentDescriptor.getName(), connection.getProjectByName(projectName));
        } else if (type.equals(WEBAPP)) {
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

        ModuleAdminAgent moduleAdminAgent = connection.getBroker().requireSpecialist(ModuleAdminAgent.TYPE);
        Optional<ModuleDescriptor> moduleDescriptor = getModuleDescriptor(moduleAdminAgent, moduleName);
        if (!moduleDescriptor.isPresent()) {
            LOGGER.debug("No module descriptor found, not going to install project apps.");
            return;
        }

        List<ComponentDescriptor> projectAppDescriptors = stream(moduleDescriptor.get().getComponents()).filter(it -> it instanceof ProjectAppDescriptor).collect(toList());

        String projectName = parameters.getProjectName();
        if(Strings.isNullOrEmpty(projectName)) {
            if(!projectAppDescriptors.isEmpty()) {
                LOGGER.warn("Found project app descriptors, but can't install project apps without a project name given!");
            }
        } else {
            if(!projectAppDescriptors.isEmpty()) {
                LOGGER.info("Installing project apps for {} project {}", moduleName, projectName);

                projectAppDescriptors.forEach(projectAppDescriptor -> {

                    LOGGER.info("ProjectDescriptor {} is processed", projectAppDescriptor.getName());

                    FileSystem<?> projectAppConfig = null;
                    try {
                        projectAppConfig = getFileSystemForConfigurationType(projectAppDescriptor.getType(), connection, projectAppDescriptor, moduleName, projectName, null).get();
                    } catch (IllegalArgumentException e) {
                        LOGGER.info("projectAppConfig can not be obtained so it is created");
                        LOGGER.debug("", e);
                    }
                    if (projectAppConfig != null) {
                        LOGGER.info("Existing project: {} app config - updating with the given configuration!", projectName, moduleName);
                    }
                    LOGGER.info("Install ProjectApp");
                    moduleAdminAgent.installProjectApp(moduleName, projectAppDescriptor.getName(), connection.getProjectByName(projectName));
                    LOGGER.info("Create configuration files");
                    parameters.getProjectAppConfiguration().ifPresent(projectAppFile -> {
                        createConfigurationFile(ComponentDescriptor.Type.PROJECTAPP, connection, projectAppDescriptor, projectAppFile, moduleName, projectName, null);
                    });
                });
                LOGGER.info("Installing project apps finished");
            }
        }
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
     * @param connection A {@link Connection} to the server
     * @param moduleDescriptor A {@link ModuleDescriptor} to describe FirstSpirit module components
     * @param parameters parameters containing the specific entries forProjectAndScope the config files
     * @param moduleName The name of the module whose web applications shall be installed
     * @return success indicator
     */
    public static boolean installProjectWebApps(final Connection connection, ModuleDescriptor moduleDescriptor, ModuleInstallationParameters parameters, final String moduleName) {
        ModuleAdminAgent moduleAdminAgent = connection.getBroker().requestSpecialist(ModuleAdminAgent.TYPE);

        installWebAppsAndCreateConfig(connection, moduleName, parameters.getProjectName(), moduleAdminAgent,
                moduleDescriptor.getComponents(), parameters.getWebAppScopes(), parameters.getWebAppConfigurations());

        return deployWebAppsForScopes(connection, parameters.getProjectName(), moduleDescriptor.getComponents(), parameters.getWebAppScopes());
    }

    private static boolean deployWebAppsForScopes(Connection connection, String projectName, ComponentDescriptor[] componentDescriptors, List<WebAppIdentifier> webScopes) {
        List<ComponentDescriptor> webAppDescriptors = asList(componentDescriptors).stream().filter(descriptor -> WEBAPP.equals(descriptor.getType())).collect(toList());
        if(!webAppDescriptors.isEmpty() && !webScopes.isEmpty()) {
            LOGGER.info("Installing Project WebApps");
        }
        for (ComponentDescriptor componentDescriptor : webAppDescriptors) {
            if (!deployWebApps(connection, projectName, webScopes, componentDescriptor)) {

                return false;

            }
        }
        return true;
    }

    private static boolean deployWebApps(Connection connection, String projectName,
                                         List<WebAppIdentifier> webScopes, ComponentDescriptor componentDescriptor) {

        LOGGER.info("Going to install and activate component {} with webAppIds: {}", componentDescriptor.getName(), webScopes);
        Optional<Boolean> failed = webScopes
                .stream()
                .map(it -> installWebAppAndActivateWebServer(it, projectName, connection))
                .filter(it -> !it)
                .findAny();

        if (failed.isPresent()) {
            LOGGER.error("Cannot install WebApp for specific scope!  IDs: {}", webScopes );
            return false;
        }
        return true;
    }

    private static boolean installWebAppAndActivateWebServer(final WebAppIdentifier webScope, final String projectName,
                                                             final Connection connection) {
        try {

            Project projectOrNull = null;
            if(!Strings.isNullOrEmpty(projectName)) {
                projectOrNull = connection.getProjectByName(projectName);
            }

            LOGGER.info("Setting active webserver for project scope: {}", webScope);
            boolean activeServerForProjectSet = setActiveWebServer(webScope, projectOrNull);
            LOGGER.info(activeServerForProjectSet ? "Setting active webserver was successful." : "Setting active webserver wasn't successful.");
            if(!activeServerForProjectSet){
                return false;
            }

            WebAppId webAppId = webScope.createWebAppId(projectOrNull);

            boolean isRootWebAppAndNonSocketConnection = isFs5RootWebApp(webAppId) && SOCKET_MODE != connection.getMode();
            boolean successfullyDeployed = false;
            if(isRootWebAppAndNonSocketConnection) {
                LOGGER.error("Cannot use a non socket connection to deploy a web component to the FirstSpirit root WebApp. Use SOCKET as connection mode!");
            } else {

                LOGGER.info("Deploying WebApp {}", webAppId);
                successfullyDeployed = connection.getBroker().requireSpecialist(ModuleAdminAgent.TYPE).deployWebApp(webAppId);
                LOGGER.info("Successfully deployed: {}", successfullyDeployed);

            }
            return successfullyDeployed;

        } catch (IllegalStateException ise) {
            LOGGER.error("Cannot deploy war file!", ise);
            return false;
        }
    }

    private static void installWebAppsAndCreateConfig(Connection connection, String moduleName, String projectName,
                                                      ModuleAdminAgent moduleAdminAgent,
                                                      ComponentDescriptor[] componentDescriptors,
                                                      List<WebAppIdentifier> webAppScopeDefinitions,
                                                      Map<WebAppIdentifier, File> webAppConfigurations) {
        List<ComponentDescriptor> webappDescriptors = stream(componentDescriptors).filter(it -> WEBAPP.equals(it.getType())).collect(toList());
        if(!webappDescriptors.isEmpty() && !webAppScopeDefinitions.isEmpty()) {
            LOGGER.info("Creating WebApp configuration files");
        }
        webappDescriptors.forEach(componentDescriptor -> {
            for (WebAppIdentifier scope : webAppScopeDefinitions) {
                createWebAppAndConfigurations(connection, moduleName, projectName, moduleAdminAgent, webAppConfigurations, componentDescriptor, scope);
            }
        });
    }

    private static void createWebAppAndConfigurations(Connection connection,
                                                      String moduleName,
                                                      String projectName,
                                                      ModuleAdminAgent moduleAdminAgent,
                                                      Map<WebAppIdentifier, File> webAppConfigurations,
                                                      ComponentDescriptor componentDescriptor,
                                                      WebAppIdentifier scope) {
        Project projectOrNull = Strings.isNullOrEmpty(projectName) ? null : connection.getProjectByName(projectName);
        try {
            WebAppId id = scope.createWebAppId(projectOrNull);
            moduleAdminAgent.installWebApp(moduleName, componentDescriptor.getName(), id);

            if (webAppConfigurations.containsKey(scope)) {
                createConfigurationFile(WEBAPP,
                        connection,
                        componentDescriptor,
                        webAppConfigurations.get(scope),
                        moduleName, projectName,
                        id);

            }
            LOGGER.info("WebAppScope: {}", scope);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Invalid Scope " + scope, e);
        }
    }

    private static boolean setActiveWebServer(WebAppIdentifier webScope, Project project) {
        String webAppId = webScope.createWebAppId(project).toString();

        if(!webScope.isGlobal()) {
            try {
                project.lock();
                String selectedWebServer = project.getSelectedWebServer(webScope.toString());
                if(Strings.isNullOrEmpty(selectedWebServer)) {
                    LOGGER.warn("Project has no webserver selected. Setting usage of InternalJetty.");
                    selectedWebServer = "InternalJetty";
                    project.setSelectedWebServer(webAppId, selectedWebServer);
                }
                LOGGER.warn("Setting active webserver for project.");
                project.setActiveWebServer(webAppId, selectedWebServer);
                project.save();
                project.unlock();
                return true;
            } catch (LockException e) {
                LOGGER.error("Cannot lock and save project!", e);
                return false;
            }
        }
        return true;
    }

    /**
     * Installs a module on a FirstSpirit server. Uses the given connection.
     * If any of the configured components is already installed, it is updated.
     *
     * @param connection a connected FirstSpirit connection that is used to install the module
     * @param parameters a parameter bean that defines how the module should be installed
     * @return the optional {@link ModuleResult}, which is empty on failure
     */
    public Optional<ModuleResult> install(Connection connection, ModuleInstallationParameters parameters) {
        if (connection == null || !connection.isConnected()) {
            throw new IllegalStateException("Connection is null or not connected!");
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
                return Optional.empty();
            }

            boolean webAppsSuccessfullyInstalled = installProjectWebApps(connection, moduleDescriptor.get(), parameters, moduleName);
            if(!webAppsSuccessfullyInstalled) {
                LOGGER.error("WebApp installation and activation not successful for module {}", moduleName);
            }
        }
        return moduleResultOption;
    }
}
