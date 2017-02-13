package com.espirit.moddev.cli.api.parsing.parser;

import com.espirit.moddev.cli.api.parsing.identifier.Identifier;
import com.espirit.moddev.cli.api.parsing.identifier.RootNodeIdentifier;
import com.espirit.moddev.cli.api.parsing.identifier.UidIdentifier;
import de.espirit.firstspirit.access.store.IDProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;

public class RegistryBasedParserTest {

    private RegistryBasedParser testling;

    @Before
    public void setUp() {
        testling = new RegistryBasedParser();
    }
    @Test(expected = IllegalArgumentException.class)
    public void registerNullParser() {
        testling.registerParser(null);
    }
    @Test
    public void registerParser() {
        Parser parser = new Parser() {
            @Override
            public List parse(List input) {
                return null;
            }

            @Override
            public boolean appliesTo(String input) {
                return false;
            }
        };
        boolean registeredParser = testling.registerParser(parser);
        Assert.assertTrue("Parser wasn't registered successfully", registeredParser);
        Assert.assertTrue("Parser wasn't unregistered successfully", testling.unregisterParser(parser));
    }

    @Test
    public void appliesTo() {
        boolean registeredParser = testling.registerParser(new Parser() {
            @Override
            public List parse(List input) {
                return null;
            }

            @Override
            public boolean appliesTo(String input) {
                return input.startsWith("xxx");
            }
        });
        Assert.assertTrue("Parser wasn't registered successfully", registeredParser);
        Assert.assertTrue("Parser should delegate appliesTo to registered parsers", testling.appliesTo("xxxaaa"));
        Assert.assertFalse("Parser should not apply to other input strings", testling.appliesTo("aaabbb"));
    }

    @Test
    public void testParseMultipleElements() throws Exception {
        testling.registerParser(new RootNodeIdentifierParser());
        testling.registerParser(new UidIdentifierParser());
        final List<Identifier> list = testling.parse(Arrays.asList("root:templatestore", "mediafolder:layout"));
        Assert.assertEquals("List should contain two identifiers!", 2, list.size());
        Assert.assertThat(list.contains(new RootNodeIdentifier(IDProvider.UidType.TEMPLATESTORE)), equalTo(true));
        Assert.assertThat(list.contains(new UidIdentifier(IDProvider.UidType.MEDIASTORE_FOLDER, "layout")), equalTo(true));
    }
}
