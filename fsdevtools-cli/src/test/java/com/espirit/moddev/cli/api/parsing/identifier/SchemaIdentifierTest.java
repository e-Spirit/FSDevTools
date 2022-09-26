/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2022 Crownpeak Technology GmbH
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

package com.espirit.moddev.cli.api.parsing.identifier;

import de.espirit.firstspirit.access.store.templatestore.Schema;
import de.espirit.firstspirit.store.access.nexport.operations.ExportOperation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class SchemaIdentifierTest {

	@Test
	public void testNullUid() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> new SchemaIdentifier(null, Collections.emptyMap()));
	}

	@Test
	public void testEquality() {

		final Map<String, String> optionsMap = new HashMap<>();
		optionsMap.put("exportGidMapping", "true");

		final Map<String, String> optionsMap2 = new HashMap<>();
		optionsMap2.put("exportGidMapping", "false");

		final SchemaIdentifier identifier = new SchemaIdentifier("products", optionsMap);
		final SchemaIdentifier equalIdentifier = new SchemaIdentifier("products", optionsMap);

		final SchemaIdentifier anUnequalIdentifier = new SchemaIdentifier("news", optionsMap);
		final SchemaIdentifier anotherUnequalIdentifier = new SchemaIdentifier("products", optionsMap2);

		assertThat("Expected a schema identifier to be equal to itself", identifier, equalTo(identifier));
		assertThat("Expected two equal schema identifiers for equal uidType", identifier, equalTo(equalIdentifier));
		assertThat("Expected two different schema identifiers to not be equal", identifier, not(equalTo(anUnequalIdentifier)));
		assertThat("Expected schema identifiers with different options to not be equal", identifier, not(equalTo(anotherUnequalIdentifier)));
	}

	@Test
	public void addToExportOperation_with_OptionsMap() {
		final HashMap<String, String> schemaOptionsMap = new HashMap<>();
		schemaOptionsMap.put(SchemaIdentifier.OPTION_EXPORT_GID_MAPPING, "true");

		final ExportOperation.SchemaOptions schemaOptions = getSchemaOptions(schemaOptionsMap);

		// initially setExportAllEntities is not supported anyway - but it may well be in the future
		verify(schemaOptions, times(0)).setExportAllEntities(false);

		// verify the SchemaOptions' setExportMapping has been set to 'true':
		verify(schemaOptions).setExportGidMapping(true);
	}

	@Test
	public void addToExportOperation_without_OptionsMap() {
		final ExportOperation.SchemaOptions schemaOptions = getSchemaOptions(Collections.emptyMap());

		// verify the SchemaOptions' setExportMapping has been set to 'true':
		verify(schemaOptions, times(0)).setExportGidMapping(true);
	}

	@Test
	public void isSchemaOptionValid_exportGidMapping() {
		assertTrue(SchemaIdentifier.isSchemaOptionValid("exportgidmapping"));
		assertTrue(SchemaIdentifier.isSchemaOptionValid("exportGidMapping"));
		assertTrue(SchemaIdentifier.isSchemaOptionValid("EXPORTGIDMAPPING"));
	}

	@Test
	public void isSchemaOptionValid_unknownOption() {
		assertFalse(SchemaIdentifier.isSchemaOptionValid("unknownoption"));
		assertFalse(SchemaIdentifier.isSchemaOptionValid("unknownOption"));
		assertFalse(SchemaIdentifier.isSchemaOptionValid("UNKNOWNOPTION"));
	}

	ExportOperation.SchemaOptions getSchemaOptions(final Map<String, String> schemaOptionsMap) {
		final String uid = "products";

		final Schema schema = mock(Schema.class);

		final ExportOperation.SchemaOptions schemaOptions = mock(ExportOperation.SchemaOptions.class);

		final ExportOperation exportOperation = mock(ExportOperation.class);
		when(exportOperation.addSchema(schema)).thenReturn(schemaOptions);

		final SchemaIdentifier identifier = new SchemaIdentifier(uid, schemaOptionsMap);

		final ExportOperation.SchemaOptions exportSchemaOptions = exportOperation.addSchema(schema);

		identifier.setSchemaOptions(exportSchemaOptions);

		return schemaOptions;
	}

}
