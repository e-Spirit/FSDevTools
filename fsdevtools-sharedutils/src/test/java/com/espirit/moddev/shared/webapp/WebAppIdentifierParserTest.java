package com.espirit.moddev.shared.webapp;

import org.junit.Before;
import org.junit.Test;


import java.util.List;

import static com.espirit.moddev.shared.webapp.WebAppIdentifier.forGlobalWebApp;
import static com.espirit.moddev.shared.webapp.WebAppIdentifier.forScope;
import static de.espirit.firstspirit.module.WebEnvironment.WebScope.PREVIEW;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class WebAppIdentifierParserTest {

    private WebAppIdentifierParser parser;

    @Before
    public void setUp() {
        parser = new WebAppIdentifierParser();
    }

    @Test
    public void testSimpleScopeParsing() {
        assertEquals(forScope(PREVIEW), parser.parseSingle("preview"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExceptionOnNonExistingWebAppScope() {
        parser.parseSingle("previewXXX");
    }
    @Test
    public void testGlobalWebAppNameParsing() {
        assertEquals(forGlobalWebApp("fs5root"), parser.parseSingle("global(fs5root)"));
    }
    @Test(expected = IllegalArgumentException.class)
    public void testExceptionOnNullWebAppScope() {
        parser.parseSingle(null);
    }

    @Test
    public void testExtractWebScopes() throws Exception {
        String testWebAppScopes ="preview,staging";
        List<WebAppIdentifier> extractedScopes = parser.extractWebScopes(testWebAppScopes);
        assertThat(extractedScopes, contains(WebAppIdentifier.PREVIEW, WebAppIdentifier.STAGING));
    }
}