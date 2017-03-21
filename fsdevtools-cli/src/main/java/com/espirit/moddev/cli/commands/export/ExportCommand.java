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

package com.espirit.moddev.cli.commands.export;

import com.espirit.moddev.cli.api.annotations.Description;
import com.espirit.moddev.cli.api.parsing.identifier.UidMapping;
import com.espirit.moddev.cli.api.parsing.parser.ProjectPropertiesParser;
import com.espirit.moddev.cli.results.ExportResult;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.help.Examples;

import java.util.stream.Collectors;

import static com.espirit.moddev.cli.api.parsing.parser.RootNodeIdentifierParser.getAllStorePostfixes;

/**
 * This command can be used to export elements from all stores at the same time.
 * It makes use of its command arguments to retrieve elements for the export.
 *
 * @author e-Spirit AG
 */
@Command(name = "export")
@Examples(examples =
            {
                "export -- pagetemplate:default page:homepage",
                "export -- root:templatestore page:homepage",
                "export -- templatestore page:homepage",
                "export -- page:homepage entities:news",
                "export -- projectproperty:LANGUAGES projectproperty:RESOLUTIONS",
                "export -- projectproperty:ALL"
            },
            descriptions = {
                "Exports a pagetemplate and a page",
                "Exports the templatestore and a page",
                "Exports the templatestore and a page",
                "Exports a page and news entities according to the configured filter",
                "Exports the project properties languages and resolutions",
                "Exports all project properties"
            })
public class ExportCommand extends AbstractExportCommand {

    @Override
    public ExportResult call() {
        return exportStoreElements();
    }

    private static final String tabSequence = "\t\t\t\t";
    @Description
    public static String getDescription() {
        return "Exports elements (specified by the <identifiers> option) from all stores.\n\r\n" + tabSequence +
                "1. If no arguments given, all store roots and project properties are exported.\n\r\n" + tabSequence
                + "2. Export elements based on uid with identifiers like 'pageref:pageRefUid'.\n" + tabSequence
                + "Known prefixes for uid-based export:\n" + tabSequence + getUidPrefixesWithNewlineEvery5thElement() + "\n\r\n" + tabSequence
                + "3. Export entities with identifiers like 'entities:news'.\n\r\n" + tabSequence
                + "4. Export projectproperties with identifiers like 'projectproperty:RESOLUTIONS'\n" + tabSequence
                + "Known project properties:\n" + tabSequence + ProjectPropertiesParser.getAllPossibleValues().stream().collect(Collectors.joining(", ")) + "\n\r\n" + tabSequence
                + "5. Export store root nodes with identifiers like 'templatestore' or 'root:tempaltestore'\n\r" + tabSequence
                + "Known root node identifiers: " + getAllStorePostfixes().keySet().stream().collect(Collectors.joining(", "));
    }

    private static String getUidPrefixesWithNewlineEvery5thElement() {
        StringBuilder result = new StringBuilder();
        for(int i = 0; i <  UidMapping.values().length; i++) {
            UidMapping currentMapping = UidMapping.values()[i];

            result.append(currentMapping.getPrefix());
            if(i != UidMapping.values().length-1) {
                result.append(",");
            }
            if(i != 0 && i%5 == 0) {
                result.append("\n\r" + tabSequence);
            }
        }
        return result.toString();
    }
}
