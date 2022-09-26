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

package com.espirit.moddev.cli.schema;

import com.espirit.moddev.cli.exception.LayerMappingException;
import de.espirit.firstspirit.access.store.templatestore.Schema;
import de.espirit.firstspirit.transport.LayerMapper;
import de.espirit.firstspirit.transport.LayerMapper.MappingContext;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public class SchemaUidToNameBasedLayerMapperTest {

	private MappingContext context;
	private Schema schema;
	private Map<String, String> map;

	@BeforeEach
	public void setUp() {
		context = mock(MappingContext.class);
		schema = mock(Schema.class);
		map = new HashMap<>();
		Mockito.when(context.getSchema()).thenReturn(schema);
	}

	private static Stream<Arguments> provideParameters() {
		return Stream.of(
				Arguments.of("*", SchemaUidToNameBasedLayerMapper.CREATE_NEW),
				Arguments.of("*", "target_layer_0"),
				Arguments.of("my_schema_uid", SchemaUidToNameBasedLayerMapper.CREATE_NEW),
				Arguments.of("my_schema_uid", "target_layer_0")
		);
	}

	@ParameterizedTest
	@MethodSource("provideParameters")
	public void testFrom(String sourceSchemaUID, final String targetLayer) {

		map.put(sourceSchemaUID, targetLayer);

		if (!"*".equals(sourceSchemaUID)) {
			// if layer is without wild card then add wild card to see that it is not used
			map.put("*", SchemaUidToNameBasedLayerMapper.CREATE_NEW);
		} else {
			// if wild card is added then trigger usage of wild card -> rename source schema
			sourceSchemaUID = "laleleu_schema";
		}

		Mockito.when(schema.getUid()).thenReturn(sourceSchemaUID);

		final LayerMapper testling = SchemaUidToNameBasedLayerMapper.from(map);

		assertThat(testling, Matchers.is(Matchers.notNullValue()));
		if (SchemaUidToNameBasedLayerMapper.CREATE_NEW.equals(targetLayer)) {
			assertThat(testling.getLayer(context), Matchers.is(LayerMapper.CREATE_NEW_DEFAULT_LAYER));
		} else {
			assertThat(testling.getLayer(context), Matchers.is(targetLayer));
		}
	}

	@Test
	public void testEmpty() {
		Assertions.assertThrows(LayerMappingException.class, () -> {
			Mockito.when(schema.getUid()).thenReturn("my_chema");

			final LayerMapper testling = SchemaUidToNameBasedLayerMapper.empty();
			assertThat(testling, Matchers.is(Matchers.notNullValue()));

			// if no mapping is provided it will raise an exception to signal mis-configuration
			testling.getLayer(context);
		});
	}

}
