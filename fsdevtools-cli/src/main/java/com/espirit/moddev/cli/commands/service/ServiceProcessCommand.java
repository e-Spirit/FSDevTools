package com.espirit.moddev.cli.commands.service;

import com.espirit.moddev.cli.commands.SimpleCommand;
import com.espirit.moddev.cli.results.ServiceProcessResult;
import com.espirit.moddev.services.ServiceUtils;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;

import de.espirit.firstspirit.access.ServiceNotFoundException;
import de.espirit.firstspirit.agency.ModuleAdminAgent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Base class to process the FirstSpirit services. Can be used to implement restart, start and stop Commands.
 */
public abstract class ServiceProcessCommand extends SimpleCommand<ServiceProcessResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceProcessCommand.class);

    @Option(type = OptionType.COMMAND, name = {"-n", "--serviceNames"}, description = "Comma separated list of Names of the FirstSpirit services to be processed. Optional. If not provided, all services with auto start enabled will be processed.")
    private String serviceNames;

    @Override
    public ServiceProcessResult call() {

        try {
            ModuleAdminAgent moduleAdminAgent = _context.getConnection().getBroker().requestSpecialist(ModuleAdminAgent.TYPE);
            return new ServiceProcessResult(processServices(moduleAdminAgent));
        } catch(ServiceNotFoundException s) {
            return new ServiceProcessResult(new ServiceNotFoundException(s.getLocalizedMessage(), s));
        } catch (Exception e) {
            return new ServiceProcessResult(new IllegalStateException("Cannot request specialist ModuleAdminAgent!", e));
        }
    }

    /**
     * Get configured service names as comma separated string.
     *
     * @return configured service names
     */
    public String getServiceNames() {
        return this.serviceNames;
    }

    /**
     * Set service names as a comma separated string.
     *
     * @param serviceNames the services that should be processed as comma separated string
     */
    public void setServiceNames(String serviceNames) {
        this.serviceNames = serviceNames;
    }

    /**
     * Calls processService for a bunch of services and returns a list of processing results.
     * If no service names are configured, all services available on the FirstSpirit server are handled.
     *
     * @param moduleAdminAgent which is required to retrieve service names
     * @return the list of configured services or all services available on the FirstSpirit server
     */
    protected List<ProcessServiceInfo> processServices(final ModuleAdminAgent moduleAdminAgent) {

        final List<String> splitServiceNames = getOptionalServiceNames();

        if (splitServiceNames.isEmpty()) {
            LOGGER.info("No --serviceNames parameter given for processing. All services will be processed!");
        }

        List<String> serviceNamesToProcess = splitServiceNames.isEmpty() ? ServiceUtils.getAllServiceNamesFromServer(moduleAdminAgent) : splitServiceNames;
        List<ProcessServiceInfo> results = serviceNamesToProcess
                .stream()
                .map(serviceName -> processService(moduleAdminAgent, serviceName))
                .collect(Collectors.toList());

        logProcessResults(results);

        return results;
    }

    @NotNull
    protected String getResultLoggingHeaderString(List<ProcessServiceInfo> results) {
        return "Processed " + results.size() + " services:";
    }

    protected void logProcessResults(List<ProcessServiceInfo> results) {
        StringBuilder builder = new StringBuilder("");
        builder.append(getResultLoggingHeaderString(results));
        builder.append("\n");

        for(ProcessServiceInfo info: results) {
            builder.append("\t");
            builder.append(info.getServiceName());
            builder.append("\t");
            builder.append("(");
            builder.append(info.getPreviousStatus().toString().toLowerCase());
            builder.append(" -> ");
            builder.append(info.getCurrentStatus().toString().toLowerCase());
            builder.append(")");
            builder.append("\n");
        }
        LOGGER.info(builder.toString());
    }


    /**
     * Processed the service with given name
     * @param moduleAdminAgent ServiceManager instance
     * @param serviceName    name of service to process
     */
    protected abstract ProcessServiceInfo processService(@NotNull ModuleAdminAgent moduleAdminAgent, @NotNull String serviceName) throws ServiceNotFoundException;


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
