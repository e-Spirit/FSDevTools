package com.espirit.moddev.cli.groups;

import com.espirit.moddev.cli.commands.project.ImportProjectCommand;
import com.github.rvesse.airline.annotations.Group;

/**
 * {@link com.github.rvesse.airline.annotations.Group} that contains commands to handle FirstSpirit projects.
 * For example for installing/importing a project export file or for deleting a project from a FirstSpirit server;
 */
@Group(name = "project", description = "Treat projects - for example import zip file based project exports or delete a project from the server.", defaultCommand = ImportProjectCommand.class)
public class ProjectGroup {
}
