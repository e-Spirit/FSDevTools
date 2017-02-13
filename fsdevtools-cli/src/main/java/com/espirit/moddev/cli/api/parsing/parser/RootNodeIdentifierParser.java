package com.espirit.moddev.cli.api.parsing.parser;

import com.espirit.moddev.cli.api.parsing.exceptions.UnknownRootNodeException;
import com.espirit.moddev.cli.api.parsing.identifier.RootNodeIdentifier;
import de.espirit.firstspirit.access.store.IDProvider;

import java.util.*;
import java.util.regex.Pattern;

import static com.espirit.moddev.cli.api.parsing.identifier.RootNodeIdentifier.getAllStorePostfixes;

/**
 * Parser implementation that is able to parse FirstSpirit StoreRoot nodes from a list of strings
 * and return a list of RootNodeIdentifier instances.
 * Is applicable to strings of the form "root:templatestore" with "root" as a prefix, ":" as a delimiter
 * and a known store root identifier as a postfix.
 */
public class RootNodeIdentifierParser implements Parser<RootNodeIdentifier> {

    private static final Pattern DELIMITER = Pattern.compile("\\s*:\\s*");

    public RootNodeIdentifierParser() {
    }

    /**
     * Parses a given list of strings and returns a list of RootNodeIdentifier instances that
     * represent FirstSpirit StoreRoot nodes.
     * @throws UnknownRootNodeException when an unknown postfix is supplied
     * @param input a list of strings to parse
     * @return a list of RootNodeIdentifiers
     */
    @Override
    public List<RootNodeIdentifier> parse(List<String> input) {
        if (input == null) {
            throw new IllegalArgumentException("input is null!");
        }
        if (input.isEmpty()) {
            return Collections.emptyList();
        }

        final List<RootNodeIdentifier> list = new ArrayList<>(input.size());
        for (final String identifier : input) {
            try(Scanner uidScanner = new Scanner(identifier)) {
                uidScanner.useDelimiter(DELIMITER);
                if (uidScanner.hasNext()) {
                    final String firstPart = uidScanner.next();
                    if (uidScanner.hasNext()) {
                        final String secondPart = uidScanner.next();
                        IDProvider.UidType uidType = getAllStorePostfixes().get(secondPart);
                        if(uidType == null) {
                            throw new UnknownRootNodeException("No root node found for '" + secondPart + "'");
                        }
                        final RootNodeIdentifier rootNodeIdentifier = new RootNodeIdentifier(uidType);
                        list.add(rootNodeIdentifier);
                    } else {
                        throw new IllegalArgumentException("Wrong input format for input string " + firstPart);
                    }
                }
            }
        }
        return list;
    }

    @Override
    public boolean appliesTo(String input) {
        return input.toLowerCase(Locale.UK).startsWith(RootNodeIdentifier.ROOT_NODE_IDENTIFIER);
    }
}
