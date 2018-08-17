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

import com.espirit.moddev.core.StringPropertiesMap;

import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Theories.class)
public class StringPropertiesMapTest {

    @DataPoints("valid strings")
    public static String[] validPropertiesStrings = {"abc=123, def=456", "abc=123,def=456", "abc=123,def=456", "abc=123,def=456",
            "abc=123" + System.lineSeparator() + "def=456", "abc=123 , def=456", " abc = 123 , def = 456 ", "abc:123,def:456",
    " abc : 123 , def : 456 "};

    @DataPoints("invalid strings")
    public static String[] invalidPropertiesStrings = {"abc=123;def=456", "abc=123 def=456"};

    @DataPoints("illegal strings")
    public static String[] illegalPropertiesStrings = {"", " ", "\t", null};

    @Theory
    public void testValidStringConstructor(@FromDataPoints("valid strings") final String propertiesString) {
        StringPropertiesMap constructed = new StringPropertiesMap(propertiesString);

        final Collection<String> values = constructed.values();
        assertThat("Wrong count of parsed entries: " + propertiesString, values, hasSize(2));
        assertThat("Wrong parsed values " + values + " for " + propertiesString, values, containsInAnyOrder("123", "456"));

        final Set<String> keys = constructed.keySet();
        assertThat("Wrong parsed keys:  " + keys + " for " + propertiesString, keys, containsInAnyOrder("abc", "def"));
    }

    @Theory
    public void testInvalidStringConstructor(@FromDataPoints("invalid strings") final String propertiesString) {
        StringPropertiesMap constructed = new StringPropertiesMap(propertiesString);

        assertThat("Wrong count of parsed entries!", constructed.values(), hasSize(1));
    }

    @Test
    public void testParameterlessConstructor() {
        StringPropertiesMap map = new StringPropertiesMap();

        assertThat("Paramaterless constructor should create empty map!", map.values(), is(empty()));
    }

    @Theory(nullsAccepted = true)
    public void testCostructorWithIllegalArgument(@FromDataPoints("illegal strings") String source) {
        StringPropertiesMap constructed = new StringPropertiesMap(source);

        assertThat("Illegal constructor parameter should create empty map!", constructed.values(), is(empty()));
    }

}
