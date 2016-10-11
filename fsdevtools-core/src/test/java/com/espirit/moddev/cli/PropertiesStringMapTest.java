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

package com.espirit.moddev.cli;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;

@RunWith(Theories.class)
public class PropertiesStringMapTest {

    @DataPoints("valid strings")
    public static String[] validPropertiesStrings = { "abc=123, def=456", "abc=123,def=456", "abc=123,def='456'", "abc=123,def=\"456\"", "abc=123\ndef='456'", "abc=123 456, xyz=bla"};

    @DataPoints("invalid strings")
    public static String[] invalidPropertiesStrings = { "abc=123;def=456"};

    @Theory
    public void testValidStringConstructor(@FromDataPoints("valid strings") final String propertiesString) {
        StringPropertiesMap constructed = new StringPropertiesMap(propertiesString);
        Assert.assertThat("Wrong count of parsed entries!", constructed.size(), is(2));
    }
    @Theory
    public void testInvalidStringConstructor(@FromDataPoints("invalid strings")final String propertiesString) {
        StringPropertiesMap constructed = new StringPropertiesMap(propertiesString);
        Assert.assertThat("Wrong count of parsed entries!", constructed.size(), is(1));
    }

    @Test
    public void testParameterlessConstructor() {
        StringPropertiesMap map = new StringPropertiesMap();
        Assert.assertThat("Paramaterless constructor should create empty map!", map.size(), is(0));
    }

}
