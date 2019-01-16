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

package com.espirit.moddev.cli.commands.service;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.help.Examples;
import de.espirit.firstspirit.agency.ModuleAdminAgent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.espirit.moddev.cli.commands.service.ProcessServiceInfo.ServiceStatus.RUNNING;
import static com.espirit.moddev.cli.commands.service.ProcessServiceInfo.ServiceStatus.STOPPED;

/**
 * Command class that can restart a FirstSpirit services.
 *
 * @author Andreas Straub
 */
@Command(name = "restart", groupNames = "service", description = "Restarts a FirstSpirit service. Starts, even if it's not running.")
@Examples(examples =
        {
                "service restart",
                "service restart  -n UXBService",
                "service restart  -n UXBService,AnotherService"
        },
        descriptions = {
                "Simply restarts the all services that have auto start enabled",
                "Simply restarts the service: 'UXBService'",
                "Simply restarts the services: 'UXBService' and 'AnotherService'"
        })

public class ServiceRestartCommand extends ServiceProcessCommand {

    @Override
    protected ProcessServiceInfo processService(@NotNull final ModuleAdminAgent moduleAdminAgent, @NotNull final String serviceName) {
        ProcessServiceInfo.ServiceStatus previousStatus = STOPPED;

        if (moduleAdminAgent.isRunning(serviceName)) {
            moduleAdminAgent.stopService(serviceName);
            previousStatus = RUNNING;
        }
        moduleAdminAgent.startService(serviceName);
        return new ProcessServiceInfo(serviceName, previousStatus, RUNNING);
    }

    @Override
    @NotNull
    protected String getResultLoggingHeaderString(List<ProcessServiceInfo> results) {
        return "Restarted " + results.size() + " services:";
    }
}
