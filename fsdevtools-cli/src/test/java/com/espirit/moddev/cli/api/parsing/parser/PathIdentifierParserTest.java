/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2020 e-Spirit AG
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

package com.espirit.moddev.cli.api.parsing.parser;

import com.espirit.moddev.cli.api.parsing.identifier.PathIdentifier;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author e-Spirit AG
 */
@RunWith(Theories.class)
public class PathIdentifierParserTest {

    @DataPoints
    public static List[] testcases =
            new List[]{Collections.singletonList("path:/TemplateStore/PageTemplates/hurz"),
                    Collections.singletonList("PATH:/TemplateStore/PageTemplates/hurz"),
                    Collections.singletonList("path :/TemplateStore/PageTemplates/hurz"),
                    Collections.singletonList("path : /TemplateStore/PageTemplates/hurz")};

    private PathIdentifierParser testling;

    @Before
    public void setUp() {
        testling = new PathIdentifierParser();
    }


    @Theory
    public void testAppliesTo(List<String> paths) throws Exception {
        for(String current : paths) {
            boolean appliesTo = testling.appliesTo(current);
            Assert.assertTrue("Parser should apply to string " + current, appliesTo);
        }
    }

    @Test
    public void testDontApplyTo() {
        boolean appliesTo = testling.appliesTo("pathxyz :bla");
        Assert.assertFalse("Parser should apply to string pathxyz :bla", appliesTo);
    }


    @Theory
    public void testParse(List<String> paths) throws Exception {
        final List<PathIdentifier> list = testling.parse(paths);

        Assertions.assertThat(list).hasSize(1);
        assertThat(list.get(0).getPath()).isEqualTo("/TemplateStore/PageTemplates/hurz");
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseWithNonExistentPrefix() throws Exception {
        testling.parse(Collections.singletonList("xxxxx:myPath"));
    }


    @Test
    public void testParseMultiple() throws Exception {
        final List<PathIdentifier> parse = testling.parse(Arrays.asList("path:/TemplateStore/PageTemplates/first", "path:/PageStore/folder"));
        Assertions.assertThat(parse).hasSize(2);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testEmptyPath() throws Exception {
        testling.parse(Collections.singletonList("path:"));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testEmptyPathWhitespaces() throws Exception {
        testling.parse(Collections.singletonList("path: "));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testNoLeadingSlash() throws Exception {
        testling.parse(Collections.singletonList("path:TemplateStore/PageTemplates/first"));
    }
}
