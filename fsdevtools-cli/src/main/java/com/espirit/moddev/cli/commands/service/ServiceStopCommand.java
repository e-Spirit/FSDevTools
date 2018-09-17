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
import de.espirit.firstspirit.manager.ServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command class that can start a FirstSpirit services.
 *
 * @author Andreas Straub
 */
@Command(name = "stop", groupNames = "service", description = "Stops a FirstSpirit service if it is running. Requires an fs-access.jar on the classpath." +
        "PLEASE NOTE: You need an 'AdminConnection' (user with 'Admin' privileges) to be able to execute this command properly")
@Examples(examples =
        {
                "service stop",
                "service stop  -n UXBService",
                "service stop  -n UXBService,AnotherService"
        },
        descriptions = {
                "Simply stop the all services those have auto start enabled and currently running",
                "Simply stop the service: 'UXBService' if it's currently running",
                "Simply stop the services: 'UXBService' and 'AnotherService' if they currently running"
        })

public class ServiceStopCommand extends ServiceProcessCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceStopCommand.class);

    @Override
    protected void processService(final ServiceManager serviceManager, final String serviceName) {
        if (serviceManager.isServiceRunning(serviceName)) {
            LOGGER.info("Service: '{}' is running and will be stopped!", serviceName);
            serviceManager.stopService(serviceName);
            LOGGER.info("Service: '{}' is stopped.", serviceName);
        } else {
            LOGGER.info("Service: '{}' is not running!", serviceName);
        }
    }
}
