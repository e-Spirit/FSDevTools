package com.espirit.moddev.cli.groups;

import com.espirit.moddev.cli.commands.scheduleEntry.ScheduleEntryListCommand;
import com.github.rvesse.airline.annotations.Group;

/**
 * {@link Group} that contains commands to handle FirstSpirit modules.
 * For example for installing, uninstalling them;
 */
@Group(name = "scheduleentry", description = "Start and list schedule entries.", defaultCommand = ScheduleEntryListCommand.class)
public class ScheduleEntryGroup {
}
