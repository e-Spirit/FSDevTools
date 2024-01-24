/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2024 Crownpeak Technology GmbH
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

package com.espirit.moddev.cli.commands.project;

import com.espirit.moddev.cli.commands.project.importCommand.ImportProjectCommand;

/**
 * {@link com.github.rvesse.airline.annotations.Group} that contains commands to handle FirstSpirit projects.
 * For example for installing/importing a project export file or for deleting a project from a FirstSpirit server;
 */
@com.github.rvesse.airline.annotations.Group(name = ProjectCommandGroup.NAME, description = "Treat projects - for example import zip file based project exports or delete a project from the server.", defaultCommand = ImportProjectCommand.class)
public class ProjectCommandGroup {

	public final static String NAME = "project";

}
