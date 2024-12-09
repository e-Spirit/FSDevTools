/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2024 Crownpeak Technology GmbH
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

import de.espirit.firstspirit.access.store.ElementInfo;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.Store;
import de.espirit.firstspirit.access.store.pagestore.Content2Section;
import de.espirit.firstspirit.access.store.pagestore.Page;
import de.espirit.firstspirit.access.store.pagestore.Section;
import de.espirit.firstspirit.access.store.pagestore.SectionReference;
import de.espirit.firstspirit.access.store.templatestore.PageTemplate;
import de.espirit.firstspirit.access.store.templatestore.SectionTemplate;
import de.espirit.firstspirit.access.store.templatestore.Template;
import de.espirit.firstspirit.agency.StoreAgent;
import de.espirit.firstspirit.store.access.nexport.*;
import de.espirit.firstspirit.transport.PropertiesTransportOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.*;

import static com.espirit.moddev.cli.results.logging.MockLogger.NEW_LINE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AdvancedLoggerTest {

	@Test
	public void testInfoLevelDisabled() {
		final MockLogger logger = new MockLogger(false);
		logger.setInfoEnabled(false);
		AdvancedLogger.logExportResult(logger, null, new MockedExportResult(true));
		assertEquals("", logger.toString());
		AdvancedLogger.logImportResult(logger, null, new MockedImportResult(true));
		assertEquals("", logger.toString());
	}

	@Test
	public void testLogEmptyExportResult() {
		{
			final MockLogger logger = new MockLogger(false);
			AdvancedLogger.logExportResult(logger, null, new MockedExportResult(false));
			final String expected = """
					[INFO] Export done.
					[INFO] == DETAILS ==
					[INFO] Created elements: 0
					[INFO] Updated elements: 0
					[INFO] Deleted elements: 0
					[INFO] Moved elements: 0
					[INFO] == SUMMARY ==
					[INFO] Created elements: 0
					[INFO] Updated elements: 0
					[INFO] Deleted elements: 0
					[INFO]   Moved elements: 0
					""";
			assertEquals(expected, logger.toString());
		}
	}

	@Test
	public void testLogEmptyImportResult() {
		{
			final MockLogger logger = new MockLogger(true);
			AdvancedLogger.logImportResult(logger, null, new MockedImportResult(false));
			final String expected = """
					[INFO] Import done.
					[INFO] == DETAILS ==
					[INFO] Created elements: 0
					[INFO] Updated elements: 0
					[INFO] Deleted elements: 0
					[INFO] Moved elements: 0
					[INFO] L&Found elements: 0
					[INFO] Problems: 0
					[INFO] == SUMMARY ==
					[INFO] Created elements: 0
					[INFO] Updated elements: 0
					[INFO] Deleted elements: 0
					[INFO]   Moved elements: 0
					[INFO] L&Found elements: 0
					[INFO]         Problems: 0
					""";
			assertEquals(expected, logger.toString());
		}
	}

	@Test
	public void testGetStoreElementIdentifier() {
		{
			// SectionTemplate
			final var identifier = storeElementIdentifier(Store.Type.TEMPLATESTORE, TagNames.TEMPLATE, mock(SectionTemplate.class));
			assertEquals(SectionTemplate.class.getSimpleName(), identifier);
		}

		{
			// PageTemplate
			final var identifier = storeElementIdentifier(Store.Type.TEMPLATESTORE, TagNames.TEMPLATE, mock(PageTemplate.class));
			assertEquals(PageTemplate.class.getSimpleName(), identifier);
		}

		{
			// Content2Section
			final var identifier = storeElementIdentifier(Store.Type.TEMPLATESTORE, TagNames.SECTION, mock(Content2Section.class));
			assertEquals(Section.class.getSimpleName(), identifier);
		}

		{
			// SectionReference
			final var identifier = storeElementIdentifier(Store.Type.TEMPLATESTORE, TagNames.SECTION, mock(SectionReference.class));
			assertEquals(Section.class.getSimpleName(), identifier);
		}

		{
			// Section
			final var identifier = storeElementIdentifier(Store.Type.TEMPLATESTORE, TagNames.SECTION, mock(Section.class));
			assertEquals(Section.class.getSimpleName(), identifier);
		}

		{
			// Normal mode (e.g. Page)
			final var identifier = storeElementIdentifier(Store.Type.PAGESTORE, TagNames.PAGE, mock(Content2Section.class));
			assertEquals(Page.class.getSimpleName(), identifier);
		}

		{
			// template is unknown in Store --> fallback to "Template"
			final var identifier = storeElementIdentifier(Store.Type.TEMPLATESTORE, TagNames.TEMPLATE, null);
			assertEquals(Template.class.getSimpleName(), identifier);
		}
	}

	private @NotNull String storeElementIdentifier(final @NotNull Store.Type storeType, final @NotNull TagNames tagName, @Nullable IDProvider element) {
		final var store = mock(Store.class);
		when(store.getStoreElement(anyLong())).thenReturn(element);

		final var storeAgent = mock(StoreAgent.class);
		when(storeAgent.getStore(storeType)).thenReturn(store);

		final var elementInfo = mock(ElementInfo.class);
		when(elementInfo.getStoreType()).thenReturn(storeType);
		when(elementInfo.getNodeTag()).thenReturn(tagName.getName());

		final var testElement = mock(ElementExportInfo.class);
		when(testElement.getElementInfo()).thenReturn(elementInfo);

		return AdvancedLogger.getStoreElementIdentifier(storeAgent, testElement);
	}

	@Test
	public void testLogExportResult() {
		{
			final MockLogger logger = new MockLogger(false);
			AdvancedLogger.logExportResult(logger, null, new MockedExportResult());
			final String expected = """
					[INFO] Export done.
					[INFO] == DETAILS ==
					[INFO] Created elements: 7
					[INFO] - project properties: 1
					[INFO]  - Users                                ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO] - store elements: 5
					[INFO]  - pagestore: 1
					[INFO]   - Page: 'first'                       ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]  - templatestore: 4
					[INFO]   - PageTemplate: 'first'               ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]   - PageTemplate: 'fourth'              ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]   - Workflow: 'second'                  ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]   - LinkTemplate: 'third'               ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO] - entity types: 1                       ( schemas: 1, entities: 1 )
					[INFO]  - Schema: 'createdSchema'              ( entity types: 1, entities: 1 )
					[INFO]   - EntityType: 'createdType'           ( entities: 1 )
					[INFO] Updated elements: 10
					[INFO] - project properties: 1
					[INFO]  - Users                                ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO] - store elements: 6
					[INFO]  - mediastore: 3
					[INFO]   - Media: 'first'                      ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]   - Media: 'second'                     ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]   - MediaFolder: 'third'                ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]  - sitestore: 3
					[INFO]   - PageRef: 'first'                    ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]   - PageRef: 'second'                   ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]   - PageRef: 'third'                    ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO] - entity types: 3                       ( schemas: 2, entities: 6 )
					[INFO]  - Schema: 'updatedSchema1'             ( entity types: 2, entities: 5 )
					[INFO]   - EntityType: 'updatedType1'          ( entities: 2 )
					[INFO]   - EntityType: 'updatedType2'          ( entities: 3 )
					[INFO]  - Schema: 'updatedSchema2'             ( entity types: 1, entities: 1 )
					[INFO]   - EntityType: 'updatedType1'          ( entities: 1 )
					[INFO] Deleted elements: 7
					[INFO] - project properties: 1
					[INFO]  - Users                                ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO] - store elements: 4
					[INFO]  - pagestore: 1
					[INFO]   - Page: 'first'                       ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]  - sitestore: 3
					[INFO]   - PageRef: 'first'                    ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]   - PageRef: 'second'                   ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]   - PageRef: 'third'                    ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO] - entity types: 2                       ( schemas: 1, entities: 7 )
					[INFO]  - Schema: 'deletedSchema'              ( entity types: 2, entities: 7 )
					[INFO]   - EntityType: 'deletedType1'          ( entities: 3 )
					[INFO]   - EntityType: 'deletedType2'          ( entities: 4 )
					[INFO] Moved elements: 10
					[INFO] - project properties: 1
					[INFO]  - Users                                ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO] - store elements: 7
					[INFO]  - mediastore: 3
					[INFO]   - Media: 'first'                      ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]   - Media: 'second'                     ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]   - MediaFolder: 'third'                ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]  - templatestore: 4
					[INFO]   - PageTemplate: 'first'               ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]   - PageTemplate: 'fourth'              ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]   - Workflow: 'second'                  ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]   - LinkTemplate: 'third'               ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO] - entity types: 2                       ( schemas: 2, entities: 3 )
					[INFO]  - Schema: 'movedSchema1'               ( entity types: 1, entities: 1 )
					[INFO]   - EntityType: 'movedType1'            ( entities: 1 )
					[INFO]  - Schema: 'movedSchema2'               ( entity types: 1, entities: 2 )
					[INFO]   - EntityType: 'movedType2'            ( entities: 2 )
					[INFO] == SUMMARY ==
					[INFO] Created elements: 7 | project properties: 1 | store elements: 5 ( pagestore: 1, templatestore: 4 ) | entity types: 1 ( schemas: 1, entities: 1 )
					[INFO] Updated elements: 10 | project properties: 1 | store elements: 6 ( mediastore: 3, sitestore: 3 ) | entity types: 3 ( schemas: 2, entities: 6 )
					[INFO] Deleted elements: 7 | project properties: 1 | store elements: 4 ( pagestore: 1, sitestore: 3 ) | entity types: 2 ( schemas: 1, entities: 7 )
					[INFO]   Moved elements: 10 | project properties: 1 | store elements: 7 ( mediastore: 3, templatestore: 4 ) | entity types: 2 ( schemas: 2, entities: 3 )
					""";
			assertEquals(expected, logger.toString());
		}
		{
			final MockLogger logger = new MockLogger(true);
			AdvancedLogger.logExportResult(logger, null, new MockedExportResult());
			final String expected = """
					[INFO] Export done.
					[INFO] == DETAILS ==
					[INFO] Created elements: 7
					[INFO] - project properties: 1
					[INFO]  - Users                                ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]   - Created files: 1
					[DEBUG]    - /path/USERS/0.txt
					[DEBUG]   - Updated files: 2
					[DEBUG]    - /path/USERS/0.txt
					[DEBUG]    - /path/USERS/1.txt
					[DEBUG]   - Deleted files: 3
					[DEBUG]    - /path/USERS/0.txt
					[DEBUG]    - /path/USERS/1.txt
					[DEBUG]    - /path/USERS/2.txt
					[DEBUG]   - Moved files: 4
					[DEBUG]    - 0.txt ( from '/from/USERS' to '/to/USERS' )
					[DEBUG]    - 1.txt ( from '/from/USERS' to '/to/USERS' )
					[DEBUG]    - 2.txt ( from '/from/USERS' to '/to/USERS' )
					[DEBUG]    - 3.txt ( from '/from/USERS' to '/to/USERS' )
					[INFO] - store elements: 5
					[INFO]  - pagestore: 1
					[INFO]   - Page: 'first'                       ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/first/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/first/0.txt
					[DEBUG]     - /path/first/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/first/0.txt
					[DEBUG]     - /path/first/1.txt
					[DEBUG]     - /path/first/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 1.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 2.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 3.txt ( from '/from/first' to '/to/first' )
					[INFO]  - templatestore: 4
					[INFO]   - PageTemplate: 'first'               ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/first/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/first/0.txt
					[DEBUG]     - /path/first/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/first/0.txt
					[DEBUG]     - /path/first/1.txt
					[DEBUG]     - /path/first/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 1.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 2.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 3.txt ( from '/from/first' to '/to/first' )
					[INFO]   - PageTemplate: 'fourth'              ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/fourth/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/fourth/0.txt
					[DEBUG]     - /path/fourth/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/fourth/0.txt
					[DEBUG]     - /path/fourth/1.txt
					[DEBUG]     - /path/fourth/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/fourth' to '/to/fourth' )
					[DEBUG]     - 1.txt ( from '/from/fourth' to '/to/fourth' )
					[DEBUG]     - 2.txt ( from '/from/fourth' to '/to/fourth' )
					[DEBUG]     - 3.txt ( from '/from/fourth' to '/to/fourth' )
					[INFO]   - Workflow: 'second'                  ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/second/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/second/0.txt
					[DEBUG]     - /path/second/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/second/0.txt
					[DEBUG]     - /path/second/1.txt
					[DEBUG]     - /path/second/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/second' to '/to/second' )
					[DEBUG]     - 1.txt ( from '/from/second' to '/to/second' )
					[DEBUG]     - 2.txt ( from '/from/second' to '/to/second' )
					[DEBUG]     - 3.txt ( from '/from/second' to '/to/second' )
					[INFO]   - LinkTemplate: 'third'               ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/third/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/third/0.txt
					[DEBUG]     - /path/third/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/third/0.txt
					[DEBUG]     - /path/third/1.txt
					[DEBUG]     - /path/third/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/third' to '/to/third' )
					[DEBUG]     - 1.txt ( from '/from/third' to '/to/third' )
					[DEBUG]     - 2.txt ( from '/from/third' to '/to/third' )
					[DEBUG]     - 3.txt ( from '/from/third' to '/to/third' )
					[INFO] - entity types: 1                       ( schemas: 1, entities: 1 )
					[INFO]  - Schema: 'createdSchema'              ( entity types: 1, entities: 1 )
					[INFO]   - EntityType: 'createdType'           ( entities: 1 )
					[DEBUG]     - Created files: 1
					[DEBUG]      - /path/createdSchema#createdType/0.txt
					[DEBUG]     - Updated files: 2
					[DEBUG]      - /path/createdSchema#createdType/0.txt
					[DEBUG]      - /path/createdSchema#createdType/1.txt
					[DEBUG]     - Deleted files: 3
					[DEBUG]      - /path/createdSchema#createdType/0.txt
					[DEBUG]      - /path/createdSchema#createdType/1.txt
					[DEBUG]      - /path/createdSchema#createdType/2.txt
					[DEBUG]     - Moved files: 4
					[DEBUG]      - 0.txt ( from '/from/createdSchema#createdType' to '/to/createdSchema#createdType' )
					[DEBUG]      - 1.txt ( from '/from/createdSchema#createdType' to '/to/createdSchema#createdType' )
					[DEBUG]      - 2.txt ( from '/from/createdSchema#createdType' to '/to/createdSchema#createdType' )
					[DEBUG]      - 3.txt ( from '/from/createdSchema#createdType' to '/to/createdSchema#createdType' )
					[INFO] Updated elements: 10
					[INFO] - project properties: 1
					[INFO]  - Users                                ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]   - Created files: 1
					[DEBUG]    - /path/USERS/0.txt
					[DEBUG]   - Updated files: 2
					[DEBUG]    - /path/USERS/0.txt
					[DEBUG]    - /path/USERS/1.txt
					[DEBUG]   - Deleted files: 3
					[DEBUG]    - /path/USERS/0.txt
					[DEBUG]    - /path/USERS/1.txt
					[DEBUG]    - /path/USERS/2.txt
					[DEBUG]   - Moved files: 4
					[DEBUG]    - 0.txt ( from '/from/USERS' to '/to/USERS' )
					[DEBUG]    - 1.txt ( from '/from/USERS' to '/to/USERS' )
					[DEBUG]    - 2.txt ( from '/from/USERS' to '/to/USERS' )
					[DEBUG]    - 3.txt ( from '/from/USERS' to '/to/USERS' )
					[INFO] - store elements: 6
					[INFO]  - mediastore: 3
					[INFO]   - Media: 'first'                      ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/first/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/first/0.txt
					[DEBUG]     - /path/first/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/first/0.txt
					[DEBUG]     - /path/first/1.txt
					[DEBUG]     - /path/first/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 1.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 2.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 3.txt ( from '/from/first' to '/to/first' )
					[INFO]   - Media: 'second'                     ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/second/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/second/0.txt
					[DEBUG]     - /path/second/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/second/0.txt
					[DEBUG]     - /path/second/1.txt
					[DEBUG]     - /path/second/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/second' to '/to/second' )
					[DEBUG]     - 1.txt ( from '/from/second' to '/to/second' )
					[DEBUG]     - 2.txt ( from '/from/second' to '/to/second' )
					[DEBUG]     - 3.txt ( from '/from/second' to '/to/second' )
					[INFO]   - MediaFolder: 'third'                ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/third/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/third/0.txt
					[DEBUG]     - /path/third/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/third/0.txt
					[DEBUG]     - /path/third/1.txt
					[DEBUG]     - /path/third/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/third' to '/to/third' )
					[DEBUG]     - 1.txt ( from '/from/third' to '/to/third' )
					[DEBUG]     - 2.txt ( from '/from/third' to '/to/third' )
					[DEBUG]     - 3.txt ( from '/from/third' to '/to/third' )
					[INFO]  - sitestore: 3
					[INFO]   - PageRef: 'first'                    ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/first/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/first/0.txt
					[DEBUG]     - /path/first/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/first/0.txt
					[DEBUG]     - /path/first/1.txt
					[DEBUG]     - /path/first/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 1.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 2.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 3.txt ( from '/from/first' to '/to/first' )
					[INFO]   - PageRef: 'second'                   ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/second/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/second/0.txt
					[DEBUG]     - /path/second/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/second/0.txt
					[DEBUG]     - /path/second/1.txt
					[DEBUG]     - /path/second/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/second' to '/to/second' )
					[DEBUG]     - 1.txt ( from '/from/second' to '/to/second' )
					[DEBUG]     - 2.txt ( from '/from/second' to '/to/second' )
					[DEBUG]     - 3.txt ( from '/from/second' to '/to/second' )
					[INFO]   - PageRef: 'third'                    ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/third/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/third/0.txt
					[DEBUG]     - /path/third/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/third/0.txt
					[DEBUG]     - /path/third/1.txt
					[DEBUG]     - /path/third/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/third' to '/to/third' )
					[DEBUG]     - 1.txt ( from '/from/third' to '/to/third' )
					[DEBUG]     - 2.txt ( from '/from/third' to '/to/third' )
					[DEBUG]     - 3.txt ( from '/from/third' to '/to/third' )
					[INFO] - entity types: 3                       ( schemas: 2, entities: 6 )
					[INFO]  - Schema: 'updatedSchema1'             ( entity types: 2, entities: 5 )
					[INFO]   - EntityType: 'updatedType1'          ( entities: 2 )
					[DEBUG]     - Created files: 1
					[DEBUG]      - /path/updatedSchema1#updatedType1/0.txt
					[DEBUG]     - Updated files: 2
					[DEBUG]      - /path/updatedSchema1#updatedType1/0.txt
					[DEBUG]      - /path/updatedSchema1#updatedType1/1.txt
					[DEBUG]     - Deleted files: 3
					[DEBUG]      - /path/updatedSchema1#updatedType1/0.txt
					[DEBUG]      - /path/updatedSchema1#updatedType1/1.txt
					[DEBUG]      - /path/updatedSchema1#updatedType1/2.txt
					[DEBUG]     - Moved files: 4
					[DEBUG]      - 0.txt ( from '/from/updatedSchema1#updatedType1' to '/to/updatedSchema1#updatedType1' )
					[DEBUG]      - 1.txt ( from '/from/updatedSchema1#updatedType1' to '/to/updatedSchema1#updatedType1' )
					[DEBUG]      - 2.txt ( from '/from/updatedSchema1#updatedType1' to '/to/updatedSchema1#updatedType1' )
					[DEBUG]      - 3.txt ( from '/from/updatedSchema1#updatedType1' to '/to/updatedSchema1#updatedType1' )
					[INFO]   - EntityType: 'updatedType2'          ( entities: 3 )
					[DEBUG]     - Created files: 1
					[DEBUG]      - /path/updatedSchema1#updatedType2/0.txt
					[DEBUG]     - Updated files: 2
					[DEBUG]      - /path/updatedSchema1#updatedType2/0.txt
					[DEBUG]      - /path/updatedSchema1#updatedType2/1.txt
					[DEBUG]     - Deleted files: 3
					[DEBUG]      - /path/updatedSchema1#updatedType2/0.txt
					[DEBUG]      - /path/updatedSchema1#updatedType2/1.txt
					[DEBUG]      - /path/updatedSchema1#updatedType2/2.txt
					[DEBUG]     - Moved files: 4
					[DEBUG]      - 0.txt ( from '/from/updatedSchema1#updatedType2' to '/to/updatedSchema1#updatedType2' )
					[DEBUG]      - 1.txt ( from '/from/updatedSchema1#updatedType2' to '/to/updatedSchema1#updatedType2' )
					[DEBUG]      - 2.txt ( from '/from/updatedSchema1#updatedType2' to '/to/updatedSchema1#updatedType2' )
					[DEBUG]      - 3.txt ( from '/from/updatedSchema1#updatedType2' to '/to/updatedSchema1#updatedType2' )
					[INFO]  - Schema: 'updatedSchema2'             ( entity types: 1, entities: 1 )
					[INFO]   - EntityType: 'updatedType1'          ( entities: 1 )
					[DEBUG]     - Created files: 1
					[DEBUG]      - /path/updatedSchema2#updatedType1/0.txt
					[DEBUG]     - Updated files: 2
					[DEBUG]      - /path/updatedSchema2#updatedType1/0.txt
					[DEBUG]      - /path/updatedSchema2#updatedType1/1.txt
					[DEBUG]     - Deleted files: 3
					[DEBUG]      - /path/updatedSchema2#updatedType1/0.txt
					[DEBUG]      - /path/updatedSchema2#updatedType1/1.txt
					[DEBUG]      - /path/updatedSchema2#updatedType1/2.txt
					[DEBUG]     - Moved files: 4
					[DEBUG]      - 0.txt ( from '/from/updatedSchema2#updatedType1' to '/to/updatedSchema2#updatedType1' )
					[DEBUG]      - 1.txt ( from '/from/updatedSchema2#updatedType1' to '/to/updatedSchema2#updatedType1' )
					[DEBUG]      - 2.txt ( from '/from/updatedSchema2#updatedType1' to '/to/updatedSchema2#updatedType1' )
					[DEBUG]      - 3.txt ( from '/from/updatedSchema2#updatedType1' to '/to/updatedSchema2#updatedType1' )
					[INFO] Deleted elements: 7
					[INFO] - project properties: 1
					[INFO]  - Users                                ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]   - Created files: 1
					[DEBUG]    - /path/USERS/0.txt
					[DEBUG]   - Updated files: 2
					[DEBUG]    - /path/USERS/0.txt
					[DEBUG]    - /path/USERS/1.txt
					[DEBUG]   - Deleted files: 3
					[DEBUG]    - /path/USERS/0.txt
					[DEBUG]    - /path/USERS/1.txt
					[DEBUG]    - /path/USERS/2.txt
					[DEBUG]   - Moved files: 4
					[DEBUG]    - 0.txt ( from '/from/USERS' to '/to/USERS' )
					[DEBUG]    - 1.txt ( from '/from/USERS' to '/to/USERS' )
					[DEBUG]    - 2.txt ( from '/from/USERS' to '/to/USERS' )
					[DEBUG]    - 3.txt ( from '/from/USERS' to '/to/USERS' )
					[INFO] - store elements: 4
					[INFO]  - pagestore: 1
					[INFO]   - Page: 'first'                       ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/first/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/first/0.txt
					[DEBUG]     - /path/first/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/first/0.txt
					[DEBUG]     - /path/first/1.txt
					[DEBUG]     - /path/first/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 1.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 2.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 3.txt ( from '/from/first' to '/to/first' )
					[INFO]  - sitestore: 3
					[INFO]   - PageRef: 'first'                    ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/first/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/first/0.txt
					[DEBUG]     - /path/first/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/first/0.txt
					[DEBUG]     - /path/first/1.txt
					[DEBUG]     - /path/first/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 1.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 2.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 3.txt ( from '/from/first' to '/to/first' )
					[INFO]   - PageRef: 'second'                   ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/second/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/second/0.txt
					[DEBUG]     - /path/second/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/second/0.txt
					[DEBUG]     - /path/second/1.txt
					[DEBUG]     - /path/second/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/second' to '/to/second' )
					[DEBUG]     - 1.txt ( from '/from/second' to '/to/second' )
					[DEBUG]     - 2.txt ( from '/from/second' to '/to/second' )
					[DEBUG]     - 3.txt ( from '/from/second' to '/to/second' )
					[INFO]   - PageRef: 'third'                    ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/third/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/third/0.txt
					[DEBUG]     - /path/third/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/third/0.txt
					[DEBUG]     - /path/third/1.txt
					[DEBUG]     - /path/third/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/third' to '/to/third' )
					[DEBUG]     - 1.txt ( from '/from/third' to '/to/third' )
					[DEBUG]     - 2.txt ( from '/from/third' to '/to/third' )
					[DEBUG]     - 3.txt ( from '/from/third' to '/to/third' )
					[INFO] - entity types: 2                       ( schemas: 1, entities: 7 )
					[INFO]  - Schema: 'deletedSchema'              ( entity types: 2, entities: 7 )
					[INFO]   - EntityType: 'deletedType1'          ( entities: 3 )
					[DEBUG]     - Created files: 1
					[DEBUG]      - /path/deletedSchema#deletedType1/0.txt
					[DEBUG]     - Updated files: 2
					[DEBUG]      - /path/deletedSchema#deletedType1/0.txt
					[DEBUG]      - /path/deletedSchema#deletedType1/1.txt
					[DEBUG]     - Deleted files: 3
					[DEBUG]      - /path/deletedSchema#deletedType1/0.txt
					[DEBUG]      - /path/deletedSchema#deletedType1/1.txt
					[DEBUG]      - /path/deletedSchema#deletedType1/2.txt
					[DEBUG]     - Moved files: 4
					[DEBUG]      - 0.txt ( from '/from/deletedSchema#deletedType1' to '/to/deletedSchema#deletedType1' )
					[DEBUG]      - 1.txt ( from '/from/deletedSchema#deletedType1' to '/to/deletedSchema#deletedType1' )
					[DEBUG]      - 2.txt ( from '/from/deletedSchema#deletedType1' to '/to/deletedSchema#deletedType1' )
					[DEBUG]      - 3.txt ( from '/from/deletedSchema#deletedType1' to '/to/deletedSchema#deletedType1' )
					[INFO]   - EntityType: 'deletedType2'          ( entities: 4 )
					[DEBUG]     - Created files: 1
					[DEBUG]      - /path/deletedSchema#deletedType2/0.txt
					[DEBUG]     - Updated files: 2
					[DEBUG]      - /path/deletedSchema#deletedType2/0.txt
					[DEBUG]      - /path/deletedSchema#deletedType2/1.txt
					[DEBUG]     - Deleted files: 3
					[DEBUG]      - /path/deletedSchema#deletedType2/0.txt
					[DEBUG]      - /path/deletedSchema#deletedType2/1.txt
					[DEBUG]      - /path/deletedSchema#deletedType2/2.txt
					[DEBUG]     - Moved files: 4
					[DEBUG]      - 0.txt ( from '/from/deletedSchema#deletedType2' to '/to/deletedSchema#deletedType2' )
					[DEBUG]      - 1.txt ( from '/from/deletedSchema#deletedType2' to '/to/deletedSchema#deletedType2' )
					[DEBUG]      - 2.txt ( from '/from/deletedSchema#deletedType2' to '/to/deletedSchema#deletedType2' )
					[DEBUG]      - 3.txt ( from '/from/deletedSchema#deletedType2' to '/to/deletedSchema#deletedType2' )
					[INFO] Moved elements: 10
					[INFO] - project properties: 1
					[INFO]  - Users                                ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]   - Created files: 1
					[DEBUG]    - /path/USERS/0.txt
					[DEBUG]   - Updated files: 2
					[DEBUG]    - /path/USERS/0.txt
					[DEBUG]    - /path/USERS/1.txt
					[DEBUG]   - Deleted files: 3
					[DEBUG]    - /path/USERS/0.txt
					[DEBUG]    - /path/USERS/1.txt
					[DEBUG]    - /path/USERS/2.txt
					[DEBUG]   - Moved files: 4
					[DEBUG]    - 0.txt ( from '/from/USERS' to '/to/USERS' )
					[DEBUG]    - 1.txt ( from '/from/USERS' to '/to/USERS' )
					[DEBUG]    - 2.txt ( from '/from/USERS' to '/to/USERS' )
					[DEBUG]    - 3.txt ( from '/from/USERS' to '/to/USERS' )
					[INFO] - store elements: 7
					[INFO]  - mediastore: 3
					[INFO]   - Media: 'first'                      ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/first/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/first/0.txt
					[DEBUG]     - /path/first/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/first/0.txt
					[DEBUG]     - /path/first/1.txt
					[DEBUG]     - /path/first/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 1.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 2.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 3.txt ( from '/from/first' to '/to/first' )
					[INFO]   - Media: 'second'                     ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/second/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/second/0.txt
					[DEBUG]     - /path/second/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/second/0.txt
					[DEBUG]     - /path/second/1.txt
					[DEBUG]     - /path/second/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/second' to '/to/second' )
					[DEBUG]     - 1.txt ( from '/from/second' to '/to/second' )
					[DEBUG]     - 2.txt ( from '/from/second' to '/to/second' )
					[DEBUG]     - 3.txt ( from '/from/second' to '/to/second' )
					[INFO]   - MediaFolder: 'third'                ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/third/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/third/0.txt
					[DEBUG]     - /path/third/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/third/0.txt
					[DEBUG]     - /path/third/1.txt
					[DEBUG]     - /path/third/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/third' to '/to/third' )
					[DEBUG]     - 1.txt ( from '/from/third' to '/to/third' )
					[DEBUG]     - 2.txt ( from '/from/third' to '/to/third' )
					[DEBUG]     - 3.txt ( from '/from/third' to '/to/third' )
					[INFO]  - templatestore: 4
					[INFO]   - PageTemplate: 'first'               ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/first/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/first/0.txt
					[DEBUG]     - /path/first/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/first/0.txt
					[DEBUG]     - /path/first/1.txt
					[DEBUG]     - /path/first/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 1.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 2.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 3.txt ( from '/from/first' to '/to/first' )
					[INFO]   - PageTemplate: 'fourth'              ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/fourth/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/fourth/0.txt
					[DEBUG]     - /path/fourth/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/fourth/0.txt
					[DEBUG]     - /path/fourth/1.txt
					[DEBUG]     - /path/fourth/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/fourth' to '/to/fourth' )
					[DEBUG]     - 1.txt ( from '/from/fourth' to '/to/fourth' )
					[DEBUG]     - 2.txt ( from '/from/fourth' to '/to/fourth' )
					[DEBUG]     - 3.txt ( from '/from/fourth' to '/to/fourth' )
					[INFO]   - Workflow: 'second'                  ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/second/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/second/0.txt
					[DEBUG]     - /path/second/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/second/0.txt
					[DEBUG]     - /path/second/1.txt
					[DEBUG]     - /path/second/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/second' to '/to/second' )
					[DEBUG]     - 1.txt ( from '/from/second' to '/to/second' )
					[DEBUG]     - 2.txt ( from '/from/second' to '/to/second' )
					[DEBUG]     - 3.txt ( from '/from/second' to '/to/second' )
					[INFO]   - LinkTemplate: 'third'               ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/third/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/third/0.txt
					[DEBUG]     - /path/third/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/third/0.txt
					[DEBUG]     - /path/third/1.txt
					[DEBUG]     - /path/third/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/third' to '/to/third' )
					[DEBUG]     - 1.txt ( from '/from/third' to '/to/third' )
					[DEBUG]     - 2.txt ( from '/from/third' to '/to/third' )
					[DEBUG]     - 3.txt ( from '/from/third' to '/to/third' )
					[INFO] - entity types: 2                       ( schemas: 2, entities: 3 )
					[INFO]  - Schema: 'movedSchema1'               ( entity types: 1, entities: 1 )
					[INFO]   - EntityType: 'movedType1'            ( entities: 1 )
					[DEBUG]     - Created files: 1
					[DEBUG]      - /path/movedSchema1#movedType1/0.txt
					[DEBUG]     - Updated files: 2
					[DEBUG]      - /path/movedSchema1#movedType1/0.txt
					[DEBUG]      - /path/movedSchema1#movedType1/1.txt
					[DEBUG]     - Deleted files: 3
					[DEBUG]      - /path/movedSchema1#movedType1/0.txt
					[DEBUG]      - /path/movedSchema1#movedType1/1.txt
					[DEBUG]      - /path/movedSchema1#movedType1/2.txt
					[DEBUG]     - Moved files: 4
					[DEBUG]      - 0.txt ( from '/from/movedSchema1#movedType1' to '/to/movedSchema1#movedType1' )
					[DEBUG]      - 1.txt ( from '/from/movedSchema1#movedType1' to '/to/movedSchema1#movedType1' )
					[DEBUG]      - 2.txt ( from '/from/movedSchema1#movedType1' to '/to/movedSchema1#movedType1' )
					[DEBUG]      - 3.txt ( from '/from/movedSchema1#movedType1' to '/to/movedSchema1#movedType1' )
					[INFO]  - Schema: 'movedSchema2'               ( entity types: 1, entities: 2 )
					[INFO]   - EntityType: 'movedType2'            ( entities: 2 )
					[DEBUG]     - Created files: 1
					[DEBUG]      - /path/movedSchema2#movedType2/0.txt
					[DEBUG]     - Updated files: 2
					[DEBUG]      - /path/movedSchema2#movedType2/0.txt
					[DEBUG]      - /path/movedSchema2#movedType2/1.txt
					[DEBUG]     - Deleted files: 3
					[DEBUG]      - /path/movedSchema2#movedType2/0.txt
					[DEBUG]      - /path/movedSchema2#movedType2/1.txt
					[DEBUG]      - /path/movedSchema2#movedType2/2.txt
					[DEBUG]     - Moved files: 4
					[DEBUG]      - 0.txt ( from '/from/movedSchema2#movedType2' to '/to/movedSchema2#movedType2' )
					[DEBUG]      - 1.txt ( from '/from/movedSchema2#movedType2' to '/to/movedSchema2#movedType2' )
					[DEBUG]      - 2.txt ( from '/from/movedSchema2#movedType2' to '/to/movedSchema2#movedType2' )
					[DEBUG]      - 3.txt ( from '/from/movedSchema2#movedType2' to '/to/movedSchema2#movedType2' )
					[INFO] == SUMMARY ==
					[INFO] Created elements: 7 | project properties: 1 | store elements: 5 ( pagestore: 1, templatestore: 4 ) | entity types: 1 ( schemas: 1, entities: 1 )
					[INFO] Updated elements: 10 | project properties: 1 | store elements: 6 ( mediastore: 3, sitestore: 3 ) | entity types: 3 ( schemas: 2, entities: 6 )
					[INFO] Deleted elements: 7 | project properties: 1 | store elements: 4 ( pagestore: 1, sitestore: 3 ) | entity types: 2 ( schemas: 1, entities: 7 )
					[INFO]   Moved elements: 10 | project properties: 1 | store elements: 7 ( mediastore: 3, templatestore: 4 ) | entity types: 2 ( schemas: 2, entities: 3 )
					""";
			assertEquals(expected, logger.toString());
		}
	}

	@Test
	public void testLogImportResult() {
		{
			final MockLogger logger = new MockLogger(false);
			final MockedImportResult mockedImportResult = new MockedImportResult(true);
			AdvancedLogger.logImportResult(logger, mockedImportResult.getStoreAgent(), mockedImportResult);
			final String expected = """
					[INFO] Import done.
					[INFO] == DETAILS ==
					[INFO] Created elements: 12
					[INFO] - store elements: 7
					[INFO]  - pagestore: 1
					[INFO]   - Page: 'created_PAGE_1'            \s
					[INFO]  - mediastore: 2
					[INFO]   - Media: 'created_MEDIUM_2'         \s
					[INFO]   - Media: 'created_MEDIUM_3'         \s
					[INFO]  - templatestore: 4
					[INFO]   - FormatTemplate: 'created_FORMATTEMPLATE_6'
					[INFO]   - FormatTemplate: 'created_FORMATTEMPLATE_7'
					[INFO]   - LinkTemplate: 'created_LINKTEMPLATE_5'
					[INFO]   - PageTemplates: 'created_PAGETEMPLATES_4'
					[INFO] - entity types: 5                       ( schemas: 3, entities: 6 )
					[INFO]  - Schema: 'schema1'                    ( entity types: 2, entities: 3 )
					[INFO]   - EntityType: 'created_entityType2'   ( entities: 1 )
					[INFO]   - EntityType: 'created_entityType1'   ( entities: 2 )
					[INFO]  - Schema: 'schema2'                    ( entity types: 2, entities: 2 )
					[INFO]   - EntityType: 'created_entityType2'   ( entities: 1 )
					[INFO]   - EntityType: 'created_entityType1'   ( entities: 1 )
					[INFO]  - Schema: 'schema3'                    ( entity types: 1, entities: 1 )
					[INFO]   - EntityType: 'created_entityType1'   ( entities: 1 )
					[INFO] Updated elements: 22
					[INFO] - project properties: 10
					[INFO]  - Common                             \s
					[INFO]  - CustomProperties                   \s
					[INFO]  - Resolutions                        \s
					[INFO]  - Groups                             \s
					[INFO]  - ScheduleEntries                    \s
					[INFO]  - TemplateSets                       \s
					[INFO]  - Fonts                              \s
					[INFO]  - ModuleConfigurations               \s
					[INFO]  - Languages                          \s
					[INFO]  - Users                              \s
					[INFO] - store elements: 7
					[INFO]  - pagestore: 1
					[INFO]   - Page: 'updated_PAGE_1'            \s
					[INFO]  - mediastore: 2
					[INFO]   - Media: 'updated_MEDIUM_2'         \s
					[INFO]   - Media: 'updated_MEDIUM_3'         \s
					[INFO]  - templatestore: 4
					[INFO]   - FormatTemplate: 'updated_FORMATTEMPLATE_6'
					[INFO]   - FormatTemplate: 'updated_FORMATTEMPLATE_7'
					[INFO]   - LinkTemplate: 'updated_LINKTEMPLATE_5'
					[INFO]   - PageTemplates: 'updated_PAGETEMPLATES_4'
					[INFO] - entity types: 5                       ( schemas: 3, entities: 6 )
					[INFO]  - Schema: 'schema1'                    ( entity types: 2, entities: 3 )
					[INFO]   - EntityType: 'updated_entityType1'   ( entities: 2 )
					[INFO]   - EntityType: 'updated_entityType2'   ( entities: 1 )
					[INFO]  - Schema: 'schema2'                    ( entity types: 2, entities: 2 )
					[INFO]   - EntityType: 'updated_entityType1'   ( entities: 1 )
					[INFO]   - EntityType: 'updated_entityType2'   ( entities: 1 )
					[INFO]  - Schema: 'schema3'                    ( entity types: 1, entities: 1 )
					[INFO]   - EntityType: 'updated_entityType1'   ( entities: 1 )
					[INFO] Deleted elements: 7
					[INFO] - store elements: 7
					[INFO]  - pagestore: 1
					[INFO]   - Page: 'deleted_PAGE_1'            \s
					[INFO]  - mediastore: 2
					[INFO]   - Media: 'deleted_MEDIUM_2'         \s
					[INFO]   - Media: 'deleted_MEDIUM_3'         \s
					[INFO]  - templatestore: 4
					[INFO]   - FormatTemplate: 'deleted_FORMATTEMPLATE_6'
					[INFO]   - FormatTemplate: 'deleted_FORMATTEMPLATE_7'
					[INFO]   - LinkTemplate: 'deleted_LINKTEMPLATE_5'
					[INFO]   - PageTemplates: 'deleted_PAGETEMPLATES_4'
					[INFO] Moved elements: 7
					[INFO] - store elements: 7
					[INFO]  - pagestore: 1
					[INFO]   - Page: 'moved_PAGE_1'              \s
					[INFO]  - mediastore: 2
					[INFO]   - Media: 'moved_MEDIUM_2'           \s
					[INFO]   - Media: 'moved_MEDIUM_3'           \s
					[INFO]  - templatestore: 4
					[INFO]   - FormatTemplate: 'moved_FORMATTEMPLATE_6'
					[INFO]   - FormatTemplate: 'moved_FORMATTEMPLATE_7'
					[INFO]   - LinkTemplate: 'moved_LINKTEMPLATE_5'
					[INFO]   - PageTemplates: 'moved_PAGETEMPLATES_4'
					[INFO] L&Found elements: 7
					[INFO] - store elements: 7
					[INFO]  - pagestore: 1
					[INFO]   - Page: 'lostAndFound_PAGE_1'       \s
					[INFO]  - mediastore: 2
					[INFO]   - Media: 'lostAndFound_MEDIUM_2'    \s
					[INFO]   - Media: 'lostAndFound_MEDIUM_3'    \s
					[INFO]  - templatestore: 4
					[INFO]   - FormatTemplate: 'lostAndFound_FORMATTEMPLATE_6'
					[INFO]   - FormatTemplate: 'lostAndFound_FORMATTEMPLATE_7'
					[INFO]   - LinkTemplate: 'lostAndFound_LINKTEMPLATE_5'
					[INFO]   - PageTemplates: 'lostAndFound_PAGETEMPLATES_4'
					[INFO] Problems: 4
					[INFO]  - store: PAGESTORE | uid: pagestore_uid_1337 | reason: IdProvider not found
					[INFO]  - store: MEDIASTORE | uid: mediastore_uid_123 | reason: Resolution invalid
					[INFO]  - store: MEDIASTORE | uid: mediastore_uid_1932 | reason: Medium invalid
					[INFO]  - store: TEMPLATESTORE | name: templatestore_name_1231 | reason: GOM is invalid
					[INFO] == SUMMARY ==
					[INFO] Created elements: 12 | store elements: 7 ( pagestore: 1, mediastore: 2, templatestore: 4 ) | entity types: 5 ( schemas: 3, entities: 6 )
					[INFO] Updated elements: 22 | project properties: 10 | store elements: 7 ( pagestore: 1, mediastore: 2, templatestore: 4 ) | entity types: 5 ( schemas: 3, entities: 6 )
					[INFO] Deleted elements: 7 | store elements: 7 ( pagestore: 1, mediastore: 2, templatestore: 4 )
					[INFO]   Moved elements: 7 | store elements: 7 ( pagestore: 1, mediastore: 2, templatestore: 4 )
					[INFO] L&Found elements: 7 | store elements: 7 ( pagestore: 1, mediastore: 2, templatestore: 4 )
					[INFO]         Problems: 4
					""";
			assertEquals(expected, logger.toString());
		}
		{
			final MockLogger logger = new MockLogger(true);
			final MockedImportResult mockedImportResult = new MockedImportResult(true);
			AdvancedLogger.logImportResult(logger, mockedImportResult.getStoreAgent(), mockedImportResult);
			final String expected = """
					[INFO] Import done.
					[INFO] == DETAILS ==
					[INFO] Created elements: 12
					[INFO] - store elements: 7
					[INFO]  - pagestore: 1
					[INFO]   - Page: 'created_PAGE_1'            \s
					[INFO]  - mediastore: 2
					[INFO]   - Media: 'created_MEDIUM_2'         \s
					[INFO]   - Media: 'created_MEDIUM_3'         \s
					[INFO]  - templatestore: 4
					[INFO]   - FormatTemplate: 'created_FORMATTEMPLATE_6'
					[INFO]   - FormatTemplate: 'created_FORMATTEMPLATE_7'
					[INFO]   - LinkTemplate: 'created_LINKTEMPLATE_5'
					[INFO]   - PageTemplates: 'created_PAGETEMPLATES_4'
					[INFO] - entity types: 5                       ( schemas: 3, entities: 6 )
					[INFO]  - Schema: 'schema1'                    ( entity types: 2, entities: 3 )
					[INFO]   - EntityType: 'created_entityType2'   ( entities: 1 )
					[INFO]   - EntityType: 'created_entityType1'   ( entities: 2 )
					[INFO]  - Schema: 'schema2'                    ( entity types: 2, entities: 2 )
					[INFO]   - EntityType: 'created_entityType2'   ( entities: 1 )
					[INFO]   - EntityType: 'created_entityType1'   ( entities: 1 )
					[INFO]  - Schema: 'schema3'                    ( entity types: 1, entities: 1 )
					[INFO]   - EntityType: 'created_entityType1'   ( entities: 1 )
					[INFO] Updated elements: 22
					[INFO] - project properties: 10
					[INFO]  - Common                             \s
					[INFO]  - CustomProperties                   \s
					[INFO]  - Resolutions                        \s
					[INFO]  - Groups                             \s
					[INFO]  - ScheduleEntries                    \s
					[INFO]  - TemplateSets                       \s
					[INFO]  - Fonts                              \s
					[INFO]  - ModuleConfigurations               \s
					[INFO]  - Languages                          \s
					[INFO]  - Users                              \s
					[INFO] - store elements: 7
					[INFO]  - pagestore: 1
					[INFO]   - Page: 'updated_PAGE_1'            \s
					[INFO]  - mediastore: 2
					[INFO]   - Media: 'updated_MEDIUM_2'         \s
					[INFO]   - Media: 'updated_MEDIUM_3'         \s
					[INFO]  - templatestore: 4
					[INFO]   - FormatTemplate: 'updated_FORMATTEMPLATE_6'
					[INFO]   - FormatTemplate: 'updated_FORMATTEMPLATE_7'
					[INFO]   - LinkTemplate: 'updated_LINKTEMPLATE_5'
					[INFO]   - PageTemplates: 'updated_PAGETEMPLATES_4'
					[INFO] - entity types: 5                       ( schemas: 3, entities: 6 )
					[INFO]  - Schema: 'schema1'                    ( entity types: 2, entities: 3 )
					[INFO]   - EntityType: 'updated_entityType1'   ( entities: 2 )
					[INFO]   - EntityType: 'updated_entityType2'   ( entities: 1 )
					[INFO]  - Schema: 'schema2'                    ( entity types: 2, entities: 2 )
					[INFO]   - EntityType: 'updated_entityType1'   ( entities: 1 )
					[INFO]   - EntityType: 'updated_entityType2'   ( entities: 1 )
					[INFO]  - Schema: 'schema3'                    ( entity types: 1, entities: 1 )
					[INFO]   - EntityType: 'updated_entityType1'   ( entities: 1 )
					[INFO] Deleted elements: 7
					[INFO] - store elements: 7
					[INFO]  - pagestore: 1
					[INFO]   - Page: 'deleted_PAGE_1'            \s
					[INFO]  - mediastore: 2
					[INFO]   - Media: 'deleted_MEDIUM_2'         \s
					[INFO]   - Media: 'deleted_MEDIUM_3'         \s
					[INFO]  - templatestore: 4
					[INFO]   - FormatTemplate: 'deleted_FORMATTEMPLATE_6'
					[INFO]   - FormatTemplate: 'deleted_FORMATTEMPLATE_7'
					[INFO]   - LinkTemplate: 'deleted_LINKTEMPLATE_5'
					[INFO]   - PageTemplates: 'deleted_PAGETEMPLATES_4'
					[INFO] Moved elements: 7
					[INFO] - store elements: 7
					[INFO]  - pagestore: 1
					[INFO]   - Page: 'moved_PAGE_1'              \s
					[INFO]  - mediastore: 2
					[INFO]   - Media: 'moved_MEDIUM_2'           \s
					[INFO]   - Media: 'moved_MEDIUM_3'           \s
					[INFO]  - templatestore: 4
					[INFO]   - FormatTemplate: 'moved_FORMATTEMPLATE_6'
					[INFO]   - FormatTemplate: 'moved_FORMATTEMPLATE_7'
					[INFO]   - LinkTemplate: 'moved_LINKTEMPLATE_5'
					[INFO]   - PageTemplates: 'moved_PAGETEMPLATES_4'
					[INFO] L&Found elements: 7
					[INFO] - store elements: 7
					[INFO]  - pagestore: 1
					[INFO]   - Page: 'lostAndFound_PAGE_1'       \s
					[INFO]  - mediastore: 2
					[INFO]   - Media: 'lostAndFound_MEDIUM_2'    \s
					[INFO]   - Media: 'lostAndFound_MEDIUM_3'    \s
					[INFO]  - templatestore: 4
					[INFO]   - FormatTemplate: 'lostAndFound_FORMATTEMPLATE_6'
					[INFO]   - FormatTemplate: 'lostAndFound_FORMATTEMPLATE_7'
					[INFO]   - LinkTemplate: 'lostAndFound_LINKTEMPLATE_5'
					[INFO]   - PageTemplates: 'lostAndFound_PAGETEMPLATES_4'
					[INFO] Problems: 4
					[INFO]  - store: PAGESTORE | uid: pagestore_uid_1337 | reason: IdProvider not found
					[INFO]  - store: MEDIASTORE | uid: mediastore_uid_123 | reason: Resolution invalid
					[INFO]  - store: MEDIASTORE | uid: mediastore_uid_1932 | reason: Medium invalid
					[INFO]  - store: TEMPLATESTORE | name: templatestore_name_1231 | reason: GOM is invalid
					[INFO] == SUMMARY ==
					[INFO] Created elements: 12 | store elements: 7 ( pagestore: 1, mediastore: 2, templatestore: 4 ) | entity types: 5 ( schemas: 3, entities: 6 )
					[INFO] Updated elements: 22 | project properties: 10 | store elements: 7 ( pagestore: 1, mediastore: 2, templatestore: 4 ) | entity types: 5 ( schemas: 3, entities: 6 )
					[INFO] Deleted elements: 7 | store elements: 7 ( pagestore: 1, mediastore: 2, templatestore: 4 )
					[INFO]   Moved elements: 7 | store elements: 7 ( pagestore: 1, mediastore: 2, templatestore: 4 )
					[INFO] L&Found elements: 7 | store elements: 7 ( pagestore: 1, mediastore: 2, templatestore: 4 )
					[INFO]         Problems: 4
					""";
			assertEquals(expected, logger.toString());
		}
	}

	@Test
	public void testLogElements() {
		{
			final MockLogger logger = new MockLogger(true);
			AdvancedLogger.logElements(logger, null, Collections.emptyList(), "myDescription");
			final String expected = "[INFO] myDescription: 0" + NEW_LINE;
			assertEquals(expected, logger.toString());
		}
		{
			// update-case ==> -1
			final MockLogger logger = new MockLogger(true);
			AdvancedLogger.logElements(logger, null, Collections.emptyList(), "myDescription");
			final String expected = "[INFO] myDescription: 0" + NEW_LINE;
			assertEquals(expected, logger.toString());
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
			AdvancedLogger.logElements(logger, null, elements, "myDescription");
			final String expected = """
					[INFO] myDescription: 16
					[INFO] - project properties: 3
					[INFO]  - Groups                               ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]  - TemplateSets                         ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]  - Users                                ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO] - store elements: 11
					[INFO]  - pagestore: 1
					[INFO]   - Page: 'first'                       ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]  - mediastore: 3
					[INFO]   - Media: 'first'                      ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]   - Media: 'second'                     ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]   - MediaFolder: 'third'                ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]  - sitestore: 3
					[INFO]   - PageRef: 'first'                    ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]   - PageRef: 'second'                   ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]   - PageRef: 'third'                    ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]  - templatestore: 4
					[INFO]   - PageTemplate: 'first'               ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]   - PageTemplate: 'fourth'              ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]   - Workflow: 'second'                  ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]   - LinkTemplate: 'third'               ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO] - entity types: 2                       ( schemas: 2, entities: 3 )
					[INFO]  - Schema: 'myFirstSchema'              ( entity types: 1, entities: 1 )
					[INFO]   - EntityType: 'myType'                ( entities: 1 )
					[INFO]  - Schema: 'mySecondSchema'             ( entity types: 1, entities: 2 )
					[INFO]   - EntityType: 'mySecondType'          ( entities: 2 )
					""";
			assertEquals(expected, logger.toString());
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
			AdvancedLogger.logElements(logger, null, elements, "myDescription");
			final String expected = """
					[INFO] myDescription: 16
					[INFO] - project properties: 3
					[INFO]  - Groups                               ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]   - Created files: 1
					[DEBUG]    - /path/GROUPS/0.txt
					[DEBUG]   - Updated files: 2
					[DEBUG]    - /path/GROUPS/0.txt
					[DEBUG]    - /path/GROUPS/1.txt
					[DEBUG]   - Deleted files: 3
					[DEBUG]    - /path/GROUPS/0.txt
					[DEBUG]    - /path/GROUPS/1.txt
					[DEBUG]    - /path/GROUPS/2.txt
					[DEBUG]   - Moved files: 4
					[DEBUG]    - 0.txt ( from '/from/GROUPS' to '/to/GROUPS' )
					[DEBUG]    - 1.txt ( from '/from/GROUPS' to '/to/GROUPS' )
					[DEBUG]    - 2.txt ( from '/from/GROUPS' to '/to/GROUPS' )
					[DEBUG]    - 3.txt ( from '/from/GROUPS' to '/to/GROUPS' )
					[INFO]  - TemplateSets                         ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]   - Created files: 1
					[DEBUG]    - /path/TEMPLATE_SETS/0.txt
					[DEBUG]   - Updated files: 2
					[DEBUG]    - /path/TEMPLATE_SETS/0.txt
					[DEBUG]    - /path/TEMPLATE_SETS/1.txt
					[DEBUG]   - Deleted files: 3
					[DEBUG]    - /path/TEMPLATE_SETS/0.txt
					[DEBUG]    - /path/TEMPLATE_SETS/1.txt
					[DEBUG]    - /path/TEMPLATE_SETS/2.txt
					[DEBUG]   - Moved files: 4
					[DEBUG]    - 0.txt ( from '/from/TEMPLATE_SETS' to '/to/TEMPLATE_SETS' )
					[DEBUG]    - 1.txt ( from '/from/TEMPLATE_SETS' to '/to/TEMPLATE_SETS' )
					[DEBUG]    - 2.txt ( from '/from/TEMPLATE_SETS' to '/to/TEMPLATE_SETS' )
					[DEBUG]    - 3.txt ( from '/from/TEMPLATE_SETS' to '/to/TEMPLATE_SETS' )
					[INFO]  - Users                                ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]   - Created files: 1
					[DEBUG]    - /path/USERS/0.txt
					[DEBUG]   - Updated files: 2
					[DEBUG]    - /path/USERS/0.txt
					[DEBUG]    - /path/USERS/1.txt
					[DEBUG]   - Deleted files: 3
					[DEBUG]    - /path/USERS/0.txt
					[DEBUG]    - /path/USERS/1.txt
					[DEBUG]    - /path/USERS/2.txt
					[DEBUG]   - Moved files: 4
					[DEBUG]    - 0.txt ( from '/from/USERS' to '/to/USERS' )
					[DEBUG]    - 1.txt ( from '/from/USERS' to '/to/USERS' )
					[DEBUG]    - 2.txt ( from '/from/USERS' to '/to/USERS' )
					[DEBUG]    - 3.txt ( from '/from/USERS' to '/to/USERS' )
					[INFO] - store elements: 11
					[INFO]  - pagestore: 1
					[INFO]   - Page: 'first'                       ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/first/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/first/0.txt
					[DEBUG]     - /path/first/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/first/0.txt
					[DEBUG]     - /path/first/1.txt
					[DEBUG]     - /path/first/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 1.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 2.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 3.txt ( from '/from/first' to '/to/first' )
					[INFO]  - mediastore: 3
					[INFO]   - Media: 'first'                      ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/first/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/first/0.txt
					[DEBUG]     - /path/first/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/first/0.txt
					[DEBUG]     - /path/first/1.txt
					[DEBUG]     - /path/first/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 1.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 2.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 3.txt ( from '/from/first' to '/to/first' )
					[INFO]   - Media: 'second'                     ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/second/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/second/0.txt
					[DEBUG]     - /path/second/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/second/0.txt
					[DEBUG]     - /path/second/1.txt
					[DEBUG]     - /path/second/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/second' to '/to/second' )
					[DEBUG]     - 1.txt ( from '/from/second' to '/to/second' )
					[DEBUG]     - 2.txt ( from '/from/second' to '/to/second' )
					[DEBUG]     - 3.txt ( from '/from/second' to '/to/second' )
					[INFO]   - MediaFolder: 'third'                ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/third/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/third/0.txt
					[DEBUG]     - /path/third/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/third/0.txt
					[DEBUG]     - /path/third/1.txt
					[DEBUG]     - /path/third/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/third' to '/to/third' )
					[DEBUG]     - 1.txt ( from '/from/third' to '/to/third' )
					[DEBUG]     - 2.txt ( from '/from/third' to '/to/third' )
					[DEBUG]     - 3.txt ( from '/from/third' to '/to/third' )
					[INFO]  - sitestore: 3
					[INFO]   - PageRef: 'first'                    ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/first/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/first/0.txt
					[DEBUG]     - /path/first/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/first/0.txt
					[DEBUG]     - /path/first/1.txt
					[DEBUG]     - /path/first/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 1.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 2.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 3.txt ( from '/from/first' to '/to/first' )
					[INFO]   - PageRef: 'second'                   ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/second/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/second/0.txt
					[DEBUG]     - /path/second/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/second/0.txt
					[DEBUG]     - /path/second/1.txt
					[DEBUG]     - /path/second/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/second' to '/to/second' )
					[DEBUG]     - 1.txt ( from '/from/second' to '/to/second' )
					[DEBUG]     - 2.txt ( from '/from/second' to '/to/second' )
					[DEBUG]     - 3.txt ( from '/from/second' to '/to/second' )
					[INFO]   - PageRef: 'third'                    ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/third/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/third/0.txt
					[DEBUG]     - /path/third/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/third/0.txt
					[DEBUG]     - /path/third/1.txt
					[DEBUG]     - /path/third/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/third' to '/to/third' )
					[DEBUG]     - 1.txt ( from '/from/third' to '/to/third' )
					[DEBUG]     - 2.txt ( from '/from/third' to '/to/third' )
					[DEBUG]     - 3.txt ( from '/from/third' to '/to/third' )
					[INFO]  - templatestore: 4
					[INFO]   - PageTemplate: 'first'               ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/first/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/first/0.txt
					[DEBUG]     - /path/first/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/first/0.txt
					[DEBUG]     - /path/first/1.txt
					[DEBUG]     - /path/first/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 1.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 2.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 3.txt ( from '/from/first' to '/to/first' )
					[INFO]   - PageTemplate: 'fourth'              ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/fourth/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/fourth/0.txt
					[DEBUG]     - /path/fourth/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/fourth/0.txt
					[DEBUG]     - /path/fourth/1.txt
					[DEBUG]     - /path/fourth/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/fourth' to '/to/fourth' )
					[DEBUG]     - 1.txt ( from '/from/fourth' to '/to/fourth' )
					[DEBUG]     - 2.txt ( from '/from/fourth' to '/to/fourth' )
					[DEBUG]     - 3.txt ( from '/from/fourth' to '/to/fourth' )
					[INFO]   - Workflow: 'second'                  ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/second/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/second/0.txt
					[DEBUG]     - /path/second/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/second/0.txt
					[DEBUG]     - /path/second/1.txt
					[DEBUG]     - /path/second/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/second' to '/to/second' )
					[DEBUG]     - 1.txt ( from '/from/second' to '/to/second' )
					[DEBUG]     - 2.txt ( from '/from/second' to '/to/second' )
					[DEBUG]     - 3.txt ( from '/from/second' to '/to/second' )
					[INFO]   - LinkTemplate: 'third'               ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/third/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/third/0.txt
					[DEBUG]     - /path/third/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/third/0.txt
					[DEBUG]     - /path/third/1.txt
					[DEBUG]     - /path/third/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/third' to '/to/third' )
					[DEBUG]     - 1.txt ( from '/from/third' to '/to/third' )
					[DEBUG]     - 2.txt ( from '/from/third' to '/to/third' )
					[DEBUG]     - 3.txt ( from '/from/third' to '/to/third' )
					[INFO] - entity types: 2                       ( schemas: 2, entities: 3 )
					[INFO]  - Schema: 'myFirstSchema'              ( entity types: 1, entities: 1 )
					[INFO]   - EntityType: 'myType'                ( entities: 1 )
					[DEBUG]     - Created files: 1
					[DEBUG]      - /path/myFirstSchema#myType/0.txt
					[DEBUG]     - Updated files: 2
					[DEBUG]      - /path/myFirstSchema#myType/0.txt
					[DEBUG]      - /path/myFirstSchema#myType/1.txt
					[DEBUG]     - Deleted files: 3
					[DEBUG]      - /path/myFirstSchema#myType/0.txt
					[DEBUG]      - /path/myFirstSchema#myType/1.txt
					[DEBUG]      - /path/myFirstSchema#myType/2.txt
					[DEBUG]     - Moved files: 4
					[DEBUG]      - 0.txt ( from '/from/myFirstSchema#myType' to '/to/myFirstSchema#myType' )
					[DEBUG]      - 1.txt ( from '/from/myFirstSchema#myType' to '/to/myFirstSchema#myType' )
					[DEBUG]      - 2.txt ( from '/from/myFirstSchema#myType' to '/to/myFirstSchema#myType' )
					[DEBUG]      - 3.txt ( from '/from/myFirstSchema#myType' to '/to/myFirstSchema#myType' )
					[INFO]  - Schema: 'mySecondSchema'             ( entity types: 1, entities: 2 )
					[INFO]   - EntityType: 'mySecondType'          ( entities: 2 )
					[DEBUG]     - Created files: 1
					[DEBUG]      - /path/mySecondSchema#mySecondType/0.txt
					[DEBUG]     - Updated files: 2
					[DEBUG]      - /path/mySecondSchema#mySecondType/0.txt
					[DEBUG]      - /path/mySecondSchema#mySecondType/1.txt
					[DEBUG]     - Deleted files: 3
					[DEBUG]      - /path/mySecondSchema#mySecondType/0.txt
					[DEBUG]      - /path/mySecondSchema#mySecondType/1.txt
					[DEBUG]      - /path/mySecondSchema#mySecondType/2.txt
					[DEBUG]     - Moved files: 4
					[DEBUG]      - 0.txt ( from '/from/mySecondSchema#mySecondType' to '/to/mySecondSchema#mySecondType' )
					[DEBUG]      - 1.txt ( from '/from/mySecondSchema#mySecondType' to '/to/mySecondSchema#mySecondType' )
					[DEBUG]      - 2.txt ( from '/from/mySecondSchema#mySecondType' to '/to/mySecondSchema#mySecondType' )
					[DEBUG]      - 3.txt ( from '/from/mySecondSchema#mySecondType' to '/to/mySecondSchema#mySecondType' )
					""";
			assertEquals(expected, logger.toString());
		}
	}

	@Test
	public void testBuildSummaryEmptyUpdate() {
		{
			// empty test
			final Collection<ExportInfo> elements = Collections.emptyList();
			final ReorganizedResult reorganizedResult = new ReorganizedResult(elements);
			final String result = AdvancedLogger.buildSummary(elements, "myDescription", reorganizedResult);
			final String expected = "myDescription: 0";
			assertEquals(expected, result);
		}
	}

	@Test
	public void testBuildSummary() {
		{
			// empty test
			final Collection<ExportInfo> elements = Collections.emptyList();
			final ReorganizedResult reorganizedResult = new ReorganizedResult(elements);
			final String result = AdvancedLogger.buildSummary(elements, "myDescription", reorganizedResult);
			final String expected = "myDescription: 0";
			assertEquals(expected, result);
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
			final String expected = "myDescription: 14 | project properties: 3 | store elements: 11 ( pagestore: 1, mediastore: 3, sitestore: 3, templatestore: 4 )";
			assertEquals(expected, result);
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
			assertEquals(expected, result);
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
			final String expected = "myDescription: 13 | store elements: 11 ( pagestore: 1, mediastore: 3, sitestore: 3, templatestore: 4 ) | entity types: 2 ( schemas: 2, entities: 3 )";
			assertEquals(expected, result);
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
			final String expected = "myDescription: 16 | project properties: 3 | store elements: 11 ( pagestore: 1, mediastore: 3, sitestore: 3, templatestore: 4 ) | entity types: 2 ( schemas: 2, entities: 3 )";
			assertEquals(expected, result);
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
			assertEquals(expected, result);
		}
	}

	@Test
	public void testAppendProjectPropertySummary() {
		{
			final StringBuilder stringBuilder = new StringBuilder();
			final Collection<PropertyTypeExportInfo> projectProperties = Collections.emptyList();
			AdvancedLogger.appendProjectPropertySummary(stringBuilder, projectProperties);
			assertEquals("", stringBuilder.toString());
		}
		{
			final StringBuilder stringBuilder = new StringBuilder();
			final Collection<PropertyTypeExportInfo> projectProperties = new ArrayList<>(Collections.singletonList(new MockedPropertyTypeExportInfo(PropertiesTransportOptions.ProjectPropertyType.USERS)));
			AdvancedLogger.appendProjectPropertySummary(stringBuilder, projectProperties);
			assertEquals(" | project properties: " + projectProperties.size(), stringBuilder.toString());
		}
		{
			final StringBuilder stringBuilder = new StringBuilder();
			final Collection<PropertyTypeExportInfo> projectProperties = new ArrayList<>(Arrays.asList(new MockedPropertyTypeExportInfo(PropertiesTransportOptions.ProjectPropertyType.USERS), new MockedPropertyTypeExportInfo(PropertiesTransportOptions.ProjectPropertyType.TEMPLATE_SETS)));
			AdvancedLogger.appendProjectPropertySummary(stringBuilder, projectProperties);
			assertEquals(" | project properties: " + projectProperties.size(), stringBuilder.toString());
		}
	}

	@Test
	public void testAppendStoreElementSummary() {
		{
			final StringBuilder stringBuilder = new StringBuilder();
			final Map<Store.Type, List<ElementExportInfo>> storeElements = Collections.emptyMap();
			AdvancedLogger.appendStoreElementSummary(stringBuilder, storeElements);
			assertEquals("", stringBuilder.toString());
		}
		{
			final Map<Store.Type, List<ElementExportInfo>> storeElements = MockedElementExportInfo.createMapWithStoreElements();
			final StringBuilder stringBuilder = new StringBuilder();
			AdvancedLogger.appendStoreElementSummary(stringBuilder, storeElements);
			assertEquals(" | store elements: 11 ( pagestore: 1, mediastore: 3, sitestore: 3, templatestore: 4 )", stringBuilder.toString());
		}
	}

	@Test
	public void testAppendEntityTypeSummary() {
		{
			final StringBuilder stringBuilder = new StringBuilder();
			final Collection<EntityTypeExportInfo> entityTypes = Collections.emptyList();
			AdvancedLogger.appendEntityTypeSummary(stringBuilder, entityTypes);
			assertEquals("", stringBuilder.toString());
		}
		{
			final StringBuilder stringBuilder = new StringBuilder();
			final Collection<EntityTypeExportInfo> entityTypes = new ArrayList<>(Collections.singletonList(new MockedEntityTypeExportInfo("myType", "mySchema", 0)));
			AdvancedLogger.appendEntityTypeSummary(stringBuilder, entityTypes);
			assertEquals(" | entity types: 1 ( schemas: 1, entities: 0 )", stringBuilder.toString());
		}
		{
			final StringBuilder stringBuilder = new StringBuilder();
			final Collection<EntityTypeExportInfo> entityTypes = new ArrayList<>(Collections.singletonList(new MockedEntityTypeExportInfo("myType", "mySchema", 1)));
			AdvancedLogger.appendEntityTypeSummary(stringBuilder, entityTypes);
			assertEquals(" | entity types: 1 ( schemas: 1, entities: 1 )", stringBuilder.toString());
		}
		{
			final StringBuilder stringBuilder = new StringBuilder();
			final Collection<EntityTypeExportInfo> entityTypes = new ArrayList<>(Collections.singletonList(new MockedEntityTypeExportInfo("myType", "mySchema", 2)));
			AdvancedLogger.appendEntityTypeSummary(stringBuilder, entityTypes);
			assertEquals(" | entity types: 1 ( schemas: 1, entities: 2 )", stringBuilder.toString());
		}
		{
			final StringBuilder stringBuilder = new StringBuilder();
			final Collection<EntityTypeExportInfo> entityTypes = new ArrayList<>(Arrays.asList(new MockedEntityTypeExportInfo("myType", "myFirstSchema", 1), new MockedEntityTypeExportInfo("mySecondType", "mySecondSchema", 2)));
			AdvancedLogger.appendEntityTypeSummary(stringBuilder, entityTypes);
			assertEquals(" | entity types: 2 ( schemas: 2, entities: 3 )", stringBuilder.toString());
		}
	}

	@Test
	public void testLogProjectProperties() {
		{
			final MockLogger logger = new MockLogger(true);
			AdvancedLogger.logProjectProperties(logger, Collections.emptyList());
			assertEquals("", logger.toString());
		}
		{
			final MockLogger logger = new MockLogger(false);
			final Collection<PropertyTypeExportInfo> projectProperties = new ArrayList<>(Collections.singletonList(new MockedPropertyTypeExportInfo(null)));
			AdvancedLogger.logProjectProperties(logger, projectProperties);
			final String expected = """
					[INFO] - project properties: 1
					[INFO]  - Property fs metadata                 ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					""";
			assertEquals(expected, logger.toString());
		}
		{
			final MockLogger logger = new MockLogger(false);
			final Collection<PropertyTypeExportInfo> projectProperties = new ArrayList<>(Arrays.asList(new MockedPropertyTypeExportInfo(null), new MockedPropertyTypeExportInfo(PropertiesTransportOptions.ProjectPropertyType.USERS)));
			AdvancedLogger.logProjectProperties(logger, projectProperties);
			final String expected = """
					[INFO] - project properties: 2
					[INFO]  - Users                                ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]  - Property fs metadata                 ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					""";
			assertEquals(expected, logger.toString());
		}
		{
			final MockLogger logger = new MockLogger(false);
			final Collection<PropertyTypeExportInfo> projectProperties = new ArrayList<>(Collections.singletonList(new MockedPropertyTypeExportInfo(PropertiesTransportOptions.ProjectPropertyType.USERS)));
			AdvancedLogger.logProjectProperties(logger, projectProperties);
			final String expected = """
					[INFO] - project properties: 1
					[INFO]  - Users                                ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					""";
			assertEquals(expected, logger.toString());
		}
		{
			final MockLogger logger = new MockLogger(true);
			final Collection<PropertyTypeExportInfo> projectProperties = new ArrayList<>(Collections.singletonList(new MockedPropertyTypeExportInfo(PropertiesTransportOptions.ProjectPropertyType.USERS)));
			AdvancedLogger.logProjectProperties(logger, projectProperties);
			final String expected = """
					[INFO] - project properties: 1
					[INFO]  - Users                                ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]   - Created files: 1
					[DEBUG]    - /path/USERS/0.txt
					[DEBUG]   - Updated files: 2
					[DEBUG]    - /path/USERS/0.txt
					[DEBUG]    - /path/USERS/1.txt
					[DEBUG]   - Deleted files: 3
					[DEBUG]    - /path/USERS/0.txt
					[DEBUG]    - /path/USERS/1.txt
					[DEBUG]    - /path/USERS/2.txt
					[DEBUG]   - Moved files: 4
					[DEBUG]    - 0.txt ( from '/from/USERS' to '/to/USERS' )
					[DEBUG]    - 1.txt ( from '/from/USERS' to '/to/USERS' )
					[DEBUG]    - 2.txt ( from '/from/USERS' to '/to/USERS' )
					[DEBUG]    - 3.txt ( from '/from/USERS' to '/to/USERS' )
					""";
			assertEquals(expected, logger.toString());
		}
	}

	@Test
	public void testLogStoreElements() {
		{
			final MockLogger logger = new MockLogger(true);
			logger.setInfoEnabled(false);
			AdvancedLogger.logStoreElements(logger, null, Collections.emptyMap());
			assertEquals("", logger.toString());
		}
		{
			final MockLogger logger = new MockLogger(true);
			AdvancedLogger.logStoreElements(logger, null, Collections.emptyMap());
			assertEquals("", logger.toString());
		}
		{
			final MockLogger logger = new MockLogger(false);
			AdvancedLogger.logStoreElements(logger, null, MockedElementExportInfo.createMapWithStoreElements());
			final String expected = """
					[INFO] - store elements: 11
					[INFO]  - pagestore: 1
					[INFO]   - Page: 'first'                       ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]  - mediastore: 3
					[INFO]   - Media: 'first'                      ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]   - Media: 'second'                     ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]   - MediaFolder: 'third'                ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]  - sitestore: 3
					[INFO]   - PageRef: 'first'                    ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]   - PageRef: 'second'                   ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]   - PageRef: 'third'                    ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]  - templatestore: 4
					[INFO]   - PageTemplate: 'first'               ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]   - PageTemplate: 'fourth'              ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]   - Workflow: 'second'                  ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[INFO]   - LinkTemplate: 'third'               ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					""";
			assertEquals(expected, logger.toString());
		}
		{
			final MockLogger logger = new MockLogger(true);
			AdvancedLogger.logStoreElements(logger, null, MockedElementExportInfo.createMapWithStoreElements());
			final String expected = """
					[INFO] - store elements: 11
					[INFO]  - pagestore: 1
					[INFO]   - Page: 'first'                       ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/first/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/first/0.txt
					[DEBUG]     - /path/first/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/first/0.txt
					[DEBUG]     - /path/first/1.txt
					[DEBUG]     - /path/first/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 1.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 2.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 3.txt ( from '/from/first' to '/to/first' )
					[INFO]  - mediastore: 3
					[INFO]   - Media: 'first'                      ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/first/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/first/0.txt
					[DEBUG]     - /path/first/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/first/0.txt
					[DEBUG]     - /path/first/1.txt
					[DEBUG]     - /path/first/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 1.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 2.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 3.txt ( from '/from/first' to '/to/first' )
					[INFO]   - Media: 'second'                     ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/second/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/second/0.txt
					[DEBUG]     - /path/second/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/second/0.txt
					[DEBUG]     - /path/second/1.txt
					[DEBUG]     - /path/second/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/second' to '/to/second' )
					[DEBUG]     - 1.txt ( from '/from/second' to '/to/second' )
					[DEBUG]     - 2.txt ( from '/from/second' to '/to/second' )
					[DEBUG]     - 3.txt ( from '/from/second' to '/to/second' )
					[INFO]   - MediaFolder: 'third'                ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/third/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/third/0.txt
					[DEBUG]     - /path/third/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/third/0.txt
					[DEBUG]     - /path/third/1.txt
					[DEBUG]     - /path/third/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/third' to '/to/third' )
					[DEBUG]     - 1.txt ( from '/from/third' to '/to/third' )
					[DEBUG]     - 2.txt ( from '/from/third' to '/to/third' )
					[DEBUG]     - 3.txt ( from '/from/third' to '/to/third' )
					[INFO]  - sitestore: 3
					[INFO]   - PageRef: 'first'                    ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/first/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/first/0.txt
					[DEBUG]     - /path/first/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/first/0.txt
					[DEBUG]     - /path/first/1.txt
					[DEBUG]     - /path/first/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 1.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 2.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 3.txt ( from '/from/first' to '/to/first' )
					[INFO]   - PageRef: 'second'                   ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/second/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/second/0.txt
					[DEBUG]     - /path/second/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/second/0.txt
					[DEBUG]     - /path/second/1.txt
					[DEBUG]     - /path/second/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/second' to '/to/second' )
					[DEBUG]     - 1.txt ( from '/from/second' to '/to/second' )
					[DEBUG]     - 2.txt ( from '/from/second' to '/to/second' )
					[DEBUG]     - 3.txt ( from '/from/second' to '/to/second' )
					[INFO]   - PageRef: 'third'                    ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/third/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/third/0.txt
					[DEBUG]     - /path/third/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/third/0.txt
					[DEBUG]     - /path/third/1.txt
					[DEBUG]     - /path/third/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/third' to '/to/third' )
					[DEBUG]     - 1.txt ( from '/from/third' to '/to/third' )
					[DEBUG]     - 2.txt ( from '/from/third' to '/to/third' )
					[DEBUG]     - 3.txt ( from '/from/third' to '/to/third' )
					[INFO]  - templatestore: 4
					[INFO]   - PageTemplate: 'first'               ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/first/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/first/0.txt
					[DEBUG]     - /path/first/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/first/0.txt
					[DEBUG]     - /path/first/1.txt
					[DEBUG]     - /path/first/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 1.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 2.txt ( from '/from/first' to '/to/first' )
					[DEBUG]     - 3.txt ( from '/from/first' to '/to/first' )
					[INFO]   - PageTemplate: 'fourth'              ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/fourth/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/fourth/0.txt
					[DEBUG]     - /path/fourth/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/fourth/0.txt
					[DEBUG]     - /path/fourth/1.txt
					[DEBUG]     - /path/fourth/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/fourth' to '/to/fourth' )
					[DEBUG]     - 1.txt ( from '/from/fourth' to '/to/fourth' )
					[DEBUG]     - 2.txt ( from '/from/fourth' to '/to/fourth' )
					[DEBUG]     - 3.txt ( from '/from/fourth' to '/to/fourth' )
					[INFO]   - Workflow: 'second'                  ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/second/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/second/0.txt
					[DEBUG]     - /path/second/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/second/0.txt
					[DEBUG]     - /path/second/1.txt
					[DEBUG]     - /path/second/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/second' to '/to/second' )
					[DEBUG]     - 1.txt ( from '/from/second' to '/to/second' )
					[DEBUG]     - 2.txt ( from '/from/second' to '/to/second' )
					[DEBUG]     - 3.txt ( from '/from/second' to '/to/second' )
					[INFO]   - LinkTemplate: 'third'               ( created files: 1, updated files: 2, deleted files: 3, moved files: 4 )
					[DEBUG]    - Created files: 1
					[DEBUG]     - /path/third/0.txt
					[DEBUG]    - Updated files: 2
					[DEBUG]     - /path/third/0.txt
					[DEBUG]     - /path/third/1.txt
					[DEBUG]    - Deleted files: 3
					[DEBUG]     - /path/third/0.txt
					[DEBUG]     - /path/third/1.txt
					[DEBUG]     - /path/third/2.txt
					[DEBUG]    - Moved files: 4
					[DEBUG]     - 0.txt ( from '/from/third' to '/to/third' )
					[DEBUG]     - 1.txt ( from '/from/third' to '/to/third' )
					[DEBUG]     - 2.txt ( from '/from/third' to '/to/third' )
					[DEBUG]     - 3.txt ( from '/from/third' to '/to/third' )
					""";
			assertEquals(expected, logger.toString());
		}
	}

	@Test
	public void testLogEntityTypes() {
		{
			final MockLogger logger = new MockLogger(true);
			AdvancedLogger.logEntityTypes(logger, Collections.emptyList());
			assertEquals("", logger.toString());
		}
		{
			final MockLogger logger = new MockLogger(false);
			final Collection<EntityTypeExportInfo> entityTypes = new ArrayList<>(Collections.singletonList(new MockedEntityTypeExportInfo("myType", "mySchema", 2)));
			AdvancedLogger.logEntityTypes(logger, entityTypes);
			final String expected = """
					[INFO] - entity types: 1                       ( schemas: 1, entities: 2 )
					[INFO]  - Schema: 'mySchema'                   ( entity types: 1, entities: 2 )
					[INFO]   - EntityType: 'myType'                ( entities: 2 )
					""";
			assertEquals(expected, logger.toString());
		}
		{
			final MockLogger logger = new MockLogger(false);
			final Collection<EntityTypeExportInfo> entityTypes = new ArrayList<>(Arrays.asList(new MockedEntityTypeExportInfo("myFirstType", "myFirstSchema", 1), new MockedEntityTypeExportInfo("mySecondType", "mySecondSchema", 2)));
			AdvancedLogger.logEntityTypes(logger, entityTypes);
			final String expected = """
					[INFO] - entity types: 2                       ( schemas: 2, entities: 3 )
					[INFO]  - Schema: 'myFirstSchema'              ( entity types: 1, entities: 1 )
					[INFO]   - EntityType: 'myFirstType'           ( entities: 1 )
					[INFO]  - Schema: 'mySecondSchema'             ( entity types: 1, entities: 2 )
					[INFO]   - EntityType: 'mySecondType'          ( entities: 2 )
					""";
			assertEquals(expected, logger.toString());
		}
		{
			final MockLogger logger = new MockLogger(true);
			final Collection<EntityTypeExportInfo> entityTypes = new ArrayList<>(Arrays.asList(new MockedEntityTypeExportInfo("myFirstType", "myFirstSchema", 1), new MockedEntityTypeExportInfo("mySecondType", "mySecondSchema", 2)));
			AdvancedLogger.logEntityTypes(logger, entityTypes);
			final String expected = """
					[INFO] - entity types: 2                       ( schemas: 2, entities: 3 )
					[INFO]  - Schema: 'myFirstSchema'              ( entity types: 1, entities: 1 )
					[INFO]   - EntityType: 'myFirstType'           ( entities: 1 )
					[DEBUG]     - Created files: 1
					[DEBUG]      - /path/myFirstSchema#myFirstType/0.txt
					[DEBUG]     - Updated files: 2
					[DEBUG]      - /path/myFirstSchema#myFirstType/0.txt
					[DEBUG]      - /path/myFirstSchema#myFirstType/1.txt
					[DEBUG]     - Deleted files: 3
					[DEBUG]      - /path/myFirstSchema#myFirstType/0.txt
					[DEBUG]      - /path/myFirstSchema#myFirstType/1.txt
					[DEBUG]      - /path/myFirstSchema#myFirstType/2.txt
					[DEBUG]     - Moved files: 4
					[DEBUG]      - 0.txt ( from '/from/myFirstSchema#myFirstType' to '/to/myFirstSchema#myFirstType' )
					[DEBUG]      - 1.txt ( from '/from/myFirstSchema#myFirstType' to '/to/myFirstSchema#myFirstType' )
					[DEBUG]      - 2.txt ( from '/from/myFirstSchema#myFirstType' to '/to/myFirstSchema#myFirstType' )
					[DEBUG]      - 3.txt ( from '/from/myFirstSchema#myFirstType' to '/to/myFirstSchema#myFirstType' )
					[INFO]  - Schema: 'mySecondSchema'             ( entity types: 1, entities: 2 )
					[INFO]   - EntityType: 'mySecondType'          ( entities: 2 )
					[DEBUG]     - Created files: 1
					[DEBUG]      - /path/mySecondSchema#mySecondType/0.txt
					[DEBUG]     - Updated files: 2
					[DEBUG]      - /path/mySecondSchema#mySecondType/0.txt
					[DEBUG]      - /path/mySecondSchema#mySecondType/1.txt
					[DEBUG]     - Deleted files: 3
					[DEBUG]      - /path/mySecondSchema#mySecondType/0.txt
					[DEBUG]      - /path/mySecondSchema#mySecondType/1.txt
					[DEBUG]      - /path/mySecondSchema#mySecondType/2.txt
					[DEBUG]     - Moved files: 4
					[DEBUG]      - 0.txt ( from '/from/mySecondSchema#mySecondType' to '/to/mySecondSchema#mySecondType' )
					[DEBUG]      - 1.txt ( from '/from/mySecondSchema#mySecondType' to '/to/mySecondSchema#mySecondType' )
					[DEBUG]      - 2.txt ( from '/from/mySecondSchema#mySecondType' to '/to/mySecondSchema#mySecondType' )
					[DEBUG]      - 3.txt ( from '/from/mySecondSchema#mySecondType' to '/to/mySecondSchema#mySecondType' )
					""";
			assertEquals(expected, logger.toString());
		}
	}

	@Test
	public void testLogFileInfos() {
		{
			final MockLogger logger = new MockLogger(false);
			AdvancedLogger.logFileInfos(logger, new MockedExportInfo(), "");
			assertEquals("", logger.toString());
		}
		{
			final MockLogger logger = new MockLogger(false);
			final MockedExportInfo exportInfo = new MockedExportInfo();
			exportInfo.setCreatedFileHandles(MockedElementExportInfo.createFileHandleCollection(exportInfo, 1));
			exportInfo.setUpdatedFileHandles(MockedElementExportInfo.createFileHandleCollection(exportInfo, 2));
			exportInfo.setDeletedFileHandles(MockedElementExportInfo.createFileHandleCollection(exportInfo, 3));
			exportInfo.setMovedFileHandles(MockedElementExportInfo.createMovedFileHandleCollection(exportInfo, 4));
			AdvancedLogger.logFileInfos(logger, exportInfo, "");
			assertEquals("", logger.toString());
		}
		{
			final MockLogger logger = new MockLogger(true);
			final MockedExportInfo exportInfo = new MockedExportInfo();
			exportInfo.setCreatedFileHandles(MockedElementExportInfo.createFileHandleCollection(exportInfo, 1));
			exportInfo.setUpdatedFileHandles(MockedElementExportInfo.createFileHandleCollection(exportInfo, 2));
			exportInfo.setDeletedFileHandles(MockedElementExportInfo.createFileHandleCollection(exportInfo, 3));
			exportInfo.setMovedFileHandles(MockedElementExportInfo.createMovedFileHandleCollection(exportInfo, 4));
			AdvancedLogger.logFileInfos(logger, exportInfo, "");
			final String expected = """
					[DEBUG]   - Created files: 1
					[DEBUG]    - /path/testName/0.txt
					[DEBUG]   - Updated files: 2
					[DEBUG]    - /path/testName/0.txt
					[DEBUG]    - /path/testName/1.txt
					[DEBUG]   - Deleted files: 3
					[DEBUG]    - /path/testName/0.txt
					[DEBUG]    - /path/testName/1.txt
					[DEBUG]    - /path/testName/2.txt
					[DEBUG]   - Moved files: 4
					[DEBUG]    - 0.txt ( from '/from/testName' to '/to/testName' )
					[DEBUG]    - 1.txt ( from '/from/testName' to '/to/testName' )
					[DEBUG]    - 2.txt ( from '/from/testName' to '/to/testName' )
					[DEBUG]    - 3.txt ( from '/from/testName' to '/to/testName' )
					""";
			assertEquals(expected, logger.toString());
		}
		{
			final MockLogger logger = new MockLogger(true);
			final MockedExportInfo exportInfo = new MockedExportInfo();
			exportInfo.setCreatedFileHandles(MockedElementExportInfo.createFileHandleCollection(exportInfo, 1));
			exportInfo.setUpdatedFileHandles(MockedElementExportInfo.createFileHandleCollection(exportInfo, 2));
			exportInfo.setDeletedFileHandles(MockedElementExportInfo.createFileHandleCollection(exportInfo, 3));
			AdvancedLogger.logFileInfos(logger, exportInfo, "");
			final String expected = """
					[DEBUG]   - Created files: 1
					[DEBUG]    - /path/testName/0.txt
					[DEBUG]   - Updated files: 2
					[DEBUG]    - /path/testName/0.txt
					[DEBUG]    - /path/testName/1.txt
					[DEBUG]   - Deleted files: 3
					[DEBUG]    - /path/testName/0.txt
					[DEBUG]    - /path/testName/1.txt
					[DEBUG]    - /path/testName/2.txt
					""";
			assertEquals(expected, logger.toString());
		}
		{
			final MockLogger logger = new MockLogger(true);
			final MockedExportInfo exportInfo = new MockedExportInfo();
			exportInfo.setUpdatedFileHandles(MockedElementExportInfo.createFileHandleCollection(exportInfo, 2));
			AdvancedLogger.logFileInfos(logger, exportInfo, "");
			final String expected = """
					[DEBUG]   - Updated files: 2
					[DEBUG]    - /path/testName/0.txt
					[DEBUG]    - /path/testName/1.txt
					""";
			assertEquals(expected, logger.toString());
		}
	}

	@Test
	public void testLogFileHandles() {
		{
			final MockLogger logger = new MockLogger(true);
			AdvancedLogger.logFileHandles(logger, MockedElementExportInfo.createFileHandleCollection(new MockedExportInfo(), 0), "description", "");
			assertEquals("", logger.toString());
		}
		{
			final MockLogger logger = new MockLogger(true);
			AdvancedLogger.logFileHandles(logger, MockedElementExportInfo.createFileHandleCollection(new MockedExportInfo(), 1), "myDescription", "");
			final String expected = """
					[DEBUG]   - myDescription: 1
					[DEBUG]    - /path/testName/0.txt
					""";
			assertEquals(expected, logger.toString());
		}
		{
			// debug disabled
			final MockLogger logger = new MockLogger();
			AdvancedLogger.logFileHandles(logger, MockedElementExportInfo.createFileHandleCollection(new MockedExportInfo(), 1), "myDescription", "");
			final String expected = "";
			assertEquals(expected, logger.toString());
		}
		{
			final MockLogger logger = new MockLogger(true);
			AdvancedLogger.logFileHandles(logger, MockedElementExportInfo.createFileHandleCollection(new MockedExportInfo(), 2), "myDescription", " ");
			final String expected = """
					[DEBUG]    - myDescription: 2
					[DEBUG]     - /path/testName/0.txt
					[DEBUG]     - /path/testName/1.txt
					""";
			assertEquals(expected, logger.toString());
		}
		{
			final MockLogger logger = new MockLogger(true);
			AdvancedLogger.logFileHandles(logger, MockedElementExportInfo.createFileHandleCollection(new MockedExportInfo(), 3), "myDescription", "  ");
			final String expected = """
					[DEBUG]     - myDescription: 3
					[DEBUG]      - /path/testName/0.txt
					[DEBUG]      - /path/testName/1.txt
					[DEBUG]      - /path/testName/2.txt
					""";
			assertEquals(expected, logger.toString());
		}
	}

	@Test
	public void testLogMovedFileHandles() {
		{
			final MockLogger logger = new MockLogger(true);
			AdvancedLogger.logMovedFileHandles(logger, MockedElementExportInfo.createMovedFileHandleCollection(new MockedExportInfo(), 0), "");
			assertEquals("", logger.toString());
		}
		{
			final MockLogger logger = new MockLogger(true);
			AdvancedLogger.logMovedFileHandles(logger, MockedElementExportInfo.createMovedFileHandleCollection(new MockedExportInfo(), 1), "");
			final String expected = """
					[DEBUG]   - Moved files: 1
					[DEBUG]    - 0.txt ( from '/from/testName' to '/to/testName' )
					""";
			assertEquals(expected, logger.toString());
		}
		{
			final MockLogger logger = new MockLogger(true);
			AdvancedLogger.logMovedFileHandles(logger, MockedElementExportInfo.createMovedFileHandleCollection(new MockedExportInfo(), 2), " ");
			final String expected = """
					[DEBUG]    - Moved files: 2
					[DEBUG]     - 0.txt ( from '/from/testName' to '/to/testName' )
					[DEBUG]     - 1.txt ( from '/from/testName' to '/to/testName' )
					""";
			assertEquals(expected, logger.toString());
		}
		{
			final MockLogger logger = new MockLogger(true);
			AdvancedLogger.logMovedFileHandles(logger, MockedElementExportInfo.createMovedFileHandleCollection(new MockedExportInfo(), 3), "  ");
			final String expected = """
					[DEBUG]     - Moved files: 3
					[DEBUG]      - 0.txt ( from '/from/testName' to '/to/testName' )
					[DEBUG]      - 1.txt ( from '/from/testName' to '/to/testName' )
					[DEBUG]      - 2.txt ( from '/from/testName' to '/to/testName' )
					""";
			assertEquals(expected, logger.toString());
		}
		{
			// debug disabled
			final MockLogger logger = new MockLogger();
			AdvancedLogger.logMovedFileHandles(logger, MockedElementExportInfo.createMovedFileHandleCollection(new MockedExportInfo(), 3), "  ");
			final String expected = "";
			assertEquals(expected, logger.toString());
		}
	}

	@Test
	public void testToCamelCase() {
		assertEquals("PageStore", AdvancedLogger.toCamelCase("_", "pAGE_sTORE"));
		assertEquals("", AdvancedLogger.toCamelCase("_", "_"));
		assertEquals("P", AdvancedLogger.toCamelCase("_", "p_"));
		assertEquals("S", AdvancedLogger.toCamelCase("_", "_s"));
		assertEquals("Page", AdvancedLogger.toCamelCase("_", "pAGE_"));
		assertEquals("Store", AdvancedLogger.toCamelCase("_", "_sTORE"));
		assertEquals("PageStoreFolder", AdvancedLogger.toCamelCase("_", "pAGE_sTORE_fOLDER"));
		assertEquals("PageFolder", AdvancedLogger.toCamelCase(";", "pAGE;;fOLDER"));
	}

	@Test
	public void testGetDirectoryForFile() {
		final String result = AdvancedLogger.getDirectoryForFile(new MockedFileHandle(null, "/test/path/fileName.txt", "fileName.txt"));
		assertEquals("/test/path", result);
	}

	@Test
	public void testGetDirectoryForFileEmptyPath() {
		final String result = AdvancedLogger.getDirectoryForFile(new MockedFileHandle(null, "", ""));
		assertEquals("", result);
	}

	@Test
	public void testGetDirectoryForFileRootPath() {
		final String result = AdvancedLogger.getDirectoryForFile(new MockedFileHandle(null, "fileName.txt", "fileName.txt"));
		assertEquals("fileName.txt", result);
	}

	@Test
	public void testGetFilesStringForElementAllPresent() {
		final MockedExportInfo exportInfo = new MockedExportInfo(ExportInfo.Type.ELEMENT, "storeElement", ExportStatus.CREATED);
		exportInfo.setCreatedFileHandles(MockedElementExportInfo.createFileHandleCollection(exportInfo, 1));
		exportInfo.setUpdatedFileHandles(MockedElementExportInfo.createFileHandleCollection(exportInfo, 2));
		exportInfo.setDeletedFileHandles(MockedElementExportInfo.createFileHandleCollection(exportInfo, 3));
		exportInfo.setMovedFileHandles(MockedElementExportInfo.createMovedFileHandleCollection(exportInfo, 4));
		final String result = AdvancedLogger.getFilesStringForElement(exportInfo);
		final String expected = " ( created files: 1, updated files: " + exportInfo.getUpdatedFileHandles().size() + ", deleted files: " + exportInfo.getDeletedFileHandles().size() + ", moved files: " + exportInfo.getMovedFileHandles().size() + " )";
		assertEquals(expected, result);
	}

	@Test
	public void testGetFilesStringForElementPartialPresent() {
		final MockedExportInfo exportInfo = new MockedExportInfo(ExportInfo.Type.ELEMENT, "storeElement", ExportStatus.CREATED);
		exportInfo.setUpdatedFileHandles(MockedElementExportInfo.createFileHandleCollection(exportInfo, 2));
		final String result = AdvancedLogger.getFilesStringForElement(exportInfo);
		final String expected = " ( updated files: " + exportInfo.getUpdatedFileHandles().size() + " )";
		assertEquals(expected, result);
	}

	@Test
	public void testGetFilesStringForElementAllEmpty() {
		final MockedExportInfo exportInfo = new MockedExportInfo(ExportInfo.Type.ELEMENT, "storeElement", ExportStatus.CREATED);
		final String result = AdvancedLogger.getFilesStringForElement(exportInfo);
		final String expected = "";
		assertEquals(expected, result);
	}

	@Test
	public void testGetFilesString() {
		// assert non empty
		final List<String> list = new ArrayList<>(Arrays.asList("First", "Second"));
		final String description = "myDescription";
		final String result = AdvancedLogger.getFilesString(description, list);
		assertEquals(" " + description + ": " + list.size() + ',', result);

		// assert empty
		assertEquals("", AdvancedLogger.getFilesString(description, Collections.emptyList()));
	}

	@Test
	public void testGetSpacedString() {
		assertEquals("", AdvancedLogger.getSpacedString(-5));
		assertEquals("", AdvancedLogger.getSpacedString(-1));
		assertEquals("", AdvancedLogger.getSpacedString(0));
		assertEquals(" ", AdvancedLogger.getSpacedString(1));
		assertEquals("     ", AdvancedLogger.getSpacedString(5));
	}

}
