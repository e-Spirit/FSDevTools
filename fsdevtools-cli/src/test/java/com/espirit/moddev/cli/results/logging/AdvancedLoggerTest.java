package com.espirit.moddev.cli.results.logging;

import de.espirit.common.util.ElementProvider;
import de.espirit.common.util.Filter;
import de.espirit.common.util.Listable;
import de.espirit.common.util.Pair;
import de.espirit.firstspirit.access.*;
import de.espirit.firstspirit.access.database.BasicEntityInfo;
import de.espirit.firstspirit.access.database.BasicEntityInfoImpl;
import de.espirit.firstspirit.access.project.Group;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.access.store.*;
import de.espirit.firstspirit.access.store.templatestore.Workflow;
import de.espirit.firstspirit.access.store.templatestore.WorkflowLockException;
import de.espirit.firstspirit.access.store.templatestore.WorkflowPermission;
import de.espirit.firstspirit.agency.StoreAgent;
import de.espirit.firstspirit.forms.FormData;
import de.espirit.firstspirit.io.FileHandle;
import de.espirit.firstspirit.storage.Contrast;
import de.espirit.firstspirit.storage.Revision;
import de.espirit.firstspirit.store.access.BasicElementInfoImpl;
import de.espirit.firstspirit.store.access.PermissionMap;
import de.espirit.firstspirit.store.access.TagNames;
import de.espirit.firstspirit.store.access.nexport.*;
import de.espirit.firstspirit.store.access.nexport.io.ExportInfoFileHandle;
import de.espirit.firstspirit.store.access.nexport.io.ExportInfoFileHandleImpl;
import de.espirit.firstspirit.store.access.nexport.operations.ExportOperation;
import de.espirit.firstspirit.store.access.nexport.operations.ImportOperation;
import de.espirit.firstspirit.transport.PropertiesTransportOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.List;
import java.util.zip.ZipFile;

import static org.junit.Assert.assertEquals;

public class AdvancedLoggerTest {

    private static final String NEW_LINE = "\n";

    @Test
    public void testInfoLevelDisabled() throws Exception {
        final MockLogger logger = new MockLogger(false);
        logger.setInfoEnabled(false);
        AdvancedLogger.logExportResult(logger, new MockedExportResult(true));
        assertEquals("Result does not match.", "", logger.toString());
        AdvancedLogger.logImportResult(logger, new MockedImportResult(true), null);
        assertEquals("Result does not match.", "", logger.toString());
    }

    @Test
    public void testLogEmptyExportResult() throws Exception {
        {
            final MockLogger logger = new MockLogger(false);
            AdvancedLogger.logExportResult(logger, new MockedExportResult(false));
            // @formatter:off
            final String expected =
                    "[INFO] Export done."                + NEW_LINE +
                            "[INFO] == DETAILS =="       + NEW_LINE +
                            "[INFO] Created elements: 0" + NEW_LINE +
                            "[INFO] Updated elements: 0" + NEW_LINE +
                            "[INFO] Deleted elements: 0" + NEW_LINE +
                            "[INFO] Moved elements: 0"   + NEW_LINE +
                            "[INFO] == SUMMARY =="       + NEW_LINE +
                            "[INFO] Created elements: 0" + NEW_LINE +
                            "[INFO] Updated elements: 0" + NEW_LINE +
                            "[INFO] Deleted elements: 0" + NEW_LINE +
                            "[INFO]   Moved elements: 0" + NEW_LINE;
            // @formatter:on
            assertEquals("Result does not match.", expected, logger.toString());
        }
    }

    @Test
    public void testLogEmptyImportResult() throws Exception {
        {
            final MockLogger logger = new MockLogger(true);
            final MockedStoreAgent storeAgent = new MockedStoreAgent();
            storeAgent.addStore(Store.Type.PAGESTORE, new MockedStore(Store.Type.PAGESTORE));
            AdvancedLogger.logImportResult(logger, new MockedImportResult(false), null);
            // @formatter:off
            final String expected =
                    "[INFO] Import done."                + NEW_LINE +
                            "[INFO] == DETAILS =="       + NEW_LINE +
                            "[INFO] Created elements: 0" + NEW_LINE +
                            "[INFO] Updated elements: 0" + NEW_LINE +
                            "[INFO] Deleted elements: 0" + NEW_LINE +
                            "[INFO] Moved elements: 0"   + NEW_LINE +
                            "[INFO] L&Found elements: 0" + NEW_LINE +
                            "[INFO] Problems: 0"         + NEW_LINE +
                            "[INFO] == SUMMARY =="       + NEW_LINE +
                            "[INFO] Created elements: 0" + NEW_LINE +
                            "[INFO] Updated elements: 0" + NEW_LINE +
                            "[INFO] Deleted elements: 0" + NEW_LINE +
                            "[INFO]   Moved elements: 0" + NEW_LINE +
                            "[INFO] L&Found elements: 0" + NEW_LINE +
                            "[INFO]         Problems: 0" + NEW_LINE;

            // @formatter:on
            assertEquals("Result does not match.", expected, logger.toString());
        }
    }

    @Test
    public void testLogExportResult() throws Exception {
        {
            final MockLogger logger = new MockLogger(false);
            AdvancedLogger.logExportResult(logger, new MockedExportResult());
            //@formatter:off
            final String expected = "[INFO] Export done."               + NEW_LINE +
                                    "[INFO] == DETAILS =="              + NEW_LINE +
                                    "[INFO] Created elements: 7"        + NEW_LINE +
                                    "[INFO] - project properties: 1"    + NEW_LINE +
                                    "[INFO]  - Users                                ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO] - store elements: 5"        + NEW_LINE +
                                        "[INFO]  - pagestore: 1"        + NEW_LINE +
                                    "[INFO]   - Page: 'first'                       ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]  - templatestore: 4"        + NEW_LINE +
                                    "[INFO]   - PageTemplate: 'first'               ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]   - PageTemplate: 'fourth'              ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]   - PageTemplate: 'second'              ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]   - PageTemplate: 'third'               ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO] - entity types: 1                       ( schemas: 1, entities: 1 )"        + NEW_LINE +
                                    "[INFO]  - Schema: 'createdSchema'              ( entity types: 1, entities: 1 )"   + NEW_LINE +
                                    "[INFO]   - EntityType: 'createdType'           ( entities: 1 )"                    + NEW_LINE +
                                    "[INFO] Updated elements: 9"        + NEW_LINE +
                                    "[INFO] - project properties: 1"    + NEW_LINE +
                                    "[INFO]  - Users                                ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO] - store elements: 5"        + NEW_LINE +
                                    "[INFO]  - mediastore: 2"           + NEW_LINE +
                                    "[INFO]   - Media: 'first'                      ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]   - Media: 'second'                     ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]  - sitestore: 3"            + NEW_LINE +
                                    "[INFO]   - PageRef: 'first'                    ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]   - PageRef: 'second'                   ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]   - PageRef: 'third'                    ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO] - entity types: 3                       ( schemas: 2, entities: 6 )"        + NEW_LINE +
                                    "[INFO]  - Schema: 'updatedSchema1'             ( entity types: 2, entities: 5 )"   + NEW_LINE +
                                    "[INFO]   - EntityType: 'updatedType1'          ( entities: 2 )"                    + NEW_LINE +
                                    "[INFO]   - EntityType: 'updatedType2'          ( entities: 3 )"                    + NEW_LINE +
                                    "[INFO]  - Schema: 'updatedSchema2'             ( entity types: 1, entities: 1 )"   + NEW_LINE +
                                    "[INFO]   - EntityType: 'updatedType1'          ( entities: 1 )"                    + NEW_LINE +
                                    "[INFO] Deleted elements: 7"        + NEW_LINE +
                                    "[INFO] - project properties: 1"    + NEW_LINE +
                                    "[INFO]  - Users                                ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO] - store elements: 4"        + NEW_LINE +
                                    "[INFO]  - pagestore: 1"            + NEW_LINE +
                                    "[INFO]   - Page: 'first'                       ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]  - sitestore: 3"            + NEW_LINE +
                                    "[INFO]   - PageRef: 'first'                    ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]   - PageRef: 'second'                   ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]   - PageRef: 'third'                    ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO] - entity types: 2                       ( schemas: 1, entities: 7 )"        + NEW_LINE +
                                    "[INFO]  - Schema: 'deletedSchema'              ( entity types: 2, entities: 7 )"   + NEW_LINE +
                                    "[INFO]   - EntityType: 'deletedType1'          ( entities: 3 )"                    + NEW_LINE +
                                    "[INFO]   - EntityType: 'deletedType2'          ( entities: 4 )"                    + NEW_LINE +
                                    "[INFO] Moved elements: 9"        + NEW_LINE +
                                    "[INFO] - project properties: 1"    + NEW_LINE +
                                    "[INFO]  - Users                                ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO] - store elements: 6"        + NEW_LINE +
                                    "[INFO]  - mediastore: 2"           + NEW_LINE +
                                    "[INFO]   - Media: 'first'                      ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]   - Media: 'second'                     ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]  - templatestore: 4"        + NEW_LINE +
                                    "[INFO]   - PageTemplate: 'first'               ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]   - PageTemplate: 'fourth'              ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]   - PageTemplate: 'second'              ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]   - PageTemplate: 'third'               ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO] - entity types: 2                       ( schemas: 2, entities: 3 )"        + NEW_LINE +
                                    "[INFO]  - Schema: 'movedSchema1'               ( entity types: 1, entities: 1 )"   + NEW_LINE +
                                    "[INFO]   - EntityType: 'movedType1'            ( entities: 1 )"                    + NEW_LINE +
                                    "[INFO]  - Schema: 'movedSchema2'               ( entity types: 1, entities: 2 )"   + NEW_LINE +
                                    "[INFO]   - EntityType: 'movedType2'            ( entities: 2 )"                    + NEW_LINE +
                                    "[INFO] == SUMMARY =="              + NEW_LINE +
                                    "[INFO] Created elements: 7 | project properties: 1 | store elements: 5 ( pagestore: 1, templatestore: 4 ) | entity types: 1 ( schemas: 1, entities: 1 )"   + NEW_LINE +
                                    "[INFO] Updated elements: 9 | project properties: 1 | store elements: 5 ( mediastore: 2, sitestore: 3 ) | entity types: 3 ( schemas: 2, entities: 6 )"      + NEW_LINE +
                                    "[INFO] Deleted elements: 7 | project properties: 1 | store elements: 4 ( pagestore: 1, sitestore: 3 ) | entity types: 2 ( schemas: 1, entities: 7 )"       + NEW_LINE +
                                    "[INFO]   Moved elements: 9 | project properties: 1 | store elements: 6 ( mediastore: 2, templatestore: 4 ) | entity types: 2 ( schemas: 2, entities: 3 )"  + NEW_LINE;
            // @formatter:on
            assertEquals("Result does not match.", expected, logger.toString());
        }
        {
            final MockLogger logger = new MockLogger(true);
            AdvancedLogger.logExportResult(logger, new MockedExportResult());
            //@formatter:off
            final String expected = "[INFO] Export done."               + NEW_LINE +
                                    "[INFO] == DETAILS =="              + NEW_LINE +
                                    "[INFO] Created elements: 7"        + NEW_LINE +
                                    "[INFO] - project properties: 1"    + NEW_LINE +
                                    "[INFO]  - Users                                ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]   - Created files: 1"      + NEW_LINE +
                                    "[DEBUG]    - /path/USERS/0.txt"    + NEW_LINE +
                                    "[DEBUG]   - Updated files: 2"      + NEW_LINE +
                                    "[DEBUG]    - /path/USERS/0.txt"    + NEW_LINE +
                                    "[DEBUG]    - /path/USERS/1.txt"    + NEW_LINE +
                                    "[DEBUG]   - Deleted files: 3"      + NEW_LINE +
                                    "[DEBUG]    - /path/USERS/0.txt"    + NEW_LINE +
                                    "[DEBUG]    - /path/USERS/1.txt"    + NEW_LINE +
                                    "[DEBUG]    - /path/USERS/2.txt"    + NEW_LINE +
                                    "[DEBUG]   - Moved files: 4"        + NEW_LINE +
                                    "[DEBUG]    - 0.txt ( from '/from/USERS' to '/to/USERS' )" + NEW_LINE +
                                    "[DEBUG]    - 1.txt ( from '/from/USERS' to '/to/USERS' )" + NEW_LINE +
                                    "[DEBUG]    - 2.txt ( from '/from/USERS' to '/to/USERS' )" + NEW_LINE +
                                    "[DEBUG]    - 3.txt ( from '/from/USERS' to '/to/USERS' )" + NEW_LINE +
                                    "[INFO] - store elements: 5"        + NEW_LINE +
                                    "[INFO]  - pagestore: 1"            + NEW_LINE +
                                    "[INFO]   - Page: 'first'                       ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]    - Created files: 1"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]    - Updated files: 2"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/1.txt"   + NEW_LINE +
                                    "[DEBUG]    - Deleted files: 3"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/1.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/2.txt"   + NEW_LINE +
                                    "[DEBUG]    - Moved files: 4"       + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 2.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 3.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[INFO]  - templatestore: 4"        + NEW_LINE +
                                    "[INFO]   - PageTemplate: 'first'               ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]    - Created files: 1"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]    - Updated files: 2"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/1.txt"   + NEW_LINE +
                                    "[DEBUG]    - Deleted files: 3"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/1.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/2.txt"   + NEW_LINE +
                                    "[DEBUG]    - Moved files: 4"       + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 2.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 3.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[INFO]   - PageTemplate: 'fourth'              ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]    - Created files: 1"     + NEW_LINE +
                                    "[DEBUG]     - /path/fourth/0.txt"  + NEW_LINE +
                                    "[DEBUG]    - Updated files: 2"     + NEW_LINE +
                                    "[DEBUG]     - /path/fourth/0.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/fourth/1.txt"  + NEW_LINE +
                                    "[DEBUG]    - Deleted files: 3"     + NEW_LINE +
                                    "[DEBUG]     - /path/fourth/0.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/fourth/1.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/fourth/2.txt"  + NEW_LINE +
                                    "[DEBUG]    - Moved files: 4"       + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/fourth' to '/to/fourth' )" + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/fourth' to '/to/fourth' )" + NEW_LINE +
                                    "[DEBUG]     - 2.txt ( from '/from/fourth' to '/to/fourth' )" + NEW_LINE +
                                    "[DEBUG]     - 3.txt ( from '/from/fourth' to '/to/fourth' )" + NEW_LINE +
                                    "[INFO]   - PageTemplate: 'second'              ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]    - Created files: 1"     + NEW_LINE +
                                    "[DEBUG]     - /path/second/0.txt"  + NEW_LINE +
                                    "[DEBUG]    - Updated files: 2"     + NEW_LINE +
                                    "[DEBUG]     - /path/second/0.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/second/1.txt"  + NEW_LINE +
                                    "[DEBUG]    - Deleted files: 3"     + NEW_LINE +
                                    "[DEBUG]     - /path/second/0.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/second/1.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/second/2.txt"  + NEW_LINE +
                                    "[DEBUG]    - Moved files: 4"       + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[DEBUG]     - 2.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[DEBUG]     - 3.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[INFO]   - PageTemplate: 'third'               ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]    - Created files: 1"     + NEW_LINE +
                                    "[DEBUG]     - /path/third/0.txt"   + NEW_LINE +
                                    "[DEBUG]    - Updated files: 2"     + NEW_LINE +
                                    "[DEBUG]     - /path/third/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/third/1.txt"   + NEW_LINE +
                                    "[DEBUG]    - Deleted files: 3"     + NEW_LINE +
                                    "[DEBUG]     - /path/third/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/third/1.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/third/2.txt"   + NEW_LINE +
                                    "[DEBUG]    - Moved files: 4"       + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/third' to '/to/third' )" + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/third' to '/to/third' )" + NEW_LINE +
                                    "[DEBUG]     - 2.txt ( from '/from/third' to '/to/third' )" + NEW_LINE +
                                    "[DEBUG]     - 3.txt ( from '/from/third' to '/to/third' )" + NEW_LINE +
                                    "[INFO] - entity types: 1                       ( schemas: 1, entities: 1 )"        + NEW_LINE +
                                    "[INFO]  - Schema: 'createdSchema'              ( entity types: 1, entities: 1 )"   + NEW_LINE +
                                    "[INFO]   - EntityType: 'createdType'           ( entities: 1 )"                    + NEW_LINE +
                                    "[DEBUG]     - Created files: 1"    + NEW_LINE +
                                    "[DEBUG]      - /path/createdSchema#createdType/0.txt" + NEW_LINE +
                                    "[DEBUG]     - Updated files: 2"    + NEW_LINE +
                                    "[DEBUG]      - /path/createdSchema#createdType/0.txt" + NEW_LINE +
                                    "[DEBUG]      - /path/createdSchema#createdType/1.txt" + NEW_LINE +
                                    "[DEBUG]     - Deleted files: 3"    + NEW_LINE +
                                    "[DEBUG]      - /path/createdSchema#createdType/0.txt" + NEW_LINE +
                                    "[DEBUG]      - /path/createdSchema#createdType/1.txt" + NEW_LINE +
                                    "[DEBUG]      - /path/createdSchema#createdType/2.txt" + NEW_LINE +
                                    "[DEBUG]     - Moved files: 4"      + NEW_LINE +
                                    "[DEBUG]      - 0.txt ( from '/from/createdSchema#createdType' to '/to/createdSchema#createdType' )" + NEW_LINE +
                                    "[DEBUG]      - 1.txt ( from '/from/createdSchema#createdType' to '/to/createdSchema#createdType' )" + NEW_LINE +
                                    "[DEBUG]      - 2.txt ( from '/from/createdSchema#createdType' to '/to/createdSchema#createdType' )" + NEW_LINE +
                                    "[DEBUG]      - 3.txt ( from '/from/createdSchema#createdType' to '/to/createdSchema#createdType' )" + NEW_LINE +
                                    "[INFO] Updated elements: 9"        + NEW_LINE +
                                    "[INFO] - project properties: 1"    + NEW_LINE +
                                    "[INFO]  - Users                                ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]   - Created files: 1"      + NEW_LINE +
                                    "[DEBUG]    - /path/USERS/0.txt"    + NEW_LINE +
                                    "[DEBUG]   - Updated files: 2"      + NEW_LINE +
                                    "[DEBUG]    - /path/USERS/0.txt"    + NEW_LINE +
                                    "[DEBUG]    - /path/USERS/1.txt"    + NEW_LINE +
                                    "[DEBUG]   - Deleted files: 3"      + NEW_LINE +
                                    "[DEBUG]    - /path/USERS/0.txt"    + NEW_LINE +
                                    "[DEBUG]    - /path/USERS/1.txt"    + NEW_LINE +
                                    "[DEBUG]    - /path/USERS/2.txt"    + NEW_LINE +
                                    "[DEBUG]   - Moved files: 4"        + NEW_LINE +
                                    "[DEBUG]    - 0.txt ( from '/from/USERS' to '/to/USERS' )" + NEW_LINE +
                                    "[DEBUG]    - 1.txt ( from '/from/USERS' to '/to/USERS' )" + NEW_LINE +
                                    "[DEBUG]    - 2.txt ( from '/from/USERS' to '/to/USERS' )" + NEW_LINE +
                                    "[DEBUG]    - 3.txt ( from '/from/USERS' to '/to/USERS' )" + NEW_LINE +
                                    "[INFO] - store elements: 5"        + NEW_LINE +
                                    "[INFO]  - mediastore: 2"           + NEW_LINE +
                                    "[INFO]   - Media: 'first'                      ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]    - Created files: 1"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]    - Updated files: 2"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/1.txt"   + NEW_LINE +
                                    "[DEBUG]    - Deleted files: 3"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/1.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/2.txt"   + NEW_LINE +
                                    "[DEBUG]    - Moved files: 4"       + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 2.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 3.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[INFO]   - Media: 'second'                     ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]    - Created files: 1"     + NEW_LINE +
                                    "[DEBUG]     - /path/second/0.txt"  + NEW_LINE +
                                    "[DEBUG]    - Updated files: 2"     + NEW_LINE +
                                    "[DEBUG]     - /path/second/0.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/second/1.txt"  + NEW_LINE +
                                    "[DEBUG]    - Deleted files: 3"     + NEW_LINE +
                                    "[DEBUG]     - /path/second/0.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/second/1.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/second/2.txt"  + NEW_LINE +
                                    "[DEBUG]    - Moved files: 4"       + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[DEBUG]     - 2.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[DEBUG]     - 3.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[INFO]  - sitestore: 3"            + NEW_LINE +
                                    "[INFO]   - PageRef: 'first'                    ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]    - Created files: 1"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]    - Updated files: 2"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/1.txt"   + NEW_LINE +
                                    "[DEBUG]    - Deleted files: 3"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/1.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/2.txt"   + NEW_LINE +
                                    "[DEBUG]    - Moved files: 4"       + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 2.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 3.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[INFO]   - PageRef: 'second'                   ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]    - Created files: 1"     + NEW_LINE +
                                    "[DEBUG]     - /path/second/0.txt"  + NEW_LINE +
                                    "[DEBUG]    - Updated files: 2"     + NEW_LINE +
                                    "[DEBUG]     - /path/second/0.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/second/1.txt"  + NEW_LINE +
                                    "[DEBUG]    - Deleted files: 3"     + NEW_LINE +
                                    "[DEBUG]     - /path/second/0.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/second/1.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/second/2.txt"  + NEW_LINE +
                                    "[DEBUG]    - Moved files: 4"       + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[DEBUG]     - 2.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[DEBUG]     - 3.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[INFO]   - PageRef: 'third'                    ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]    - Created files: 1"     + NEW_LINE +
                                    "[DEBUG]     - /path/third/0.txt"   + NEW_LINE +
                                    "[DEBUG]    - Updated files: 2"     + NEW_LINE +
                                    "[DEBUG]     - /path/third/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/third/1.txt"   + NEW_LINE +
                                    "[DEBUG]    - Deleted files: 3"     + NEW_LINE +
                                    "[DEBUG]     - /path/third/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/third/1.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/third/2.txt"   + NEW_LINE +
                                    "[DEBUG]    - Moved files: 4"       + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/third' to '/to/third' )" + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/third' to '/to/third' )" + NEW_LINE +
                                    "[DEBUG]     - 2.txt ( from '/from/third' to '/to/third' )" + NEW_LINE +
                                    "[DEBUG]     - 3.txt ( from '/from/third' to '/to/third' )" + NEW_LINE +
                                    "[INFO] - entity types: 3                       ( schemas: 2, entities: 6 )"        + NEW_LINE +
                                    "[INFO]  - Schema: 'updatedSchema1'             ( entity types: 2, entities: 5 )"   + NEW_LINE +
                                    "[INFO]   - EntityType: 'updatedType1'          ( entities: 2 )"                    + NEW_LINE +
                                    "[DEBUG]     - Created files: 1"                            + NEW_LINE +
                                    "[DEBUG]      - /path/updatedSchema1#updatedType1/0.txt"    + NEW_LINE +
                                    "[DEBUG]     - Updated files: 2"                            + NEW_LINE +
                                    "[DEBUG]      - /path/updatedSchema1#updatedType1/0.txt"    + NEW_LINE +
                                    "[DEBUG]      - /path/updatedSchema1#updatedType1/1.txt"    + NEW_LINE +
                                    "[DEBUG]     - Deleted files: 3"                            + NEW_LINE +
                                    "[DEBUG]      - /path/updatedSchema1#updatedType1/0.txt"    + NEW_LINE +
                                    "[DEBUG]      - /path/updatedSchema1#updatedType1/1.txt"    + NEW_LINE +
                                    "[DEBUG]      - /path/updatedSchema1#updatedType1/2.txt"    + NEW_LINE +
                                    "[DEBUG]     - Moved files: 4"                              + NEW_LINE +
                                    "[DEBUG]      - 0.txt ( from '/from/updatedSchema1#updatedType1' to '/to/updatedSchema1#updatedType1' )" + NEW_LINE +
                                    "[DEBUG]      - 1.txt ( from '/from/updatedSchema1#updatedType1' to '/to/updatedSchema1#updatedType1' )" + NEW_LINE +
                                    "[DEBUG]      - 2.txt ( from '/from/updatedSchema1#updatedType1' to '/to/updatedSchema1#updatedType1' )" + NEW_LINE +
                                    "[DEBUG]      - 3.txt ( from '/from/updatedSchema1#updatedType1' to '/to/updatedSchema1#updatedType1' )" + NEW_LINE +
                                    "[INFO]   - EntityType: 'updatedType2'          ( entities: 3 )"                                         + NEW_LINE +
                                    "[DEBUG]     - Created files: 1"                            + NEW_LINE +
                                    "[DEBUG]      - /path/updatedSchema1#updatedType2/0.txt"    + NEW_LINE +
                                    "[DEBUG]     - Updated files: 2"                            + NEW_LINE +
                                    "[DEBUG]      - /path/updatedSchema1#updatedType2/0.txt"    + NEW_LINE +
                                    "[DEBUG]      - /path/updatedSchema1#updatedType2/1.txt"    + NEW_LINE +
                                    "[DEBUG]     - Deleted files: 3"                            + NEW_LINE +
                                    "[DEBUG]      - /path/updatedSchema1#updatedType2/0.txt"    + NEW_LINE +
                                    "[DEBUG]      - /path/updatedSchema1#updatedType2/1.txt"    + NEW_LINE +
                                    "[DEBUG]      - /path/updatedSchema1#updatedType2/2.txt"    + NEW_LINE +
                                    "[DEBUG]     - Moved files: 4"                              + NEW_LINE +
                                    "[DEBUG]      - 0.txt ( from '/from/updatedSchema1#updatedType2' to '/to/updatedSchema1#updatedType2' )" + NEW_LINE +
                                    "[DEBUG]      - 1.txt ( from '/from/updatedSchema1#updatedType2' to '/to/updatedSchema1#updatedType2' )" + NEW_LINE +
                                    "[DEBUG]      - 2.txt ( from '/from/updatedSchema1#updatedType2' to '/to/updatedSchema1#updatedType2' )" + NEW_LINE +
                                    "[DEBUG]      - 3.txt ( from '/from/updatedSchema1#updatedType2' to '/to/updatedSchema1#updatedType2' )" + NEW_LINE +
                                    "[INFO]  - Schema: 'updatedSchema2'             ( entity types: 1, entities: 1 )"                        + NEW_LINE +
                                    "[INFO]   - EntityType: 'updatedType1'          ( entities: 1 )"                                         + NEW_LINE +
                                    "[DEBUG]     - Created files: 1"                            + NEW_LINE +
                                    "[DEBUG]      - /path/updatedSchema2#updatedType1/0.txt"    + NEW_LINE +
                                    "[DEBUG]     - Updated files: 2"                            + NEW_LINE +
                                    "[DEBUG]      - /path/updatedSchema2#updatedType1/0.txt"    + NEW_LINE +
                                    "[DEBUG]      - /path/updatedSchema2#updatedType1/1.txt"    + NEW_LINE +
                                    "[DEBUG]     - Deleted files: 3"                            + NEW_LINE +
                                    "[DEBUG]      - /path/updatedSchema2#updatedType1/0.txt"    + NEW_LINE +
                                    "[DEBUG]      - /path/updatedSchema2#updatedType1/1.txt"    + NEW_LINE +
                                    "[DEBUG]      - /path/updatedSchema2#updatedType1/2.txt"    + NEW_LINE +
                                    "[DEBUG]     - Moved files: 4"                              + NEW_LINE +
                                    "[DEBUG]      - 0.txt ( from '/from/updatedSchema2#updatedType1' to '/to/updatedSchema2#updatedType1' )" + NEW_LINE +
                                    "[DEBUG]      - 1.txt ( from '/from/updatedSchema2#updatedType1' to '/to/updatedSchema2#updatedType1' )" + NEW_LINE +
                                    "[DEBUG]      - 2.txt ( from '/from/updatedSchema2#updatedType1' to '/to/updatedSchema2#updatedType1' )" + NEW_LINE +
                                    "[DEBUG]      - 3.txt ( from '/from/updatedSchema2#updatedType1' to '/to/updatedSchema2#updatedType1' )" + NEW_LINE +
                                    "[INFO] Deleted elements: 7"                                + NEW_LINE +
                                    "[INFO] - project properties: 1"                            + NEW_LINE +
                                    "[INFO]  - Users                                ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]   - Created files: 1"                              + NEW_LINE +
                                    "[DEBUG]    - /path/USERS/0.txt"                            + NEW_LINE +
                                    "[DEBUG]   - Updated files: 2"                              + NEW_LINE +
                                    "[DEBUG]    - /path/USERS/0.txt"                            + NEW_LINE +
                                    "[DEBUG]    - /path/USERS/1.txt"                            + NEW_LINE +
                                    "[DEBUG]   - Deleted files: 3"                              + NEW_LINE +
                                    "[DEBUG]    - /path/USERS/0.txt"                            + NEW_LINE +
                                    "[DEBUG]    - /path/USERS/1.txt"                            + NEW_LINE +
                                    "[DEBUG]    - /path/USERS/2.txt"                            + NEW_LINE +
                                    "[DEBUG]   - Moved files: 4"                                + NEW_LINE +
                                    "[DEBUG]    - 0.txt ( from '/from/USERS' to '/to/USERS' )"  + NEW_LINE +
                                    "[DEBUG]    - 1.txt ( from '/from/USERS' to '/to/USERS' )"  + NEW_LINE +
                                    "[DEBUG]    - 2.txt ( from '/from/USERS' to '/to/USERS' )"  + NEW_LINE +
                                    "[DEBUG]    - 3.txt ( from '/from/USERS' to '/to/USERS' )"  + NEW_LINE +
                                    "[INFO] - store elements: 4"                                + NEW_LINE +
                                    "[INFO]  - pagestore: 1"                                    + NEW_LINE +
                                    "[INFO]   - Page: 'first'                       ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]    - Created files: 1"                             + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"                           + NEW_LINE +
                                    "[DEBUG]    - Updated files: 2"                             + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"                           + NEW_LINE +
                                    "[DEBUG]     - /path/first/1.txt"                           + NEW_LINE +
                                    "[DEBUG]    - Deleted files: 3"                             + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"                           + NEW_LINE +
                                    "[DEBUG]     - /path/first/1.txt"                           + NEW_LINE +
                                    "[DEBUG]     - /path/first/2.txt"                           + NEW_LINE +
                                    "[DEBUG]    - Moved files: 4"                               + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 2.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 3.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[INFO]  - sitestore: 3"                                    + NEW_LINE +
                                    "[INFO]   - PageRef: 'first'                    ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]    - Created files: 1"                             + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"                           + NEW_LINE +
                                    "[DEBUG]    - Updated files: 2"                             + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"                           + NEW_LINE +
                                    "[DEBUG]     - /path/first/1.txt"                           + NEW_LINE +
                                    "[DEBUG]    - Deleted files: 3"                             + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"                           + NEW_LINE +
                                    "[DEBUG]     - /path/first/1.txt"                           + NEW_LINE +
                                    "[DEBUG]     - /path/first/2.txt"                           + NEW_LINE +
                                    "[DEBUG]    - Moved files: 4"                               + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 2.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 3.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[INFO]   - PageRef: 'second'                   ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]    - Created files: 1"                                 + NEW_LINE +
                                    "[DEBUG]     - /path/second/0.txt"                              + NEW_LINE +
                                    "[DEBUG]    - Updated files: 2"                                 + NEW_LINE +
                                    "[DEBUG]     - /path/second/0.txt"                              + NEW_LINE +
                                    "[DEBUG]     - /path/second/1.txt"                              + NEW_LINE +
                                    "[DEBUG]    - Deleted files: 3"                                 + NEW_LINE +
                                    "[DEBUG]     - /path/second/0.txt"                              + NEW_LINE +
                                    "[DEBUG]     - /path/second/1.txt"                              + NEW_LINE +
                                    "[DEBUG]     - /path/second/2.txt"                              + NEW_LINE +
                                    "[DEBUG]    - Moved files: 4"                                   + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/second' to '/to/second' )"   + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/second' to '/to/second' )"   + NEW_LINE +
                                    "[DEBUG]     - 2.txt ( from '/from/second' to '/to/second' )"   + NEW_LINE +
                                    "[DEBUG]     - 3.txt ( from '/from/second' to '/to/second' )"   + NEW_LINE +
                                    "[INFO]   - PageRef: 'third'                    ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]    - Created files: 1"                                 + NEW_LINE +
                                    "[DEBUG]     - /path/third/0.txt"                               + NEW_LINE +
                                    "[DEBUG]    - Updated files: 2"                                 + NEW_LINE +
                                    "[DEBUG]     - /path/third/0.txt"                               + NEW_LINE +
                                    "[DEBUG]     - /path/third/1.txt"                               + NEW_LINE +
                                    "[DEBUG]    - Deleted files: 3"                                 + NEW_LINE +
                                    "[DEBUG]     - /path/third/0.txt"                               + NEW_LINE +
                                    "[DEBUG]     - /path/third/1.txt"                               + NEW_LINE +
                                    "[DEBUG]     - /path/third/2.txt"                               + NEW_LINE +
                                    "[DEBUG]    - Moved files: 4"                                   + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/third' to '/to/third' )"     + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/third' to '/to/third' )"     + NEW_LINE +
                                    "[DEBUG]     - 2.txt ( from '/from/third' to '/to/third' )"     + NEW_LINE +
                                    "[DEBUG]     - 3.txt ( from '/from/third' to '/to/third' )"     + NEW_LINE +
                                    "[INFO] - entity types: 2                       ( schemas: 1, entities: 7 )"        + NEW_LINE +
                                    "[INFO]  - Schema: 'deletedSchema'              ( entity types: 2, entities: 7 )"   + NEW_LINE +
                                    "[INFO]   - EntityType: 'deletedType1'          ( entities: 3 )"                    + NEW_LINE +
                                    "[DEBUG]     - Created files: 1"                                + NEW_LINE +
                                    "[DEBUG]      - /path/deletedSchema#deletedType1/0.txt"         + NEW_LINE +
                                    "[DEBUG]     - Updated files: 2"                                + NEW_LINE +
                                    "[DEBUG]      - /path/deletedSchema#deletedType1/0.txt"         + NEW_LINE +
                                    "[DEBUG]      - /path/deletedSchema#deletedType1/1.txt"         + NEW_LINE +
                                    "[DEBUG]     - Deleted files: 3"                                + NEW_LINE +
                                    "[DEBUG]      - /path/deletedSchema#deletedType1/0.txt"         + NEW_LINE +
                                    "[DEBUG]      - /path/deletedSchema#deletedType1/1.txt"         + NEW_LINE +
                                    "[DEBUG]      - /path/deletedSchema#deletedType1/2.txt"         + NEW_LINE +
                                    "[DEBUG]     - Moved files: 4"                                  + NEW_LINE +
                                    "[DEBUG]      - 0.txt ( from '/from/deletedSchema#deletedType1' to '/to/deletedSchema#deletedType1' )" + NEW_LINE +
                                    "[DEBUG]      - 1.txt ( from '/from/deletedSchema#deletedType1' to '/to/deletedSchema#deletedType1' )" + NEW_LINE +
                                    "[DEBUG]      - 2.txt ( from '/from/deletedSchema#deletedType1' to '/to/deletedSchema#deletedType1' )" + NEW_LINE +
                                    "[DEBUG]      - 3.txt ( from '/from/deletedSchema#deletedType1' to '/to/deletedSchema#deletedType1' )" + NEW_LINE +
                                    "[INFO]   - EntityType: 'deletedType2'          ( entities: 4 )"    + NEW_LINE +
                                    "[DEBUG]     - Created files: 1"                                    + NEW_LINE +
                                    "[DEBUG]      - /path/deletedSchema#deletedType2/0.txt"             + NEW_LINE +
                                    "[DEBUG]     - Updated files: 2"                                    + NEW_LINE +
                                    "[DEBUG]      - /path/deletedSchema#deletedType2/0.txt"             + NEW_LINE +
                                    "[DEBUG]      - /path/deletedSchema#deletedType2/1.txt"             + NEW_LINE +
                                    "[DEBUG]     - Deleted files: 3"                                    + NEW_LINE +
                                    "[DEBUG]      - /path/deletedSchema#deletedType2/0.txt"             + NEW_LINE +
                                    "[DEBUG]      - /path/deletedSchema#deletedType2/1.txt"             + NEW_LINE +
                                    "[DEBUG]      - /path/deletedSchema#deletedType2/2.txt"             + NEW_LINE +
                                    "[DEBUG]     - Moved files: 4"                                      + NEW_LINE +
                                    "[DEBUG]      - 0.txt ( from '/from/deletedSchema#deletedType2' to '/to/deletedSchema#deletedType2' )" + NEW_LINE +
                                    "[DEBUG]      - 1.txt ( from '/from/deletedSchema#deletedType2' to '/to/deletedSchema#deletedType2' )" + NEW_LINE +
                                    "[DEBUG]      - 2.txt ( from '/from/deletedSchema#deletedType2' to '/to/deletedSchema#deletedType2' )" + NEW_LINE +
                                    "[DEBUG]      - 3.txt ( from '/from/deletedSchema#deletedType2' to '/to/deletedSchema#deletedType2' )" + NEW_LINE +
                                    "[INFO] Moved elements: 9"        + NEW_LINE +
                                    "[INFO] - project properties: 1"                               + NEW_LINE +
                                    "[INFO]  - Users                                ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]   - Created files: 1"      + NEW_LINE +
                                    "[DEBUG]    - /path/USERS/0.txt"    + NEW_LINE +
                                    "[DEBUG]   - Updated files: 2"      + NEW_LINE +
                                    "[DEBUG]    - /path/USERS/0.txt"    + NEW_LINE +
                                    "[DEBUG]    - /path/USERS/1.txt"    + NEW_LINE +
                                    "[DEBUG]   - Deleted files: 3"      + NEW_LINE +
                                    "[DEBUG]    - /path/USERS/0.txt"    + NEW_LINE +
                                    "[DEBUG]    - /path/USERS/1.txt"    + NEW_LINE +
                                    "[DEBUG]    - /path/USERS/2.txt"    + NEW_LINE +
                                    "[DEBUG]   - Moved files: 4"        + NEW_LINE +
                                    "[DEBUG]    - 0.txt ( from '/from/USERS' to '/to/USERS' )" + NEW_LINE +
                                    "[DEBUG]    - 1.txt ( from '/from/USERS' to '/to/USERS' )" + NEW_LINE +
                                    "[DEBUG]    - 2.txt ( from '/from/USERS' to '/to/USERS' )" + NEW_LINE +
                                    "[DEBUG]    - 3.txt ( from '/from/USERS' to '/to/USERS' )" + NEW_LINE +
                                    "[INFO] - store elements: 6"        + NEW_LINE +
                                    "[INFO]  - mediastore: 2"           + NEW_LINE +
                                    "[INFO]   - Media: 'first'                      ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]    - Created files: 1"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]    - Updated files: 2"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/1.txt"   + NEW_LINE +
                                    "[DEBUG]    - Deleted files: 3"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/1.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/2.txt"   + NEW_LINE +
                                    "[DEBUG]    - Moved files: 4"       + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 2.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 3.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[INFO]   - Media: 'second'                     ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]    - Created files: 1"     + NEW_LINE +
                                    "[DEBUG]     - /path/second/0.txt"  + NEW_LINE +
                                    "[DEBUG]    - Updated files: 2"     + NEW_LINE +
                                    "[DEBUG]     - /path/second/0.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/second/1.txt"  + NEW_LINE +
                                    "[DEBUG]    - Deleted files: 3"     + NEW_LINE +
                                    "[DEBUG]     - /path/second/0.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/second/1.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/second/2.txt"  + NEW_LINE +
                                    "[DEBUG]    - Moved files: 4"       + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[DEBUG]     - 2.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[DEBUG]     - 3.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[INFO]  - templatestore: 4"        + NEW_LINE +
                                    "[INFO]   - PageTemplate: 'first'               ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]    - Created files: 1"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]    - Updated files: 2"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/1.txt"   + NEW_LINE +
                                    "[DEBUG]    - Deleted files: 3"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/1.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/2.txt"   + NEW_LINE +
                                    "[DEBUG]    - Moved files: 4"       + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 2.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 3.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[INFO]   - PageTemplate: 'fourth'              ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]    - Created files: 1"     + NEW_LINE +
                                    "[DEBUG]     - /path/fourth/0.txt"  + NEW_LINE +
                                    "[DEBUG]    - Updated files: 2"     + NEW_LINE +
                                    "[DEBUG]     - /path/fourth/0.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/fourth/1.txt"  + NEW_LINE +
                                    "[DEBUG]    - Deleted files: 3"     + NEW_LINE +
                                    "[DEBUG]     - /path/fourth/0.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/fourth/1.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/fourth/2.txt"  + NEW_LINE +
                                    "[DEBUG]    - Moved files: 4"       + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/fourth' to '/to/fourth' )" + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/fourth' to '/to/fourth' )" + NEW_LINE +
                                    "[DEBUG]     - 2.txt ( from '/from/fourth' to '/to/fourth' )" + NEW_LINE +
                                    "[DEBUG]     - 3.txt ( from '/from/fourth' to '/to/fourth' )" + NEW_LINE +
                                    "[INFO]   - PageTemplate: 'second'              ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]    - Created files: 1"     + NEW_LINE +
                                    "[DEBUG]     - /path/second/0.txt"  + NEW_LINE +
                                    "[DEBUG]    - Updated files: 2"     + NEW_LINE +
                                    "[DEBUG]     - /path/second/0.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/second/1.txt"  + NEW_LINE +
                                    "[DEBUG]    - Deleted files: 3"     + NEW_LINE +
                                    "[DEBUG]     - /path/second/0.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/second/1.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/second/2.txt"  + NEW_LINE +
                                    "[DEBUG]    - Moved files: 4"       + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[DEBUG]     - 2.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[DEBUG]     - 3.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[INFO]   - PageTemplate: 'third'               ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]    - Created files: 1"     + NEW_LINE +
                                    "[DEBUG]     - /path/third/0.txt"   + NEW_LINE +
                                    "[DEBUG]    - Updated files: 2"     + NEW_LINE +
                                    "[DEBUG]     - /path/third/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/third/1.txt"   + NEW_LINE +
                                    "[DEBUG]    - Deleted files: 3"     + NEW_LINE +
                                    "[DEBUG]     - /path/third/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/third/1.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/third/2.txt"   + NEW_LINE +
                                    "[DEBUG]    - Moved files: 4"       + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/third' to '/to/third' )" + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/third' to '/to/third' )" + NEW_LINE +
                                    "[DEBUG]     - 2.txt ( from '/from/third' to '/to/third' )" + NEW_LINE +
                                    "[DEBUG]     - 3.txt ( from '/from/third' to '/to/third' )" + NEW_LINE +
                                    "[INFO] - entity types: 2                       ( schemas: 2, entities: 3 )"        + NEW_LINE +
                                    "[INFO]  - Schema: 'movedSchema1'               ( entity types: 1, entities: 1 )"   + NEW_LINE +
                                    "[INFO]   - EntityType: 'movedType1'            ( entities: 1 )"                    + NEW_LINE +
                                    "[DEBUG]     - Created files: 1"                        + NEW_LINE +
                                    "[DEBUG]      - /path/movedSchema1#movedType1/0.txt"    + NEW_LINE +
                                    "[DEBUG]     - Updated files: 2"                        + NEW_LINE +
                                    "[DEBUG]      - /path/movedSchema1#movedType1/0.txt"    + NEW_LINE +
                                    "[DEBUG]      - /path/movedSchema1#movedType1/1.txt"    + NEW_LINE +
                                    "[DEBUG]     - Deleted files: 3"                        + NEW_LINE +
                                    "[DEBUG]      - /path/movedSchema1#movedType1/0.txt"    + NEW_LINE +
                                    "[DEBUG]      - /path/movedSchema1#movedType1/1.txt"    + NEW_LINE +
                                    "[DEBUG]      - /path/movedSchema1#movedType1/2.txt"    + NEW_LINE +
                                    "[DEBUG]     - Moved files: 4"                          + NEW_LINE +
                                    "[DEBUG]      - 0.txt ( from '/from/movedSchema1#movedType1' to '/to/movedSchema1#movedType1' )"    + NEW_LINE +
                                    "[DEBUG]      - 1.txt ( from '/from/movedSchema1#movedType1' to '/to/movedSchema1#movedType1' )"    + NEW_LINE +
                                    "[DEBUG]      - 2.txt ( from '/from/movedSchema1#movedType1' to '/to/movedSchema1#movedType1' )"    + NEW_LINE +
                                    "[DEBUG]      - 3.txt ( from '/from/movedSchema1#movedType1' to '/to/movedSchema1#movedType1' )"    + NEW_LINE +
                                    "[INFO]  - Schema: 'movedSchema2'               ( entity types: 1, entities: 2 )"                   + NEW_LINE +
                                    "[INFO]   - EntityType: 'movedType2'            ( entities: 2 )"                                    + NEW_LINE +
                                    "[DEBUG]     - Created files: 1"                        + NEW_LINE +
                                    "[DEBUG]      - /path/movedSchema2#movedType2/0.txt"    + NEW_LINE +
                                    "[DEBUG]     - Updated files: 2"                        + NEW_LINE +
                                    "[DEBUG]      - /path/movedSchema2#movedType2/0.txt"    + NEW_LINE +
                                    "[DEBUG]      - /path/movedSchema2#movedType2/1.txt"    + NEW_LINE +
                                    "[DEBUG]     - Deleted files: 3"                        + NEW_LINE +
                                    "[DEBUG]      - /path/movedSchema2#movedType2/0.txt"    + NEW_LINE +
                                    "[DEBUG]      - /path/movedSchema2#movedType2/1.txt"    + NEW_LINE +
                                    "[DEBUG]      - /path/movedSchema2#movedType2/2.txt"    + NEW_LINE +
                                    "[DEBUG]     - Moved files: 4"                          + NEW_LINE +
                                    "[DEBUG]      - 0.txt ( from '/from/movedSchema2#movedType2' to '/to/movedSchema2#movedType2' )" + NEW_LINE +
                                    "[DEBUG]      - 1.txt ( from '/from/movedSchema2#movedType2' to '/to/movedSchema2#movedType2' )" + NEW_LINE +
                                    "[DEBUG]      - 2.txt ( from '/from/movedSchema2#movedType2' to '/to/movedSchema2#movedType2' )" + NEW_LINE +
                                    "[DEBUG]      - 3.txt ( from '/from/movedSchema2#movedType2' to '/to/movedSchema2#movedType2' )" + NEW_LINE +
                                    "[INFO] == SUMMARY ==" + NEW_LINE +
                                    "[INFO] Created elements: 7 | project properties: 1 | store elements: 5 ( pagestore: 1, templatestore: 4 ) | entity types: 1 ( schemas: 1, entities: 1 )"   + NEW_LINE +
                                    "[INFO] Updated elements: 9 | project properties: 1 | store elements: 5 ( mediastore: 2, sitestore: 3 ) | entity types: 3 ( schemas: 2, entities: 6 )"      + NEW_LINE +
                                    "[INFO] Deleted elements: 7 | project properties: 1 | store elements: 4 ( pagestore: 1, sitestore: 3 ) | entity types: 2 ( schemas: 1, entities: 7 )"       + NEW_LINE +
                                    "[INFO]   Moved elements: 9 | project properties: 1 | store elements: 6 ( mediastore: 2, templatestore: 4 ) | entity types: 2 ( schemas: 2, entities: 3 )"  + NEW_LINE;
            // @formatter:on
            assertEquals("Result does not match.", expected, logger.toString());
        }
    }

    @Test
    public void testLogImportResult() throws Exception {
        {
            final MockLogger logger = new MockLogger(false);
            final MockedImportResult mockedImportResult = new MockedImportResult(true);
            AdvancedLogger.logImportResult(logger, mockedImportResult, mockedImportResult.getStoreAgent());
            //@formatter:off
            final String expected =
                    "[INFO] Import done."                                                               + NEW_LINE +
                    "[INFO] == DETAILS =="                                                              + NEW_LINE +
                    "[INFO] Created elements: 17"                                                       + NEW_LINE +
                    "[INFO] - store elements: 7"                                                        + NEW_LINE +
                    "[INFO]  - pagestore: 1"                                                            + NEW_LINE +
                    "[INFO]   - Page: 'created_PAGE_1'             "                                    + NEW_LINE +
                    "[INFO]  - mediastore: 2"                                                           + NEW_LINE +
                    "[INFO]   - Media: 'created_MEDIUM_2'          "                                    + NEW_LINE +
                    "[INFO]   - Media: 'created_MEDIUM_3'          "                                    + NEW_LINE +
                    "[INFO]  - templatestore: 4"                                                        + NEW_LINE +
                    "[INFO]   - FormatTemplate: 'created_FORMATTEMPLATE_6'"                             + NEW_LINE +
                    "[INFO]   - FormatTemplate: 'created_FORMATTEMPLATE_7'"                             + NEW_LINE +
                    "[INFO]   - LinkTemplate: 'created_LINKTEMPLATE_5'"                                 + NEW_LINE +
                    "[INFO]   - PageTemplates: 'created_PAGETEMPLATES_4'"                               + NEW_LINE +
                    "[INFO] - entity types: 10                      ( schemas: 3, entities: 12 )"       + NEW_LINE +
                    "[INFO]  - Schema: 'schema1'                    ( entity types: 4, entities: 6 )"   + NEW_LINE +
                    "[INFO]   - EntityType: 'created_entityType2'   ( entities: 1 )"                    + NEW_LINE +
                    "[INFO]   - EntityType: 'created_entityType1'   ( entities: 2 )"                    + NEW_LINE +
                    "[INFO]   - EntityType: 'updated_entityType1'   ( entities: 2 )"                    + NEW_LINE +
                    "[INFO]   - EntityType: 'updated_entityType2'   ( entities: 1 )"                    + NEW_LINE +
                    "[INFO]  - Schema: 'schema2'                    ( entity types: 4, entities: 4 )"   + NEW_LINE +
                    "[INFO]   - EntityType: 'created_entityType2'   ( entities: 1 )"                    + NEW_LINE +
                    "[INFO]   - EntityType: 'created_entityType1'   ( entities: 1 )"                    + NEW_LINE +
                    "[INFO]   - EntityType: 'updated_entityType1'   ( entities: 1 )"                    + NEW_LINE +
                    "[INFO]   - EntityType: 'updated_entityType2'   ( entities: 1 )"                    + NEW_LINE +
                    "[INFO]  - Schema: 'schema3'                    ( entity types: 2, entities: 2 )"   + NEW_LINE +
                    "[INFO]   - EntityType: 'created_entityType1'   ( entities: 1 )"                    + NEW_LINE +
                    "[INFO]   - EntityType: 'updated_entityType1'   ( entities: 1 )"                    + NEW_LINE +
                    "[INFO] Updated elements: 26"                                                       + NEW_LINE +
                    "[INFO] - project properties: 9"                                                    + NEW_LINE +
                    "[INFO]  - Common                              "                                    + NEW_LINE +
                    "[INFO]  - Resolutions                         "                                    + NEW_LINE +
                    "[INFO]  - Groups                              "                                    + NEW_LINE +
                    "[INFO]  - ScheduleEntries                     "                                    + NEW_LINE +
                    "[INFO]  - TemplateSets                        "                                    + NEW_LINE +
                    "[INFO]  - Fonts                               "                                    + NEW_LINE +
                    "[INFO]  - ModuleConfigurations                "                                    + NEW_LINE +
                    "[INFO]  - Languages                           "                                    + NEW_LINE +
                    "[INFO]  - Users                               "                                    + NEW_LINE +
                    "[INFO] - store elements: 7"                                                        + NEW_LINE +
                    "[INFO]  - pagestore: 1"                                                            + NEW_LINE +
                    "[INFO]   - Page: 'updated_PAGE_1'             "                                    + NEW_LINE +
                    "[INFO]  - mediastore: 2"                                                           + NEW_LINE +
                    "[INFO]   - Media: 'updated_MEDIUM_2'          "                                    + NEW_LINE +
                    "[INFO]   - Media: 'updated_MEDIUM_3'          "                                    + NEW_LINE +
                    "[INFO]  - templatestore: 4"                                                        + NEW_LINE +
                    "[INFO]   - FormatTemplate: 'updated_FORMATTEMPLATE_6'"                             + NEW_LINE +
                    "[INFO]   - FormatTemplate: 'updated_FORMATTEMPLATE_7'"                             + NEW_LINE +
                    "[INFO]   - LinkTemplate: 'updated_LINKTEMPLATE_5'"                                 + NEW_LINE +
                    "[INFO]   - PageTemplates: 'updated_PAGETEMPLATES_4'"                               + NEW_LINE +
                    "[INFO] - entity types: 10                      ( schemas: 3, entities: 12 )"       + NEW_LINE +
                    "[INFO]  - Schema: 'schema1'                    ( entity types: 4, entities: 6 )"   + NEW_LINE +
                    "[INFO]   - EntityType: 'created_entityType2'   ( entities: 1 )"                    + NEW_LINE +
                    "[INFO]   - EntityType: 'created_entityType1'   ( entities: 2 )"                    + NEW_LINE +
                    "[INFO]   - EntityType: 'updated_entityType1'   ( entities: 2 )"                    + NEW_LINE +
                    "[INFO]   - EntityType: 'updated_entityType2'   ( entities: 1 )"                    + NEW_LINE +
                    "[INFO]  - Schema: 'schema2'                    ( entity types: 4, entities: 4 )"   + NEW_LINE +
                    "[INFO]   - EntityType: 'created_entityType2'   ( entities: 1 )"                    + NEW_LINE +
                    "[INFO]   - EntityType: 'created_entityType1'   ( entities: 1 )"                    + NEW_LINE +
                    "[INFO]   - EntityType: 'updated_entityType1'   ( entities: 1 )"                    + NEW_LINE +
                    "[INFO]   - EntityType: 'updated_entityType2'   ( entities: 1 )"                    + NEW_LINE +
                    "[INFO]  - Schema: 'schema3'                    ( entity types: 2, entities: 2 )"   + NEW_LINE +
                    "[INFO]   - EntityType: 'created_entityType1'   ( entities: 1 )"                    + NEW_LINE +
                    "[INFO]   - EntityType: 'updated_entityType1'   ( entities: 1 )"                    + NEW_LINE +
                    "[INFO] Deleted elements: 7"                                                        + NEW_LINE +
                    "[INFO] - store elements: 7"                                                        + NEW_LINE +
                    "[INFO]  - pagestore: 1"                                                            + NEW_LINE +
                    "[INFO]   - Page: 'deleted_PAGE_1'             "                                    + NEW_LINE +
                    "[INFO]  - mediastore: 2"                                                           + NEW_LINE +
                    "[INFO]   - Media: 'deleted_MEDIUM_2'          "                                    + NEW_LINE +
                    "[INFO]   - Media: 'deleted_MEDIUM_3'          "                                    + NEW_LINE +
                    "[INFO]  - templatestore: 4"                                                        + NEW_LINE +
                    "[INFO]   - FormatTemplate: 'deleted_FORMATTEMPLATE_6'"                             + NEW_LINE +
                    "[INFO]   - FormatTemplate: 'deleted_FORMATTEMPLATE_7'"                             + NEW_LINE +
                    "[INFO]   - LinkTemplate: 'deleted_LINKTEMPLATE_5'"                                 + NEW_LINE +
                    "[INFO]   - PageTemplates: 'deleted_PAGETEMPLATES_4'"                               + NEW_LINE +
                    "[INFO] Moved elements: 7"                                                          + NEW_LINE +
                    "[INFO] - store elements: 7"                                                        + NEW_LINE +
                    "[INFO]  - pagestore: 1"                                                            + NEW_LINE +
                    "[INFO]   - Page: 'moved_PAGE_1'               "                                    + NEW_LINE +
                    "[INFO]  - mediastore: 2"                                                           + NEW_LINE +
                    "[INFO]   - Media: 'moved_MEDIUM_2'            "                                    + NEW_LINE +
                    "[INFO]   - Media: 'moved_MEDIUM_3'            "                                    + NEW_LINE +
                    "[INFO]  - templatestore: 4"                                                        + NEW_LINE +
                    "[INFO]   - FormatTemplate: 'moved_FORMATTEMPLATE_6'"                               + NEW_LINE +
                    "[INFO]   - FormatTemplate: 'moved_FORMATTEMPLATE_7'"                               + NEW_LINE +
                    "[INFO]   - LinkTemplate: 'moved_LINKTEMPLATE_5'"                                   + NEW_LINE +
                    "[INFO]   - PageTemplates: 'moved_PAGETEMPLATES_4'"                                 + NEW_LINE +
                    "[INFO] L&Found elements: 7"                                                        + NEW_LINE +
                    "[INFO] - store elements: 7"                                                        + NEW_LINE +
                    "[INFO]  - pagestore: 1"                                                            + NEW_LINE +
                    "[INFO]   - Page: 'lostAndFound_PAGE_1'        "                                    + NEW_LINE +
                    "[INFO]  - mediastore: 2"                                                           + NEW_LINE +
                    "[INFO]   - Media: 'lostAndFound_MEDIUM_2'     "                                    + NEW_LINE +
                    "[INFO]   - Media: 'lostAndFound_MEDIUM_3'     "                                    + NEW_LINE +
                    "[INFO]  - templatestore: 4"                                                        + NEW_LINE +
                    "[INFO]   - FormatTemplate: 'lostAndFound_FORMATTEMPLATE_6'"                        + NEW_LINE +
                    "[INFO]   - FormatTemplate: 'lostAndFound_FORMATTEMPLATE_7'"                        + NEW_LINE +
                    "[INFO]   - LinkTemplate: 'lostAndFound_LINKTEMPLATE_5'"                            + NEW_LINE +
                    "[INFO]   - PageTemplates: 'lostAndFound_PAGETEMPLATES_4'"                          + NEW_LINE +
                    "[INFO] Problems: 4"                                                                + NEW_LINE +
                    "[INFO]  - store: PAGESTORE | uid: pagestore_uid_1337 | reason: IdProvider not found"                                                                                       + NEW_LINE +
                    "[INFO]  - store: MEDIASTORE | uid: mediastore_uid_123 | reason: Resolution invalid"                                                                                        + NEW_LINE +
                    "[INFO]  - store: MEDIASTORE | uid: mediastore_uid_1932 | reason: Medium invalid"                                                                                           + NEW_LINE +
                    "[INFO]  - store: TEMPLATESTORE | name: templatestore_name_1231 | reason: GOM is invalid"                                                                                   + NEW_LINE +
                    "[INFO] == SUMMARY =="                                                                                                                                                      + NEW_LINE +
                    "[INFO] Created elements: 17 | store elements: 7 ( pagestore: 1, mediastore: 2, templatestore: 4 ) | entity types: 10 ( schemas: 3, entities: 12 )"                         + NEW_LINE +
                    "[INFO] Updated elements: 26 | project properties: 9 | store elements: 7 ( pagestore: 1, mediastore: 2, templatestore: 4 ) | entity types: 10 ( schemas: 3, entities: 12 )" + NEW_LINE +
                    "[INFO] Deleted elements: 7 | store elements: 7 ( pagestore: 1, mediastore: 2, templatestore: 4 )" + NEW_LINE +
                    "[INFO]   Moved elements: 7 | store elements: 7 ( pagestore: 1, mediastore: 2, templatestore: 4 )" + NEW_LINE +
                    "[INFO] L&Found elements: 7 | store elements: 7 ( pagestore: 1, mediastore: 2, templatestore: 4 )" + NEW_LINE +
                    "[INFO]         Problems: 4"                                                                       + NEW_LINE;
            // @formatter:on
            assertEquals("Result does not match.", expected, logger.toString());
        }
        {
            final MockLogger logger = new MockLogger(true);
            final MockedImportResult mockedImportResult = new MockedImportResult(true);
            AdvancedLogger.logImportResult(logger, mockedImportResult, mockedImportResult.getStoreAgent());
            //@formatter:off
            final String expected =
                    "[INFO] Import done."                                            + NEW_LINE +
                    "[INFO] == DETAILS =="                                           + NEW_LINE +
                    "[INFO] Created elements: 17"                                    + NEW_LINE +
                    "[INFO] - store elements: 7"                                     + NEW_LINE +
                    "[INFO]  - pagestore: 1"                                         + NEW_LINE +
                    "[INFO]   - Page: 'created_PAGE_1'             "                 + NEW_LINE +
                    "[INFO]  - mediastore: 2"                                        + NEW_LINE +
                    "[INFO]   - Media: 'created_MEDIUM_2'          "                 + NEW_LINE +
                    "[INFO]   - Media: 'created_MEDIUM_3'          "                 + NEW_LINE +
                    "[INFO]  - templatestore: 4"                                     + NEW_LINE +
                    "[INFO]   - FormatTemplate: 'created_FORMATTEMPLATE_6'"          + NEW_LINE +
                    "[INFO]   - FormatTemplate: 'created_FORMATTEMPLATE_7'"          + NEW_LINE +
                    "[INFO]   - LinkTemplate: 'created_LINKTEMPLATE_5'"              + NEW_LINE +
                    "[INFO]   - PageTemplates: 'created_PAGETEMPLATES_4'"            + NEW_LINE +
                    "[INFO] - entity types: 10                      ( schemas: 3, entities: 12 )"       + NEW_LINE +
                    "[INFO]  - Schema: 'schema1'                    ( entity types: 4, entities: 6 )"   + NEW_LINE +
                    "[INFO]   - EntityType: 'created_entityType2'   ( entities: 1 )" + NEW_LINE +
                    "[INFO]   - EntityType: 'created_entityType1'   ( entities: 2 )" + NEW_LINE +
                    "[INFO]   - EntityType: 'updated_entityType1'   ( entities: 2 )" + NEW_LINE +
                    "[INFO]   - EntityType: 'updated_entityType2'   ( entities: 1 )" + NEW_LINE +
                    "[INFO]  - Schema: 'schema2'                    ( entity types: 4, entities: 4 )"   + NEW_LINE +
                    "[INFO]   - EntityType: 'created_entityType2'   ( entities: 1 )" + NEW_LINE +
                    "[INFO]   - EntityType: 'created_entityType1'   ( entities: 1 )" + NEW_LINE +
                    "[INFO]   - EntityType: 'updated_entityType1'   ( entities: 1 )" + NEW_LINE +
                    "[INFO]   - EntityType: 'updated_entityType2'   ( entities: 1 )" + NEW_LINE +
                    "[INFO]  - Schema: 'schema3'                    ( entity types: 2, entities: 2 )"   + NEW_LINE +
                    "[INFO]   - EntityType: 'created_entityType1'   ( entities: 1 )" + NEW_LINE +
                    "[INFO]   - EntityType: 'updated_entityType1'   ( entities: 1 )" + NEW_LINE +
                    "[INFO] Updated elements: 26"                                    + NEW_LINE +
                    "[INFO] - project properties: 9"                                 + NEW_LINE +
                    "[INFO]  - Common                              "                 + NEW_LINE +
                    "[INFO]  - Resolutions                         "                 + NEW_LINE +
                    "[INFO]  - Groups                              "                 + NEW_LINE +
                    "[INFO]  - ScheduleEntries                     "                 + NEW_LINE +
                    "[INFO]  - TemplateSets                        "                 + NEW_LINE +
                    "[INFO]  - Fonts                               "                 + NEW_LINE +
                    "[INFO]  - ModuleConfigurations                "                 + NEW_LINE +
                    "[INFO]  - Languages                           "                 + NEW_LINE +
                    "[INFO]  - Users                               "                 + NEW_LINE +
                    "[INFO] - store elements: 7"                                     + NEW_LINE +
                    "[INFO]  - pagestore: 1"                                         + NEW_LINE +
                    "[INFO]   - Page: 'updated_PAGE_1'             "                 + NEW_LINE +
                    "[INFO]  - mediastore: 2"                                        + NEW_LINE +
                    "[INFO]   - Media: 'updated_MEDIUM_2'          "                 + NEW_LINE +
                    "[INFO]   - Media: 'updated_MEDIUM_3'          "                 + NEW_LINE +
                    "[INFO]  - templatestore: 4"                                     + NEW_LINE +
                    "[INFO]   - FormatTemplate: 'updated_FORMATTEMPLATE_6'"          + NEW_LINE +
                    "[INFO]   - FormatTemplate: 'updated_FORMATTEMPLATE_7'"          + NEW_LINE +
                    "[INFO]   - LinkTemplate: 'updated_LINKTEMPLATE_5'"              + NEW_LINE +
                    "[INFO]   - PageTemplates: 'updated_PAGETEMPLATES_4'"            + NEW_LINE +
                    "[INFO] - entity types: 10                      ( schemas: 3, entities: 12 )"       + NEW_LINE +
                    "[INFO]  - Schema: 'schema1'                    ( entity types: 4, entities: 6 )"   + NEW_LINE +
                    "[INFO]   - EntityType: 'created_entityType2'   ( entities: 1 )" + NEW_LINE +
                    "[INFO]   - EntityType: 'created_entityType1'   ( entities: 2 )" + NEW_LINE +
                    "[INFO]   - EntityType: 'updated_entityType1'   ( entities: 2 )" + NEW_LINE +
                    "[INFO]   - EntityType: 'updated_entityType2'   ( entities: 1 )" + NEW_LINE +
                    "[INFO]  - Schema: 'schema2'                    ( entity types: 4, entities: 4 )"   + NEW_LINE +
                    "[INFO]   - EntityType: 'created_entityType2'   ( entities: 1 )" + NEW_LINE +
                    "[INFO]   - EntityType: 'created_entityType1'   ( entities: 1 )" + NEW_LINE +
                    "[INFO]   - EntityType: 'updated_entityType1'   ( entities: 1 )" + NEW_LINE +
                    "[INFO]   - EntityType: 'updated_entityType2'   ( entities: 1 )" + NEW_LINE +
                    "[INFO]  - Schema: 'schema3'                    ( entity types: 2, entities: 2 )"   + NEW_LINE +
                    "[INFO]   - EntityType: 'created_entityType1'   ( entities: 1 )" + NEW_LINE +
                    "[INFO]   - EntityType: 'updated_entityType1'   ( entities: 1 )" + NEW_LINE +
                    "[INFO] Deleted elements: 7"                                     + NEW_LINE +
                    "[INFO] - store elements: 7"                                     + NEW_LINE +
                    "[INFO]  - pagestore: 1"                                         + NEW_LINE +
                    "[INFO]   - Page: 'deleted_PAGE_1'             "                 + NEW_LINE +
                    "[INFO]  - mediastore: 2"                                        + NEW_LINE +
                    "[INFO]   - Media: 'deleted_MEDIUM_2'          "                 + NEW_LINE +
                    "[INFO]   - Media: 'deleted_MEDIUM_3'          "                 + NEW_LINE +
                    "[INFO]  - templatestore: 4"                                     + NEW_LINE +
                    "[INFO]   - FormatTemplate: 'deleted_FORMATTEMPLATE_6'"          + NEW_LINE +
                    "[INFO]   - FormatTemplate: 'deleted_FORMATTEMPLATE_7'"          + NEW_LINE +
                    "[INFO]   - LinkTemplate: 'deleted_LINKTEMPLATE_5'"              + NEW_LINE +
                    "[INFO]   - PageTemplates: 'deleted_PAGETEMPLATES_4'"            + NEW_LINE +
                    "[INFO] Moved elements: 7"                                       + NEW_LINE +
                    "[INFO] - store elements: 7"                                     + NEW_LINE +
                    "[INFO]  - pagestore: 1"                                         + NEW_LINE +
                    "[INFO]   - Page: 'moved_PAGE_1'               "                 + NEW_LINE +
                    "[INFO]  - mediastore: 2"                                        + NEW_LINE +
                    "[INFO]   - Media: 'moved_MEDIUM_2'            "                 + NEW_LINE +
                    "[INFO]   - Media: 'moved_MEDIUM_3'            "                 + NEW_LINE +
                    "[INFO]  - templatestore: 4"                                     + NEW_LINE +
                    "[INFO]   - FormatTemplate: 'moved_FORMATTEMPLATE_6'"            + NEW_LINE +
                    "[INFO]   - FormatTemplate: 'moved_FORMATTEMPLATE_7'"            + NEW_LINE +
                    "[INFO]   - LinkTemplate: 'moved_LINKTEMPLATE_5'"                + NEW_LINE +
                    "[INFO]   - PageTemplates: 'moved_PAGETEMPLATES_4'"              + NEW_LINE +
                    "[INFO] L&Found elements: 7"                                     + NEW_LINE +
                    "[INFO] - store elements: 7"                                     + NEW_LINE +
                    "[INFO]  - pagestore: 1"                                         + NEW_LINE +
                    "[INFO]   - Page: 'lostAndFound_PAGE_1'        "                 + NEW_LINE +
                    "[INFO]  - mediastore: 2"                                        + NEW_LINE +
                    "[INFO]   - Media: 'lostAndFound_MEDIUM_2'     "                 + NEW_LINE +
                    "[INFO]   - Media: 'lostAndFound_MEDIUM_3'     "                 + NEW_LINE +
                    "[INFO]  - templatestore: 4"                                     + NEW_LINE +
                    "[INFO]   - FormatTemplate: 'lostAndFound_FORMATTEMPLATE_6'"     + NEW_LINE +
                    "[INFO]   - FormatTemplate: 'lostAndFound_FORMATTEMPLATE_7'"     + NEW_LINE +
                    "[INFO]   - LinkTemplate: 'lostAndFound_LINKTEMPLATE_5'"         + NEW_LINE +
                    "[INFO]   - PageTemplates: 'lostAndFound_PAGETEMPLATES_4'"       + NEW_LINE +
                    "[INFO] Problems: 4"                                             + NEW_LINE +
                    "[INFO]  - store: PAGESTORE | uid: pagestore_uid_1337 | reason: IdProvider not found"                                                                                       + NEW_LINE +
                    "[INFO]  - store: MEDIASTORE | uid: mediastore_uid_123 | reason: Resolution invalid"                                                                                        + NEW_LINE +
                    "[INFO]  - store: MEDIASTORE | uid: mediastore_uid_1932 | reason: Medium invalid"                                                                                           + NEW_LINE +
                    "[INFO]  - store: TEMPLATESTORE | name: templatestore_name_1231 | reason: GOM is invalid"                                                                                   + NEW_LINE +
                    "[INFO] == SUMMARY =="                                                                                                                                                      + NEW_LINE +
                    "[INFO] Created elements: 17 | store elements: 7 ( pagestore: 1, mediastore: 2, templatestore: 4 ) | entity types: 10 ( schemas: 3, entities: 12 )"                         + NEW_LINE +
                    "[INFO] Updated elements: 26 | project properties: 9 | store elements: 7 ( pagestore: 1, mediastore: 2, templatestore: 4 ) | entity types: 10 ( schemas: 3, entities: 12 )" + NEW_LINE +
                    "[INFO] Deleted elements: 7 | store elements: 7 ( pagestore: 1, mediastore: 2, templatestore: 4 )" + NEW_LINE +
                    "[INFO]   Moved elements: 7 | store elements: 7 ( pagestore: 1, mediastore: 2, templatestore: 4 )" + NEW_LINE +
                    "[INFO] L&Found elements: 7 | store elements: 7 ( pagestore: 1, mediastore: 2, templatestore: 4 )" + NEW_LINE +
                    "[INFO]         Problems: 4"                                                                       + NEW_LINE;
            // @formatter:on
            assertEquals("Result does not match.", expected, logger.toString());
        }
    }

    @Test
    public void testLogElements() throws Exception {
        {
            final MockLogger logger = new MockLogger(true);
            AdvancedLogger.logElements(logger, Collections.emptyList(), "myDescription");
            // @formatter:off
            final String expected = "[INFO] myDescription: 0" + NEW_LINE;
            // @formatter:on
            assertEquals("Result does not match.", expected, logger.toString());
        }
        {
            // update-case ==> -1
            final MockLogger logger = new MockLogger(true);
            AdvancedLogger.logElements(logger, Collections.emptyList(), "myDescription");
            // @formatter:off
            final String expected = "[INFO] myDescription: 0" + NEW_LINE;
            // @formatter:on
            assertEquals("Result does not match.", expected, logger.toString());
        }
        {
            // update-case ==> -1
            final MockLogger logger = new MockLogger(false);
            // full test
            final Collection<ExportInfo> elements = new ArrayList<>();
            {
                // create project properties
                elements.add(new MockedPropertyTypeExportInfo(PropertiesTransportOptions.ProjectPropertyType.USERS));
                elements.add(new MockedPropertyTypeExportInfo(PropertiesTransportOptions.ProjectPropertyType.GROUPS));
                elements.add(new MockedPropertyTypeExportInfo(PropertiesTransportOptions.ProjectPropertyType.TEMPLATE_SETS));
            }
            {
                // create store elements
                final Map<Store.Type, List<ElementExportInfo>> storeElements = createMapWithStoreElements();
                for (final List<ElementExportInfo> exportInfos : storeElements.values()) {
                    elements.addAll(exportInfos);
                }
            }
            {
                // create entities
                elements.add(new MockedEntityTypeExportInfo("myType", "myFirstSchema", 1));
                elements.add(new MockedEntityTypeExportInfo("mySecondType", "mySecondSchema", 2));
            }
            AdvancedLogger.logElements(logger, elements, "myDescription");
            // @formatter:off
            final String expected = "[INFO] myDescription: 15"          + NEW_LINE +
                                    "[INFO] - project properties: 3"    + NEW_LINE +
                                    "[INFO]  - Groups                               ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]  - TemplateSets                         ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]  - Users                                ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO] - store elements: 10"       + NEW_LINE +
                                    "[INFO]  - pagestore: 1"            + NEW_LINE +
                                    "[INFO]   - Page: 'first'                       ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]  - mediastore: 2"           + NEW_LINE +
                                    "[INFO]   - Media: 'first'                      ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]   - Media: 'second'                     ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]  - sitestore: 3"            + NEW_LINE +
                                    "[INFO]   - PageRef: 'first'                    ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]   - PageRef: 'second'                   ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]   - PageRef: 'third'                    ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]  - templatestore: 4"        + NEW_LINE +
                                    "[INFO]   - PageTemplate: 'first'               ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]   - PageTemplate: 'fourth'              ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]   - PageTemplate: 'second'              ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]   - PageTemplate: 'third'               ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO] - entity types: 2                       ( schemas: 2, entities: 3 )"        + NEW_LINE +
                                    "[INFO]  - Schema: 'myFirstSchema'              ( entity types: 1, entities: 1 )"   + NEW_LINE +
                                    "[INFO]   - EntityType: 'myType'                ( entities: 1 )"                    + NEW_LINE +
                                    "[INFO]  - Schema: 'mySecondSchema'             ( entity types: 1, entities: 2 )"   + NEW_LINE +
                                    "[INFO]   - EntityType: 'mySecondType'          ( entities: 2 )"                    + NEW_LINE;
            // @formatter:on
            assertEquals("Result does not match.", expected, logger.toString());
        }
        {
            // update-case ==> -1
            final MockLogger logger = new MockLogger(true);
            // full test
            final Collection<ExportInfo> elements = new ArrayList<>();
            {
                // create project properties
                elements.add(new MockedPropertyTypeExportInfo(PropertiesTransportOptions.ProjectPropertyType.USERS));
                elements.add(new MockedPropertyTypeExportInfo(PropertiesTransportOptions.ProjectPropertyType.GROUPS));
                elements.add(new MockedPropertyTypeExportInfo(PropertiesTransportOptions.ProjectPropertyType.TEMPLATE_SETS));
            }
            {
                // create store elements
                final Map<Store.Type, List<ElementExportInfo>> storeElements = createMapWithStoreElements();
                for (final List<ElementExportInfo> exportInfos : storeElements.values()) {
                    elements.addAll(exportInfos);
                }
            }
            {
                // create entities
                elements.add(new MockedEntityTypeExportInfo("myType", "myFirstSchema", 1));
                elements.add(new MockedEntityTypeExportInfo("mySecondType", "mySecondSchema", 2));
            }
            AdvancedLogger.logElements(logger, elements, "myDescription");
            // @formatter:off
            final String expected = "[INFO] myDescription: 15"          + NEW_LINE +
                                    "[INFO] - project properties: 3"    + NEW_LINE +
                                    "[INFO]  - Groups                               ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]   - Created files: 1"      + NEW_LINE +
                                    "[DEBUG]    - /path/GROUPS/0.txt"   + NEW_LINE +
                                    "[DEBUG]   - Updated files: 2"      + NEW_LINE +
                                    "[DEBUG]    - /path/GROUPS/0.txt"   + NEW_LINE +
                                    "[DEBUG]    - /path/GROUPS/1.txt"   + NEW_LINE +
                                    "[DEBUG]   - Deleted files: 3"      + NEW_LINE +
                                    "[DEBUG]    - /path/GROUPS/0.txt"   + NEW_LINE +
                                    "[DEBUG]    - /path/GROUPS/1.txt"   + NEW_LINE +
                                    "[DEBUG]    - /path/GROUPS/2.txt"   + NEW_LINE +
                                    "[DEBUG]   - Moved files: 4"        + NEW_LINE +
                                    "[DEBUG]    - 0.txt ( from '/from/GROUPS' to '/to/GROUPS' )" + NEW_LINE +
                                    "[DEBUG]    - 1.txt ( from '/from/GROUPS' to '/to/GROUPS' )" + NEW_LINE +
                                    "[DEBUG]    - 2.txt ( from '/from/GROUPS' to '/to/GROUPS' )" + NEW_LINE +
                                    "[DEBUG]    - 3.txt ( from '/from/GROUPS' to '/to/GROUPS' )" + NEW_LINE +
                                    "[INFO]  - TemplateSets                         ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]   - Created files: 1"              + NEW_LINE +
                                    "[DEBUG]    - /path/TEMPLATE_SETS/0.txt"    + NEW_LINE +
                                    "[DEBUG]   - Updated files: 2"              + NEW_LINE +
                                    "[DEBUG]    - /path/TEMPLATE_SETS/0.txt"    + NEW_LINE +
                                    "[DEBUG]    - /path/TEMPLATE_SETS/1.txt"    + NEW_LINE +
                                    "[DEBUG]   - Deleted files: 3"              + NEW_LINE +
                                    "[DEBUG]    - /path/TEMPLATE_SETS/0.txt"    + NEW_LINE +
                                    "[DEBUG]    - /path/TEMPLATE_SETS/1.txt"    + NEW_LINE +
                                    "[DEBUG]    - /path/TEMPLATE_SETS/2.txt"    + NEW_LINE +
                                    "[DEBUG]   - Moved files: 4"                + NEW_LINE +
                                    "[DEBUG]    - 0.txt ( from '/from/TEMPLATE_SETS' to '/to/TEMPLATE_SETS' )" + NEW_LINE +
                                    "[DEBUG]    - 1.txt ( from '/from/TEMPLATE_SETS' to '/to/TEMPLATE_SETS' )" + NEW_LINE +
                                    "[DEBUG]    - 2.txt ( from '/from/TEMPLATE_SETS' to '/to/TEMPLATE_SETS' )" + NEW_LINE +
                                    "[DEBUG]    - 3.txt ( from '/from/TEMPLATE_SETS' to '/to/TEMPLATE_SETS' )" + NEW_LINE +
                                    "[INFO]  - Users                                ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]   - Created files: 1"      + NEW_LINE +
                                    "[DEBUG]    - /path/USERS/0.txt"    + NEW_LINE +
                                    "[DEBUG]   - Updated files: 2"      + NEW_LINE +
                                    "[DEBUG]    - /path/USERS/0.txt"    + NEW_LINE +
                                    "[DEBUG]    - /path/USERS/1.txt"    + NEW_LINE +
                                    "[DEBUG]   - Deleted files: 3"      + NEW_LINE +
                                    "[DEBUG]    - /path/USERS/0.txt"    + NEW_LINE +
                                    "[DEBUG]    - /path/USERS/1.txt"    + NEW_LINE +
                                    "[DEBUG]    - /path/USERS/2.txt"    + NEW_LINE +
                                    "[DEBUG]   - Moved files: 4"        + NEW_LINE +
                                    "[DEBUG]    - 0.txt ( from '/from/USERS' to '/to/USERS' )" + NEW_LINE +
                                    "[DEBUG]    - 1.txt ( from '/from/USERS' to '/to/USERS' )" + NEW_LINE +
                                    "[DEBUG]    - 2.txt ( from '/from/USERS' to '/to/USERS' )" + NEW_LINE +
                                    "[DEBUG]    - 3.txt ( from '/from/USERS' to '/to/USERS' )" + NEW_LINE +
                                    "[INFO] - store elements: 10"       + NEW_LINE +
                                    "[INFO]  - pagestore: 1"            + NEW_LINE +
                                    "[INFO]   - Page: 'first'                       ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]    - Created files: 1"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]    - Updated files: 2"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/1.txt"   + NEW_LINE +
                                    "[DEBUG]    - Deleted files: 3"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/1.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/2.txt"   + NEW_LINE +
                                    "[DEBUG]    - Moved files: 4"       + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 2.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 3.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[INFO]  - mediastore: 2"           + NEW_LINE +
                                    "[INFO]   - Media: 'first'                      ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]    - Created files: 1"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]    - Updated files: 2"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/1.txt"   + NEW_LINE +
                                    "[DEBUG]    - Deleted files: 3"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/1.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/2.txt"   + NEW_LINE +
                                    "[DEBUG]    - Moved files: 4"       + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 2.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 3.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[INFO]   - Media: 'second'                     ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]    - Created files: 1"     + NEW_LINE +
                                    "[DEBUG]     - /path/second/0.txt"  + NEW_LINE +
                                    "[DEBUG]    - Updated files: 2"     + NEW_LINE +
                                    "[DEBUG]     - /path/second/0.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/second/1.txt"  + NEW_LINE +
                                    "[DEBUG]    - Deleted files: 3"     + NEW_LINE +
                                    "[DEBUG]     - /path/second/0.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/second/1.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/second/2.txt"  + NEW_LINE +
                                    "[DEBUG]    - Moved files: 4"       + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[DEBUG]     - 2.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[DEBUG]     - 3.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[INFO]  - sitestore: 3"            + NEW_LINE +
                                    "[INFO]   - PageRef: 'first'                    ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]    - Created files: 1"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]    - Updated files: 2"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/1.txt"   + NEW_LINE +
                                    "[DEBUG]    - Deleted files: 3"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/1.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/2.txt"   + NEW_LINE +
                                    "[DEBUG]    - Moved files: 4"       + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 2.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 3.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[INFO]   - PageRef: 'second'                   ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]    - Created files: 1"     + NEW_LINE +
                                    "[DEBUG]     - /path/second/0.txt"  + NEW_LINE +
                                    "[DEBUG]    - Updated files: 2"     + NEW_LINE +
                                    "[DEBUG]     - /path/second/0.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/second/1.txt"  + NEW_LINE +
                                    "[DEBUG]    - Deleted files: 3"     + NEW_LINE +
                                    "[DEBUG]     - /path/second/0.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/second/1.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/second/2.txt"  + NEW_LINE +
                                    "[DEBUG]    - Moved files: 4"       + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[DEBUG]     - 2.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[DEBUG]     - 3.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[INFO]   - PageRef: 'third'                    ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]    - Created files: 1"     + NEW_LINE +
                                    "[DEBUG]     - /path/third/0.txt"   + NEW_LINE +
                                    "[DEBUG]    - Updated files: 2"     + NEW_LINE +
                                    "[DEBUG]     - /path/third/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/third/1.txt"   + NEW_LINE +
                                    "[DEBUG]    - Deleted files: 3"     + NEW_LINE +
                                    "[DEBUG]     - /path/third/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/third/1.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/third/2.txt"   + NEW_LINE +
                                    "[DEBUG]    - Moved files: 4"       + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/third' to '/to/third' )" + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/third' to '/to/third' )" + NEW_LINE +
                                    "[DEBUG]     - 2.txt ( from '/from/third' to '/to/third' )" + NEW_LINE +
                                    "[DEBUG]     - 3.txt ( from '/from/third' to '/to/third' )" + NEW_LINE +
                                    "[INFO]  - templatestore: 4"        + NEW_LINE +
                                    "[INFO]   - PageTemplate: 'first'               ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]    - Created files: 1"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]    - Updated files: 2"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/1.txt"   + NEW_LINE +
                                    "[DEBUG]    - Deleted files: 3"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/1.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/2.txt"   + NEW_LINE +
                                    "[DEBUG]    - Moved files: 4"       + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 2.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 3.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[INFO]   - PageTemplate: 'fourth'              ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]    - Created files: 1"     + NEW_LINE +
                                    "[DEBUG]     - /path/fourth/0.txt"  + NEW_LINE +
                                    "[DEBUG]    - Updated files: 2"     + NEW_LINE +
                                    "[DEBUG]     - /path/fourth/0.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/fourth/1.txt"  + NEW_LINE +
                                    "[DEBUG]    - Deleted files: 3"     + NEW_LINE +
                                    "[DEBUG]     - /path/fourth/0.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/fourth/1.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/fourth/2.txt"  + NEW_LINE +
                                    "[DEBUG]    - Moved files: 4"       + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/fourth' to '/to/fourth' )" + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/fourth' to '/to/fourth' )" + NEW_LINE +
                                    "[DEBUG]     - 2.txt ( from '/from/fourth' to '/to/fourth' )" + NEW_LINE +
                                    "[DEBUG]     - 3.txt ( from '/from/fourth' to '/to/fourth' )" + NEW_LINE +
                                    "[INFO]   - PageTemplate: 'second'              ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]    - Created files: 1"     + NEW_LINE +
                                    "[DEBUG]     - /path/second/0.txt"  + NEW_LINE +
                                    "[DEBUG]    - Updated files: 2"     + NEW_LINE +
                                    "[DEBUG]     - /path/second/0.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/second/1.txt"  + NEW_LINE +
                                    "[DEBUG]    - Deleted files: 3"     + NEW_LINE +
                                    "[DEBUG]     - /path/second/0.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/second/1.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/second/2.txt"  + NEW_LINE +
                                    "[DEBUG]    - Moved files: 4"       + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[DEBUG]     - 2.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[DEBUG]     - 3.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[INFO]   - PageTemplate: 'third'               ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]    - Created files: 1"     + NEW_LINE +
                                    "[DEBUG]     - /path/third/0.txt"   + NEW_LINE +
                                    "[DEBUG]    - Updated files: 2"     + NEW_LINE +
                                    "[DEBUG]     - /path/third/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/third/1.txt"   + NEW_LINE +
                                    "[DEBUG]    - Deleted files: 3"     + NEW_LINE +
                                    "[DEBUG]     - /path/third/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/third/1.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/third/2.txt"   + NEW_LINE +
                                    "[DEBUG]    - Moved files: 4"       + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/third' to '/to/third' )" + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/third' to '/to/third' )" + NEW_LINE +
                                    "[DEBUG]     - 2.txt ( from '/from/third' to '/to/third' )" + NEW_LINE +
                                    "[DEBUG]     - 3.txt ( from '/from/third' to '/to/third' )" + NEW_LINE +
                                    "[INFO] - entity types: 2                       ( schemas: 2, entities: 3 )"        + NEW_LINE +
                                    "[INFO]  - Schema: 'myFirstSchema'              ( entity types: 1, entities: 1 )"   + NEW_LINE +
                                    "[INFO]   - EntityType: 'myType'                ( entities: 1 )"                    + NEW_LINE +
                                    "[DEBUG]     - Created files: 1"                  + NEW_LINE +
                                    "[DEBUG]      - /path/myFirstSchema#myType/0.txt" + NEW_LINE +
                                    "[DEBUG]     - Updated files: 2"                  + NEW_LINE +
                                    "[DEBUG]      - /path/myFirstSchema#myType/0.txt" + NEW_LINE +
                                    "[DEBUG]      - /path/myFirstSchema#myType/1.txt" + NEW_LINE +
                                    "[DEBUG]     - Deleted files: 3"                  + NEW_LINE +
                                    "[DEBUG]      - /path/myFirstSchema#myType/0.txt" + NEW_LINE +
                                    "[DEBUG]      - /path/myFirstSchema#myType/1.txt" + NEW_LINE +
                                    "[DEBUG]      - /path/myFirstSchema#myType/2.txt" + NEW_LINE +
                                    "[DEBUG]     - Moved files: 4"                    + NEW_LINE +
                                    "[DEBUG]      - 0.txt ( from '/from/myFirstSchema#myType' to '/to/myFirstSchema#myType' )" + NEW_LINE +
                                    "[DEBUG]      - 1.txt ( from '/from/myFirstSchema#myType' to '/to/myFirstSchema#myType' )" + NEW_LINE +
                                    "[DEBUG]      - 2.txt ( from '/from/myFirstSchema#myType' to '/to/myFirstSchema#myType' )" + NEW_LINE +
                                    "[DEBUG]      - 3.txt ( from '/from/myFirstSchema#myType' to '/to/myFirstSchema#myType' )" + NEW_LINE +
                                    "[INFO]  - Schema: 'mySecondSchema'             ( entity types: 1, entities: 2 )"          + NEW_LINE +
                                    "[INFO]   - EntityType: 'mySecondType'          ( entities: 2 )"                           + NEW_LINE +
                                    "[DEBUG]     - Created files: 1"                            + NEW_LINE +
                                    "[DEBUG]      - /path/mySecondSchema#mySecondType/0.txt"    + NEW_LINE +
                                    "[DEBUG]     - Updated files: 2"                            + NEW_LINE +
                                    "[DEBUG]      - /path/mySecondSchema#mySecondType/0.txt"    + NEW_LINE +
                                    "[DEBUG]      - /path/mySecondSchema#mySecondType/1.txt"    + NEW_LINE +
                                    "[DEBUG]     - Deleted files: 3"                            + NEW_LINE +
                                    "[DEBUG]      - /path/mySecondSchema#mySecondType/0.txt"    + NEW_LINE +
                                    "[DEBUG]      - /path/mySecondSchema#mySecondType/1.txt"    + NEW_LINE +
                                    "[DEBUG]      - /path/mySecondSchema#mySecondType/2.txt"    + NEW_LINE +
                                    "[DEBUG]     - Moved files: 4"                              + NEW_LINE +
                                    "[DEBUG]      - 0.txt ( from '/from/mySecondSchema#mySecondType' to '/to/mySecondSchema#mySecondType' )" + NEW_LINE +
                                    "[DEBUG]      - 1.txt ( from '/from/mySecondSchema#mySecondType' to '/to/mySecondSchema#mySecondType' )" + NEW_LINE +
                                    "[DEBUG]      - 2.txt ( from '/from/mySecondSchema#mySecondType' to '/to/mySecondSchema#mySecondType' )" + NEW_LINE +
                                    "[DEBUG]      - 3.txt ( from '/from/mySecondSchema#mySecondType' to '/to/mySecondSchema#mySecondType' )" + NEW_LINE;
            // @formatter:on
            assertEquals("Result does not match.", expected, logger.toString());
        }
    }

    @Test
    public void testBuildSummaryEmptyUpdate() throws Exception {
        {
            // empty test
            final Collection<ExportInfo> elements = Collections.emptyList();
            final ReorganizedResult reorganizedResult = new ReorganizedResult(elements);
            final String result = AdvancedLogger.buildSummary(elements, "myDescription", reorganizedResult);
            final String expected = "myDescription: 0";
            assertEquals("Result does not match.", expected, result);
        }
    }

    @Test
    public void testBuildSummary() throws Exception {
        {
            // empty test
            final Collection<ExportInfo> elements = Collections.emptyList();
            final ReorganizedResult reorganizedResult = new ReorganizedResult(elements);
            final String result = AdvancedLogger.buildSummary(elements, "myDescription", reorganizedResult);
            final String expected = "myDescription: 0";
            assertEquals("Result does not match.", expected, result);
        }
        {
            // partial test #1
            final Collection<ExportInfo> elements = new ArrayList<>();
            {
                // create project properties
                elements.add(new MockedPropertyTypeExportInfo(PropertiesTransportOptions.ProjectPropertyType.USERS));
                elements.add(new MockedPropertyTypeExportInfo(PropertiesTransportOptions.ProjectPropertyType.GROUPS));
                elements.add(new MockedPropertyTypeExportInfo(PropertiesTransportOptions.ProjectPropertyType.TEMPLATE_SETS));
            }
            {
                // create store elements
                final Map<Store.Type, List<ElementExportInfo>> storeElements = createMapWithStoreElements();
                for (final List<ElementExportInfo> exportInfos : storeElements.values()) {
                    elements.addAll(exportInfos);
                }
            }
            final ReorganizedResult reorganizedResult = new ReorganizedResult(elements);
            final String result = AdvancedLogger.buildSummary(elements, "myDescription", reorganizedResult);
            final String expected = "myDescription: 13 | project properties: 3 | store elements: 10 ( pagestore: 1, mediastore: 2, sitestore: 3, templatestore: 4 )";
            assertEquals("Result does not match.", expected, result);
        }
        {
            // partial test #2
            final Collection<ExportInfo> elements = new ArrayList<>();
            {
                // create project properties
                elements.add(new MockedPropertyTypeExportInfo(PropertiesTransportOptions.ProjectPropertyType.USERS));
                elements.add(new MockedPropertyTypeExportInfo(PropertiesTransportOptions.ProjectPropertyType.GROUPS));
                elements.add(new MockedPropertyTypeExportInfo(PropertiesTransportOptions.ProjectPropertyType.TEMPLATE_SETS));
            }
            final ReorganizedResult reorganizedResult = new ReorganizedResult(elements);
            final String result = AdvancedLogger.buildSummary(elements, "myDescription", reorganizedResult);
            final String expected = "myDescription: 3 | project properties: 3";
            assertEquals("Result does not match.", expected, result);
        }
        {
            // partial test #3
            final Collection<ExportInfo> elements = new ArrayList<>();
            {
                // create store elements
                final Map<Store.Type, List<ElementExportInfo>> storeElements = createMapWithStoreElements();
                for (final List<ElementExportInfo> exportInfos : storeElements.values()) {
                    elements.addAll(exportInfos);
                }
            }
            {
                // create entities
                elements.add(new MockedEntityTypeExportInfo("myType", "myFirstSchema", 1));
                elements.add(new MockedEntityTypeExportInfo("mySecondType", "mySecondSchema", 2));
            }
            final ReorganizedResult reorganizedResult = new ReorganizedResult(elements);
            final String result = AdvancedLogger.buildSummary(elements, "myDescription", reorganizedResult);
            final String expected = "myDescription: 12 | store elements: 10 ( pagestore: 1, mediastore: 2, sitestore: 3, templatestore: 4 ) | entity types: 2 ( schemas: 2, entities: 3 )";
            assertEquals("Result does not match.", expected, result);
        }
        {
            // full test
            final Collection<ExportInfo> elements = new ArrayList<>();
            {
                // create project properties
                elements.add(new MockedPropertyTypeExportInfo(PropertiesTransportOptions.ProjectPropertyType.USERS));
                elements.add(new MockedPropertyTypeExportInfo(PropertiesTransportOptions.ProjectPropertyType.GROUPS));
                elements.add(new MockedPropertyTypeExportInfo(PropertiesTransportOptions.ProjectPropertyType.TEMPLATE_SETS));
            }
            {
                // create store elements
                final Map<Store.Type, List<ElementExportInfo>> storeElements = createMapWithStoreElements();
                for (final List<ElementExportInfo> exportInfos : storeElements.values()) {
                    elements.addAll(exportInfos);
                }
            }
            {
                // create entities
                elements.add(new MockedEntityTypeExportInfo("myType", "myFirstSchema", 1));
                elements.add(new MockedEntityTypeExportInfo("mySecondType", "mySecondSchema", 2));
            }
            final ReorganizedResult reorganizedResult = new ReorganizedResult(elements);
            final String result = AdvancedLogger.buildSummary(elements, "myDescription", reorganizedResult);
            final String expected = "myDescription: 15 | project properties: 3 | store elements: 10 ( pagestore: 1, mediastore: 2, sitestore: 3, templatestore: 4 ) | entity types: 2 ( schemas: 2, entities: 3 )";
            assertEquals("Result does not match.", expected, result);
        }
        {
            // fs-meta-test
            final Collection<ExportInfo> elements = new ArrayList<>();
            {
                // create project properties
                elements.add(new MockedPropertyTypeExportInfo(PropertiesTransportOptions.ProjectPropertyType.USERS));
                elements.add(new MockedPropertyTypeExportInfo(PropertiesTransportOptions.ProjectPropertyType.TEMPLATE_SETS));
            }
            {
                // create fs-meta
                elements.add(new MockedExportInfo(ExportInfo.Type.FS_META));
            }
            final ReorganizedResult reorganizedResult = new ReorganizedResult(elements);
            final String result = AdvancedLogger.buildSummary(elements, "myDescription", reorganizedResult);
            final String expected = "myDescription: 2 | project properties: 2";
            assertEquals("Result does not match.", expected, result);
        }
    }

    @Test
    public void testAppendProjectPropertySummary() throws Exception {
        {
            final StringBuilder stringBuilder = new StringBuilder();
            final Collection<PropertyTypeExportInfo> projectProperties = Collections.emptyList();
            AdvancedLogger.appendProjectPropertySummary(stringBuilder, projectProperties);
            assertEquals("Result does not match.", "", stringBuilder.toString());
        }
        {
            final StringBuilder stringBuilder = new StringBuilder();
            final Collection<PropertyTypeExportInfo> projectProperties = new ArrayList<>(Collections.singletonList(new MockedPropertyTypeExportInfo(PropertiesTransportOptions.ProjectPropertyType.USERS)));
            AdvancedLogger.appendProjectPropertySummary(stringBuilder, projectProperties);
            assertEquals("Result does not match.", " | project properties: " + projectProperties.size(), stringBuilder.toString());
        }
        {
            final StringBuilder stringBuilder = new StringBuilder();
            final Collection<PropertyTypeExportInfo> projectProperties = new ArrayList<>(Arrays.asList(new MockedPropertyTypeExportInfo(PropertiesTransportOptions.ProjectPropertyType.USERS), new MockedPropertyTypeExportInfo(PropertiesTransportOptions.ProjectPropertyType.TEMPLATE_SETS)));
            AdvancedLogger.appendProjectPropertySummary(stringBuilder, projectProperties);
            assertEquals("Result does not match.", " | project properties: " + projectProperties.size(), stringBuilder.toString());
        }
    }

    @Test
    public void testAppendStoreElementSummary() throws Exception {
        {
            final StringBuilder stringBuilder = new StringBuilder();
            final Map<Store.Type, List<ElementExportInfo>> storeElements = Collections.emptyMap();
            AdvancedLogger.appendStoreElementSummary(stringBuilder, storeElements);
            assertEquals("Result does not match.", "", stringBuilder.toString());
        }
        {
            final Map<Store.Type, List<ElementExportInfo>> storeElements = createMapWithStoreElements();
            final StringBuilder stringBuilder = new StringBuilder();
            AdvancedLogger.appendStoreElementSummary(stringBuilder, storeElements);
            assertEquals("Result does not match.", " | store elements: 10 ( pagestore: 1, mediastore: 2, sitestore: 3, templatestore: 4 )", stringBuilder.toString());
        }
    }

    @Test
    public void testAppendEntityTypeSummary() throws Exception {
        {
            final StringBuilder stringBuilder = new StringBuilder();
            final Collection<EntityTypeExportInfo> entityTypes = Collections.emptyList();
            AdvancedLogger.appendEntityTypeSummary(stringBuilder, entityTypes);
            assertEquals("Result does not match.", "", stringBuilder.toString());
        }
        {
            final StringBuilder stringBuilder = new StringBuilder();
            final Collection<EntityTypeExportInfo> entityTypes = new ArrayList<>(Collections.singletonList(new MockedEntityTypeExportInfo("myType", "mySchema", 0)));
            AdvancedLogger.appendEntityTypeSummary(stringBuilder, entityTypes);
            assertEquals("Result does not match.", " | entity types: 1 ( schemas: 1, entities: 0 )", stringBuilder.toString());
        }
        {
            final StringBuilder stringBuilder = new StringBuilder();
            final Collection<EntityTypeExportInfo> entityTypes = new ArrayList<>(Collections.singletonList(new MockedEntityTypeExportInfo("myType", "mySchema", 1)));
            AdvancedLogger.appendEntityTypeSummary(stringBuilder, entityTypes);
            assertEquals("Result does not match.", " | entity types: 1 ( schemas: 1, entities: 1 )", stringBuilder.toString());
        }
        {
            final StringBuilder stringBuilder = new StringBuilder();
            final Collection<EntityTypeExportInfo> entityTypes = new ArrayList<>(Collections.singletonList(new MockedEntityTypeExportInfo("myType", "mySchema", 2)));
            AdvancedLogger.appendEntityTypeSummary(stringBuilder, entityTypes);
            assertEquals("Result does not match.", " | entity types: 1 ( schemas: 1, entities: 2 )", stringBuilder.toString());
        }
        {
            final StringBuilder stringBuilder = new StringBuilder();
            final Collection<EntityTypeExportInfo> entityTypes = new ArrayList<>(Arrays.asList(new MockedEntityTypeExportInfo("myType", "myFirstSchema", 1), new MockedEntityTypeExportInfo("mySecondType", "mySecondSchema", 2)));
            AdvancedLogger.appendEntityTypeSummary(stringBuilder, entityTypes);
            assertEquals("Result does not match.", " | entity types: 2 ( schemas: 2, entities: 3 )", stringBuilder.toString());
        }
    }

    @Test
    public void testLogProjectProperties() throws Exception {
        {
            final MockLogger logger = new MockLogger(true);
            AdvancedLogger.logProjectProperties(logger, Collections.emptyList());
            assertEquals("Result does not match.", "", logger.toString());
        }
        {
            final MockLogger logger = new MockLogger(false);
            final Collection<PropertyTypeExportInfo> projectProperties = new ArrayList<>(Collections.singletonList(new MockedPropertyTypeExportInfo(null)));
            AdvancedLogger.logProjectProperties(logger, projectProperties);
            //@formatter:off
            final String expected = "[INFO] - project properties: 1" + NEW_LINE +
                                    "[INFO]  - Property fs metadata                 ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE;
            //@formatter:on
            assertEquals("Result does not match.", expected, logger.toString());
        }
        {
            final MockLogger logger = new MockLogger(false);
            final Collection<PropertyTypeExportInfo> projectProperties = new ArrayList<>(Arrays.asList(new MockedPropertyTypeExportInfo(null), new MockedPropertyTypeExportInfo(PropertiesTransportOptions.ProjectPropertyType.USERS)));
            AdvancedLogger.logProjectProperties(logger, projectProperties);
            //@formatter:off
            final String expected = "[INFO] - project properties: 2" + NEW_LINE +
                                    "[INFO]  - Users                                ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]  - Property fs metadata                 ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE;
            //@formatter:on
            assertEquals("Result does not match.", expected, logger.toString());
        }
        {
            final MockLogger logger = new MockLogger(false);
            final Collection<PropertyTypeExportInfo> projectProperties = new ArrayList<>(Collections.singletonList(new MockedPropertyTypeExportInfo(PropertiesTransportOptions.ProjectPropertyType.USERS)));
            AdvancedLogger.logProjectProperties(logger, projectProperties);
            //@formatter:off
            final String expected = "[INFO] - project properties: 1" + NEW_LINE +
                                    "[INFO]  - Users                                ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE;
            //@formatter:on
            assertEquals("Result does not match.", expected, logger.toString());
        }
        {
            final MockLogger logger = new MockLogger(true);
            final Collection<PropertyTypeExportInfo> projectProperties = new ArrayList<>(Collections.singletonList(new MockedPropertyTypeExportInfo(PropertiesTransportOptions.ProjectPropertyType.USERS)));
            AdvancedLogger.logProjectProperties(logger, projectProperties);
            //@formatter:off
            final String expected = "[INFO] - project properties: 1"    + NEW_LINE +
                                    "[INFO]  - Users                                ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]   - Created files: 1"      + NEW_LINE +
                                    "[DEBUG]    - /path/USERS/0.txt"    + NEW_LINE +
                                    "[DEBUG]   - Updated files: 2"      + NEW_LINE +
                                    "[DEBUG]    - /path/USERS/0.txt"    + NEW_LINE +
                                    "[DEBUG]    - /path/USERS/1.txt"    + NEW_LINE +
                                    "[DEBUG]   - Deleted files: 3"      + NEW_LINE +
                                    "[DEBUG]    - /path/USERS/0.txt"    + NEW_LINE +
                                    "[DEBUG]    - /path/USERS/1.txt"    + NEW_LINE +
                                    "[DEBUG]    - /path/USERS/2.txt"    + NEW_LINE +
                                    "[DEBUG]   - Moved files: 4"        + NEW_LINE +
                                    "[DEBUG]    - 0.txt ( from '/from/USERS' to '/to/USERS' )" + NEW_LINE +
                                    "[DEBUG]    - 1.txt ( from '/from/USERS' to '/to/USERS' )" + NEW_LINE +
                                    "[DEBUG]    - 2.txt ( from '/from/USERS' to '/to/USERS' )" + NEW_LINE +
                                    "[DEBUG]    - 3.txt ( from '/from/USERS' to '/to/USERS' )" + NEW_LINE;
            //@formatter:on
            assertEquals("Result does not match.", expected, logger.toString());
        }
    }

    @Test
    public void testLogStoreElements() throws Exception {
        {
            final MockLogger logger = new MockLogger(true);
            logger.setInfoEnabled(false);
            AdvancedLogger.logStoreElements(logger, Collections.emptyMap());
            assertEquals("Result does not match.", "", logger.toString());
        }
        {
            final MockLogger logger = new MockLogger(true);
            AdvancedLogger.logStoreElements(logger, Collections.emptyMap());
            assertEquals("Result does not match.", "", logger.toString());
        }
        {
            final MockLogger logger = new MockLogger(false);
            AdvancedLogger.logStoreElements(logger, createMapWithStoreElements());
            // @formatter:off
            final String expected = "[INFO] - store elements: 10"                                                                                             + NEW_LINE +
                                    "[INFO]  - pagestore: 1"                                                                                                  + NEW_LINE +
                                    "[INFO]   - Page: 'first'                       ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]  - mediastore: 2"                                                                                                 + NEW_LINE +
                                    "[INFO]   - Media: 'first'                      ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]   - Media: 'second'                     ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]  - sitestore: 3"                                                                                                  + NEW_LINE +
                                    "[INFO]   - PageRef: 'first'                    ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]   - PageRef: 'second'                   ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]   - PageRef: 'third'                    ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]  - templatestore: 4"                                                                                              + NEW_LINE +
                                    "[INFO]   - PageTemplate: 'first'               ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]   - PageTemplate: 'fourth'              ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]   - PageTemplate: 'second'              ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]   - PageTemplate: 'third'               ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE;
            // @formatter:on
            assertEquals("Result does not match.", expected, logger.toString());
        }
        {
            final MockLogger logger = new MockLogger(true);
            AdvancedLogger.logStoreElements(logger, createMapWithStoreElements());
            // @formatter:off
            final String expected = "[INFO] - store elements: 10"       + NEW_LINE +
                                    "[INFO]  - pagestore: 1"            + NEW_LINE +
                                    "[INFO]   - Page: 'first'                       ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]    - Created files: 1"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]    - Updated files: 2"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/1.txt"   + NEW_LINE +
                                    "[DEBUG]    - Deleted files: 3"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/1.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/2.txt"   + NEW_LINE +
                                    "[DEBUG]    - Moved files: 4"       + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 2.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 3.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[INFO]  - mediastore: 2"           + NEW_LINE +
                                    "[INFO]   - Media: 'first'                      ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]    - Created files: 1"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]    - Updated files: 2"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/1.txt"   + NEW_LINE +
                                    "[DEBUG]    - Deleted files: 3"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/1.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/2.txt"   + NEW_LINE +
                                    "[DEBUG]    - Moved files: 4"       + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 2.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 3.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[INFO]   - Media: 'second'                     ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]    - Created files: 1"     + NEW_LINE +
                                    "[DEBUG]     - /path/second/0.txt"  + NEW_LINE +
                                    "[DEBUG]    - Updated files: 2"     + NEW_LINE +
                                    "[DEBUG]     - /path/second/0.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/second/1.txt"  + NEW_LINE +
                                    "[DEBUG]    - Deleted files: 3"     + NEW_LINE +
                                    "[DEBUG]     - /path/second/0.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/second/1.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/second/2.txt"  + NEW_LINE +
                                    "[DEBUG]    - Moved files: 4"       + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[DEBUG]     - 2.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[DEBUG]     - 3.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[INFO]  - sitestore: 3"            + NEW_LINE +
                                    "[INFO]   - PageRef: 'first'                    ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]    - Created files: 1"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]    - Updated files: 2"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/1.txt"   + NEW_LINE +
                                    "[DEBUG]    - Deleted files: 3"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/1.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/2.txt"   + NEW_LINE +
                                    "[DEBUG]    - Moved files: 4"       + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 2.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 3.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[INFO]   - PageRef: 'second'                   ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]    - Created files: 1"     + NEW_LINE +
                                    "[DEBUG]     - /path/second/0.txt"  + NEW_LINE +
                                    "[DEBUG]    - Updated files: 2"     + NEW_LINE +
                                    "[DEBUG]     - /path/second/0.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/second/1.txt"  + NEW_LINE +
                                    "[DEBUG]    - Deleted files: 3"     + NEW_LINE +
                                    "[DEBUG]     - /path/second/0.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/second/1.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/second/2.txt"  + NEW_LINE +
                                    "[DEBUG]    - Moved files: 4"       + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[DEBUG]     - 2.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[DEBUG]     - 3.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[INFO]   - PageRef: 'third'                    ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]    - Created files: 1"     + NEW_LINE +
                                    "[DEBUG]     - /path/third/0.txt"   + NEW_LINE +
                                    "[DEBUG]    - Updated files: 2"     + NEW_LINE +
                                    "[DEBUG]     - /path/third/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/third/1.txt"   + NEW_LINE +
                                    "[DEBUG]    - Deleted files: 3"     + NEW_LINE +
                                    "[DEBUG]     - /path/third/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/third/1.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/third/2.txt"   + NEW_LINE +
                                    "[DEBUG]    - Moved files: 4"       + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/third' to '/to/third' )" + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/third' to '/to/third' )" + NEW_LINE +
                                    "[DEBUG]     - 2.txt ( from '/from/third' to '/to/third' )" + NEW_LINE +
                                    "[DEBUG]     - 3.txt ( from '/from/third' to '/to/third' )" + NEW_LINE +
                                    "[INFO]  - templatestore: 4"        + NEW_LINE +
                                    "[INFO]   - PageTemplate: 'first'               ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]    - Created files: 1"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]    - Updated files: 2"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/1.txt"   + NEW_LINE +
                                    "[DEBUG]    - Deleted files: 3"     + NEW_LINE +
                                    "[DEBUG]     - /path/first/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/1.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/first/2.txt"   + NEW_LINE +
                                    "[DEBUG]    - Moved files: 4"       + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 2.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[DEBUG]     - 3.txt ( from '/from/first' to '/to/first' )" + NEW_LINE +
                                    "[INFO]   - PageTemplate: 'fourth'              ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]    - Created files: 1"     + NEW_LINE +
                                    "[DEBUG]     - /path/fourth/0.txt"  + NEW_LINE +
                                    "[DEBUG]    - Updated files: 2"     + NEW_LINE +
                                    "[DEBUG]     - /path/fourth/0.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/fourth/1.txt"  + NEW_LINE +
                                    "[DEBUG]    - Deleted files: 3"     + NEW_LINE +
                                    "[DEBUG]     - /path/fourth/0.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/fourth/1.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/fourth/2.txt"  + NEW_LINE +
                                    "[DEBUG]    - Moved files: 4"       + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/fourth' to '/to/fourth' )" + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/fourth' to '/to/fourth' )" + NEW_LINE +
                                    "[DEBUG]     - 2.txt ( from '/from/fourth' to '/to/fourth' )" + NEW_LINE +
                                    "[DEBUG]     - 3.txt ( from '/from/fourth' to '/to/fourth' )" + NEW_LINE +
                                    "[INFO]   - PageTemplate: 'second'              ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]    - Created files: 1"     + NEW_LINE +
                                    "[DEBUG]     - /path/second/0.txt"  + NEW_LINE +
                                    "[DEBUG]    - Updated files: 2"     + NEW_LINE +
                                    "[DEBUG]     - /path/second/0.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/second/1.txt"  + NEW_LINE +
                                    "[DEBUG]    - Deleted files: 3"     + NEW_LINE +
                                    "[DEBUG]     - /path/second/0.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/second/1.txt"  + NEW_LINE +
                                    "[DEBUG]     - /path/second/2.txt"  + NEW_LINE +
                                    "[DEBUG]    - Moved files: 4"       + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[DEBUG]     - 2.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[DEBUG]     - 3.txt ( from '/from/second' to '/to/second' )" + NEW_LINE +
                                    "[INFO]   - PageTemplate: 'third'               ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[DEBUG]    - Created files: 1"     + NEW_LINE +
                                    "[DEBUG]     - /path/third/0.txt"   + NEW_LINE +
                                    "[DEBUG]    - Updated files: 2"     + NEW_LINE +
                                    "[DEBUG]     - /path/third/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/third/1.txt"   + NEW_LINE +
                                    "[DEBUG]    - Deleted files: 3"     + NEW_LINE +
                                    "[DEBUG]     - /path/third/0.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/third/1.txt"   + NEW_LINE +
                                    "[DEBUG]     - /path/third/2.txt"   + NEW_LINE +
                                    "[DEBUG]    - Moved files: 4"       + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/third' to '/to/third' )" + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/third' to '/to/third' )" + NEW_LINE +
                                    "[DEBUG]     - 2.txt ( from '/from/third' to '/to/third' )" + NEW_LINE +
                                    "[DEBUG]     - 3.txt ( from '/from/third' to '/to/third' )" + NEW_LINE;
            // @formatter:on
            assertEquals("Result does not match.", expected, logger.toString());
        }
    }

    @Test
    public void testLogEntityTypes() throws Exception {
        {
            final MockLogger logger = new MockLogger(true);
            AdvancedLogger.logEntityTypes(logger, Collections.emptyList());
            assertEquals("Result does not match.", "", logger.toString());
        }
        {
            final MockLogger logger = new MockLogger(false);
            final Collection<EntityTypeExportInfo> entityTypes = new ArrayList<>(Collections.singletonList(new MockedEntityTypeExportInfo("myType", "mySchema", 2)));
            AdvancedLogger.logEntityTypes(logger, entityTypes);
            //@formatter:off
            final String expected = "[INFO] - entity types: 1                       ( schemas: 1, entities: 2 )"        + NEW_LINE +
                                    "[INFO]  - Schema: 'mySchema'                   ( entity types: 1, entities: 2 )"   + NEW_LINE +
                                    "[INFO]   - EntityType: 'myType'                ( entities: 2 )"                    + NEW_LINE;
            //@formatter:on
            assertEquals("Result does not match.", expected, logger.toString());
        }
        {
            final MockLogger logger = new MockLogger(false);
            final Collection<EntityTypeExportInfo> entityTypes = new ArrayList<>(Arrays.asList(new MockedEntityTypeExportInfo("myFirstType", "myFirstSchema", 1), new MockedEntityTypeExportInfo("mySecondType", "mySecondSchema", 2)));
            AdvancedLogger.logEntityTypes(logger, entityTypes);
            //@formatter:off
            final String expected = "[INFO] - entity types: 2                       ( schemas: 2, entities: 3 )"        + NEW_LINE +
                                    "[INFO]  - Schema: 'myFirstSchema'              ( entity types: 1, entities: 1 )"   + NEW_LINE +
                                    "[INFO]   - EntityType: 'myFirstType'           ( entities: 1 )"                    + NEW_LINE +
                                    "[INFO]  - Schema: 'mySecondSchema'             ( entity types: 1, entities: 2 )"   + NEW_LINE +
                                    "[INFO]   - EntityType: 'mySecondType'          ( entities: 2 )"                    + NEW_LINE;
            //@formatter:on
            assertEquals("Result does not match.", expected, logger.toString());
        }
        {
            final MockLogger logger = new MockLogger(true);
            final Collection<EntityTypeExportInfo> entityTypes = new ArrayList<>(Arrays.asList(new MockedEntityTypeExportInfo("myFirstType", "myFirstSchema", 1), new MockedEntityTypeExportInfo("mySecondType", "mySecondSchema", 2)));
            AdvancedLogger.logEntityTypes(logger, entityTypes);
            //@formatter:off
            final String expected = "[INFO] - entity types: 2                       ( schemas: 2, entities: 3 )"        + NEW_LINE +
                                    "[INFO]  - Schema: 'myFirstSchema'              ( entity types: 1, entities: 1 )"   + NEW_LINE +
                                    "[INFO]   - EntityType: 'myFirstType'           ( entities: 1 )"    + NEW_LINE +
                                    "[DEBUG]     - Created files: 1"                                    + NEW_LINE +
                                    "[DEBUG]      - /path/myFirstSchema#myFirstType/0.txt"              + NEW_LINE +
                                    "[DEBUG]     - Updated files: 2"                                    + NEW_LINE +
                                    "[DEBUG]      - /path/myFirstSchema#myFirstType/0.txt"              + NEW_LINE +
                                    "[DEBUG]      - /path/myFirstSchema#myFirstType/1.txt"              + NEW_LINE +
                                    "[DEBUG]     - Deleted files: 3"                                    + NEW_LINE +
                                    "[DEBUG]      - /path/myFirstSchema#myFirstType/0.txt"              + NEW_LINE +
                                    "[DEBUG]      - /path/myFirstSchema#myFirstType/1.txt"              + NEW_LINE +
                                    "[DEBUG]      - /path/myFirstSchema#myFirstType/2.txt"              + NEW_LINE +
                                    "[DEBUG]     - Moved files: 4"                                      + NEW_LINE +
                                    "[DEBUG]      - 0.txt ( from '/from/myFirstSchema#myFirstType' to '/to/myFirstSchema#myFirstType' )"    + NEW_LINE +
                                    "[DEBUG]      - 1.txt ( from '/from/myFirstSchema#myFirstType' to '/to/myFirstSchema#myFirstType' )"    + NEW_LINE +
                                    "[DEBUG]      - 2.txt ( from '/from/myFirstSchema#myFirstType' to '/to/myFirstSchema#myFirstType' )"    + NEW_LINE +
                                    "[DEBUG]      - 3.txt ( from '/from/myFirstSchema#myFirstType' to '/to/myFirstSchema#myFirstType' )"    + NEW_LINE +
                                    "[INFO]  - Schema: 'mySecondSchema'             ( entity types: 1, entities: 2 )"                       + NEW_LINE +
                                    "[INFO]   - EntityType: 'mySecondType'          ( entities: 2 )"                                        + NEW_LINE +
                                    "[DEBUG]     - Created files: 1"                            + NEW_LINE +
                                    "[DEBUG]      - /path/mySecondSchema#mySecondType/0.txt"    + NEW_LINE +
                                    "[DEBUG]     - Updated files: 2"                            + NEW_LINE +
                                    "[DEBUG]      - /path/mySecondSchema#mySecondType/0.txt"    + NEW_LINE +
                                    "[DEBUG]      - /path/mySecondSchema#mySecondType/1.txt"    + NEW_LINE +
                                    "[DEBUG]     - Deleted files: 3"                            + NEW_LINE +
                                    "[DEBUG]      - /path/mySecondSchema#mySecondType/0.txt"    + NEW_LINE +
                                    "[DEBUG]      - /path/mySecondSchema#mySecondType/1.txt"    + NEW_LINE +
                                    "[DEBUG]      - /path/mySecondSchema#mySecondType/2.txt"    + NEW_LINE +
                                    "[DEBUG]     - Moved files: 4"                              + NEW_LINE +
                                    "[DEBUG]      - 0.txt ( from '/from/mySecondSchema#mySecondType' to '/to/mySecondSchema#mySecondType' )" + NEW_LINE +
                                    "[DEBUG]      - 1.txt ( from '/from/mySecondSchema#mySecondType' to '/to/mySecondSchema#mySecondType' )" + NEW_LINE +
                                    "[DEBUG]      - 2.txt ( from '/from/mySecondSchema#mySecondType' to '/to/mySecondSchema#mySecondType' )" + NEW_LINE +
                                    "[DEBUG]      - 3.txt ( from '/from/mySecondSchema#mySecondType' to '/to/mySecondSchema#mySecondType' )" + NEW_LINE;
            //@formatter:on
            assertEquals("Result does not match.", expected, logger.toString());
        }
    }

    @Test
    public void testLogFileInfos() throws Exception {
        {
            final MockLogger logger = new MockLogger(false);
            AdvancedLogger.logFileInfos(logger, new MockedExportInfo(), "");
            assertEquals("Result does not match.", "", logger.toString());
        }
        {
            final MockLogger logger = new MockLogger(false);
            final MockedExportInfo exportInfo = new MockedExportInfo();
            exportInfo.setCreatedFileHandles(createFileHandleCollection(exportInfo, 1));
            exportInfo.setUpdatedFileHandles(createFileHandleCollection(exportInfo, 2));
            exportInfo.setDeletedFileHandles(createFileHandleCollection(exportInfo, 3));
            exportInfo.setMovedFileHandles(createMovedFileHandleCollection(exportInfo, 4));
            AdvancedLogger.logFileInfos(logger, exportInfo, "");
            assertEquals("Result does not match.", "", logger.toString());
        }
        {
            final MockLogger logger = new MockLogger(true);
            final MockedExportInfo exportInfo = new MockedExportInfo();
            exportInfo.setCreatedFileHandles(createFileHandleCollection(exportInfo, 1));
            exportInfo.setUpdatedFileHandles(createFileHandleCollection(exportInfo, 2));
            exportInfo.setDeletedFileHandles(createFileHandleCollection(exportInfo, 3));
            exportInfo.setMovedFileHandles(createMovedFileHandleCollection(exportInfo, 4));
            AdvancedLogger.logFileInfos(logger, exportInfo, "");
            // @formatter:off
            final String expected = "[DEBUG]   - Created files: 1"          + NEW_LINE +
                                    "[DEBUG]    - /path/testName/0.txt"     + NEW_LINE +
                                    "[DEBUG]   - Updated files: 2"          + NEW_LINE +
                                    "[DEBUG]    - /path/testName/0.txt"     + NEW_LINE +
                                    "[DEBUG]    - /path/testName/1.txt"     + NEW_LINE +
                                    "[DEBUG]   - Deleted files: 3"          + NEW_LINE +
                                    "[DEBUG]    - /path/testName/0.txt"     + NEW_LINE +
                                    "[DEBUG]    - /path/testName/1.txt"     + NEW_LINE +
                                    "[DEBUG]    - /path/testName/2.txt"     + NEW_LINE +
                                    "[DEBUG]   - Moved files: 4"            + NEW_LINE +
                                    "[DEBUG]    - 0.txt ( from '/from/testName' to '/to/testName' )" + NEW_LINE +
                                    "[DEBUG]    - 1.txt ( from '/from/testName' to '/to/testName' )" + NEW_LINE +
                                    "[DEBUG]    - 2.txt ( from '/from/testName' to '/to/testName' )" + NEW_LINE +
                                    "[DEBUG]    - 3.txt ( from '/from/testName' to '/to/testName' )" + NEW_LINE;
            // @formatter:on
            assertEquals("Result does not match.", expected, logger.toString());
        }
        {
            final MockLogger logger = new MockLogger(true);
            final MockedExportInfo exportInfo = new MockedExportInfo();
            exportInfo.setCreatedFileHandles(createFileHandleCollection(exportInfo, 1));
            exportInfo.setUpdatedFileHandles(createFileHandleCollection(exportInfo, 2));
            exportInfo.setDeletedFileHandles(createFileHandleCollection(exportInfo, 3));
            AdvancedLogger.logFileInfos(logger, exportInfo, "");
            // @formatter:off
            final String expected = "[DEBUG]   - Created files: 1"      + NEW_LINE +
                                    "[DEBUG]    - /path/testName/0.txt" + NEW_LINE +
                                    "[DEBUG]   - Updated files: 2"      + NEW_LINE +
                                    "[DEBUG]    - /path/testName/0.txt" + NEW_LINE +
                                    "[DEBUG]    - /path/testName/1.txt" + NEW_LINE +
                                    "[DEBUG]   - Deleted files: 3"      + NEW_LINE +
                                    "[DEBUG]    - /path/testName/0.txt" + NEW_LINE +
                                    "[DEBUG]    - /path/testName/1.txt" + NEW_LINE +
                                    "[DEBUG]    - /path/testName/2.txt" + NEW_LINE;
            // @formatter:on
            assertEquals("Result does not match.", expected, logger.toString());
        }
        {
            final MockLogger logger = new MockLogger(true);
            final MockedExportInfo exportInfo = new MockedExportInfo();
            exportInfo.setUpdatedFileHandles(createFileHandleCollection(exportInfo, 2));
            AdvancedLogger.logFileInfos(logger, exportInfo, "");
            // @formatter:off
            final String expected = "[DEBUG]   - Updated files: 2"      + NEW_LINE +
                                    "[DEBUG]    - /path/testName/0.txt" + NEW_LINE +
                                    "[DEBUG]    - /path/testName/1.txt" + NEW_LINE;
            // @formatter:on
            assertEquals("Result does not match.", expected, logger.toString());
        }
    }

    @Test
    public void testLogFileHandles() throws Exception {
        {
            final MockLogger logger = new MockLogger(true);
            AdvancedLogger.logFileHandles(logger, createFileHandleCollection(new MockedExportInfo(), 0), "description", "");
            assertEquals("Result does not match.", "", logger.toString());
        }
        {
            final MockLogger logger = new MockLogger(true);
            AdvancedLogger.logFileHandles(logger, createFileHandleCollection(new MockedExportInfo(), 1), "myDescription", "");
            // @formatter:off
            final String expected = "[DEBUG]   - myDescription: 1"      + NEW_LINE +
                                    "[DEBUG]    - /path/testName/0.txt" + NEW_LINE;
            // @formatter:on
            assertEquals("Result does not match.", expected, logger.toString());
        }
        {
            // debug disabled
            final MockLogger logger = new MockLogger();
            AdvancedLogger.logFileHandles(logger, createFileHandleCollection(new MockedExportInfo(), 1), "myDescription", "");
            // @formatter:off
            final String expected = "";
            // @formatter:on
            assertEquals("Result does not match.", expected, logger.toString());
        }
        {
            final MockLogger logger = new MockLogger(true);
            AdvancedLogger.logFileHandles(logger, createFileHandleCollection(new MockedExportInfo(), 2), "myDescription", " ");
            // @formatter:off
            final String expected = "[DEBUG]    - myDescription: 2"         + NEW_LINE +
                                    "[DEBUG]     - /path/testName/0.txt"    + NEW_LINE +
                                    "[DEBUG]     - /path/testName/1.txt"    + NEW_LINE;
            // @formatter:on
            assertEquals("Result does not match.", expected, logger.toString());
        }
        {
            final MockLogger logger = new MockLogger(true);
            AdvancedLogger.logFileHandles(logger, createFileHandleCollection(new MockedExportInfo(), 3), "myDescription", "  ");
            // @formatter:off
            final String expected = "[DEBUG]     - myDescription: 3"        + NEW_LINE +
                                    "[DEBUG]      - /path/testName/0.txt"   + NEW_LINE +
                                    "[DEBUG]      - /path/testName/1.txt"   + NEW_LINE +
                                    "[DEBUG]      - /path/testName/2.txt"   + NEW_LINE;
            // @formatter:on
            assertEquals("Result does not match.", expected, logger.toString());
        }
    }

    @Test
    public void testLogMovedFileHandles() throws Exception {
        {
            final MockLogger logger = new MockLogger(true);
            AdvancedLogger.logMovedFileHandles(logger, createMovedFileHandleCollection(new MockedExportInfo(), 0), "");
            assertEquals("Result does not match.", "", logger.toString());
        }
        {
            final MockLogger logger = new MockLogger(true);
            AdvancedLogger.logMovedFileHandles(logger, createMovedFileHandleCollection(new MockedExportInfo(), 1), "");
            // @formatter:off
            final String expected = "[DEBUG]   - Moved files: 1" + NEW_LINE +
                                    "[DEBUG]    - 0.txt ( from '/from/testName' to '/to/testName' )" + NEW_LINE;
            // @formatter:on
            assertEquals("Result does not match.", expected, logger.toString());
        }
        {
            final MockLogger logger = new MockLogger(true);
            AdvancedLogger.logMovedFileHandles(logger, createMovedFileHandleCollection(new MockedExportInfo(), 2), " ");
            // @formatter:off
            final String expected = "[DEBUG]    - Moved files: 2"                                     + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/testName' to '/to/testName' )" + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/testName' to '/to/testName' )" + NEW_LINE;
            // @formatter:on
            assertEquals("Result does not match.", expected, logger.toString());
        }
        {
            final MockLogger logger = new MockLogger(true);
            AdvancedLogger.logMovedFileHandles(logger, createMovedFileHandleCollection(new MockedExportInfo(), 3), "  ");
            // @formatter:off
            final String expected = "[DEBUG]     - Moved files: 3"                                     + NEW_LINE +
                                    "[DEBUG]      - 0.txt ( from '/from/testName' to '/to/testName' )" + NEW_LINE +
                                    "[DEBUG]      - 1.txt ( from '/from/testName' to '/to/testName' )" + NEW_LINE +
                                    "[DEBUG]      - 2.txt ( from '/from/testName' to '/to/testName' )" + NEW_LINE;
            // @formatter:on
            assertEquals("Result does not match.", expected, logger.toString());
        }
        {
            // debug disabled
            final MockLogger logger = new MockLogger();
            AdvancedLogger.logMovedFileHandles(logger, createMovedFileHandleCollection(new MockedExportInfo(), 3), "  ");
            // @formatter:off
            final String expected = "";
            // @formatter:on
            assertEquals("Result does not match.", expected, logger.toString());
        }
    }

    @Test
    public void testToCamelCase() throws Exception {
        assertEquals("Result does not match.", "PageStore", AdvancedLogger.toCamelCase("_", "pAGE_sTORE"));
        assertEquals("Result does not match.", "", AdvancedLogger.toCamelCase("_", "_"));
        assertEquals("Result does not match.", "P", AdvancedLogger.toCamelCase("_", "p_"));
        assertEquals("Result does not match.", "S", AdvancedLogger.toCamelCase("_", "_s"));
        assertEquals("Result does not match.", "Page", AdvancedLogger.toCamelCase("_", "pAGE_"));
        assertEquals("Result does not match.", "Store", AdvancedLogger.toCamelCase("_", "_sTORE"));
        assertEquals("Result does not match.", "PageStoreFolder", AdvancedLogger.toCamelCase("_", "pAGE_sTORE_fOLDER"));
        assertEquals("Result does not match.", "PageFolder", AdvancedLogger.toCamelCase(";", "pAGE;;fOLDER"));
    }

    @Test
    public void testGetDirectoryForFile() throws Exception {
        final String result = AdvancedLogger.getDirectoryForFile(new MockedFileHandle(null, "/test/path/fileName.txt", "fileName.txt"));
        assertEquals("Result does not match.", "/test/path", result);
    }

    @Test
    public void testGetDirectoryForFileEmptyPath() throws Exception {
        final String result = AdvancedLogger.getDirectoryForFile(new MockedFileHandle(null, "", ""));
        assertEquals("Result does not match.", "", result);
    }

    @Test
    public void testGetDirectoryForFileRootPath() throws Exception {
        final String result = AdvancedLogger.getDirectoryForFile(new MockedFileHandle(null, "fileName.txt", "fileName.txt"));
        assertEquals("Result does not match.", "fileName.txt", result);
    }

    @Test
    public void testGetFilesStringForElementAllPresent() throws Exception {
        final MockedExportInfo exportInfo = new MockedExportInfo(ExportInfo.Type.ELEMENT, "storeElement", ExportStatus.CREATED);
        exportInfo.setCreatedFileHandles(createFileHandleCollection(exportInfo, 1));
        exportInfo.setUpdatedFileHandles(createFileHandleCollection(exportInfo, 2));
        exportInfo.setDeletedFileHandles(createFileHandleCollection(exportInfo, 3));
        exportInfo.setMovedFileHandles(createMovedFileHandleCollection(exportInfo, 4));
        final String result = AdvancedLogger.getFilesStringForElement(exportInfo);
        final String expected = " ( created files: 1, updated files: " + exportInfo.getUpdatedFileHandles().size() + ", deleted files: " + exportInfo.getDeletedFileHandles().size() + ", moved files: " + exportInfo.getMovedFileHandles().size() + " )";
        assertEquals("Result does not match.", expected, result);
    }

    @Test
    public void testGetFilesStringForElementPartialPresent() throws Exception {
        final MockedExportInfo exportInfo = new MockedExportInfo(ExportInfo.Type.ELEMENT, "storeElement", ExportStatus.CREATED);
        exportInfo.setUpdatedFileHandles(createFileHandleCollection(exportInfo, 2));
        final String result = AdvancedLogger.getFilesStringForElement(exportInfo);
        final String expected = " ( updated files: " + exportInfo.getUpdatedFileHandles().size() + " )";
        assertEquals("Result does not match.", expected, result);
    }

    @Test
    public void testGetFilesStringForElementAllEmpty() throws Exception {
        final MockedExportInfo exportInfo = new MockedExportInfo(ExportInfo.Type.ELEMENT, "storeElement", ExportStatus.CREATED);
        final String result = AdvancedLogger.getFilesStringForElement(exportInfo);
        final String expected = "";
        assertEquals("Result does not match.", expected, result);
    }

    @Test
    public void testGetFilesString() throws Exception {
        // assert non empty
        final List<String> list = new ArrayList<>(Arrays.asList("First", "Second"));
        final String description = "myDescription";
        final String result = AdvancedLogger.getFilesString(description, list);
        assertEquals("Result does not match.", " " + description + ": " + list.size() + ',', result);

        // assert empty
        assertEquals("Result does not match.", "", AdvancedLogger.getFilesString(description, Collections.emptyList()));
    }

    @Test
    public void testGetSpacedString() {
        assertEquals("Result does not match.", "", AdvancedLogger.getSpacedString(-5));
        assertEquals("Result does not match.", "", AdvancedLogger.getSpacedString(-1));
        assertEquals("Result does not match.", "", AdvancedLogger.getSpacedString(0));
        assertEquals("Result does not match.", " ", AdvancedLogger.getSpacedString(1));
        assertEquals("Result does not match.", "     ", AdvancedLogger.getSpacedString(5));
    }

    @NotNull
    private static Map<Store.Type, List<ElementExportInfo>> createMapWithStoreElements() {
        return createMapWithStoreElements(ExportStatus.CREATED, true, true, true, true);
    }

    @NotNull
    private static Map<Store.Type, List<ElementExportInfo>> createMapWithStoreElements(@NotNull final ExportStatus status, final boolean pageStore, final boolean mediaStore, final boolean siteStore, final boolean templateStore) {
        final Map<Store.Type, List<ElementExportInfo>> storeElements = new TreeMap<>();
        if (pageStore) {
            final Store.Type storeType = Store.Type.PAGESTORE;
            final List<ElementExportInfo> list = new ArrayList<>();
            list.add(new MockedElementExportInfo(storeType, "first", TagNames.PAGE, status));
            storeElements.put(storeType, list);
        }
        if (mediaStore) {
            final Store.Type storeType = Store.Type.MEDIASTORE;
            final List<ElementExportInfo> list = new ArrayList<>();
            list.add(new MockedElementExportInfo(storeType, "second", TagNames.MEDIUM, status));
            list.add(new MockedElementExportInfo(storeType, "first", TagNames.MEDIUM, status));
            storeElements.put(storeType, list);
        }
        if (siteStore) {
            final Store.Type storeType = Store.Type.SITESTORE;
            final List<ElementExportInfo> list = new ArrayList<>();
            list.add(new MockedElementExportInfo(storeType, "third", TagNames.PAGEREF, status));
            list.add(new MockedElementExportInfo(storeType, "first", TagNames.PAGEREF, status));
            list.add(new MockedElementExportInfo(storeType, "second", TagNames.PAGEREF, status));
            storeElements.put(storeType, list);
        }
        if (templateStore) {
            final Store.Type storeType = Store.Type.TEMPLATESTORE;
            final List<ElementExportInfo> list = new ArrayList<>();
            list.add(new MockedElementExportInfo(storeType, "second", TagNames.TEMPLATE, status));
            list.add(new MockedElementExportInfo(storeType, "fourth", TagNames.TEMPLATE, status));
            list.add(new MockedElementExportInfo(storeType, "third", TagNames.TEMPLATE, status));
            list.add(new MockedElementExportInfo(storeType, "first", TagNames.TEMPLATE, status));
            storeElements.put(storeType, list);
        }
        return storeElements;
    }

    private static Collection<ExportInfoFileHandle> createFileHandleCollection(final ExportInfo exportInfo, final int amount) {
        final Collection<ExportInfoFileHandle> result = new ArrayList<>();
        for (int index = 0; index < amount; index++) {
            // reversed order for file names & paths (to check sorting)
            result.add(new MockedFileHandle(exportInfo, "/path/" + exportInfo.getName() + "/" + (amount - index - 1) + ".txt", (amount - index - 1) + ".txt"));
        }
        return result;
    }

    private static Collection<Pair<ExportInfoFileHandle, ExportInfoFileHandle>> createMovedFileHandleCollection(final ExportInfo exportInfo, final int amount) {
        final Collection<Pair<ExportInfoFileHandle, ExportInfoFileHandle>> result = new ArrayList<>();
        for (int index = 0; index < amount; index++) {
            // reversed order for file names & paths (to check sorting)
            result.add(new Pair<>(new MockedFileHandle(exportInfo, "/from/" + exportInfo.getName() + "/" + (amount - index - 1) + ".txt", (amount - index - 1) + ".txt"), new MockedFileHandle(exportInfo, "/to/" + exportInfo.getName() + "/" + (amount - index - 1) + ".txt", (amount - index - 1) + ".txt")));
        }
        return result;
    }

    private static class MockedFileHandle extends ExportInfoFileHandleImpl {

        private final String _path;
        private final String _fileName;

        MockedFileHandle(@Nullable final ExportInfo exportInfo, @NotNull final String path, @NotNull final String fileName) {
            super(null, exportInfo);
            _path = path;
            _fileName = fileName;
        }

        @Override
        public String getName() {
            return _fileName;
        }

        @NotNull
        @Override
        public String getPath() {
            return _path;
        }
    }

    private static class MockedPropertyTypeExportInfo extends MockedExportInfo implements PropertyTypeExportInfo {

        private final PropertiesTransportOptions.ProjectPropertyType _propertyType;

        MockedPropertyTypeExportInfo(@Nullable final PropertiesTransportOptions.ProjectPropertyType propertyType) {
            super(Type.PROJECT_PROPERTY);
            _propertyType = propertyType;
            setCreatedFileHandles(createFileHandleCollection(this, 1));
            setUpdatedFileHandles(createFileHandleCollection(this, 2));
            setDeletedFileHandles(createFileHandleCollection(this, 3));
            setMovedFileHandles(createMovedFileHandleCollection(this, 4));
        }

        @NotNull
        @Override
        public String getName() {
            return _propertyType == null ? "Property FS metadata" : _propertyType.name();
        }

        @Nullable
        @Override
        public PropertiesTransportOptions.ProjectPropertyType getPropertyType() {
            return _propertyType;
        }
    }

    private static class MockedElementExportInfo extends MockedExportInfo implements ElementExportInfo {

        private final BasicElementInfoImpl _basicElementInfo;

        MockedElementExportInfo(@NotNull final Store.Type storeType, @NotNull final String name) {
            this(storeType, name, ExportStatus.CREATED);
        }

        MockedElementExportInfo(@NotNull final Store.Type storeType, @NotNull final String name, @NotNull final String nodeTag) {
            this(storeType, name, nodeTag, ExportStatus.CREATED);
        }

        MockedElementExportInfo(@NotNull final Store.Type storeType, @NotNull final String name, @NotNull final ExportStatus exportStatus) {
            this(storeType, name, TagNames.PAGE, exportStatus);
        }

        MockedElementExportInfo(@NotNull final Store.Type storeType, @NotNull final String name, @NotNull final String nodeTag, @NotNull final ExportStatus exportStatus) {
            super(Type.ELEMENT, name, exportStatus);
            _basicElementInfo = new BasicElementInfoImpl(storeType, nodeTag, -1, name, -1);
            setCreatedFileHandles(createFileHandleCollection(this, 1));
            setUpdatedFileHandles(createFileHandleCollection(this, 2));
            setDeletedFileHandles(createFileHandleCollection(this, 3));
            setMovedFileHandles(createMovedFileHandleCollection(this, 4));
        }

        @Nullable
        @Override
        public BasicElementInfo getElementInfo() {
            return _basicElementInfo;
        }
    }

    private static class MockedEntityTypeExportInfo extends MockedExportInfo implements EntityTypeExportInfo {

        private final String _entityType;
        private final BasicElementInfoImpl _schemaElementInfo;
        private Collection<BasicEntityInfo> _entities;

        MockedEntityTypeExportInfo(@NotNull final String entityType) {
            this(entityType, "mockSchema");
        }

        MockedEntityTypeExportInfo(@NotNull final String entityType, @NotNull final String schemaName) {
            this(entityType, schemaName, 0);
        }

        MockedEntityTypeExportInfo(@NotNull final String entityType, @NotNull final String schemaName, final int entityCount) {
            super(Type.ENTITY_TYPE);
            _entityType = entityType;
            _schemaElementInfo = new BasicElementInfoImpl(Store.Type.TEMPLATESTORE, schemaName, -1, schemaName, -1);
            _entities = new ArrayList<>();
            for (int index = 0; index < entityCount; index++) {
                _entities.add(new BasicEntityInfoImpl(UUID.nameUUIDFromBytes(("entity_" + index).getBytes()), entityType, schemaName));
            }
            setCreatedFileHandles(createFileHandleCollection(this, 1));
            setUpdatedFileHandles(createFileHandleCollection(this, 2));
            setDeletedFileHandles(createFileHandleCollection(this, 3));
            setMovedFileHandles(createMovedFileHandleCollection(this, 4));
        }

        @NotNull
        @Override
        public String getName() {
            return _schemaElementInfo.getUid() + '#' + getEntityType();
        }

        @NotNull
        @Override
        public String getEntityType() {
            return _entityType;
        }

        @NotNull
        @Override
        public BasicElementInfo getSchema() {
            return _schemaElementInfo;
        }

        @NotNull
        @Override
        public Collection<BasicEntityInfo> getEntities() {
            return _entities;
        }

        @Override
        public boolean allEntitiesExported() {
            return false;
        }
    }

    private static class MockedExportInfo implements ExportInfo {

        private final Type _type;
        private final String _name;
        private final ExportStatus _exportStatus;
        private final Set<ExportInfoFileHandle> _createdFileHandles, _updatedFileHandles, _deletedFileHandles;
        private final Collection<Pair<ExportInfoFileHandle, ExportInfoFileHandle>> _movedFileHandles;

        public MockedExportInfo() {
            this(Type.ELEMENT);
        }

        public MockedExportInfo(final Type type) {
            this(type, "testName", ExportStatus.CREATED);
        }

        public MockedExportInfo(@NotNull final Type type, @NotNull final String name, @NotNull final ExportStatus exportStatus) {
            _type = type;
            _name = name;
            _exportStatus = exportStatus;
            _createdFileHandles = new HashSet<>();
            _updatedFileHandles = new HashSet<>();
            _deletedFileHandles = new HashSet<>();
            _movedFileHandles = new ArrayList<>();
        }

        @NotNull
        @Override
        public Type getType() {
            return _type;
        }

        @NotNull
        @Override
        public String getName() {
            return _name;
        }

        @NotNull
        @Override
        public ExportStatus getStatus() {
            return _exportStatus;
        }

        public void setCreatedFileHandles(@NotNull final Collection<ExportInfoFileHandle> createdFileHandles) {
            _createdFileHandles.clear();
            _createdFileHandles.addAll(createdFileHandles);
        }

        @NotNull
        @Override
        public Set<ExportInfoFileHandle> getCreatedFileHandles() {
            return _createdFileHandles;
        }

        public void setUpdatedFileHandles(@NotNull final Collection<ExportInfoFileHandle> updatedFileHandles) {
            _updatedFileHandles.clear();
            _updatedFileHandles.addAll(updatedFileHandles);
        }

        @NotNull
        @Override
        public Set<ExportInfoFileHandle> getUpdatedFileHandles() {
            return _updatedFileHandles;
        }

        public void setDeletedFileHandles(@NotNull final Collection<ExportInfoFileHandle> deletedFileHandles) {
            _deletedFileHandles.clear();
            _deletedFileHandles.addAll(deletedFileHandles);
        }

        @NotNull
        @Override
        public Set<ExportInfoFileHandle> getDeletedFileHandles() {
            return _deletedFileHandles;
        }

        public void setMovedFileHandles(@NotNull final Collection<Pair<ExportInfoFileHandle, ExportInfoFileHandle>> movedFileHandles) {
            _movedFileHandles.clear();
            _movedFileHandles.addAll(movedFileHandles);
        }

        @NotNull
        @Override
        public Collection<Pair<ExportInfoFileHandle, ExportInfoFileHandle>> getMovedFileHandles() {
            return _movedFileHandles;
        }
    }

    private static class MockLogger implements Logger {
        private final StringBuilder _stringBuilder = new StringBuilder();
        private final boolean _debugEnabled;
        private boolean _infoEnabled;

        public MockLogger() {
            this(false);
        }

        public MockLogger(final boolean debugEnabled) {
            _debugEnabled = debugEnabled;
            _infoEnabled = true;
        }

        public void setInfoEnabled(final boolean infoEnabled) {
            _infoEnabled = infoEnabled;
        }

        @Override
        public String getName() {
            return "MockLogger";
        }

        private void append(final String prefix, final String s) {
            _stringBuilder.append('[').append(prefix).append("] ").append(s).append(NEW_LINE);
        }

        @Override
        public boolean isTraceEnabled() {
            return true;
        }

        @Override
        public void trace(final String s) {
            append("TRACE", s);
        }

        @Override
        public void trace(final String s, final Object o) {
            trace(s);
        }

        @Override
        public void trace(final String s, final Object o, final Object o1) {
            trace(s);
        }

        @Override
        public void trace(final String s, final Object[] objects) {
            trace(s);
        }

        @Override
        public void trace(final String s, final Throwable throwable) {
            trace(s);
        }

        @Override
        public boolean isTraceEnabled(final Marker marker) {
            return true;
        }

        @Override
        public void trace(final Marker marker, final String s) {
            trace(s);
        }

        @Override
        public void trace(final Marker marker, final String s, final Object o) {
            trace(s);
        }

        @Override
        public void trace(final Marker marker, final String s, final Object o, final Object o1) {
            trace(s);
        }

        @Override
        public void trace(final Marker marker, final String s, final Object[] objects) {
            trace(s);
        }

        @Override
        public void trace(final Marker marker, final String s, final Throwable throwable) {
            trace(s);
        }

        @Override
        public boolean isDebugEnabled() {
            return _debugEnabled;
        }

        @Override
        public void debug(final String s) {
            append("DEBUG", s);
        }

        @Override
        public void debug(final String s, final Object o) {
            debug(s);
        }

        @Override
        public void debug(final String s, final Object o, final Object o1) {
            debug(s);
        }

        @Override
        public void debug(final String s, final Object[] objects) {
            debug(s);
        }

        @Override
        public void debug(final String s, final Throwable throwable) {
            debug(s);
        }

        @Override
        public boolean isDebugEnabled(final Marker marker) {
            return _debugEnabled;
        }

        @Override
        public void debug(final Marker marker, final String s) {
            debug(s);
        }

        @Override
        public void debug(final Marker marker, final String s, final Object o) {
            debug(s);
        }

        @Override
        public void debug(final Marker marker, final String s, final Object o, final Object o1) {
            debug(s);
        }

        @Override
        public void debug(final Marker marker, final String s, final Object[] objects) {
            debug(s);
        }

        @Override
        public void debug(final Marker marker, final String s, final Throwable throwable) {
            debug(s);
        }

        @Override
        public boolean isInfoEnabled() {
            return _infoEnabled;
        }

        @Override
        public void info(final String s) {
            append("INFO", s);
        }

        @Override
        public void info(final String s, final Object o) {
            info(s);
        }

        @Override
        public void info(final String s, final Object o, final Object o1) {
            info(s);
        }

        @Override
        public void info(final String s, final Object[] objects) {
            info(s);
        }

        @Override
        public void info(final String s, final Throwable throwable) {
            info(s);
        }

        @Override
        public boolean isInfoEnabled(final Marker marker) {
            return true;
        }

        @Override
        public void info(final Marker marker, final String s) {
            info(s);
        }

        @Override
        public void info(final Marker marker, final String s, final Object o) {
            info(s);
        }

        @Override
        public void info(final Marker marker, final String s, final Object o, final Object o1) {
            info(s);
        }

        @Override
        public void info(final Marker marker, final String s, final Object[] objects) {
            info(s);
        }

        @Override
        public void info(final Marker marker, final String s, final Throwable throwable) {
            info(s);
        }

        @Override
        public boolean isWarnEnabled() {
            return true;
        }

        @Override
        public void warn(final String s) {
            append("WARN", s);
        }

        @Override
        public void warn(final String s, final Object o) {
            warn(s);
        }

        @Override
        public void warn(final String s, final Object[] objects) {
            warn(s);
        }

        @Override
        public void warn(final String s, final Object o, final Object o1) {
            warn(s);
        }

        @Override
        public void warn(final String s, final Throwable throwable) {
            warn(s);
        }

        @Override
        public boolean isWarnEnabled(final Marker marker) {
            return true;
        }

        @Override
        public void warn(final Marker marker, final String s) {
            warn(s);
        }

        @Override
        public void warn(final Marker marker, final String s, final Object o) {
            warn(s);
        }

        @Override
        public void warn(final Marker marker, final String s, final Object o, final Object o1) {
            warn(s);
        }

        @Override
        public void warn(final Marker marker, final String s, final Object[] objects) {
            warn(s);
        }

        @Override
        public void warn(final Marker marker, final String s, final Throwable throwable) {
            warn(s);
        }

        @Override
        public boolean isErrorEnabled() {
            return true;
        }

        @Override
        public void error(final String s) {
            append("ERROR", s);
        }

        @Override
        public void error(final String s, final Object o) {
            error(s);
        }

        @Override
        public void error(final String s, final Object o, final Object o1) {
            error(s);
        }

        @Override
        public void error(final String s, final Object[] objects) {
            error(s);
        }

        @Override
        public void error(final String s, final Throwable throwable) {
            error(s);
        }

        @Override
        public boolean isErrorEnabled(final Marker marker) {
            return true;
        }

        @Override
        public void error(final Marker marker, final String s) {
            error(s);
        }

        @Override
        public void error(final Marker marker, final String s, final Object o) {
            error(s);
        }

        @Override
        public void error(final Marker marker, final String s, final Object o, final Object o1) {
            error(s);
        }

        @Override
        public void error(final Marker marker, final String s, final Object[] objects) {
            error(s);
        }

        @Override
        public void error(final Marker marker, final String s, final Throwable throwable) {
            error(s);
        }

        @Override
        public String toString() {
            return _stringBuilder.toString();
        }
    }

    private static class MockedExportResult implements ExportOperation.Result {

        private Collection<ExportInfo> _createdElements, _updateElements, _deletedElements, _movedElements;

        MockedExportResult() {
            this(true);
        }

        MockedExportResult(boolean fill) {
            if (fill) {
                fill();
            } else {
                _createdElements = Collections.emptyList();
                _updateElements = Collections.emptyList();
                _deletedElements = Collections.emptyList();
                _movedElements = Collections.emptyList();
            }
        }

        void fill() {
            {
                _createdElements = fillCollection(createMapWithStoreElements(ExportStatus.CREATED, true, false, false, true).values());
                _createdElements.add(new MockedPropertyTypeExportInfo(PropertiesTransportOptions.ProjectPropertyType.USERS));
                _createdElements.add(new MockedEntityTypeExportInfo("createdType", "createdSchema", 1));
            }
            {
                _updateElements = fillCollection(createMapWithStoreElements(ExportStatus.UPDATED, false, true, true, false).values());
                _updateElements.add(new MockedPropertyTypeExportInfo(PropertiesTransportOptions.ProjectPropertyType.USERS));
                _updateElements.add(new MockedEntityTypeExportInfo("updatedType1", "updatedSchema1", 2));
                _updateElements.add(new MockedEntityTypeExportInfo("updatedType2", "updatedSchema1", 3));
                _updateElements.add(new MockedEntityTypeExportInfo("updatedType1", "updatedSchema2", 1));
                _updateElements.add(new MockedExportInfo(ExportInfo.Type.FS_META));
            }
            {
                _deletedElements = fillCollection(createMapWithStoreElements(ExportStatus.DELETED, true, false, true, false).values());
                _deletedElements.add(new MockedPropertyTypeExportInfo(PropertiesTransportOptions.ProjectPropertyType.USERS));
                _deletedElements.add(new MockedEntityTypeExportInfo("deletedType1", "deletedSchema", 3));
                _deletedElements.add(new MockedEntityTypeExportInfo("deletedType2", "deletedSchema", 4));
            }
            {
                _movedElements = fillCollection(createMapWithStoreElements(ExportStatus.MOVED, false, true, false, true).values());
                _movedElements.add(new MockedPropertyTypeExportInfo(PropertiesTransportOptions.ProjectPropertyType.USERS));
                _movedElements.add(new MockedEntityTypeExportInfo("movedType1", "movedSchema1", 1));
                _movedElements.add(new MockedEntityTypeExportInfo("movedType2", "movedSchema2", 2));
            }
        }

        private Collection<ExportInfo> fillCollection(final Collection<List<ElementExportInfo>> values) {
            final Collection<ExportInfo> result = new ArrayList<>();
            for (final List<ElementExportInfo> list : values) {
                result.addAll(list);
            }
            return result;
        }

        @Override
        public Set<FileHandle> getCreatedFiles() {
            return null;
        }

        @Override
        public Set<FileHandle> getUpdatedFiles() {
            return null;
        }

        @Override
        public Set<FileHandle> getDeletedFiles() {
            return null;
        }

        @Override
        public Set<ExportInfoFileHandle> getCreatedFileHandles() {
            return null;
        }

        @Override
        public Set<ExportInfoFileHandle> getUpdatedFileHandles() {
            return null;
        }

        @Override
        public Set<ExportInfoFileHandle> getDeletedFileHandles() {
            return null;
        }

        @Override
        public Collection<Pair<ExportInfoFileHandle, ExportInfoFileHandle>> getMovedFileHandles() {
            return null;
        }

        @Override
        public Collection<ExportInfo> getCreatedElements() {
            return _createdElements;
        }

        @Override
        public Collection<ExportInfo> getUpdatedElements() {
            return _updateElements;
        }

        @Override
        public Collection<ExportInfo> getDeletedElements() {
            return _deletedElements;
        }

        @Override
        public Collection<ExportInfo> getMovedElements() {
            return _movedElements;
        }
    }

    private static class MockedImportResult implements ImportOperation.Result {

        private Set<BasicElementInfo> _createdElements, _updateElements, _deletedElements, _movedElements, _lostAndFoundElements;
        private Set<BasicEntityInfo> _createdEntities, _updatedEntities;
        private EnumSet<PropertiesTransportOptions.ProjectPropertyType> _projectProperties;
        private List<ImportOperation.Problem> _problems;

        MockedImportResult(boolean fill) {
            if (fill) {
                fill();
            } else {
                _createdElements = Collections.emptySet();
                _updateElements = Collections.emptySet();
                _deletedElements = Collections.emptySet();
                _movedElements = Collections.emptySet();
                _lostAndFoundElements = Collections.emptySet();
                _createdEntities = Collections.emptySet();
                _updatedEntities = Collections.emptySet();
                _problems = Collections.emptyList();
                _projectProperties = EnumSet.noneOf(PropertiesTransportOptions.ProjectPropertyType.class);
            }
        }

        void fill() {
            {
                _createdElements = new HashSet<>();
                fillCollection(_createdElements, "created");
            }
            {
                _updateElements = new HashSet<>();
                fillCollection(_updateElements, "updated");
            }
            {
                _deletedElements = new HashSet<>();
                fillCollection(_deletedElements, "deleted");
            }
            {
                _movedElements = new HashSet<>();
                fillCollection(_movedElements, "moved");
            }
            {
                _lostAndFoundElements = new HashSet<>();
                fillCollection(_lostAndFoundElements, "lostAndFound");
            }
            {
                _lostAndFoundElements = new HashSet<>();
                fillCollection(_lostAndFoundElements, "lostAndFound");
            }
            {
                _projectProperties = EnumSet.allOf(PropertiesTransportOptions.ProjectPropertyType.class);
            }
            {
                _createdEntities = new HashSet<>();
                fillEntities(_createdEntities, "created");
                _updatedEntities = new HashSet<>();
                fillEntities(_updatedEntities, "updated");
            }
            {
                _problems = new ArrayList<>();
                _problems.add(createProblem(Store.Type.PAGESTORE, 1337, "IdProvider not found"));
                _problems.add(createProblem(Store.Type.MEDIASTORE, 1932, "Medium invalid"));
                _problems.add(createProblem(Store.Type.MEDIASTORE, 123, "Resolution invalid"));
                _problems.add(createProblem(Store.Type.TEMPLATESTORE, 1231, "GOM is invalid"));
            }
        }

        private ImportOperation.Problem createProblem(@NotNull final Store.Type storeType, final long nodeId, @NotNull final String message) {
            return new ImportOperation.Problem() {
                @Override
                public Store.Type getStoreType() {
                    return storeType;
                }

                @Override
                public long getNodeId() {
                    return nodeId;
                }

                @Override
                public String getMessage() {
                    return message;
                }
            };
        }

        private void fillEntities(final Set<BasicEntityInfo> set, final String description) {
            set.add(createEntityInfo(4, "entityType1", "schema2", description));
            set.add(createEntityInfo(1, "entityType1", "schema1", description));
            set.add(createEntityInfo(6, "entityType1", "schema3", description));
            set.add(createEntityInfo(3, "entityType2", "schema1", description));
            set.add(createEntityInfo(2, "entityType1", "schema1", description));
            set.add(createEntityInfo(5, "entityType2", "schema2", description));
        }

        private void fillCollection(final Set<BasicElementInfo> set, final String description) {
            set.add(createElementInfo(4, Store.Type.TEMPLATESTORE, TagNames.PAGETEMPLATES, description));
            set.add(createElementInfo(3, Store.Type.MEDIASTORE, TagNames.MEDIUM, description));
            set.add(createElementInfo(1, Store.Type.PAGESTORE, TagNames.PAGE, description));
            set.add(createElementInfo(5, Store.Type.TEMPLATESTORE, TagNames.LINKTEMPLATE, description));
            set.add(createElementInfo(2, Store.Type.MEDIASTORE, TagNames.MEDIUM, description));
            set.add(createElementInfo(6, Store.Type.TEMPLATESTORE, TagNames.FORMATTEMPLATE, description));
            set.add(createElementInfo(7, Store.Type.TEMPLATESTORE, TagNames.FORMATTEMPLATE, description));
        }

        @NotNull
        private BasicElementInfoImpl createElementInfo(final int id, final Store.Type storeType, final String nodeTag, final String description) {
            return new BasicElementInfoImpl(storeType, nodeTag, id, description + "_" + nodeTag + "_" + id, -1);
        }

        @NotNull
        private BasicEntityInfoImpl createEntityInfo(final int id, final String entityType, final String schemaUid, final String description) {
            return new BasicEntityInfoImpl(UUID.nameUUIDFromBytes((description + "_" + entityType + "_" + schemaUid + "_" + id).getBytes()), description + "_" + entityType, schemaUid);
        }

        @Override
        public Set<BasicElementInfo> getCreatedElements() {
            return _createdElements;
        }

        @Override
        public Set<BasicElementInfo> getUpdatedElements() {
            return _updateElements;
        }

        @Override
        public Set<BasicElementInfo> getDeletedElements() {
            return _deletedElements;
        }

        @Override
        public Set<BasicElementInfo> getLostAndFoundElements() {
            return _lostAndFoundElements;
        }

        @Override
        public Set<BasicElementInfo> getMovedElements() {
            return _movedElements;
        }

        @Override
        public Set<BasicEntityInfo> getCreatedEntities() {
            return _createdEntities;
        }

        @Override
        public Set<BasicEntityInfo> getUpdatedEntities() {
            return _updatedEntities;
        }

        @Override
        public EnumSet<PropertiesTransportOptions.ProjectPropertyType> getModifiedProjectProperties() {
            return _projectProperties;
        }

        @Override
        public List<ImportOperation.Problem> getProblems() {
            return _problems;
        }

        StoreAgent getStoreAgent() {
            final MockedStoreAgent storeAgent = new MockedStoreAgent();
            final List<ImportOperation.Problem> problems = getProblems();
            for (final ImportOperation.Problem problem : problems) {
                final MockedStore store = (MockedStore) storeAgent.getStore(problem.getStoreType());
                store.getOrCreateElement(problem.getNodeId());
            }
            return storeAgent;
        }
    }

    private static class MockedStoreAgent implements StoreAgent {

        private final Map<Store.Type, Store> _stores = new HashMap<>();

        void addStore(@NotNull final Store.Type type, @NotNull final Store store) {
            _stores.put(type, store);
        }

        @Override
        public Store getStore(@NotNull final Store.Type type) {
            Store store = _stores.get(type);
            if (store == null) {
                store = new MockedStore(type);
                addStore(type, store);
            }
            return store;
        }

        @Override
        public Store getStore(@NotNull final Store.Type type, final boolean b) {
            return getStore(type);
        }
    }

    private static class MockedStore implements Store {

        private boolean _release;
        private Type _type;
        private Map<Long, IDProvider> _storeElementsById;

        MockedStore(final Type type) {
            _type = type;
            _storeElementsById = new HashMap<>();
            setRelease(false);
        }

        public void addMockedStoreElement(final IDProvider element) {
            _storeElementsById.put(element.getId(), element);
        }

        void setRelease(final boolean release) {
            _release = release;
        }

        @Override
        public Type getType() {
            return _type;
        }

        @Override
        public void addStoreListener(final StoreListener storeListener) {
            // nothing to do
        }

        @Override
        public void removeStoreListener(final StoreListener storeListener) {
            // nothing to do
        }

        @Override
        public boolean isRelease() {
            return _release;
        }

        @Override
        public List<DeletedElementsInfo> getDeletedChilds() {
            return Collections.EMPTY_LIST;
        }

        @Override
        public List<DeletedElementsInfo> getDeletedChildren() {
            return Collections.EMPTY_LIST;
        }

        @Override
        public List<DeletedElementsInfo> getDeletedChilds(final long l, final int i) {
            return Collections.EMPTY_LIST;
        }

        @Override
        public List<DeletedElementsInfo> getDeletedChildren(final long l, final int i) {
            return Collections.EMPTY_LIST;
        }

        @Override
        public StoreElement restore(final ElementInfo elementInfo, final IDProvider idProvider) throws LockException {
            return null;
        }

        @Override
        public boolean isFolder() {
            return false;
        }

        @Override
        public boolean isPermissionSupported() {
            return false;
        }

        @Override
        public boolean hasPermissions() {
            return false;
        }

        @Override
        public Permission getPermission() {
            return null;
        }

        @Override
        public Permission getPermission(final User user) {
            return null;
        }

        @Override
        public Permission getPermission(final Group group) {
            return null;
        }

        @Override
        public void setPermission(final User user, final Permission permission) {
            // nothing to do
        }

        @Override
        public void setPermission(final User[] users, final Permission permission) {
            // nothing to do
        }

        @Override
        public void setPermission(final Group group, final Permission permission) {
            // nothing to do
        }

        @Override
        public void removePermission(final User user) {
            // nothing to do
        }

        @Override
        public void removePermission(final User[] users) {
            // nothing to do
        }

        @Override
        public void removePermission(final Group group) {
            // nothing to do
        }

        @Override
        public PermissionMap getTreePermission() {
            return null;
        }

        @Override
        public List<Principal> getDefinedPrincipalPermissions() {
            return null;
        }

        @Override
        public List<Principal> getInheritedPrincipalPermissions() {
            return null;
        }

        @Override
        public long getLastChanged() {
            return 0;
        }

        @Override
        public User getEditor() {
            return null;
        }

        @Override
        public boolean isWorkflowSupported() {
            return false;
        }

        @Override
        public WorkflowPermission[] getWorkflowPermissions() {
            return new WorkflowPermission[0];
        }

        @Override
        public WorkflowPermission getWorkflowPermission(final Workflow workflow) {
            return null;
        }

        @Override
        public WorkflowPermission getCreateWorkflowPermission(final Workflow workflow) {
            return null;
        }

        @Override
        public void setWorkflowPermission(final WorkflowPermission workflowPermission) {
            // nothing to do
        }

        @Override
        public void setWorkflowPermissions(final WorkflowPermission[] workflowPermissions) {
            // nothing to do
        }

        @Override
        public void removeWorkflowPermission(final Workflow workflow) {
            // nothing to do
        }

        @Override
        public void removeAllWorkflowPermissions() {
            // nothing to do
        }

        @Override
        public boolean isWorkflowAllowed(final Workflow workflow, final User user) {
            return false;
        }

        @Override
        public boolean inheritWorkflowPermission() {
            return false;
        }

        @Override
        public void setInheritWorkflowPermission(final boolean b) {
            // nothing to do
        }

        @Override
        public void setWriteLock(final boolean b) {
            // nothing to do
        }

        @Override
        public boolean getWriteLock() {
            return false;
        }

        @Override
        public boolean isLockSupported() {
            return false;
        }

        @Override
        public void setLock(final boolean b) throws LockException, ElementDeletedException {
            // nothing to do
        }

        @Override
        public void setLock(final boolean b, final boolean b1) throws LockException, ElementDeletedException {
            // nothing to do
        }

        @Override
        public boolean isLocked() {
            return false;
        }

        @Override
        public boolean isLockedOnServer(final boolean b) {
            return false;
        }

        @Override
        public void save() {
            // nothing to do
        }

        @Override
        public void save(final String s) {
            // nothing to do
        }

        @Override
        public void save(final String s, final boolean b) {
            // nothing to do
        }

        @Override
        public boolean hasTask() {
            return false;
        }

        @Override
        public Task getTask() {
            return null;
        }

        @Override
        public void setTask(final Task task) {
            // nothing to do
        }

        @Override
        public void removeTask() {
            // nothing to do
        }

        @Override
        public Color getColor() {
            return null;
        }

        @Override
        public void setColor(final Color color) {
            // nothing to do
        }

        @Override
        public void delete() throws LockException {
            // nothing to do
        }

        @Override
        public void refresh() {
            // nothing to do
        }

        @Override
        public String toXml() {
            return null;
        }

        @Override
        public String toXml(final boolean b) {
            return null;
        }

        @Override
        public String toXml(final boolean b, final boolean b1) {
            return null;
        }

        @Override
        public boolean isImportSupported() {
            return false;
        }

        @Override
        public boolean isExportSupported() {
            return false;
        }

        @Override
        public void exportStoreElement(final OutputStream outputStream, final ExportHandler exportHandler) throws IOException {
            // nothing to do
        }

        @Override
        public StoreElement importStoreElement(final ZipFile zipFile, final ImportHandler importHandler) throws IOException, ElementDeletedException, WorkflowLockException {
            return null;
        }

        @Override
        public Listable<StoreElement> importStoreElements(final ZipFile zipFile, final ImportHandler importHandler) throws IOException, ElementDeletedException, WorkflowLockException {
            return null;
        }

        @Override
        public String getElementType() {
            return null;
        }

        @Override
        public IDProvider getStoreElement(final long id) {
            return _storeElementsById.get(id);
        }

        @Override
        public IDProvider getStoreElement(final Long id) {
            return getStoreElement((long) id);
        }

        @Override
        public List<? extends IDProvider> getElements(final Collection<Long> collection) {
            return null;
        }

        @Override
        public IDProvider getStoreElement(final String s, final UidType uidType) {
            return null;
        }

        @Override
        public IDProvider getStoreElement(final String s, final String s1) {
            return null;
        }

        @Override
        public Project getProject() {
            return null;
        }

        @Override
        public ReferenceEntry[] getIncomingReferences() {
            return new ReferenceEntry[0];
        }

        @Override
        public boolean hasIncomingReferences() {
            return false;
        }

        @Override
        public ReferenceEntry[] getOutgoingReferences() {
            return new ReferenceEntry[0];
        }

        @Override
        public String getReferenceName() {
            return null;
        }

        @Override
        public Set<ReferenceEntry> getReferences() {
            return null;
        }

        @Override
        public boolean isDeleted() {
            return false;
        }

        @Override
        public UserService getUserService() {
            return null;
        }

        @Override
        public boolean isReadOnly() {
            return false;
        }

        @Override
        public Revision getMaxRevision() {
            return null;
        }

        @Override
        public long getId() {
            return _type.ordinal();
        }

        @Override
        public Long getLongID() {
            return getId();
        }

        @Override
        public Revision getRevision() {
            return null;
        }

        @Override
        public Revision getReleaseRevision() {
            return null;
        }

        @Override
        public IDProvider getInRevision(final Revision revision) {
            return null;
        }

        @Override
        public String getUid() {
            return null;
        }

        @Override
        public void setUid(final String s) {

        }

        @Override
        public UidType getUidType() {
            return null;
        }

        @Override
        public boolean hasUid() {
            return false;
        }

        @Override
        public LanguageInfo getLanguageInfo(final Language language) {
            return null;
        }

        @Override
        public void moveChild(final IDProvider idProvider) throws LockException, ElementMovedException {
            // nothing to do
        }

        @Override
        public void moveChild(final IDProvider idProvider, final int i) throws LockException, ElementMovedException {
            // nothing to do
        }

        @Override
        public String getDisplayName(final Language language) {
            return null;
        }

        @Override
        public void setDisplayName(final Language language, final String s) {
            // nothing to do
        }

        @Override
        public boolean isReleaseSupported() {
            return false;
        }

        @Override
        public int getReleaseStatus() {
            return 0;
        }

        @Override
        public boolean isReleased() {
            return false;
        }

        @Override
        public User getReleasedBy() {
            return null;
        }

        @Override
        public boolean isInReleaseStore() {
            return false;
        }

        @Override
        public void release() {
            // nothing to do
        }

        @Override
        public void release(final boolean b) {
            // nothing to do
        }

        @Override
        public String getName() {
            return _type.getName();
        }

        @Override
        public Listable<StoreElement> getChildren() {
            return null;
        }

        @Override
        public <T extends StoreElement> Listable<T> getChildren(final Class<T> aClass) {
            return null;
        }

        @Override
        public <T extends StoreElement> Listable<T> getChildren(final Class<T> aClass, final boolean b) {
            return null;
        }

        @Override
        public <T extends StoreElement> Listable<T> getChildren(final Filter.TypedFilter<T> typedFilter, final boolean b) {
            return null;
        }

        @Override
        public void appendChild(final StoreElement storeElement) {
            // nothing to do
        }

        @Override
        public void appendChildBefore(final StoreElement storeElement, final StoreElement storeElement1) {
            // nothing to do
        }

        @Override
        public void removeChild(final StoreElement storeElement) {
            // nothing to do
        }

        @Override
        public void replaceChild(final StoreElement storeElement, final StoreElement storeElement1) {
            // nothing to do
        }

        @Override
        public int getChildCount() {
            return 0;
        }

        @Override
        public int getChildIndex(final StoreElement storeElement) {
            return 0;
        }

        @Override
        public IDProvider getParent() {
            return null;
        }

        @Override
        public StoreElement getNextSibling() {
            return null;
        }

        @Override
        public StoreElement getFirstChild() {
            return null;
        }

        @Override
        public Store getStore() {
            return null;
        }

        @Override
        public Set<Contrast> contrastWith(final IDProvider idProvider) {
            return null;
        }

        @Override
        public void revert(final Revision revision, final boolean b, final EnumSet<RevertType> enumSet) throws LockException {
            // nothing to do
        }

        @Override
        public Data getMeta() {
            return null;
        }

        @Override
        public void setMeta(final Data data) {
            // nothing to do
        }

        @Override
        public boolean hasMeta() {
            return false;
        }

        @Override
        public FormData getMetaFormData() {
            return null;
        }

        @Override
        public void setMetaFormData(final FormData formData) {
            // nothing to do
        }

        @Override
        public List<Revision> getHistory() {
            return null;
        }

        @Override
        public List<Revision> getHistory(final Date date, final Date date1, final int i, final Filter<Revision> filter) {
            return null;
        }

        @Override
        public ElementProvider<Revision> asRevisionProvider() {
            return null;
        }

        @Override
        public int compareTo(@NotNull final StoreElement o) {
            return 0;
        }

        IDProvider getOrCreateElement(final long nodeId) {
            IDProvider storeElement = getStoreElement(nodeId);
            if (storeElement == null) {
                storeElement = new MockedStoreElement(nodeId, getName() + "_name_" + nodeId, getType() == Store.Type.TEMPLATESTORE ? null : getName() + "_uid_" + nodeId);
                addMockedStoreElement(storeElement);
            }
            return storeElement;
        }
    }

    private static class MockedStoreElement implements IDProvider {
        private long _id;
        private String _name;
        private String _uid;

        MockedStoreElement(final long id, final String name, final String uid) {
            _id = id;
            _name = name;
            _uid = uid;
        }

        @Override
        public long getId() {
            return _id;
        }

        @Override
        public Long getLongID() {
            return getId();
        }

        @Override
        public Revision getRevision() {
            return null;
        }

        @Override
        public Revision getReleaseRevision() {
            return null;
        }

        @Override
        public IDProvider getInRevision(final Revision revision) {
            return null;
        }

        @Override
        public String getUid() {
            return _uid;
        }

        @Override
        public void setUid(final String uid) {
            _uid = uid;
        }

        @Override
        public UidType getUidType() {
            return null;
        }

        @Override
        public boolean hasUid() {
            return _uid != null;
        }

        @Override
        public LanguageInfo getLanguageInfo(final Language language) {
            return null;
        }

        @Override
        public void moveChild(final IDProvider idProvider) throws LockException, ElementMovedException {

        }

        @Override
        public void moveChild(final IDProvider idProvider, final int i) throws LockException, ElementMovedException {

        }

        @Override
        public String getDisplayName(final Language language) {
            return _name;
        }

        @Override
        public void setDisplayName(final Language language, final String s) {

        }

        @Override
        public boolean isReleaseSupported() {
            return false;
        }

        @Override
        public int getReleaseStatus() {
            return 0;
        }

        @Override
        public boolean isReleased() {
            return false;
        }

        @Override
        public User getReleasedBy() {
            return null;
        }

        @Override
        public boolean isInReleaseStore() {
            return false;
        }

        @Override
        public void release() {

        }

        @Override
        public void release(final boolean b) {

        }

        @Override
        public String getName() {
            return _name;
        }

        @Override
        public Listable<StoreElement> getChildren() {
            return null;
        }

        @Override
        public <T extends StoreElement> Listable<T> getChildren(final Class<T> aClass) {
            return null;
        }

        @Override
        public <T extends StoreElement> Listable<T> getChildren(final Class<T> aClass, final boolean b) {
            return null;
        }

        @Override
        public <T extends StoreElement> Listable<T> getChildren(final Filter.TypedFilter<T> typedFilter, final boolean b) {
            return null;
        }

        @Override
        public void appendChild(final StoreElement storeElement) {

        }

        @Override
        public void appendChildBefore(final StoreElement storeElement, final StoreElement storeElement1) {

        }

        @Override
        public void removeChild(final StoreElement storeElement) {

        }

        @Override
        public void replaceChild(final StoreElement storeElement, final StoreElement storeElement1) {

        }

        @Override
        public int getChildCount() {
            return 0;
        }

        @Override
        public int getChildIndex(final StoreElement storeElement) {
            return 0;
        }

        @Override
        public IDProvider getParent() {
            return null;
        }

        @Override
        public StoreElement getNextSibling() {
            return null;
        }

        @Override
        public StoreElement getFirstChild() {
            return null;
        }

        @Override
        public Store getStore() {
            return null;
        }

        @Override
        public boolean isFolder() {
            return false;
        }

        @Override
        public boolean isPermissionSupported() {
            return false;
        }

        @Override
        public boolean hasPermissions() {
            return false;
        }

        @Override
        public Permission getPermission() {
            return null;
        }

        @Override
        public Permission getPermission(final User user) {
            return null;
        }

        @Override
        public Permission getPermission(final Group group) {
            return null;
        }

        @Override
        public void setPermission(final User user, final Permission permission) {

        }

        @Override
        public void setPermission(final User[] users, final Permission permission) {

        }

        @Override
        public void setPermission(final Group group, final Permission permission) {

        }

        @Override
        public void removePermission(final User user) {

        }

        @Override
        public void removePermission(final User[] users) {

        }

        @Override
        public void removePermission(final Group group) {

        }

        @Override
        public PermissionMap getTreePermission() {
            return null;
        }

        @Override
        public List<Principal> getDefinedPrincipalPermissions() {
            return null;
        }

        @Override
        public List<Principal> getInheritedPrincipalPermissions() {
            return null;
        }

        @Override
        public long getLastChanged() {
            return 0;
        }

        @Override
        public User getEditor() {
            return null;
        }

        @Override
        public boolean isWorkflowSupported() {
            return false;
        }

        @Override
        public WorkflowPermission[] getWorkflowPermissions() {
            return new WorkflowPermission[0];
        }

        @Override
        public WorkflowPermission getWorkflowPermission(final Workflow workflow) {
            return null;
        }

        @Override
        public WorkflowPermission getCreateWorkflowPermission(final Workflow workflow) {
            return null;
        }

        @Override
        public void setWorkflowPermission(final WorkflowPermission workflowPermission) {

        }

        @Override
        public void setWorkflowPermissions(final WorkflowPermission[] workflowPermissions) {

        }

        @Override
        public void removeWorkflowPermission(final Workflow workflow) {

        }

        @Override
        public void removeAllWorkflowPermissions() {

        }

        @Override
        public boolean isWorkflowAllowed(final Workflow workflow, final User user) {
            return false;
        }

        @Override
        public boolean inheritWorkflowPermission() {
            return false;
        }

        @Override
        public void setInheritWorkflowPermission(final boolean b) {

        }

        @Override
        public void setWriteLock(final boolean b) {

        }

        @Override
        public boolean getWriteLock() {
            return false;
        }

        @Override
        public boolean isLockSupported() {
            return false;
        }

        @Override
        public void setLock(final boolean b) throws LockException, ElementDeletedException {

        }

        @Override
        public void setLock(final boolean b, final boolean b1) throws LockException, ElementDeletedException {

        }

        @Override
        public boolean isLocked() {
            return false;
        }

        @Override
        public boolean isLockedOnServer(final boolean b) {
            return false;
        }

        @Override
        public void save() {

        }

        @Override
        public void save(final String s) {

        }

        @Override
        public void save(final String s, final boolean b) {

        }

        @Override
        public boolean hasTask() {
            return false;
        }

        @Override
        public Task getTask() {
            return null;
        }

        @Override
        public void setTask(final Task task) {

        }

        @Override
        public void removeTask() {

        }

        @Override
        public Color getColor() {
            return null;
        }

        @Override
        public void setColor(final Color color) {

        }

        @Override
        public void delete() throws LockException {

        }

        @Override
        public void refresh() {

        }

        @Override
        public String toXml() {
            return null;
        }

        @Override
        public String toXml(final boolean b) {
            return null;
        }

        @Override
        public String toXml(final boolean b, final boolean b1) {
            return null;
        }

        @Override
        public boolean isImportSupported() {
            return false;
        }

        @Override
        public boolean isExportSupported() {
            return false;
        }

        @Override
        public void exportStoreElement(final OutputStream outputStream, final ExportHandler exportHandler) throws IOException {

        }

        @Override
        public StoreElement importStoreElement(final ZipFile zipFile, final ImportHandler importHandler) throws IOException, ElementDeletedException, WorkflowLockException {
            return null;
        }

        @Override
        public Listable<StoreElement> importStoreElements(final ZipFile zipFile, final ImportHandler importHandler) throws IOException, ElementDeletedException, WorkflowLockException {
            return null;
        }

        @Override
        public String getElementType() {
            return null;
        }

        @Override
        public Project getProject() {
            return null;
        }

        @Override
        public ReferenceEntry[] getIncomingReferences() {
            return new ReferenceEntry[0];
        }

        @Override
        public boolean hasIncomingReferences() {
            return false;
        }

        @Override
        public ReferenceEntry[] getOutgoingReferences() {
            return new ReferenceEntry[0];
        }

        @Override
        public String getReferenceName() {
            return null;
        }

        @Override
        public Set<ReferenceEntry> getReferences() {
            return null;
        }

        @Override
        public boolean isDeleted() {
            return false;
        }

        @Override
        public Set<Contrast> contrastWith(final IDProvider idProvider) {
            return null;
        }

        @Override
        public void revert(final Revision revision, final boolean b, final EnumSet<RevertType> enumSet) throws LockException {

        }

        @Override
        public Data getMeta() {
            return null;
        }

        @Override
        public void setMeta(final Data data) {

        }

        @Override
        public boolean hasMeta() {
            return false;
        }

        @Override
        public FormData getMetaFormData() {
            return null;
        }

        @Override
        public void setMetaFormData(final FormData formData) {

        }

        @Override
        public List<Revision> getHistory() {
            return null;
        }

        @Override
        public List<Revision> getHistory(final Date date, final Date date1, final int i, final Filter<Revision> filter) {
            return null;
        }

        @Override
        public ElementProvider<Revision> asRevisionProvider() {
            return null;
        }

        @Override
        public int compareTo(@NotNull final StoreElement o) {
            return 0;
        }
    }
}