package com.espirit.moddev.cli.commands.schedule;

import com.github.rvesse.airline.annotations.Group;


@Group(name = "schedule", description = "All commands in this group refer to schedule tasks", defaultCommand = ScheduleListCommand.class)
public class ScheduleGroup {
}
