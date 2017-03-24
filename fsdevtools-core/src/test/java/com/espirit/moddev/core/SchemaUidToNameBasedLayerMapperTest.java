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

package com.espirit.moddev.core;

import de.espirit.firstspirit.access.store.templatestore.Schema;
import de.espirit.firstspirit.transport.LayerMapper;
import de.espirit.firstspirit.transport.LayerMapper.MappingContext;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(Theories.class)
public class SchemaUidToNameBasedLayerMapperTest {

    @DataPoints("schemas")
    public static String[] schemas = {"*", "my_schema_uid"};

    @DataPoints("layers")
    public static String[] layers = {SchemaUidToNameBasedLayerMapper.CREATE_NEW, "target_layer_0"};

    @Rule
    public MockitoRule mockInjection = MockitoJUnit.rule();

    @Mock
    private MappingContext context;

    @Mock
    private Schema schema;

    private Map<String, String> map;


    @Before
    public void setUp() throws Exception {
        map = new HashMap<String, String>();
        when(context.getSchema()).thenReturn(schema);
    }

    @Theory
    public void testFrom(@FromDataPoints("schemas") String sourceSchemaUID, @FromDataPoints("layers") final String targetLayer)
            throws Exception {

        map.put(sourceSchemaUID, targetLayer);

        if (!"*".equals(sourceSchemaUID)) {
            // if layer is without wild card then add wild card to see that it is not used
            map.put("*", SchemaUidToNameBasedLayerMapper.CREATE_NEW);
        } else {
            // if wild card is added then trigger usage of wild card -> rename source schema
            sourceSchemaUID = "laleleu_schema";
        }

        when(schema.getUid()).thenReturn(sourceSchemaUID);

        final LayerMapper testling = SchemaUidToNameBasedLayerMapper.from(map);

        assertThat(testling, is(notNullValue()));
        if (SchemaUidToNameBasedLayerMapper.CREATE_NEW.equals(targetLayer)) {
            assertThat(testling.getLayer(context), is(LayerMapper.CREATE_NEW_DEFAULT_LAYER));
        } else {
            assertThat(testling.getLayer(context), is(targetLayer));
        }
    }

    @Test(expected = LayerMappingException.class)
    public void testEmpty() throws Exception {
        when(schema.getUid()).thenReturn("my_chema");

        final LayerMapper testling = SchemaUidToNameBasedLayerMapper.empty();
        assertThat(testling, is(notNullValue()));

        // if no mapping is provided it will raise an exception to signal mis-configuration
        testling.getLayer(context);
    }

}
