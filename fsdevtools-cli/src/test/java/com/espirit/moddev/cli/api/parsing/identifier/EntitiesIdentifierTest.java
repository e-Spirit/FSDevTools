package com.espirit.moddev.cli.api.parsing.identifier;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class EntitiesIdentifierTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testNullUid() {
        expectedException.expect(IllegalArgumentException.class);
        new EntitiesIdentifier(null);
    }

    @Test
    public void testEquality() {
        EntitiesIdentifier identifier = new EntitiesIdentifier("products");
        EntitiesIdentifier equalIdentifier = new EntitiesIdentifier("products");

        EntitiesIdentifier anUnequalIdentifier = new EntitiesIdentifier("news");

        assertThat("Expected an entities identifier to be equal to itself", identifier, equalTo(identifier));
        assertThat("Expected two equal entities identifiers for equal uidType", identifier, equalTo(equalIdentifier));
        assertThat("Expected two different entities identifiers to not be equal", identifier, not(equalTo(anUnequalIdentifier)));
    }
}
