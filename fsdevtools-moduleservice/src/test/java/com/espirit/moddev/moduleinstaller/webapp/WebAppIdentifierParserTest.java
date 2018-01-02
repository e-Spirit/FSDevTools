package com.espirit.moddev.moduleinstaller.webapp;

import org.junit.Before;
import org.junit.Test;

import static com.espirit.moddev.moduleinstaller.WebAppIdentifier.forGlobalWebApp;
import static com.espirit.moddev.moduleinstaller.WebAppIdentifier.forScope;
import static de.espirit.firstspirit.module.WebEnvironment.WebScope.PREVIEW;
import static org.junit.Assert.assertEquals;

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

}