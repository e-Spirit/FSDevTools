package com.espirit.moddev.cli.groups;

import com.espirit.moddev.cli.commands.project.ImportProjectCommand;
import com.github.rvesse.airline.annotations.Group;

/**
 * {@link com.github.rvesse.airline.annotations.Group} that contains commands to handle FirstSpirit projects.
 * For example for installing/importing a project export file;
 */
@Group(name = "project", description = "Treat projects - for example import zip file based project exports.", defaultCommand = ImportProjectCommand.class)
public class ProjectGroup {
}
