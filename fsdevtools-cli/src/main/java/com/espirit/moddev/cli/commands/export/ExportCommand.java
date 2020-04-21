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
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

import static com.espirit.moddev.cli.api.parsing.parser.RootNodeIdentifierParser.getAllStorePostfixes;
import static com.espirit.moddev.cli.api.parsing.parser.SchemaIdentifierParser.CUSTOM_PREFIX_SCHEMA_OPTION;

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
                "export -- path:/templatestore/pagetemplates/folderName/folderToExport",
                "export -- path:/PageStore/pageFolderUid/pageUid",
                "export -- entities:products",
                "export -- page:homepage entities:news",
                "export -- schema:news",
                "export -- schema:news[setExportGidMapping=true]",
                "export -- projectproperty:LANGUAGES projectproperty:RESOLUTIONS",
                "export -- projectproperty:ALL"
            },
            descriptions = {
                "Exports a pagetemplate and a page",
                "Exports the templatestore and a page",
                "Exports the templatestore and a page",
                "Exports the first occurrence of the folder named 'folderToExport' beneath folder 'folderName'",
                "Exports the page identified by the path",
                "Exports all entities of the content2 node 'products' according to the configured filter",
                "Exports a page and news entities according to the configured filter",
                "Exports the database schema 'news'",
                "Exports the database schema 'news' with an additional Mapping.xml",
                "Exports the project properties languages and resolutions",
                "Exports all project properties"
            })
public class ExportCommand extends AbstractExportCommand {

    private static final String TAB_SEQUENCE = "\t\t\t\t";
    private static final int PREFIX_COUNT_PER_LINE = 5;

    @Override
    public ExportResult call() {
        return exportStoreElements();
    }

    @Description
    public static String getDescription() {
        return "Exports elements, entities, project properties. Use one or more of following identifiers to specify export objects.\n\r\n" + TAB_SEQUENCE
                + "1. Export elements based on uid with identifiers like 'pageref:pageRefUid'.\n" + TAB_SEQUENCE
                + "Known prefixes for uid-based export:\n" + TAB_SEQUENCE + getUidPrefixesWithNewlineEvery5thElement() + "\n\r\n" + TAB_SEQUENCE
                + "2. Export elements based on path 'path:/<STORE>/<UID>|<NAME>'.\n\r\n" + TAB_SEQUENCE
                + "3. Database schemas can be exported with or without options '" + CUSTOM_PREFIX_SCHEMA_OPTION + ":<schemaName>[<option>,<option2>]'\n\r\n" + TAB_SEQUENCE
                + "4. Export entities (database contents) with identifiers like 'entities:<CONTENT2_UID>'.\n\r\n" + TAB_SEQUENCE
                + "5. Export projectproperties with identifiers like 'projectproperty:RESOLUTIONS'\n" + TAB_SEQUENCE
                + "Known project properties:\n" + TAB_SEQUENCE + ProjectPropertiesParser.getAllPossibleValues().stream().collect(Collectors.joining(", ")) + "\n\r\n" + TAB_SEQUENCE
                + "6. Export store root nodes with identifiers like 'templatestore' or 'root:templatestore'\n\r" + TAB_SEQUENCE
                + "Known root node identifiers: " + getAllStorePostfixes().keySet().stream().collect(Collectors.joining(", "));
    }

    @NotNull
    private static String getUidPrefixesWithNewlineEvery5thElement() {
        final StringBuilder result = new StringBuilder();
        // extra counter for line breaks (because of added uid "Schema"):
        int uidNumber = 0;
        for(int i = 0; i <  UidMapping.values().length; i++) {
            final UidMapping currentMapping = UidMapping.values()[i];
            result.append(currentMapping.getPrefix());
            uidNumber = addCommaAndLineBreak(result, i, uidNumber);
            // 'schema' disappeared from this list when SchemaIdentifier was introduced, but should still be here:
            if (currentMapping.getPrefix().equalsIgnoreCase("pagetemplate")){
                result.append("schema");
                uidNumber = addCommaAndLineBreak(result, i, uidNumber);
            }
        }
        return result.toString();
    }

    private static int addCommaAndLineBreak(@NotNull final StringBuilder result, final int itemNumber, final int uidNumber){
        if(itemNumber != UidMapping.values().length-1) {
            result.append(",");
        }
        if(uidNumber != 0 && uidNumber % PREFIX_COUNT_PER_LINE == 0) {
            result.append("\n\r" + TAB_SEQUENCE);
        }
        return uidNumber + 1;
    }

}
