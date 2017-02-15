package com.espirit.moddev.cli.api.parsing.identifier;

import de.espirit.firstspirit.access.store.IDProvider;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class RootNodeIdentifierTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test(expected = IllegalArgumentException.class)
    public void testNullUidType() {
        new RootNodeIdentifier(null);
    }

    @Test
    public void testEquality() {
        RootNodeIdentifier identifier = new RootNodeIdentifier(IDProvider.UidType.TEMPLATESTORE);
        RootNodeIdentifier equalIdentifier = new RootNodeIdentifier(IDProvider.UidType.TEMPLATESTORE);

        RootNodeIdentifier anUnequalIdentifier = new RootNodeIdentifier(IDProvider.UidType.CONTENTSTORE);

        assertThat("Expected two equal root node identifiers for equal uidType", identifier, equalTo(equalIdentifier));
        assertThat("Expected two different root node identifiers to not be equal", identifier, not(equalTo(anUnequalIdentifier)));
    }

    @Test
    public void testExceptionOnWrongUidTypeForStoreRoot() {
        expectedException.expect(IllegalArgumentException.class);
        new RootNodeIdentifier(IDProvider.UidType.TEMPLATESTORE_FORMATTEMPLATE);
    }
}
