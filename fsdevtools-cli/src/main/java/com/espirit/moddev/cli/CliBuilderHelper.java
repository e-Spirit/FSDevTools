/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2021 e-Spirit GmbH
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

package com.espirit.moddev.cli;

import com.espirit.moddev.cli.api.command.Command;
import com.espirit.moddev.cli.commands.help.HelpCommand;
import com.espirit.moddev.cli.reflection.ReflectionUtils;
import com.github.rvesse.airline.annotations.Group;
import com.github.rvesse.airline.builder.CliBuilder;
import com.github.rvesse.airline.builder.GroupBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Helper class for configuration retrieval in context of a cli application.
 */
public final class CliBuilderHelper {

    private CliBuilderHelper() {}

    //TODO: Test these methods
    @SuppressWarnings("squid:S1905")
    static Map<GroupWrapper, List<Class<Command>>> gatherGroupsFromCommandClasses(Set<Class<? extends Command>> commandClasses,
                                                                                  Set<Class<?>> groupClasses) {
        Map<GroupWrapper, List<Class<Command>>> groupMappings = new HashMap<>();

        initializeGroupMappings(groupClasses, groupMappings);

        addCommandClasses(commandClasses, groupMappings);

        return groupMappings;
    }

    private static void addCommandClasses(Set<Class<? extends Command>> commandClasses, Map<GroupWrapper, List<Class<Command>>> groupMappings) {
        for (Class<? extends Command> commandClass : commandClasses) {
            com.github.rvesse.airline.annotations.Command
                annotation =
                commandClass.getAnnotation(com.github.rvesse.airline.annotations.Command.class);

            if (annotation != null) {
                List<String> groupNames;
                String[] groupNamesFromAnnotation = annotation.groupNames();
                boolean annotationHasGroupNames = groupNamesFromAnnotation.length > 0;
                if (annotationHasGroupNames) {
                    groupNames = new ArrayList<>(Arrays.asList(groupNamesFromAnnotation));
                } else {
                    groupNames = new ArrayList<>();
                }
                if (groupNames.isEmpty()) {
                    groupNames.add(GroupWrapper.NO_GROUP_GROUPNAME);
                }
                for (String groupName : groupNames) {
                    String description = annotation.description();
                    GroupWrapper key = new GroupWrapper(groupName, description);
                    if (!groupMappings.containsKey(key)) {
                        groupMappings.put(key, new ArrayList<>());
                    }
                    groupMappings.get(key).add((Class) commandClass);
                }
            }
        }
    }

    private static void initializeGroupMappings(Set<Class<?>> groupClasses, Map<GroupWrapper, List<Class<Command>>> groupMappings) {
        for (Class groupClass : groupClasses) {
            Group annotation = (Group) groupClass.getAnnotation(Group.class);
            if (annotation != null) {
                groupMappings.put(new GroupWrapper(annotation), new ArrayList<>());
            }
        }
    }

    /**
     * Initialize all available groups and their commands in the given {@link com.github.rvesse.airline.builder.CliBuilder}.
     *
     * @param builder {@link com.github.rvesse.airline.builder.CliBuilder} to add the groups to
     */
    public static void buildCommandGroups(CliBuilder<Command> builder) {
        Map<GroupWrapper, List<Class<Command>>> allGroups =
            gatherGroupsFromCommandClasses(Cli.getCommandClasses(),
                                           Cli.getGroupClasses());

        for (Map.Entry<GroupWrapper, List<Class<Command>>> entry : allGroups.entrySet()) {
            if (entry.getKey().equals(GroupWrapper.NO_GROUP)) {
                List<Class<Command>> commandsInGroup = entry.getValue();
                for (Class<Command> command : commandsInGroup) {
                    replaceDescriptionFromAnnotation(command);
                    builder.withCommand(command);
                }

            } else {
                GroupBuilder<Command> group = builder.withGroup(entry.getKey().name);
                if (!entry.getKey().description.isEmpty()) {
                    group.withDescription(entry.getKey().description);
                }
                if (entry.getKey().defaultCommand != null) {
                    group.withDefaultCommand((Class<? extends Command>) entry.getKey().defaultCommand);
                }
                List<Class<Command>> commandsInGroup = entry.getValue();
                for (Class<Command> command : commandsInGroup) {
                    replaceDescriptionFromAnnotation(command);
                    group.withCommand(command);
                }
                if (!commandsInGroup.isEmpty() && !entry.getKey().hasDefaultCommand()) {
                    group.withDefaultCommand(commandsInGroup.get(0));
                }
            }
        }
    }

    private static void replaceDescriptionFromAnnotation(Class<Command> command) {
        com.github.rvesse.airline.annotations.Command annotation = command.getAnnotation(com.github.rvesse.airline.annotations.Command.class);
        String description = ReflectionUtils.getDescriptionFromClass(command);
        if (!description.isEmpty()) {
            ReflectionUtils.changeAnnotationValue(annotation, "description", description);
        }
    }

    /**
     * Adds all available commands (annotated with {@link Command}) as callables. The {@link HelpCommand} is not included, since it clashes with the
     * builtin help command from airline.
     *
     * @param builder the cli builder to add all commands to
     */
    public static void buildCallableCommandGroups(CliBuilder<Callable> builder) {
        Map<GroupWrapper, List<Class<Command>>> allGroupsAsCommands =
            gatherGroupsFromCommandClasses(Cli.getCommandClasses(),
                                           Cli.getGroupClasses());

        allGroupsAsCommands.remove(new GroupWrapper(HelpCommand.COMMAND_NAME));
        List<Class<Command>> noGroupCommands = allGroupsAsCommands.get(GroupWrapper.NO_GROUP);
        int indexOfHelp = noGroupCommands.indexOf(HelpCommand.class);
        if (indexOfHelp > -1) {
            noGroupCommands.remove(indexOfHelp);
        }

        Map<GroupWrapper, List<Class<Command>>> allGroups = new HashMap<>();
        for (Map.Entry<GroupWrapper, List<Class<Command>>> entry : allGroupsAsCommands.entrySet()) {
            allGroups.put(entry.getKey(), entry.getValue());
        }

        addCommandsAndGroupsToBuilder(builder, allGroups);
    }

    private static void addCommandsAndGroupsToBuilder(CliBuilder<Callable> builder, Map<GroupWrapper, List<Class<Command>>> allGroups) {
        for (Map.Entry<GroupWrapper, List<Class<Command>>> entry : allGroups.entrySet()) {
            if (entry.getKey().equals(GroupWrapper.NO_GROUP)) {
                List<Class<Command>> commandsInGroup = entry.getValue();
                for (Class<Command> command : commandsInGroup) {
                    builder.withCommand(command);
                }
            } else {
                GroupBuilder<Callable> group = builder.withGroup(entry.getKey().name);
                if (!entry.getKey().description.isEmpty()) {
                    group.withDescription(entry.getKey().description);
                }
                if (entry.getKey().defaultCommand != null) {
                    group.withDefaultCommand((Class<? extends Callable>) entry.getKey().defaultCommand);
                }
                List<Class<Command>> commandsInGroup = entry.getValue();
                for (Class<Command> command : commandsInGroup) {
                    group.withCommand(command);
                }
                if (!commandsInGroup.isEmpty() && !entry.getKey().hasDefaultCommand()) {
                    group.withDefaultCommand(commandsInGroup.get(0));
                }
            }
        }
    }
}
