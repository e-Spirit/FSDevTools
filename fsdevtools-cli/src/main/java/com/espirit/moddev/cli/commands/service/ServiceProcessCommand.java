package com.espirit.moddev.cli.commands.service;

import com.espirit.moddev.cli.ConnectionBuilder;
import com.espirit.moddev.cli.commands.SimpleCommand;
import com.espirit.moddev.cli.results.SimpleResult;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.manager.ServiceManager;
import de.espirit.firstspirit.server.ManagerProvider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Base class to process the FirstSpirit services. Can be used to implement restart, start and stop Commands.
 */
public abstract class ServiceProcessCommand extends SimpleCommand<SimpleResult<Boolean>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceProcessCommand.class);

    @Option(type = OptionType.COMMAND, name = {"-n", "--serviceNames"}, description = "Comma separated list of Names of the FirstSpirit services to be restarted. Optional. If not provided, all services with auto start enabled will be restarted")
    private String serviceNames;

    @Override
    public SimpleResult<Boolean> call() {
        try (final Connection connection = createConnection()) {
            connection.connect();

            return new SimpleResult<>(processServices(getServiceManager(connection)));

        } catch (final Exception e) {
            return new SimpleResult<>(e);
        }
    }

    @Override
    public boolean needsContext() {
        return false;
    }

    /**
     * Get configured service names als comma separated string.
     *
     * @return configured service names
     */
    public String getServiceNames() {
        return this.serviceNames;
    }

    protected ServiceManager getServiceManager(Connection connection) {
        if (connection instanceof ManagerProvider) {
            final ManagerProvider managerProvider = (ManagerProvider) connection;
            final ServiceManager serviceManager = managerProvider.getManager(ServiceManager.class);
            if (serviceManager == null) {
                throw new IllegalStateException("Could not retrieve the ServiceManager from ManagerProvider");
            }

            LOGGER.debug("Successfully retrieved a ServiceManager instance: class impl = '{}'", serviceManager.getClass().getName());
            return serviceManager;
        }

        throw new IllegalStateException("Connection is not a ManagerProvider implementation.");
    }

    protected boolean processServices(final ServiceManager serviceManager) {

        final List<String> serviceNames = getOptionalServiceNames();

        if (serviceNames.size() > 0) {

            // process provided services by name
            for (String serviceName : serviceNames) {
                processService(serviceManager, serviceName);
            }

        } else {
            LOGGER.info("No --serviceNames parameter given for processing. All services with auto start enabled will be processed!");
            // no services where provided by param ==> process all services with auto start == enabled
            for (String serviceName : serviceManager.getServices()) {
                if (serviceManager.isAutostartEnabled(serviceName)) {
                    processService(serviceManager, serviceName);
                }
            }
        }

        return true;
    }

    /**
     * Processed the service with given name
     *
     * @param serviceManager ServiceManager instance
     * @param serviceName    name of service to process
     */
    protected abstract void processService(ServiceManager serviceManager, String serviceName);

    /**
     * Creates a connection to a FirstSpirit Server with this instance as config.
     *
     * @return A connection from a ConnectionBuild.
     * @see ConnectionBuilder
     */
    protected Connection createConnection() {
        return ConnectionBuilder.with(this).build();
    }


    /**
     * Get configured service names als List.
     *
     * @return configured service names als List or empty List if no names where configured.
     */
    protected List<String> getOptionalServiceNames() {
        if (StringUtils.isBlank(this.getServiceNames())) {
            return Collections.emptyList();
        }

        return Arrays.stream(StringUtils.split(this.getServiceNames(), ","))
                .filter(StringUtils::isNoneBlank)
                .map(StringUtils::trim)
                .collect(Collectors.toList());
    }
}
