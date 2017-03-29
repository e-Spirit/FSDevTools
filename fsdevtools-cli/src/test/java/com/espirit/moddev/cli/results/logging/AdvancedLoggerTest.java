package com.espirit.moddev.cli.results.logging;

import de.espirit.common.util.Pair;
import de.espirit.firstspirit.access.database.BasicEntityInfo;
import de.espirit.firstspirit.access.database.BasicEntityInfoImpl;
import de.espirit.firstspirit.access.store.BasicElementInfo;
import de.espirit.firstspirit.access.store.Store;
import de.espirit.firstspirit.io.FileHandle;
import de.espirit.firstspirit.store.access.BasicElementInfoImpl;
import de.espirit.firstspirit.store.access.TagNames;
import de.espirit.firstspirit.store.access.nexport.*;
import de.espirit.firstspirit.store.access.nexport.io.ExportInfoFileHandle;
import de.espirit.firstspirit.store.access.nexport.io.ExportInfoFileHandleImpl;
import de.espirit.firstspirit.store.access.nexport.operations.ExportOperation;
import de.espirit.firstspirit.store.access.pagestore.PageImpl;
import de.espirit.firstspirit.transport.PropertiesTransportOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class AdvancedLoggerTest {

    private static final String NEW_LINE = "\n";

    @Test
    public void testLogEmptyResult() throws Exception {
        {
            final MockLogger logger = new MockLogger(false);
            AdvancedLogger.logResult(logger, new MockedResult(false));
            final String expected =
                    "[INFO] Export done."               + NEW_LINE +
                    "[INFO] == DETAILS =="              + NEW_LINE +
                    "[INFO] Created elements: 0"        + NEW_LINE +
                    "[INFO] Updated elements: 0"        + NEW_LINE +
                    "[INFO] Deleted elements: 0"        + NEW_LINE +
                    "[INFO]   Moved elements: 0"        + NEW_LINE +
                    "[INFO] == SUMMARY =="              + NEW_LINE +
                    "[INFO] Created elements: 0"        + NEW_LINE +
                    "[INFO] Updated elements: 0"        + NEW_LINE +
                    "[INFO] Deleted elements: 0"        + NEW_LINE +
                    "[INFO]   Moved elements: 0"        + NEW_LINE;

            // @formatter:on
            assertEquals("Result does not match.", expected, logger.toString());
        }
    }

    @Test
    public void testLogResult() throws Exception {
        {
            final MockLogger logger = new MockLogger(false);
            AdvancedLogger.logResult(logger, new MockedResult());
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
                                    "[INFO]   Moved elements: 9"        + NEW_LINE +
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
            AdvancedLogger.logResult(logger, new MockedResult());
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
                                    "[INFO]   Moved elements: 9"        + NEW_LINE +
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
            AdvancedLogger.logStoreElements(logger, Collections.emptyMap());
            assertEquals("Result does not match.", "", logger.toString());
        }
        {
            final MockLogger logger = new MockLogger(false);
            AdvancedLogger.logStoreElements(logger, createMapWithStoreElements());
            // @formatter:off
            final String expected = "[INFO] - store elements: 10"   + NEW_LINE +
                                    "[INFO]  - pagestore: 1"        + NEW_LINE +
                                    "[INFO]   - Page: 'first'                       ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]  - mediastore: 2"       + NEW_LINE +
                                    "[INFO]   - Media: 'first'                      ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]   - Media: 'second'                     ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]  - sitestore: 3"        + NEW_LINE +
                                    "[INFO]   - PageRef: 'first'                    ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]   - PageRef: 'second'                   ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]   - PageRef: 'third'                    ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )" + NEW_LINE +
                                    "[INFO]  - templatestore: 4"    + NEW_LINE +
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
        final String expected = " ( )";
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

        public MockLogger() {
            this(false);
        }

        public MockLogger(final boolean debugEnabled) {
            _debugEnabled = debugEnabled;
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
            return true;
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

    private static class MockedResult implements ExportOperation.Result {

        private Collection<ExportInfo> _createdElements, _updateElements, _deletedElements, _movedElements;

        MockedResult() {
            this(true);
        }

        MockedResult(boolean fill) {
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

}