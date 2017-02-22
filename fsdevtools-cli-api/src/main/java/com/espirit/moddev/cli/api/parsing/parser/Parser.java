package com.espirit.moddev.cli.api.parsing.parser;

import com.espirit.moddev.cli.api.parsing.identifier.Identifier;

import java.util.List;

/**
 * Interface for a parser. A parser can parse a given string into a
 * generic identifier.
 * @param <T> the identifier subclass that the parser implementation can return from a string
 */
public interface Parser<T extends Identifier> {

    /**
     * Parses a list of strings to a list of generic identifier instances.
     * @param input a list of strings to parse
     * @return a list of parsed identifiers
     */
    List<T> parse(List<String> input);

    /**
     * Indicates if the parser implementation can handle the given input string.
     * @param input the string to test applicability for
     * @return true if the input string can be handled somehow
     */
    boolean appliesTo(String input);
}
