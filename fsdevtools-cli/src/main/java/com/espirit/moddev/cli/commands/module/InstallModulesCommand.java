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
import com.espirit.moddev.cli.results.InstallModulesResult;
import com.espirit.moddev.cli.results.SimpleResult;
import com.espirit.moddev.module.common.ModuleInstallationConfiguration;
import com.espirit.moddev.moduleinstaller.ModuleInstallationParameters;
import com.espirit.moddev.moduleinstaller.ModuleInstaller;
import com.espirit.moddev.shared.exception.MultiException;
import com.espirit.moddev.shared.webapp.WebAppIdentifier;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.Examples;
import com.github.rvesse.airline.annotations.restrictions.Once;
import com.github.rvesse.airline.annotations.restrictions.PathKind;
import com.github.rvesse.airline.annotations.restrictions.Required;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.agency.ModuleAdminAgent;
import de.espirit.firstspirit.agency.WebAppId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.espirit.moddev.shared.webapp.WebAppIdentifier.isFs5RootWebApp;
import static de.espirit.firstspirit.access.ConnectionManager.SOCKET_MODE;

/**
 * Installs a set of modules on a FirstSpirit server. The configuration of this command is done by a config file in
 * json format.
 * Already installed modules will be updated (with its usages).
 */
@Command(name = "installBulk", groupNames = {"module"}, description = "Installs a FirstSpirit module into a FirstSpirit Server. If a given component is already installed, it is updated.")
@Examples(examples = {"module installBulk -mcf \"folder\\configFile.json\"", "module installBulk -mcf \"myConfigFile.json\" --deployWebApps false", "example configFile.json:"},
        descriptions = {"Installs the modules of the given configuration file and deploys all related webapps.", "Installs the modules of the given configuration file but does not deploy the related webapps.",
        "[\n" +
                "{\n" +
                " \"fsm\": \"H:\\\\path\\\\fs-saml-login-1.1.fsm\"\n" +
                "},\n" +
                "{\n" +
                " \"fsm\": \"C:\\\\path\\\\fs-tpp-api-1.2.11-SNAPSHOT.fsm\",\n" +
                " \"moduleProjectName\": \"Mithras\",\n" +
                " \"webAppScopes\" : [ \"webedit\", \"global(fs5root)\" ]\n" +
                "}\n" +
                "]\n"
        })
public class InstallModulesCommand extends SimpleCommand<InstallModulesResult> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(InstallModulesCommand.class);

    @Option(type = OptionType.COMMAND, name = {"-mcf", "--moduleConfigFile"}, description = "Path to the configuration json file")
    @com.github.rvesse.airline.annotations.restrictions.Path(mustExist = true, kind = PathKind.FILE, writable = false)
    @Required
    private String _configFile;

    @Option(type = OptionType.COMMAND, name = {"-dwa", "--deployWebApps"}, description = "Define whether all related web apps of the modules should be immediately deployed after the installation or not [true = deploy (default) | false = no deploy]")
    @Once
    private String deploy = String.valueOf(true);

    @Override
    public InstallModulesResult call() {
        try (Connection connection = ConnectionBuilder.with(this).build()) {
            connection.connect();
            return bulkInstall(connection);
        } catch (Exception e) {
            return new InstallModulesResult(new MultiException(e.getMessage(), Collections.singletonList(e)));
        }
    }

    private InstallModulesResult bulkInstall(Connection connection) throws IOException {
        final List<ModuleInstallationConfiguration> configurations = ModuleInstallationConfiguration.fromFile(_configFile);
        final List<ModuleInstallationParameters> installationParameters = new ArrayList<>();

        if (configurations.isEmpty()) {
            throw new IllegalArgumentException("File '" + _configFile + "' must contain at least one configuration.");
        }

        // verify the config file and build parameters for all configurations
        final ArrayList<Exception> parameterErrorResults = verifyAndCreateParameters(connection, configurations, installationParameters);

        // we got at least one error --> return here without installing anything
        if (!parameterErrorResults.isEmpty()) {
            return new InstallModulesResult(new MultiException("Error verifying configurations.", parameterErrorResults));
        }

        // finally: install modules, deploy and return the result
        return installModulesAndDeploy(connection, installationParameters);
    }

    private ArrayList<Exception> verifyAndCreateParameters(final Connection connection, final List<ModuleInstallationConfiguration> configurations, final List<ModuleInstallationParameters> installationParameters) {
        final ArrayList<Exception> results = new ArrayList<>();
        for (final ModuleInstallationConfiguration config : configurations) {
            try {
                // override deploy parameter for a single module, because we don't want to deploy all web apps for each
                // installed module.
                config.setDeploy(String.valueOf(false));
                // verify the configuration
                config.verify(connection);
                // create parameters object
                installationParameters.add(ModuleInstallationParameters.forConfiguration(config));
            } catch (final Exception exception) {
                results.add(exception);
            }
        }
        return results;
    }

    /**
     * Installs the modules for all given {@link ModuleInstallationParameters parameters}.
     * <p>
     * What happens in this method:
     * - iterate over each parameter-set
     * - install the module, based on the parameter-set
     * --> fetch the result:
     * * if the installation was successful: simply add the result of the installation to the list of results
     * * if the installation failed: add an error-result to the list of results
     * - return the list of results
     * <p>
     * This way we make sure that we collect all installation results, independent of the installation-result of a single module
     *
     * @param connection the connection to use for the installation
     * @param parameters the list of {@link ModuleInstallationParameters module parameters} used for the installation
     * @return a {@link List list} of {@link InstallModuleResult InstallModuleResults} containing results for {@code all} installation processes.
     */
    private InstallModulesResult installModulesAndDeploy(final Connection connection, final List<ModuleInstallationParameters> parameters) {
        final ArrayList<InstallModuleResult> results = new ArrayList<>();
        final Set<WebAppId> updatedWebApps = new HashSet<>();
        for (final ModuleInstallationParameters singleParameter : parameters) {
            InstallModuleResult totalInstallResult;
            try {
                if (connection == null || !connection.isConnected()) {
                    throw new IllegalStateException("Connection is null or not connected!");
                }

                final SingleModuleInstallResult singleInstallResult = installModule(connection, singleParameter);
                final Optional<ModuleAdminAgent.ModuleResult> installResult = singleInstallResult.getModuleResult();
                if (installResult.isPresent()) {
                    // store updated web apps
                    updatedWebApps.addAll(singleInstallResult.getUpdatedWebApps());
                    // store all configured web apps in the list of updated web apps ; needed for the initial installation of a module
                    final List<WebAppIdentifier> webAppScopes = singleParameter.getWebAppScopes();
                    for (final WebAppIdentifier webAppScope : webAppScopes) {
                        if (webAppScope.isGlobal()) {
                            // global web app --> no project needed
                            updatedWebApps.add(webAppScope.createWebAppId(null));
                        } else {
                            // project specific web app --> get the project and use it for the web app scope
                            final Project project = getProject(connection, singleParameter.getProjectName());
                            if (project != null) {
                                updatedWebApps.add(webAppScope.createWebAppId(project));
                            }
                        }
                    }
                }
                totalInstallResult = installResult.map(moduleResult -> new InstallModuleResult(moduleResult.getDescriptor().getModuleName())).orElseGet(() -> {
                    final IllegalStateException exception = new IllegalStateException("Cannot get installation result for module " + singleParameter.getFsm());
                    return new InstallModuleResult(singleParameter.getFsm().toString(), exception);
                });
            } catch (final Exception installException) {
                totalInstallResult = new InstallModuleResult(singleParameter.getFsm().toString(), installException);
            }
            results.add(totalInstallResult);
        }

        // return if we have an error
        final boolean hasError = results.stream().anyMatch(SimpleResult::isError);
        if (hasError) {
            final List<Exception> exceptions = results.stream().filter(SimpleResult::isError).map(SimpleResult::getError).collect(Collectors.toList());
            return new InstallModulesResult(results, new MultiException("Error installing modules!", exceptions));
        }

        // deploy related web apps
        if (shouldDeploy()) {
            if (updatedWebApps.isEmpty()) {
                LOGGER.info("No web apps to deploy.");
            } else {
                final ModuleInstaller moduleInstaller = new ModuleInstaller(connection);
                // check for fs5root web app if the connection mode != SOCKET
                if (SOCKET_MODE != connection.getMode()) {
                    for (final WebAppId updatedWebApp : updatedWebApps) {
                        if (isFs5RootWebApp(updatedWebApp)) {
                            throw new IllegalStateException("Cannot use a non socket connection to deploy a web component to the FirstSpirit root WebApp. Use SOCKET as connection mode!");
                        }
                    }
                }
                if (moduleInstaller.updateWebApps(updatedWebApps)) {
                    LOGGER.info("Web apps successfully deployed.");
                } else {
                    throw new IllegalStateException("One or more web apps could not be deployed.");
                }
            }
        }
        return new InstallModulesResult(results);
    }

    private Project getProject(final Connection connection, final String projectName) {
        String projectToRetrieve = projectName;
        if (projectToRetrieve == null || projectToRetrieve.isEmpty()) {
            projectToRetrieve = super.getProject();
        }
        return connection.getProjectByName(projectToRetrieve);
    }

    @Override
    public boolean needsContext() {
        return false;
    }

    private boolean shouldDeploy() {
        return Boolean.TRUE.toString().equalsIgnoreCase(deploy);
    }

    /**
     * Installs a module on a FirstSpirit server. Uses the given connection.
     * If any of the configured components is already installed, it is updated.
     *
     * @param connection a connected FirstSpirit connection that is used to install the module
     * @param parameters a parameter bean that defines how the module should be installed
     * @return the optional {@link ModuleAdminAgent.ModuleResult}, which is empty on failure
     */
    public SingleModuleInstallResult installModule(Connection connection, ModuleInstallationParameters parameters) {
        final ModuleInstaller moduleInstaller = new ModuleInstaller(connection);
        final Optional<ModuleAdminAgent.ModuleResult> moduleResultOption = moduleInstaller.install(parameters, false);
        if (moduleResultOption.isPresent()) {
            final ModuleAdminAgent.ModuleResult moduleResult = moduleResultOption.get();
            final ArrayList<WebAppId> updatedWebApps = new ArrayList<>(moduleResult.getUpdatedWebApps());
            return new SingleModuleInstallResult(moduleResultOption.get(), updatedWebApps);
        } else {
            throw new IllegalStateException("Error installing fsm '" + parameters.getFsm() + "'!");
        }
    }

    private static class SingleModuleInstallResult {

        private final ModuleAdminAgent.ModuleResult _moduleResult;
        private final Collection<WebAppId> _updatedWebApps;

        private SingleModuleInstallResult(final ModuleAdminAgent.ModuleResult moduleResult, final Collection<WebAppId> updatedWebApps) {
            _moduleResult = moduleResult;
            _updatedWebApps = new ArrayList<>(updatedWebApps);
        }

        public Optional<ModuleAdminAgent.ModuleResult> getModuleResult() {
            return Optional.of(_moduleResult);
        }

        public Collection<WebAppId> getUpdatedWebApps() {
            return _updatedWebApps;
        }
    }

}