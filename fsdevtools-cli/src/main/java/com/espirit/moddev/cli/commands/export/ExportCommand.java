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
import com.espirit.moddev.cli.api.parsing.identifier.Identifier;
import com.espirit.moddev.cli.api.parsing.identifier.RootNodeIdentifier;
import com.espirit.moddev.cli.api.parsing.parser.UidIdentifierParser;
import com.espirit.moddev.cli.results.ExportResult;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.help.Examples;

import de.espirit.firstspirit.agency.OperationAgent;
import de.espirit.firstspirit.agency.StoreAgent;
import de.espirit.firstspirit.store.access.nexport.operations.ExportOperation;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This command can be used to export elements from all stores at the same time.
 * It makes use of its command arguments to retrieve elements for the export.
 *
 * @author e-Spirit AG
 */
@Command(name = "all", groupNames = {"export"})
@Examples(examples =
            {
                "export all -- pagetemplate:default page:homepage",
                "export all -- root:templatestore page:homepage"
            },
            descriptions = {
                "Exports a pagetemplate and a page",
                "Exports the templatestore and a page"
            })
public class ExportCommand extends AbstractExportCommand {

    @Override
    public ExportResult call() {
        List<Identifier> uids = getIdentifiers();

        final ExportOperation exportOperation = this.getContext().requireSpecialist(OperationAgent.TYPE).getOperation(ExportOperation.TYPE);
        exportOperation.setDeleteObsoleteFiles(isDeleteObsoleteFiles());
        exportOperation.setExportChildElements(isExportChildElements());
        exportOperation.setExportParentElements(isExportParentElements());
        exportOperation.setExportRelease(isExportReleaseState());

        addExportElements(this.getContext().requireSpecialist(StoreAgent.TYPE), uids, exportOperation);

        final ExportOperation.Result result;
        try {
            result = exportOperation.perform(getSynchronizationDirectory());
        } catch (IOException e) {
            return new ExportResult(e);
        }

        return new ExportResult(result);
    }

    @Description
    public static String getDescription() {
        return "Exports elements from all stores. If no arguments given, the store roots are exported. \n"
                + "Known prefixes for export: " + UidIdentifierParser.getAllKnownPrefixStrings()
                                                    .stream()
                                                    .filter(prefix -> !UidIdentifierParser.getAllKnownPrefixStrings().contains(prefix))
                                                    .collect(Collectors.joining(", ")) + "\n"
                + "Known root node identifiers: " + RootNodeIdentifier.getAllStorePostfixes().keySet().stream().collect(Collectors.joining(", ")) + "\n\n";
    }
}
