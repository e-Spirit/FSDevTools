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

import com.espirit.moddev.cli.commands.service.ProcessServiceInfo.ServiceStatus;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.help.Examples;
import de.espirit.firstspirit.agency.ModuleAdminAgent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.espirit.moddev.cli.commands.service.ProcessServiceInfo.ServiceStatus.*;

/**
 * Command class that can start a FirstSpirit services.
 *
 * @author Andreas Straub
 */
@Command(name = "start", groupNames = "service", description = "Starts a FirstSpirit service if it is not running.")
@Examples(examples =
        {
                "service start",
                "service start  -n UXBService",
                "service start  -n UXBService,AnotherService"
        },
        descriptions = {
                "Simply start the all services that have auto start enabled and are currently not running",
                "Simply start the service: 'UXBService' if it's currently not running",
                "Simply start the services: 'UXBService' and 'AnotherService' if they are currently not running"
        })

public class ServiceStartCommand extends ServiceProcessCommand {

    @Override
    protected ProcessServiceInfo processService(@NotNull final ModuleAdminAgent moduleAdminAgent, @NotNull final String serviceName) {

        ServiceStatus previousStatus = STOPPED;
        if (moduleAdminAgent.isRunning(serviceName)) {
            previousStatus = RUNNING;
        } else {
            moduleAdminAgent.startService(serviceName);
        }

        return new ProcessServiceInfo(serviceName, previousStatus, RUNNING);
    }

    @Override
    @NotNull
    protected String getResultLoggingHeaderString(List<ProcessServiceInfo> results) {
        return "Started " + results.size() + " services:";
    }
}
