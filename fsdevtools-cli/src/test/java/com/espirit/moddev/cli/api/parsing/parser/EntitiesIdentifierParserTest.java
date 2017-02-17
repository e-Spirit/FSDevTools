package com.espirit.moddev.cli.api.parsing.parser;

import com.espirit.moddev.cli.api.parsing.identifier.EntitiesIdentifier;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import static com.espirit.moddev.cli.api.parsing.parser.EntitiesIdentifierParser.ENTITIES_IDENTIFIER;
import static org.junit.Assert.*;

@RunWith(Theories.class)
public class EntitiesIdentifierParserTest {

    @DataPoints
    public static List[] testcases =
            new List[]{ Arrays.asList("entities:myuid"),
                    Arrays.asList("ENTITIES:myuid"),
                    Arrays.asList("ENTITIES :myuid"),
                    Arrays.asList("ENTITIES : myuid")};

    private EntitiesIdentifierParser testling;

    @Before
    public void setUp() {
        testling = new EntitiesIdentifierParser();
    }


    @Theory
    public void testAppliesTo(List<String> uids) throws Exception {
        for(String current : uids) {
            boolean appliesTo = testling.appliesTo(current);
            Assert.assertTrue("Parser should apply to string " + current, appliesTo);
        }
    }

    @Test
    public void parse() throws Exception {
        List<EntitiesIdentifier> result = testling.parse(Arrays.asList("entities:xyz"));
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(new EntitiesIdentifier("xyz"), result.get(0));
    }

    @Test
    public void testAppliesTo() throws Exception {
        Assert.assertTrue(testling.appliesTo("entities:products"));
    }
    @Test
    public void testDontApplyTo() throws Exception {
        Assert.assertFalse(testling.appliesTo("asdasd"));
    }
    @Test
    public void testDontApplyToStartsWithEntitiesIdentifier() throws Exception {
        Assert.assertFalse(testling.appliesTo("entitiesaasd:asd"));
    }

}
