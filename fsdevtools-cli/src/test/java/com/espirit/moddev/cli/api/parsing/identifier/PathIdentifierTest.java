package com.espirit.moddev.cli.api.parsing.identifier;

import de.espirit.firstspirit.transport.PropertiesTransportOptions;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.nio.file.Path;
import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author e-Spirit AG
 */
@RunWith(Theories.class)
public class PathIdentifierTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test(expected = IllegalArgumentException.class)
    public void testNullStore() {
        new PathIdentifier(null);
    }


    @Test
    public void testEquality() {
        final PathIdentifier identifier = new PathIdentifier("/templatestore/hurz");

        final PathIdentifier equalIdentifier = new PathIdentifier("/templatestore/hurz");
        assertThat(equalIdentifier).describedAs("non equal identifier").isEqualTo(identifier);

        final PathIdentifier nonEqualIdentifier = new PathIdentifier("/templatestore/blubs");
        assertThat(nonEqualIdentifier).describedAs("non equal identifier").isNotEqualTo(identifier);
    }
}
