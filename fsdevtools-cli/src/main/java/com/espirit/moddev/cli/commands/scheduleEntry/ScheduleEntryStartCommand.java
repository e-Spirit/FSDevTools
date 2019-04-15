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
import com.github.rvesse.airline.annotations.help.Examples;
import de.espirit.firstspirit.access.AdminService;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.ServicesBroker;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.access.schedule.ScheduleEntry;
import de.espirit.firstspirit.access.schedule.ScheduleEntryControl;
import de.espirit.firstspirit.access.schedule.ScheduleEntryRunningException;
import de.espirit.firstspirit.common.MaximumNumberOfSessionsExceededException;
import de.espirit.firstspirit.server.authentication.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * This Command can start a project schedule entry.
 *
 * @author e-Spirit AG
 */
@Command(name = "start", groupNames = {"scheduleentry"}, description = "Starts a Project schedule entry.")

@Examples(examples =
        {
                "scheduleentry start -sen \"myScheduleEntry\""
        },
        descriptions = {
                "Starts the schedule entry.",
        })
@SuppressWarnings("squid:S1200")
public class ScheduleEntryStartCommand extends SimpleCommand<SimpleResult<Boolean>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleEntryStartCommand.class);

    @Option(name = {"-sen", "--scheduleEntryName"}, description = "The schedule entry name.")
    private String scheduleEntryName = "";

    @Option(name = {"-wff", "--waitForFinish"}, description = "Wait for the task to finish. (default is true)")
    private String waitForFinish = "true";

    @Override
    public SimpleResult<Boolean> call() {

        try (Connection connection = create()) {
            connection.connect();

            if (!connection.isConnected()) {
                throw new IllegalStateException("Connection is null or not connected!");
            }

            return executeScheduleEntry(connection);
        } catch (IOException | AuthenticationException | MaximumNumberOfSessionsExceededException | IllegalArgumentException | IllegalStateException e) {
            LOGGER.error("Can't connect to the FirstSpirit Server", e);
            return new SimpleResult<>(e);
        } catch (ScheduleEntryRunningException e) {
            LOGGER.error("Can't execute schedule entry", e);
            return new SimpleResult<>(e);
        }
    }

    private SimpleResult<Boolean> executeScheduleEntry(Connection connection) throws ScheduleEntryRunningException {
        ServicesBroker servicesBroker = connection.getBroker().requireSpecialist(ServicesBroker.TYPE);
        AdminService adminService = servicesBroker.getService(AdminService.class);
        Project project = adminService.getProjectStorage().getProject(super.getProject());
        ScheduleEntry scheduleEntry = adminService.getScheduleStorage().getScheduleEntry(project, getScheduleEntryName());

        if (scheduleEntry == null) {
            LOGGER.error("Can't find schedule entry. Nothing to execute");
            return new SimpleResult<>(false);
        }

        LOGGER.info(String.format("Found schedule %s on project %s", getScheduleEntryName(), super.getProject()));
        LOGGER.info("Execute schedule");
        ScheduleEntryControl scheduleEntryControl = scheduleEntry.execute();
        if (isWaitForFinish()) {
            LOGGER.info("Wait for the task to finish.");
            scheduleEntryControl.awaitTermination();
        }

        return new SimpleResult<>(true);
    }

    public boolean isWaitForFinish() {
        return Boolean.valueOf(waitForFinish);
    }

    public void setWaitForFinish(String waitForFinish) {
        this.waitForFinish = waitForFinish;
    }

    public String getScheduleEntryName() {
        return scheduleEntryName;
    }

    public void setScheduleEntryName(String scheduleEntryName) {
        this.scheduleEntryName = scheduleEntryName;
    }

    protected Connection create() {
        return ConnectionBuilder.with(this).build();
    }
}
