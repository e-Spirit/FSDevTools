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

import com.espirit.moddev.cli.api.parsing.exceptions.IDProviderNotFoundException;
import com.espirit.moddev.cli.api.parsing.identifier.Identifier;
import com.espirit.moddev.cli.api.parsing.identifier.UidIdentifier;
import com.espirit.moddev.cli.api.parsing.parser.EntitiesIdentifierParser;
import com.espirit.moddev.cli.api.parsing.parser.PathIdentifierParser;
import com.espirit.moddev.cli.api.parsing.parser.ProjectPropertiesParser;
import com.espirit.moddev.cli.api.parsing.parser.RegistryBasedParser;
import com.espirit.moddev.cli.api.parsing.parser.RootNodeIdentifierParser;
import com.espirit.moddev.cli.api.parsing.parser.UidIdentifierParser;
import com.espirit.moddev.cli.commands.SimpleCommand;
import com.espirit.moddev.cli.commands.help.HelpCommand;
import com.espirit.moddev.cli.results.ExportResult;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Option;

import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.Store;
import de.espirit.firstspirit.agency.OperationAgent;
import de.espirit.firstspirit.agency.StoreAgent;
import de.espirit.firstspirit.store.access.nexport.operations.ExportOperation;
import de.espirit.firstspirit.transport.PropertiesTransportOptions;
import de.espirit.firstspirit.transport.PropertiesTransportOptions.ProjectPropertyType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

/**
 * This class gathers shared logic and options for different export commands. It can be extended for custom implementations of uid filtering, or to
 * override configurations.
 *
 * @author e -Spirit AG
 */
public abstract class AbstractExportCommand extends SimpleCommand<ExportResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Option(name = "--keepObsoleteFiles", description = "keep obsolete files in sync dir which are deleted in project")
    private boolean keepObsoleteFiles;

    @Option(name = "--excludeChildElements", description = "exclude child store elements")
    private boolean excludeChildElements;

    @Option(name = "--excludeParentElements", description = "exclude parent store elements")
    private boolean excludeParentElements;

    @Option(name = "--useReleaseState",
            description = "export only the release state of store elements; default is false (export of current state)")
    private boolean exportReleaseState;

    @Option(name = "--includeProjectProperties", description = "DEPRECATED: use '" + ProjectPropertiesParser.CUSTOM_PREFIX_PROJECT_PROPERTIES + ":" + ProjectPropertiesParser.ALL + "' instead. Export with project properties like resolutions or fonts")
    private boolean includeProjectProperties;

    @Arguments(title = "identifiers", description = "A list of various parsable identifiers. Please have a look at the command description for further information.")
    private List<String> identifiers = new LinkedList<>();

    private RegistryBasedParser parser;

    /**
     * Creates a new AbstractExportCommand and configures a set of default argument parsers.
     */
    public AbstractExportCommand() {
        parser = new RegistryBasedParser();
        parser.registerParser(new RootNodeIdentifierParser());
        parser.registerParser(new EntitiesIdentifierParser());
        parser.registerParser(new UidIdentifierParser());
        parser.registerParser(new ProjectPropertiesParser());
        parser.registerParser(new PathIdentifierParser());
    }

    /**
     * Gets delete obsolete files.
     *
     * @return the delete obsolete files
     */
    public boolean isDeleteObsoleteFiles() {
        return !keepObsoleteFiles;
    }

    /**
     * Gets export child elements.
     *
     * @return the export child elements
     */
    public boolean isExportChildElements() {
        return !excludeChildElements;
    }

    /**
     * Gets export parent elements.
     *
     * @return the export parent elements
     */
    public boolean isExportParentElements() {
        return !excludeParentElements;
    }

    /**
     * Indicates whether the release state should be used for belonging ExportOperation.
     *
     * @return true: operate on release state or false (default): on the current state.
     */
    public boolean isExportReleaseState() {
        return exportReleaseState;
    }


    /**
     * Defines whether this command should export the release state or not (default)
     *
     * @param exportReleaseState use {@code true} to export release state, {@code false} otherwise (default)
     * @see #isExportReleaseState()
     */
    public void setExportReleaseState(boolean exportReleaseState) {
        this.exportReleaseState = exportReleaseState;
    }

    /**
     * Log release state.
     *
     * @param idProvider the id provider
     */
    protected void logReleaseState(final IDProvider idProvider) {
        LOGGER.debug(idProvider.getUid() + " is release state? " + idProvider.getStore().isRelease());
    }

    /**
     * Adds elements to the given export operation. Uses registered parsers to retrieve elements.
     *
     * @param storeAgent      the StoreAgent to retrieve IDProviders with
     * @param identifiers     the identifiers of elements that should be added to the ExportOperation
     * @param exportOperation the ExportOperation to add the elements to
     * @throws IllegalArgumentException if the ExportOperation is null
     * @throws IDProviderNotFoundException if {@link Identifier#addToExportOperation(StoreAgent, boolean, ExportOperation)} throws it
     */
    public void addExportElements(final StoreAgent storeAgent, final List<Identifier> identifiers, final ExportOperation exportOperation) {
        if (exportOperation == null) {
            throw new IllegalArgumentException("No null ExportOperation allowed");
        }

        LOGGER.debug("Adding export elements...");
        if (identifiers.isEmpty()) {
            LOGGER.error("no identifiers found - pass at least 1 identifier --> call 'fs-cli help export' for details");
        } else {
            LOGGER.debug("addExportedElements - UIDs {}", identifiers);
            for (Identifier identifier : identifiers) {
                identifier.addToExportOperation(storeAgent, isExportReleaseState(), exportOperation);
            }

            if (isIncludeProjectProperties()) {
                LOGGER.warn("usage of flag '--includeProjectProperties' is deprecated - use {}:{}' instead", ProjectPropertiesParser.CUSTOM_PREFIX_PROJECT_PROPERTIES, ProjectPropertiesParser.ALL);
                addProjectProperties(exportOperation);
            }
        }
    }
     
    /**
     * Get a list of {@link UidIdentifier}s that specify the elements that should be synchronized.
     *
     * @return a {@link java.util.List} of {@link UidIdentifier}s that specify the elements that should be synchronized
     */
    public List<Identifier> getIdentifiers() {
        return identifiers.isEmpty() ? Collections.emptyList() : parser.parse(identifiers);
    }

    /**
     * Add project properties.
     *
     * @param exportOperation the export operation
     */
    public static void addProjectProperties(final ExportOperation exportOperation) {
        final PropertiesTransportOptions options = exportOperation.configurePropertiesExport();
        final EnumSet<ProjectPropertyType> propertiesToTransport = EnumSet.allOf(ProjectPropertyType.class);
        options.setProjectPropertiesTransport(propertiesToTransport);
    }

    /**
     * Adds store root nodes to the given export operation directly. This operation is different from adding a store root node's children
     * individually. This method can be overridden to add a subset of store roots only.
     *
     * @param storeAgent      the StoreAgent to retrieve store roots from
     * @param exportOperation the ExportOperation to add the store roots to
     */
    protected void addStoreRoots(final StoreAgent storeAgent, final ExportOperation exportOperation) {
        for (final Store.Type storeType : Store.Type.values()) {
            exportOperation.addElement(storeAgent.getStore(storeType, isExportReleaseState()));
        }
    }

    /**
     * Sets include project properties.
     *
     * @param includeProjectProperties the with project properties
     */
    public void setIncludeProjectProperties(final boolean includeProjectProperties) {
        this.includeProjectProperties = includeProjectProperties;
    }

    /**
     * Include project properties.
     *
     * @return the boolean
     */
    public boolean isIncludeProjectProperties() {
        return includeProjectProperties;
    }

    /**
     * Creates an {@link de.espirit.firstspirit.store.access.nexport.operations.ExportOperation} based on the current configuration and exports the
     * elements to the file system.
     *
     * @return the export result
     */
    @SuppressWarnings("squid:S2221")
    protected ExportResult exportStoreElements() {
        try {
            // no arguments --> call help-command
            final List<Identifier> identifierList = getIdentifiers();
            if (identifierList.isEmpty()) {
                LOGGER.error("no identifiers for export command found - pass at least 1 identifier --> see 'fs-cli help export' for details\nfs-cli help export");
                final HelpCommand helpCommand = new HelpCommand();
                helpCommand.addArguments("export");
                helpCommand.call();
                // return result with exception to force exit code 1
                final IllegalArgumentException exception = new IllegalArgumentException("no identifiers for export command found - pass at least 1 identifier --> see help message above");
                exception.setStackTrace(new StackTraceElement[0]);
                return new ExportResult(exception);
            }

            // create export operation
            final ExportOperation exportOperation = this.getContext().requireSpecialist(OperationAgent.TYPE).getOperation(ExportOperation.TYPE);
            exportOperation.setDeleteObsoleteFiles(isDeleteObsoleteFiles());
            exportOperation.setExportChildElements(isExportChildElements());
            exportOperation.setExportParentElements(isExportParentElements());
            exportOperation.setExportRelease(isExportReleaseState());
            addExportElements(this.getContext().requireSpecialist(StoreAgent.TYPE), identifierList, exportOperation);

            // export
            final String syncDirStr = getSynchronizationDirectoryString();
            LOGGER.info("exporting to directory '{}'", syncDirStr);
            return new ExportResult(getContext().requireSpecialist(StoreAgent.TYPE), exportOperation.perform(getSynchronizationDirectory(syncDirStr)));
        } catch (final Exception e) {
            return new ExportResult(e);
        }
    }

    /**
     * Adds the given string based UidIdentifier to this command's argument list. This method doesn't validate the input at all.
     *
     * @param identifier the string based UidIdentifier that should be added to this command's argument list
     */
    public void addIdentifier(final String identifier) {
        identifiers.add(identifier);
    }

}
