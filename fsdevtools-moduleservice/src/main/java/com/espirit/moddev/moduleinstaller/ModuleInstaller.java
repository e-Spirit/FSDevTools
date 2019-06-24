package com.espirit.moddev.moduleinstaller;

import com.espirit.moddev.shared.StringUtils;
import com.espirit.moddev.shared.webapp.WebAppIdentifier;
import de.espirit.common.tools.Strings;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.ServerConfiguration;
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
import de.espirit.firstspirit.server.module.WebAppType;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.espirit.moddev.shared.webapp.WebAppIdentifier.isFs5RootWebApp;
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

    private static boolean setActiveWebServer(final ServerConfiguration serverConfiguration, final WebAppIdentifier webScope, final Project project) {
        if (webScope.isGlobal()) {
            return true;
        }
        final String scopeName = webScope.getScope().name();
        String activeWebServer = project.getActiveWebServer(scopeName);
        if (StringUtils.isNullOrEmpty(project.getActiveWebServer(scopeName))) {
            activeWebServer = serverConfiguration.getActiveWebserverConfiguration(WebAppType.FS5ROOT.getId());
            if (StringUtils.isNullOrEmpty(activeWebServer)) {
                LOGGER.warn("Project has no active web server. Using default webserver of global root.");
            } else {
                LOGGER.warn("Project has no active web server. Using webserver '" + activeWebServer + "' of global root.");
            }
            try {
                project.lock();
                project.setActiveWebServer(scopeName, activeWebServer);
                project.save();
            } catch (LockException e) {
                LOGGER.error("Cannot lock and save project!", e);
                return false;
            } finally {
                LOGGER.debug("Unlocking project");
                project.unlock();
            }
        } else {
            LOGGER.info("'{}' already has an active web server for scope '{}'. Active web server is: {}", project.getName(), scopeName, activeWebServer);
        }
        return true;
    }

    private final Connection _connection;
    private ModuleAdminAgent _moduleAdminAgent;

    /**
     * Instantiates a {@link ModuleInstaller}. Doesn't do anything else.
     */
    public ModuleInstaller(final Connection connection) {
        // Nothing to do here
        _connection = connection;
    }

    private synchronized ModuleAdminAgent getModuleAdminAgent() {
        if (_moduleAdminAgent == null) {
            _moduleAdminAgent = _connection.getBroker().requireSpecialist(ModuleAdminAgent.TYPE);
        }
        return _moduleAdminAgent;
    }

    private Optional<ModuleDescriptor> getModule(final String moduleName) {
        return getModuleAdminAgent().getModules().stream().filter(it -> it.getName().equals(moduleName)).findFirst();
    }

    /**
     * Method for installing a given FirstSpirit module (only the module itself will be installed, no components will be added to any project).
     *
     * @param fsm The path to the FirstSpirit module file (fsm) to be installed
     * @return An InstallModuleResult. Result might be absent when there's an exception with the fsm file stream.
     */
    private Optional<ModuleResult> installFsm(final File fsm, final boolean deploy) {
        LOGGER.info("Starting module installation");
        try (FileInputStream fsmStream = new FileInputStream(fsm)) {
            ModuleResult result = getModuleAdminAgent().install(fsmStream, true, deploy);
            getModuleAdminAgent().setTrusted(result.getDescriptor().getName(), true);
            return Optional.of(result);
        } catch (IOException e) {
            LOGGER.error("Exception during module installation!", e);
        }
        return Optional.empty();
    }

    /**
     * Method for activating auto start of services of a given module
     *
     * @param descriptor the module descriptor
     * @param parameters the {@link ModuleInstallationParameters parameters} of the command
     */
    private void activateServices(final ModuleDescriptor descriptor, final ModuleInstallationParameters parameters) {
        String moduleName = descriptor.getModuleName();
        Optional<ModuleDescriptor> moduleDescriptor = getModuleAdminAgent().getModules().stream().filter(it -> it.getName().equals(moduleName)).findFirst();

        if (!moduleDescriptor.isPresent()) {
            LOGGER.info("ModuleDescriptor not present!");
        }

        final ComponentDescriptor[] componentDescriptors = descriptor.getComponents();
        if (componentDescriptors == null) {
            LOGGER.error("No components found for module: {}", moduleName);
        } else {
            List<ComponentDescriptor> serviceDescriptors = stream(componentDescriptors).filter(it -> it.getType().equals(SERVICE)).collect(toList());
            if (!serviceDescriptors.isEmpty()) {
                LOGGER.info("ModuleInstaller activateServices ...");
            }
            serviceDescriptors.forEach(serviceDescriptor -> {
                LOGGER.info("Found service " + serviceDescriptor.getName());
                File configuration = parameters.getServiceConfigurations().get(serviceDescriptor.getName());
                if (configuration != null) {
                    createConfigurationFile(SERVICE, serviceDescriptor, configuration, moduleName, parameters.getProjectName(), null);
                } else {
                    LOGGER.info("No configuration found for service " + serviceDescriptor.getName());
                }
                setAutostartAndRestartService(serviceDescriptor);
            });
        }
    }

    private void setAutostartAndRestartService(final ComponentDescriptor descriptor) {
        final ModuleAdminAgent moduleAdminAgent = getModuleAdminAgent();
        String componentDescriptorName = descriptor.getName();
        moduleAdminAgent.setAutostart(componentDescriptorName, true);
        LOGGER.info("Stopping service {}", componentDescriptorName);
        moduleAdminAgent.stopService(componentDescriptorName);
        LOGGER.info("Starting service {}", componentDescriptorName);
        moduleAdminAgent.startService(componentDescriptorName);
        LOGGER.info("Service {} running: {}", componentDescriptorName, moduleAdminAgent.isRunning(componentDescriptorName));
    }

    /**
     * Convenience method for copying the configuration files forProjectAndScope the module to the server-dirs
     *
     * @param type                Type of the module whose configuration should be written e.g. Service, ProjectApp
     * @param componentDescriptor The component forProjectAndScope the module.xml to use
     * @param configurationFile   The map forProjectAndScope the pom.xml that includes the configuration files
     * @param moduleName          The name of the module whose configuration should be written (nullable)
     * @param projectName         The optional project name applications shall be installed to
     * @param webAppId            The webAppId to use - only used by webapp configurations
     */
    private void createConfigurationFile(final ComponentDescriptor.Type type,
                                         final ComponentDescriptor componentDescriptor,
                                         final File configurationFile,
                                         final String moduleName,
                                         final String projectName, WebAppId webAppId) {
        LOGGER.info("Config created, preparing for saving");
        Optional<FileSystem<?>> fsOptional = getFileSystemForConfigurationType(type, componentDescriptor, moduleName, projectName, webAppId);
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

    private Optional<FileSystem<?>> getFileSystemForConfigurationType(final ComponentDescriptor.Type type,
                                                                      final ComponentDescriptor componentDescriptor,
                                                                      final String moduleName,
                                                                      final String projectName,
                                                                      final WebAppId webAppId) {
        ModuleAdminAgent moduleAdminAgent = getModuleAdminAgent();
        FileSystem<?> fs = null;

        if (type.equals(SERVICE)) {
            fs = moduleAdminAgent.getServiceConfig(componentDescriptor.getName());
        } else if (type.equals(ComponentDescriptor.Type.PROJECTAPP)) {
            Project project = safelyRetrieveProject(projectName);
            fs = moduleAdminAgent.getProjectAppConfig(moduleName, componentDescriptor.getName(), project);
        } else if (type.equals(WEBAPP)) {
            LOGGER.info("ComponentDescriptor: " + componentDescriptor.getName());
            fs = moduleAdminAgent.getWebAppConfig(moduleName, componentDescriptor.getName(), webAppId);
        }
        return Optional.ofNullable(fs);
    }

    private Project safelyRetrieveProject(final String projectName) {
        if (StringUtils.isNullOrEmpty(projectName)) {
            throw new IllegalArgumentException("No project given, can't get a project app configuration!");
        }
        Project project = _connection.getProjectByName(projectName);
        if (project == null) {
            throw new IllegalArgumentException("Cannot find project " + projectName + "!");
        }
        return project;
    }

    /**
     * Method for installing the project applications of a given module into a given project
     *
     * @param descriptor The descriptor of the module whose project applications shall be installed
     * @param parameters
     */
    private boolean installProjectApps(final ModuleDescriptor descriptor, final ModuleInstallationParameters parameters) {
        List<ComponentDescriptor> projectAppDescriptors = stream(descriptor.getComponents()).filter(it -> it instanceof ProjectAppDescriptor).collect(toList());

        String projectName = parameters.getProjectName();
        if (StringUtils.isNullOrEmpty(projectName)) {
            if (!projectAppDescriptors.isEmpty()) {
                LOGGER.warn("Found project app descriptors, but can't install project apps without a project name given!");
                return false;
            }
        } else {
            if (!projectAppDescriptors.isEmpty()) {
                LOGGER.info("Installing project apps for {} project {}", descriptor.getModuleName(), projectName);

                projectAppDescriptors.forEach(projectAppDescriptor -> {
                    LOGGER.info("ProjectDescriptor {} is processed", projectAppDescriptor.getName());
                    createProjectAppConfiguration(projectName, descriptor.getModuleName(), projectAppDescriptor);

                    LOGGER.info("Install ProjectApp");
                    Project project = safelyRetrieveProject(projectName);
                    getModuleAdminAgent().installProjectApp(descriptor.getModuleName(), projectAppDescriptor.getName(), project);
                    LOGGER.info("Create configuration files");
                    parameters.getProjectAppConfiguration().ifPresent(projectAppFile -> {
                        createConfigurationFile(ComponentDescriptor.Type.PROJECTAPP, projectAppDescriptor, projectAppFile, descriptor.getModuleName(), projectName, null);
                    });
                });
                LOGGER.info("Installing project apps finished");
            }
        }
        return true;
    }

    private void createProjectAppConfiguration(final String projectName, final String moduleName, final ComponentDescriptor projectAppDescriptor) {
        FileSystem<?> projectAppConfig = null;
        try {
            projectAppConfig = getFileSystemForConfigurationType(projectAppDescriptor.getType(), projectAppDescriptor, moduleName, projectName, null).get();
        } catch (IllegalArgumentException e) {
            LOGGER.info("projectAppConfig can not be obtained so it is created");
            LOGGER.debug("", e);
        }
        if (projectAppConfig != null) {
            LOGGER.info("Existing project: {} app config - updating with the given configuration!", projectName, moduleName);
        }
    }

    private boolean installProjectWebAppsAndCreateConfig(final ModuleDescriptor descriptor, final ModuleInstallationParameters parameters) {
        final List<ComponentDescriptor> webappDescriptors = stream(descriptor.getComponents()).filter(it -> WEBAPP.equals(it.getType())).collect(toList());
        final List<WebAppIdentifier> webAppScopes = parameters.getWebAppScopes();
        if (!webappDescriptors.isEmpty() && !webAppScopes.isEmpty()) {
            LOGGER.info("Creating WebApp configuration files");
        }
        final AtomicBoolean result = new AtomicBoolean(true);
        webappDescriptors.forEach(componentDescriptor -> {
            for (WebAppIdentifier scope : webAppScopes) {
                final boolean webAppAndConfigurationsCreated = createWebAppAndConfigurations(descriptor, parameters, componentDescriptor, scope);
                result.set(result.get() && webAppAndConfigurationsCreated);
            }
        });
        return result.get();
    }

    /**
     * Method for installing the web applications of a given module into a given project
     *
     * @param descriptor A {@link ModuleDescriptor} to describe FirstSpirit module components
     * @param parameters parameters containing the specific entries forProjectAndScope the config files
     * @return success indicator
     */
    private boolean installProjectWebAppsAndDeploy(final ModuleDescriptor descriptor, final ModuleInstallationParameters parameters) {
        if (!installProjectWebAppsAndCreateConfig(descriptor, parameters)) {
            return false;
        }
        if (parameters.getDeploy()) {
            return deployWebAppsForScopes(descriptor, parameters);
        } else {
            return true;
        }
    }

    private boolean installWebAppAndActivateWebServer(final WebAppIdentifier webScope, final ModuleInstallationParameters parameters) {
        try {
            final String projectName = parameters.getProjectName();
            Project projectOrNull = null;
            if (!StringUtils.isNullOrEmpty(projectName)) {
                projectOrNull = _connection.getProjectByName(projectName);
            }

            LOGGER.info("Setting active webserver for project scope: {}", webScope);
            boolean activeServerForProjectSet = setActiveWebServer(_connection.getServerConfiguration(), webScope, projectOrNull);
            LOGGER.info(activeServerForProjectSet ? "Setting active webserver was successful." : "Setting active webserver wasn't successful.");
            if (!activeServerForProjectSet) {
                return false;
            }
            return deployWebApp(webScope, projectOrNull);
        } catch (IllegalStateException ise) {
            LOGGER.error("Cannot deploy war file!", ise);
            return false;
        }
    }

    private boolean createWebAppAndConfigurations(final ModuleDescriptor moduleDescriptor,
                                                  final ModuleInstallationParameters parameters,
                                                  final ComponentDescriptor componentDescriptor,
                                                  final WebAppIdentifier scope) {
        final String projectName = parameters.getProjectName();
        final String moduleName = moduleDescriptor.getModuleName();
        final Map<WebAppIdentifier, File> webAppConfigurations = parameters.getWebAppConfigurations();

        Project projectOrNull = StringUtils.isNullOrEmpty(projectName) ? null : _connection.getProjectByName(projectName);
        try {
            WebAppId id = scope.createWebAppId(projectOrNull);
            getModuleAdminAgent().installWebApp(moduleName, componentDescriptor.getName(), id, parameters.getDeploy());
            if (webAppConfigurations.containsKey(scope)) {
                createConfigurationFile(WEBAPP,
                        componentDescriptor,
                        webAppConfigurations.get(scope),
                        moduleName, projectName,
                        id);
            }
            LOGGER.info("WebAppScope: {}", scope);
            return true;
        } catch (IllegalArgumentException e) {
            LOGGER.error("Invalid Scope " + scope, e);
            return false;
        }
    }

    private boolean deployWebAppsForScopes(final ModuleDescriptor descriptor, final ModuleInstallationParameters parameters) {
        final ComponentDescriptor[] componentDescriptors = descriptor.getComponents();
        final List<WebAppIdentifier> webAppScopes = parameters.getWebAppScopes();
        List<ComponentDescriptor> webAppDescriptors = asList(componentDescriptors).stream().filter(componentDescriptor -> WEBAPP.equals(componentDescriptor.getType())).collect(toList());
        if (!webAppDescriptors.isEmpty() && !webAppScopes.isEmpty()) {
            LOGGER.info("Installing Project WebApps");
        }
        for (ComponentDescriptor componentDescriptor : webAppDescriptors) {
            if (!deployWebApps(componentDescriptor, parameters)) {
                return false;
            }
        }
        return true;
    }

    private boolean deployWebApps(final ComponentDescriptor descriptor, final ModuleInstallationParameters parameters) {
        final List<WebAppIdentifier> webAppScopes = parameters.getWebAppScopes();
        LOGGER.info("Going to install and activate component {} with webAppIds: {}", descriptor.getName(), webAppScopes);
        Optional<Boolean> failed = webAppScopes
                .stream()
                .map(it -> installWebAppAndActivateWebServer(it, parameters))
                .filter(it -> !it)
                .findAny();

        if (failed.isPresent()) {
            LOGGER.error("Cannot install WebApp for specific scope!  IDs: {}", webAppScopes);
            return false;
        }
        return true;
    }

    private boolean deployWebApp(final WebAppIdentifier webScope, final Project projectOrNull) {
        final WebAppId webAppId = webScope.createWebAppId(projectOrNull);
        final boolean isRootWebAppAndNonSocketConnection = isFs5RootWebApp(webAppId) && SOCKET_MODE != _connection.getMode();
        boolean successfullyDeployed = false;
        if (isRootWebAppAndNonSocketConnection) {
            LOGGER.error("Cannot use a non socket connection to deploy a web component to the FirstSpirit root WebApp. Use SOCKET as connection mode!");
        } else {
            LOGGER.info("Deploying WebApp {}", projectOrNull == null ? webScope.toString() : (projectOrNull.getName() + '/' + webScope.getScope()));
            successfullyDeployed = getModuleAdminAgent().deployWebApp(webAppId);
            LOGGER.info("Successfully deployed: {}", successfullyDeployed);
        }
        return successfullyDeployed;
    }

    public boolean updateWebApps(final Collection<WebAppId> webAppsToUpdate) {
        LOGGER.info("Deploying web apps: " + Strings.implode(webAppsToUpdate, ", "));
        return getModuleAdminAgent().deployWebApps(webAppsToUpdate);
    }

    /**
     * Installs a module on a FirstSpirit server. Uses the given connection.
     * If any of the configured components is already installed, it is updated.
     *
     * @param parameters a parameter bean that defines how the module should be installed
     * @return the optional {@link ModuleResult}, which is empty on failure
     */
    public Optional<ModuleResult> install(final ModuleInstallationParameters parameters, final boolean deploy) {
        if (_connection == null || !_connection.isConnected()) {
            throw new IllegalStateException("Connection is null or not connected!");
        }

        final Optional<ModuleResult> moduleResultOption = installFsm(parameters.getFsm(), parameters.getDeploy());
        if (moduleResultOption.isPresent()) {
            activateServices(moduleResultOption.get().getDescriptor(), parameters);

            String moduleName = moduleResultOption.get().getDescriptor().getName();
            LOGGER.info("Finished module installation for {}", moduleName);

            // safely retrieve the module from the server side
            Optional<ModuleDescriptor> moduleDescriptor = getModule(moduleName);
            if (!moduleDescriptor.isPresent()) {
                LOGGER.info("ModuleDescriptor for module '" + moduleName + "' not present!");
                return Optional.empty();
            }

            // install project apps
            if (!installProjectApps(moduleDescriptor.get(), parameters)) {
                LOGGER.error("Project app installation not successful for module {}", moduleName);
            }

            if (deploy) {
                // install and deploy project web apps
                final boolean webAppsSuccessfullyInstalledAndDeployed = installProjectWebAppsAndDeploy(moduleDescriptor.get(), parameters);
                if (!webAppsSuccessfullyInstalledAndDeployed) {
                    LOGGER.error("WebApp installation and activation not successful for module {}", moduleName);
                }
            } else {
                // only create configurations
                final boolean webAppsConfigured = installProjectWebAppsAndCreateConfig(moduleDescriptor.get(), parameters);
                if (!webAppsConfigured) {
                    LOGGER.error("WebApp configuration not successful for module {}", moduleName);
                }
            }
        }
        return moduleResultOption;
    }

}
