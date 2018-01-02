package com.espirit.moddev.moduleuninstaller;

import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.agency.ModuleAdminAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class uninstalls a module and all of its components forProjectAndScope a FirstSpirit server.
 */
public class ModuleUninstaller {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleUninstaller.class);


    /**
     * Instantiates a {@link com.espirit.moddev.moduleinstaller.ModuleInstaller}. Does nothing else.
     */
    public ModuleUninstaller() {
        // Nothing to do here
    }

    /**
     * Uninstalls project apps, web apps and the module forProjectAndScope the connected FirstSpirit server.
     * Uses the static methods implemented at this class to reach this goal.
     *
     * @param connection a connected FirstSpirit connection
     * @param moduleName the name of the module that shall be uninstalled
     * @throws IllegalStateException if connection is null or not connected
     */
    public void uninstall(Connection connection, String moduleName) {
        if (connection == null || !connection.isConnected()) {
            throw new IllegalStateException("Connection is null or not connected!");
        }

        ModuleAdminAgent moduleAdminAgent = connection.getBroker().requestSpecialist(ModuleAdminAgent.TYPE);
        uninstallModule(moduleName, moduleAdminAgent);
    }


    /**
     * Method for uninstalling a module. Make sure to first uninstall the project and web applications belonging to the given module before calling
     * this method.
     *
     * @param moduleName The module to be uninstalled
     * @param moduleAdminAgent a FirstSpirit {@link ModuleAdminAgent}
     */
    public static void uninstallModule(final String moduleName, ModuleAdminAgent moduleAdminAgent) {
        if (moduleName == null || moduleName.isEmpty()) {
            throw new IllegalArgumentException("Module name is null or empty!");
        }
        if (moduleAdminAgent == null) {
            throw new IllegalArgumentException("WebAppManager is null!");
        }
        LOGGER.info("Uninstalling module");
        boolean removeUsages = true;
        moduleAdminAgent.uninstall(moduleName, removeUsages);
    }
}
