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

package com.espirit.moddev.cli.commands.scheduleEntry;

import com.espirit.moddev.cli.ConnectionBuilder;
import com.espirit.moddev.cli.commands.SimpleCommand;
import com.espirit.moddev.cli.results.SimpleResult;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import de.espirit.firstspirit.access.AdminService;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.ServicesBroker;
import de.espirit.firstspirit.access.schedule.ScheduleEntry;
import de.espirit.firstspirit.agency.ModuleAdminAgent;
import de.espirit.firstspirit.common.MaximumNumberOfSessionsExceededException;
import de.espirit.firstspirit.server.authentication.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 *  This Command can list project schedule entries.
 *
 * @author e-Spirit AG
 */
@Command(name = "list", groupNames = {"scheduleentry"}, description = "List all project schedule entries.")

@SuppressWarnings("squid:S1200")
public class ScheduleEntryListCommand extends SimpleCommand<SimpleResult<Boolean>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleEntryListCommand.class);

    @Override
    public SimpleResult<Boolean> call() {

        try(Connection connection = create()) {
            connection.connect();

            if (!connection.isConnected()) {
                throw new IllegalStateException("Connection is null or not connected!");
            }

            ServicesBroker servicesBroker = connection.getBroker().requireSpecialist(ServicesBroker.TYPE);
            AdminService adminService = servicesBroker.getService(AdminService.class);
            List<ScheduleEntry> scheduleEntries = adminService.getScheduleStorage().getScheduleEntries(connection.getProjectByName(super.getProject()));
            printScheduleEntries(scheduleEntries);

            return new SimpleResult<>(true);
        } catch (IOException | AuthenticationException | MaximumNumberOfSessionsExceededException | IllegalArgumentException | IllegalStateException e) {
                LOGGER.error("Can't connect to the FirstSpirit Server", e);

            return new SimpleResult<>(new IllegalStateException("Cannot request schedule entries!", e));
        }
    }

    private void printScheduleEntries(List<ScheduleEntry> scheduleEntries) {
        StringBuilder builder = new StringBuilder("");
        if(!scheduleEntries.isEmpty()) {
            builder.append(String.format("Found %s schedule entries on project %s:", scheduleEntries.size(), super.getProject()));
        } else {
            builder.append(String.format("No schedule entries found on project %s.", super.getProject()));
        }
        builder.append("\n");

        for(ScheduleEntry scheduleEntry: scheduleEntries) {
            builder.append("\t");
            builder.append(scheduleEntry.getName());
            builder.append(String.format(" (id: %s)", scheduleEntry.getId()));
            builder.append("\n");
        }
        LOGGER.info(builder.toString());
    }

    protected Connection create() {
        return ConnectionBuilder.with(this).build();
    }
}
