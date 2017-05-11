package com.espirit.moddev.cli.results.logging;

import de.espirit.firstspirit.access.store.Store;
import de.espirit.firstspirit.store.access.nexport.*;
import de.espirit.firstspirit.transport.PropertiesTransportOptions;
import org.junit.Test;

import java.util.*;

import static com.espirit.moddev.cli.results.logging.MockLogger.NEW_LINE;
import static org.junit.Assert.assertEquals;

public class AdvancedLoggerTest {

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
                final Map<Store.Type, List<ElementExportInfo>> storeElements = MockedElementExportInfo.createMapWithStoreElements();
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
                final Map<Store.Type, List<ElementExportInfo>> storeElements = MockedElementExportInfo.createMapWithStoreElements();
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
                final Map<Store.Type, List<ElementExportInfo>> storeElements = MockedElementExportInfo.createMapWithStoreElements();
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
                final Map<Store.Type, List<ElementExportInfo>> storeElements = MockedElementExportInfo.createMapWithStoreElements();
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
                final Map<Store.Type, List<ElementExportInfo>> storeElements = MockedElementExportInfo.createMapWithStoreElements();
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
            final Map<Store.Type, List<ElementExportInfo>> storeElements = MockedElementExportInfo.createMapWithStoreElements();
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
            AdvancedLogger.logStoreElements(logger, MockedElementExportInfo.createMapWithStoreElements());
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
            AdvancedLogger.logStoreElements(logger, MockedElementExportInfo.createMapWithStoreElements());
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
            exportInfo.setCreatedFileHandles(MockedElementExportInfo.createFileHandleCollection(exportInfo, 1));
            exportInfo.setUpdatedFileHandles(MockedElementExportInfo.createFileHandleCollection(exportInfo, 2));
            exportInfo.setDeletedFileHandles(MockedElementExportInfo.createFileHandleCollection(exportInfo, 3));
            exportInfo.setMovedFileHandles(MockedElementExportInfo.createMovedFileHandleCollection(exportInfo, 4));
            AdvancedLogger.logFileInfos(logger, exportInfo, "");
            assertEquals("Result does not match.", "", logger.toString());
        }
        {
            final MockLogger logger = new MockLogger(true);
            final MockedExportInfo exportInfo = new MockedExportInfo();
            exportInfo.setCreatedFileHandles(MockedElementExportInfo.createFileHandleCollection(exportInfo, 1));
            exportInfo.setUpdatedFileHandles(MockedElementExportInfo.createFileHandleCollection(exportInfo, 2));
            exportInfo.setDeletedFileHandles(MockedElementExportInfo.createFileHandleCollection(exportInfo, 3));
            exportInfo.setMovedFileHandles(MockedElementExportInfo.createMovedFileHandleCollection(exportInfo, 4));
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
            exportInfo.setCreatedFileHandles(MockedElementExportInfo.createFileHandleCollection(exportInfo, 1));
            exportInfo.setUpdatedFileHandles(MockedElementExportInfo.createFileHandleCollection(exportInfo, 2));
            exportInfo.setDeletedFileHandles(MockedElementExportInfo.createFileHandleCollection(exportInfo, 3));
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
            exportInfo.setUpdatedFileHandles(MockedElementExportInfo.createFileHandleCollection(exportInfo, 2));
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
            AdvancedLogger.logFileHandles(logger, MockedElementExportInfo.createFileHandleCollection(new MockedExportInfo(), 0), "description", "");
            assertEquals("Result does not match.", "", logger.toString());
        }
        {
            final MockLogger logger = new MockLogger(true);
            AdvancedLogger.logFileHandles(logger, MockedElementExportInfo.createFileHandleCollection(new MockedExportInfo(), 1), "myDescription", "");
            // @formatter:off
            final String expected = "[DEBUG]   - myDescription: 1"      + NEW_LINE +
                                    "[DEBUG]    - /path/testName/0.txt" + NEW_LINE;
            // @formatter:on
            assertEquals("Result does not match.", expected, logger.toString());
        }
        {
            // debug disabled
            final MockLogger logger = new MockLogger();
            AdvancedLogger.logFileHandles(logger, MockedElementExportInfo.createFileHandleCollection(new MockedExportInfo(), 1), "myDescription", "");
            // @formatter:off
            final String expected = "";
            // @formatter:on
            assertEquals("Result does not match.", expected, logger.toString());
        }
        {
            final MockLogger logger = new MockLogger(true);
            AdvancedLogger.logFileHandles(logger, MockedElementExportInfo.createFileHandleCollection(new MockedExportInfo(), 2), "myDescription", " ");
            // @formatter:off
            final String expected = "[DEBUG]    - myDescription: 2"         + NEW_LINE +
                                    "[DEBUG]     - /path/testName/0.txt"    + NEW_LINE +
                                    "[DEBUG]     - /path/testName/1.txt"    + NEW_LINE;
            // @formatter:on
            assertEquals("Result does not match.", expected, logger.toString());
        }
        {
            final MockLogger logger = new MockLogger(true);
            AdvancedLogger.logFileHandles(logger, MockedElementExportInfo.createFileHandleCollection(new MockedExportInfo(), 3), "myDescription", "  ");
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
            AdvancedLogger.logMovedFileHandles(logger, MockedElementExportInfo.createMovedFileHandleCollection(new MockedExportInfo(), 0), "");
            assertEquals("Result does not match.", "", logger.toString());
        }
        {
            final MockLogger logger = new MockLogger(true);
            AdvancedLogger.logMovedFileHandles(logger, MockedElementExportInfo.createMovedFileHandleCollection(new MockedExportInfo(), 1), "");
            // @formatter:off
            final String expected = "[DEBUG]   - Moved files: 1" + NEW_LINE +
                                    "[DEBUG]    - 0.txt ( from '/from/testName' to '/to/testName' )" + NEW_LINE;
            // @formatter:on
            assertEquals("Result does not match.", expected, logger.toString());
        }
        {
            final MockLogger logger = new MockLogger(true);
            AdvancedLogger.logMovedFileHandles(logger, MockedElementExportInfo.createMovedFileHandleCollection(new MockedExportInfo(), 2), " ");
            // @formatter:off
            final String expected = "[DEBUG]    - Moved files: 2"                                     + NEW_LINE +
                                    "[DEBUG]     - 0.txt ( from '/from/testName' to '/to/testName' )" + NEW_LINE +
                                    "[DEBUG]     - 1.txt ( from '/from/testName' to '/to/testName' )" + NEW_LINE;
            // @formatter:on
            assertEquals("Result does not match.", expected, logger.toString());
        }
        {
            final MockLogger logger = new MockLogger(true);
            AdvancedLogger.logMovedFileHandles(logger, MockedElementExportInfo.createMovedFileHandleCollection(new MockedExportInfo(), 3), "  ");
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
            AdvancedLogger.logMovedFileHandles(logger, MockedElementExportInfo.createMovedFileHandleCollection(new MockedExportInfo(), 3), "  ");
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
        exportInfo.setCreatedFileHandles(MockedElementExportInfo.createFileHandleCollection(exportInfo, 1));
        exportInfo.setUpdatedFileHandles(MockedElementExportInfo.createFileHandleCollection(exportInfo, 2));
        exportInfo.setDeletedFileHandles(MockedElementExportInfo.createFileHandleCollection(exportInfo, 3));
        exportInfo.setMovedFileHandles(MockedElementExportInfo.createMovedFileHandleCollection(exportInfo, 4));
        final String result = AdvancedLogger.getFilesStringForElement(exportInfo);
        final String expected = " ( created files: 1, updated files: " + exportInfo.getUpdatedFileHandles().size() + ", deleted files: " + exportInfo.getDeletedFileHandles().size() + ", moved files: " + exportInfo.getMovedFileHandles().size() + " )";
        assertEquals("Result does not match.", expected, result);
    }

    @Test
    public void testGetFilesStringForElementPartialPresent() throws Exception {
        final MockedExportInfo exportInfo = new MockedExportInfo(ExportInfo.Type.ELEMENT, "storeElement", ExportStatus.CREATED);
        exportInfo.setUpdatedFileHandles(MockedElementExportInfo.createFileHandleCollection(exportInfo, 2));
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

}