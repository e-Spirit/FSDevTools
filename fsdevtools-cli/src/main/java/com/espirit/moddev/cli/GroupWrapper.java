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

package com.espirit.moddev.cli;

import com.github.rvesse.airline.annotations.Group;

/**
 * A wrapper value class for airline groups. Offers convenience
 * over airline's API. The different constructors can be used to
 * wrap a given annotation, or to create a custom group for later
 * usage.
 *
 * @author e-Spirit AG
 */
public class GroupWrapper {
    public final String name;
    public final String description;
    public final Class<?>[] commands;
    public final Class<?> defaultCommand;
    public final boolean hidden;

    public GroupWrapper(String name) {
        this(name, "");
    }
    public GroupWrapper(String name, String description) {
        this.name = name.toLowerCase();
        this.description = description;
        commands = new Class<?>[0];
        defaultCommand = null;
        hidden = false;
    }

    public GroupWrapper(Group groupAnnotation) {
        name = groupAnnotation.name().toLowerCase();
        description = groupAnnotation.description();
        commands = groupAnnotation.commands();
        defaultCommand = groupAnnotation.defaultCommand();
        hidden = groupAnnotation.hidden();
    }

    /**
     * Indicates if a corresponding group has a default command registered.
     * @return true if defaultCommand is not null
     */
    public boolean hasDefaultCommand() {
        return defaultCommand != null;
    }

    @Override
    public boolean equals(Object object) {
        if(object == null) {
            return false;
        } else if(object.getClass() == this.getClass()) {
            return ((GroupWrapper) object).name.equals(name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public static final String NO_GROUP_GROUPNAME = "NO_GROUP";
    /**
     * Since airline doesn't provide proper handling of groupless commands,
     * a dummy group has to be used to add commands comfortably. This group
     * can be treated as a special container for those commands.
     */
    public static final GroupWrapper NO_GROUP = new GroupWrapper(NO_GROUP_GROUPNAME);
}
