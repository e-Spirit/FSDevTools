/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2021 e-Spirit AG
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

package com.espirit.moddev.cli.results.logging;

import com.espirit.moddev.cli.results.imports.ElementImportInfoImpl;
import com.espirit.moddev.cli.results.imports.EntityTypeImportInfoImpl;
import com.espirit.moddev.cli.results.imports.PropertyTypeImportInfoImpl;
import de.espirit.common.util.Pair;
import de.espirit.firstspirit.access.database.BasicEntityInfo;
import de.espirit.firstspirit.access.store.BasicElementInfo;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.Store;
import de.espirit.firstspirit.access.store.templatestore.*;
import de.espirit.firstspirit.agency.StoreAgent;
import de.espirit.firstspirit.io.FileHandle;
import de.espirit.firstspirit.store.access.BasicElementInfoImpl;
import de.espirit.firstspirit.store.access.StoreElements;
import de.espirit.firstspirit.store.access.nexport.*;
import de.espirit.firstspirit.store.access.nexport.io.ExportInfoFileHandle;
import de.espirit.firstspirit.store.access.nexport.operations.ExportOperation;
import de.espirit.firstspirit.store.access.nexport.operations.ImportOperation;
import de.espirit.firstspirit.transport.PropertiesTransportOptions;
import org.slf4j.Logger;

import java.io.Serializable;
import java.util.*;

/**
 * Utility class providing means to log the result of an ExportOperation in the element based
 * format.
 *
 * @author e -Spirit AG
 */
public enum AdvancedLogger {
    ;

    private static final int SPACE_INDENT = 35;

    /**
     * Logs the given {@code exportResult} to the given logger. Only performed if the log level is at least INFO.
     * A summary and some minor information will be logged to info. If loglevel DEBUG is enabled, detailed information
     * including file handles will be logged.
     * @param logger the logger the export result information will be logged to
     * @param storeAgent the store agent to use
     * @param exportResult the result to be loggged
     */
    public static void logExportResult(final Logger logger, final StoreAgent storeAgent, final ExportOperation.Result exportResult) {
        if (! logger.isInfoEnabled()) {
            // nothing to do if loglevel is not at least info
            return;
        }
        logger.info("Export done.");

        // log details and fetch summary
        logger.info("== DETAILS ==");
        final String created = logElements(logger, storeAgent, exportResult.getCreatedElements(), "Created elements");
        final String updated = logElements(logger, storeAgent, exportResult.getUpdatedElements(), "Updated elements");
        final String deleted = logElements(logger, storeAgent, exportResult.getDeletedElements(), "Deleted elements");
        final String moved = logElements(logger, storeAgent, exportResult.getMovedElements(), "  Moved elements");

        // log summary
        logger.info("== SUMMARY ==");
        logger.info(created);
        logger.info(updated);
        logger.info(deleted);
        logger.info(moved);
    }

    /**
     * Logs the given {@code importResult} to the given logger. Only performed if the log level is at least INFO.
     * A summary and some minor information will be logged to info. If loglevel DEBUG is enabled, detailed information
     * including file handles will be logged.
     * @param logger the logger the import result information will be logged to
     * @param storeAgent the store agent to use
     * @param importResult the result to be logged
     */
    public static void logImportResult(final Logger logger, final StoreAgent storeAgent, final ImportOperation.Result importResult) {
        if (!logger.isInfoEnabled()) {
            // nothing to do if loglevel is not at least info
            return;
        }
        logger.info("Import done.");

        // log details and fetch summary
        logger.info("== DETAILS ==");
        final String created = logElements(logger, storeAgent, createElementExportInfo(storeAgent, importResult, importResult.getCreatedElements(), ExportStatus.CREATED, null),                              "Created elements");
        final String updated = logElements(logger, storeAgent, createElementExportInfo(storeAgent, importResult, importResult.getUpdatedElements(), ExportStatus.UPDATED, importResult.getModifiedProjectProperties()),      "Updated elements");
        final String deleted = logElements(logger, storeAgent, createElementExportInfo(storeAgent, importResult, importResult.getDeletedElements(), ExportStatus.DELETED, null),                              "Deleted elements");
        final String moved = logElements(logger, storeAgent, createElementExportInfo(storeAgent, importResult, importResult.getMovedElements(), ExportStatus.MOVED, null),                                    "  Moved elements");
        final String lostAndFound = logElements(logger, storeAgent, createElementExportInfo(storeAgent, importResult, importResult.getLostAndFoundElements(), ExportStatus.MOVED, null),                      "L&Found elements");
        final String importProblems = logImportProblems(logger, storeAgent, importResult);

        // log summary
        logger.info("== SUMMARY ==");
        logger.info(created);
        logger.info(updated);
        logger.info(deleted);
        logger.info(moved);
        logger.info(lostAndFound);
        logger.info(importProblems);
    }

    static String logElements(final Logger logger, final StoreAgent storeAgent, final Collection<ExportInfo> elements, final String description) {
        if (logger.isInfoEnabled()) {
            // re-organize result
            final ReorganizedResult reorganizedResult = new ReorganizedResult(elements);

            // log short description
            final StringBuilder headline = new StringBuilder(description).append(": ");
            int count = elements.size();
            if (reorganizedResult.containsFsMeta()) {
                // ExportInfo.Type.FS_META (.FirstSpirit/Import*.txt) is always update --> do not show in result summary
                count--;
            }
            headline.append(count);

            logger.info(headline.toString().trim());

            // log elements
            logProjectProperties(logger, reorganizedResult.getProjectProperties());
            logStoreElements(logger, storeAgent, reorganizedResult.getStoreElements());
            logEntityTypes(logger, reorganizedResult.getEntityTypes());
            return buildSummary(elements, description, reorganizedResult);
        }
        return "";
    }

    static String logImportProblems(final Logger logger, final StoreAgent storeAgent, final ImportOperation.Result importResult) {
        // sort problems and create text
        final List<ImportOperation.Problem> problems = getSortedProblems(importResult);
        final StringBuilder builder = new StringBuilder();
        final String description = new StringBuilder("Problems: ").append(problems.size()).toString();
        logger.info(description);
        for (final ImportOperation.Problem problem : problems) {
            builder.setLength(0);
            builder.append(" - store: ").append(problem.getStoreType());
            problemAppendUidOrName(builder, storeAgent, problem);
            builder.append(" | reason: ").append(problem.getMessage());
            final String text = builder.toString();
            logger.info(text);
        }
        return getSpacedString(8) + "Problems: " + importResult.getProblems().size();
    }

    private static void problemAppendUidOrName(final StringBuilder builder, final StoreAgent storeAgent, final ImportOperation.Problem problem) {
        // we need a store agent
        if (storeAgent != null) {
            final Store store = storeAgent.getStore(problem.getStoreType());
            final IDProvider storeElement = store.getStoreElement(problem.getNodeId());
            // we need a store element
            if (storeElement != null) {
                if (storeElement.hasUid()) {
                    builder.append(" | uid: ").append(storeElement.getUid());
                } else {
                    builder.append(" | name: ").append(storeElement.getName());
                }
            }
        }
    }

    static String buildSummary(final Collection<ExportInfo> allElements, final String description, final ReorganizedResult reorganizedResult) {
        final StringBuilder summaryOutput = new StringBuilder();
        summaryOutput.append(description);
        summaryOutput.append(": ");
        int summaryCount = allElements.size();
        if (reorganizedResult.containsFsMeta()) {
            // ExportInfo.Type.FS_META (.FirstSpirit/Import*.txt) is always update --> do not show in result summary
            summaryCount--;
        }
        summaryOutput.append(summaryCount);

        // append project properties
        appendProjectPropertySummary(summaryOutput, reorganizedResult.getProjectProperties());
        // append store elements
        appendStoreElementSummary(summaryOutput, reorganizedResult.getStoreElements());
        // append entity types
        appendEntityTypeSummary(summaryOutput, reorganizedResult.getEntityTypes());

        // return result
        return summaryOutput.toString();
    }

    static void appendProjectPropertySummary(final StringBuilder stringBuilder, final Collection<PropertyTypeExportInfo> projectProperties) {
        if (!projectProperties.isEmpty()) {
            stringBuilder.append(" | project properties: ");
            stringBuilder.append(projectProperties.size());
        }
    }

    static void appendStoreElementSummary(final StringBuilder stringBuilder, final Map<Store.Type, List<ElementExportInfo>> storeElements) {
        if (!storeElements.isEmpty()) {
            // count total elements
            int totalStoreElements = 0;
            for (final List<ElementExportInfo> list : storeElements.values()) {
                totalStoreElements += list.size();
            }
            stringBuilder.append(" | store elements: ");
            stringBuilder.append(totalStoreElements);
            stringBuilder.append(" ( ");

            // append single store types
            int index = 0;
            for (final Map.Entry<Store.Type, List<ElementExportInfo>> entry : storeElements.entrySet()) {
                index++;
                stringBuilder.append(entry.getKey().getName());
                stringBuilder.append(": ");
                stringBuilder.append(entry.getValue().size());
                if (index < storeElements.size()) {
                    stringBuilder.append(", ");
                }
            }
            stringBuilder.append(" )");
        }
    }

    static void appendEntityTypeSummary(final StringBuilder stringBuilder, final Collection<EntityTypeExportInfo> entityTypes) {
        if (!entityTypes.isEmpty()) {
            // count total entities
            int totalEntityCount = 0;
            final Set<String> usedSchemas = new HashSet<>();
            for (final EntityTypeExportInfo exportInfo : entityTypes) {
                totalEntityCount += exportInfo.getEntities().size();
                usedSchemas.add(exportInfo.getSchema().getUid());
            }
            // put all schemas into a set
            stringBuilder.append(" | entity types: ");
            stringBuilder.append(entityTypes.size());
            stringBuilder.append(" ( ");
            stringBuilder.append("schemas: ");
            stringBuilder.append(usedSchemas.size());
            stringBuilder.append(", entities: ");
            stringBuilder.append(totalEntityCount);
            stringBuilder.append(" )");
        }
    }

    static void logProjectProperties(Logger logger, final Collection<PropertyTypeExportInfo> projectProperties) {
        if (logger.isInfoEnabled()) {
            // ignore empty properties
            if (projectProperties.isEmpty()) {
                return;
            }

            // append headline
            logger.info("- project properties: " + projectProperties.size());

            // append single properties
            final List<PropertyTypeExportInfo> sortedProjectProperties = new ArrayList<>(projectProperties);
            sortedProjectProperties.sort((first, second) -> {
                String firstName = first.getPropertyType() == null ? first.getName() : String.valueOf(first.getPropertyType().ordinal());
                String secondName = second.getPropertyType() == null ? second.getName() : String.valueOf(second.getPropertyType().ordinal());
                return firstName.compareTo(secondName);
            });
            for (final PropertyTypeExportInfo exportInfo : sortedProjectProperties) {
                final String identifier = toCamelCase("_", exportInfo.getName());
                final String spacedString = getSpacedString(SPACE_INDENT - identifier.length() + 1);
                logger.info(" - " + identifier + spacedString + getFilesStringForElement(exportInfo));
                logFileInfos(logger, exportInfo, "");
            }
        }
    }

    @SuppressWarnings("squid:S2629")
    static void logStoreElements(Logger logger, final StoreAgent storeAgent, final Map<Store.Type, List<ElementExportInfo>> storeElements) {
        if (! logger.isInfoEnabled()) {
            // nothing to do if loglevel is not at least info
            return;
        }
        // ignore empty store elements
        if (storeElements.isEmpty()) {
            return;
        }

        // count total elements
        int totalElements = 0;
        for (final Map.Entry<Store.Type, List<ElementExportInfo>> entry : storeElements.entrySet()) {
            totalElements += entry.getValue().size();
        }

        // append headline
        logger.info("- store elements: " + totalElements);

        // append single elements
        final List<ElementExportInfo> sortedElements = new ArrayList<>();
        for (final Map.Entry<Store.Type, List<ElementExportInfo>> entry : storeElements.entrySet()) {
            sortedElements.addAll(entry.getValue());
            sortedElements.sort(new ExportInfoComparator());
            logger.info(" - " + entry.getKey().getName() + ": " + sortedElements.size());
            for (final ElementExportInfo element : sortedElements) {
                String identifier = getStoreElementIdentifier(storeAgent, element);
                identifier += ": '" + element.getName() + "'";
                final String spacedString = getSpacedString(SPACE_INDENT - identifier.length());
                final String files = getFilesStringForElement(element);
                logger.info("  - " + identifier + spacedString + files);
                logFileInfos(logger, element, " ");
            }
            sortedElements.clear();
        }
    }

    @SuppressWarnings("squid:S2629")
    static void logEntityTypes(Logger logger, final Collection<EntityTypeExportInfo> entityTypes) {
        if (! logger.isInfoEnabled()) {
            return;
        }
        // ignore empty entity types
        if (entityTypes.isEmpty()) {
            return;
        }

        // sort entity types by schema
        final Map<String, List<EntityTypeExportInfo>> schemaMap = new TreeMap<>();
        int totalEntityCount = 0;
        for (final EntityTypeExportInfo exportInfo : entityTypes) {
            List<EntityTypeExportInfo> list = schemaMap.computeIfAbsent(exportInfo.getSchema().getUid(), k -> new ArrayList<>());
            list.add(exportInfo);
            totalEntityCount += exportInfo.getEntities().size();
        }

        // append headline
        final String entityTypesIdentifier = "entity types: " + entityTypes.size();
        final String spacedStringEntityTypes = getSpacedString(SPACE_INDENT - entityTypesIdentifier.length() + 2);
        final String headline = "- " + entityTypesIdentifier + spacedStringEntityTypes + " ( schemas: " + schemaMap.size() + ", entities: " + totalEntityCount + " )";
        logger.info(headline);

        // log schemas & entity types
        for (final Map.Entry<String, List<EntityTypeExportInfo>> entry : schemaMap.entrySet()) {
            // count entities of schema
            int entityCount = 0;
            for (final EntityTypeExportInfo entityType : entry.getValue()) {
                entityCount += entityType.getEntities().size();
            }

            final String schemaIdentifier = "Schema: '" + entry.getKey() + '\'';
            final String spacedStringSchema = getSpacedString(SPACE_INDENT - schemaIdentifier.length() + 1);
            logger.info(" - " + schemaIdentifier + spacedStringSchema + " ( entity types: " + entry.getValue().size() + ", entities: " + entityCount + " )");
            for (final EntityTypeExportInfo entityType : entry.getValue()) {
                final String identifier = "EntityType: '" + entityType.getEntityType() + "'";
                final String spacedString = getSpacedString(SPACE_INDENT - identifier.length());
                logger.info("  - " + identifier + spacedString + " ( entities: " + entityType.getEntities().size() + " )");
                logFileInfos(logger, entityType, "  ");
            }
        }
    }

    static void logFileInfos(Logger logger, final ExportInfo exportInfo, final String extraIndent) {
        if (!logger.isDebugEnabled()) {
            return;
        }
        logFileHandles(logger, exportInfo.getCreatedFileHandles(), "Created files", extraIndent);
        logFileHandles(logger, exportInfo.getUpdatedFileHandles(), "Updated files", extraIndent);
        logFileHandles(logger, exportInfo.getDeletedFileHandles(), "Deleted files", extraIndent);
        logMovedFileHandles(logger, exportInfo.getMovedFileHandles(), extraIndent);
    }

    //////////////////////////////////////////////////////////
    //
    // HELPER METHODS
    //
    //////////////////////////////////////////////////////////

    /**
     * Logging file handles will only be performed in LogLevel DEBUG.
     */
    static void logFileHandles(Logger logger, final Collection<ExportInfoFileHandle> fileHandles, final String description, final String extraIndent) {
       if (logger.isDebugEnabled()) {
            // ignore empty sets
            if (fileHandles.isEmpty()) {
                return;
            }

            // append headline
            logger.debug(extraIndent + "  - " + description + ": " + fileHandles.size());

            // sort file handles
            final ArrayList<ExportInfoFileHandle> sortedFileHandles = new ArrayList<>(fileHandles);
            sortedFileHandles.sort(Comparator.comparing(FileHandle::getPath));

            // append file handles
            for (final ExportInfoFileHandle fileHandle : sortedFileHandles) {
                logger.debug(extraIndent + "   - " + fileHandle.getPath());
            }
        }
    }

    /**
     * Logging file handles will only be performed in LogLevel DEBUG.
     */
    static void logMovedFileHandles(final Logger logger, final Collection<Pair<ExportInfoFileHandle, ExportInfoFileHandle>> fileHandles, final String extraIndent) {
        if (logger.isDebugEnabled()) {
            // ignore empty sets
            if (fileHandles.isEmpty()) {
                return;
            }

            // append headline
            logger.debug(extraIndent + "  - Moved files: " + fileHandles.size());

            // sort file handles
            final ArrayList<Pair<ExportInfoFileHandle, ExportInfoFileHandle>> sortedFileHandles = new ArrayList<>(fileHandles);
            sortedFileHandles.sort(Comparator.comparing(exportInfoFileHandleExportInfoFileHandlePair -> exportInfoFileHandleExportInfoFileHandlePair.getValue().getPath()));

            // append filehandles
            for (final Pair<ExportInfoFileHandle, ExportInfoFileHandle> pair : sortedFileHandles) {
                final String pathAppendix = " ( from '" + getDirectoryForFile(pair.getKey()) + "' to '" + getDirectoryForFile(pair.getValue()) + "' )";
                logger.debug(extraIndent + "   - " + pair.getKey().getName() + pathAppendix);
            }
        }
    }

    static String getStoreElementIdentifier(final StoreAgent storeAgent, final ElementExportInfo element) {
        String identifier = "";
        final BasicElementInfo elementInfo = element.getElementInfo();

        // workaround for duplicate TagNames
        if (TagNames.TEMPLATE.getName().equals(elementInfo.getNodeTag()) && storeAgent != null) {
            final Store store = storeAgent.getStore(elementInfo.getStoreType());
            final IDProvider storeElement = store.getStoreElement(elementInfo.getNodeId());
            if (storeElement != null) {
                // equal tag for PageTemplate & SectionTemplate
                final Class<? extends IDProvider> clazz = storeElement.getClass();
                if (SectionTemplate.class.isAssignableFrom(clazz)) {
                    identifier = StoreElements.determineElementType(SectionTemplate.class, null);
                } else if (PageTemplate.class.isAssignableFrom(clazz)) {
                    identifier = StoreElements.determineElementType(PageTemplate.class, null);
                }
            } else {
                // element not found in store --> fallback to "Template"
                identifier = StoreElements.determineElementType(Template.class, null);
            }
        }

        // identify by type
        if(identifier.isEmpty()) {
            identifier = StoreElements.determineElementType(elementInfo.getNodeTag());
        }
        return identifier;
    }

    /**
     * Converts the given string into a string with camel-case-notation (e.g. PAGE_STORE --> PageStore).
     *
     * @param regex  the regex to use
     * @param string the split to convert
     * @return the converted string in camel-case-notation (e.g. PAGE_STORE --> PageStore)
     */
    static String toCamelCase(final String regex, final String string) {
        final String[] split = string.split(regex);
        StringBuilder nameBuf = new StringBuilder();
        for (final String text : split) {
            if (text.isEmpty()) {
                continue;
            }

            nameBuf.append(Character.toUpperCase(text.charAt(0)));
            if (text.length() > 1) {
                nameBuf.append(text.substring(1, text.length()).toLowerCase(Locale.ENGLISH));
            }
        }
        return nameBuf.toString();
    }

    static String getDirectoryForFile(final ExportInfoFileHandle fileHandle) {
        final String path = fileHandle.getPath();
        final String name = fileHandle.getName();
        final int lastIndexOf = path.lastIndexOf(name);
        return path.isEmpty() || lastIndexOf < 1 ? path : path.substring(0, lastIndexOf - 1);
    }

    static String getFilesStringForElement(final ExportInfo element) {
        final StringBuilder builder = new StringBuilder();
        String fileString = getFilesString("created files", element.getCreatedFileHandles());
        fileString += getFilesString("updated files", element.getUpdatedFileHandles());
        fileString += getFilesString("deleted files", element.getDeletedFileHandles());
        fileString += getFilesString("moved files", element.getMovedFileHandles());
        if (!fileString.isEmpty()) {
            fileString = fileString.substring(0, fileString.length() - 1);
        }
        if(!fileString.isEmpty()) {
            builder.append(" (");
            builder.append(fileString);
            builder.append(" )");
        }
        return builder.toString();
    }

    static String getFilesString(final String description, final Collection<?> collection) {
        if (collection.isEmpty()) {
            return "";
        }
        return ' ' + description + ": " + collection.size() + ',';
    }

    static String getSpacedString(final int length) {
        final StringBuilder stringBuilder = new StringBuilder();
        for (int index = 0; index < length; index++) {
            stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }

    private static Collection<ExportInfo> createElementExportInfo(final StoreAgent storeAgent, final ImportOperation.Result importResult, final Collection<BasicElementInfo> elements, final ExportStatus status, final EnumSet<PropertiesTransportOptions.ProjectPropertyType> projectProperties) {
        final Collection<ExportInfo> result = new ArrayList<>();
        // add store elements to result
        for (final BasicElementInfo element : elements) {
            result.add(new ElementImportInfoImpl(status, element));
        }
        // add project properties to result
        if (projectProperties != null) {
            for (final PropertiesTransportOptions.ProjectPropertyType property : projectProperties) {
                result.add(new PropertyTypeImportInfoImpl(ExportStatus.UPDATED, property));
            }
        }
        // add entities to result
        if (status == ExportStatus.CREATED) {
            addEntitiesToResult(storeAgent, ExportStatus.CREATED, result, importResult.getCreatedEntities());
        } else if (status == ExportStatus.UPDATED) {
            try {
                addEntitiesToResult(storeAgent, ExportStatus.UPDATED, result, importResult.getUpdatedEntities());
            } catch (@SuppressWarnings("squid:S1166") final Exception ignore) {
                // ignore
                // -> we need to catch this because of 5.2.R8
                // --> ImportOperation.Result#getUpdatedEntities() does not exist in versions < 5.2.800
            }
        }
        return result;
    }

    private static void addEntitiesToResult(final StoreAgent storeAgent, final ExportStatus status, final Collection<ExportInfo> result, final Set<BasicEntityInfo> entities) {
        final Map<String, Collection<BasicEntityInfo>> schema2EntityMap = new HashMap<>();
        // add all entities to a map with key: SchemaUid#EntityType
        for (final BasicEntityInfo entity : entities) {
            final String key = entity.getSchemaUid() + "#" + entity.getEntityType();
            final Collection<BasicEntityInfo> collection = schema2EntityMap.computeIfAbsent(key, k -> new ArrayList<>());
            collection.add(entity);
        }
        // create EntityTypeImportInfos based on the collection
        for (final Collection<BasicEntityInfo> collection : schema2EntityMap.values()) {
            // ignore empty collections
            if (collection.isEmpty()) {
                continue;
            }
            // get the first entity
            final BasicEntityInfo firstEntity = collection.iterator().next();

            final String schemaNodeTag = TagNames.SCHEMA.getName();
            if (storeAgent != null) {
                // get the schema ... (if the store is a templateStore --> may not be the case in tests)
                final Store store = storeAgent.getStore(Store.Type.TEMPLATESTORE);
                Schema schema = null;
                if (store instanceof TemplateStoreRoot) {
                    final TemplateStoreRoot templateStore = (TemplateStoreRoot) store;
                    schema = templateStore.getSchemes().getSchemaByName(firstEntity.getSchemaUid());
                }
                final EntityTypeImportInfoImpl entityTypeImportInfo;
                // ... and create a new EntityTypeImportInfo
                if (schema != null) {
                    entityTypeImportInfo = new EntityTypeImportInfoImpl(status, new BasicElementInfoImpl(Store.Type.TEMPLATESTORE, schemaNodeTag, schema.getId(), schema.getUid(), schema.getRevision().getId()), firstEntity.getEntityType(), collection);
                } else {
                    entityTypeImportInfo = new EntityTypeImportInfoImpl(status, new BasicElementInfoImpl(Store.Type.TEMPLATESTORE, schemaNodeTag, -1, firstEntity.getSchemaUid(), -1), firstEntity.getEntityType(), collection);
                }
                result.add(entityTypeImportInfo);
            } else {
                // fallback --> used for tests only
                final EntityTypeImportInfoImpl entityTypeImportInfo = new EntityTypeImportInfoImpl(status, new BasicElementInfoImpl(Store.Type.TEMPLATESTORE, schemaNodeTag, -1, firstEntity.getSchemaUid(), -1), firstEntity.getEntityType(), collection);
                result.add(entityTypeImportInfo);
            }
        }
    }

    private static List<ImportOperation.Problem> getSortedProblems(final ImportOperation.Result importResult) {
        final List<ImportOperation.Problem> problems = new ArrayList<>(importResult.getProblems());
        problems.sort((problem1, problem2) -> {
            int result = problem1.getStoreType().compareTo(problem2.getStoreType());
            if (result == 0) {
                result = (int) (problem1.getNodeId() - problem2.getNodeId());
            }
            return result;
        });
        return problems;
    }

    private static class ExportInfoComparator implements Comparator<ElementExportInfo>, Serializable {

        private static final long serialVersionUID = -2121789213L;

        @Override
        public int compare(final ElementExportInfo first, final ElementExportInfo second) {
            return getPath(first).compareTo(getPath(second));
        }

        private static String getPath(final ElementExportInfo exportInfo) {
            String path = null;
            if (!exportInfo.getCreatedFileHandles().isEmpty()) {
                path = exportInfo.getCreatedFileHandles().iterator().next().getPath();
            } else if (!exportInfo.getUpdatedFileHandles().isEmpty()) {
                path = exportInfo.getUpdatedFileHandles().iterator().next().getPath();
            } else if (!exportInfo.getDeletedFileHandles().isEmpty()) {
                path = exportInfo.getDeletedFileHandles().iterator().next().getPath();
            } else if (!exportInfo.getMovedFileHandles().isEmpty()) {
                path = exportInfo.getMovedFileHandles().iterator().next().getValue().getPath();
            }
            return path != null ? path : exportInfo.getName();
        }

    }
}
