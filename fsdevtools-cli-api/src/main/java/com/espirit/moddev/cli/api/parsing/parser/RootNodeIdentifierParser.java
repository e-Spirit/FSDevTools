package com.espirit.moddev.cli.api.parsing.parser;

import com.espirit.moddev.cli.api.exceptions.UnknownRootNodeException;
import com.espirit.moddev.cli.api.parsing.identifier.RootNodeIdentifier;
import de.espirit.firstspirit.access.store.IDProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import static com.espirit.moddev.cli.api.parsing.identifier.RootNodeIdentifier.getAllStorePostfixes;

public class RootNodeIdentifierParser implements Parser<RootNodeIdentifier> {

    private static final Pattern DELIMITER = Pattern.compile("\\s*:\\s*");

    public RootNodeIdentifierParser() {
    }

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
        return input.toLowerCase().startsWith(RootNodeIdentifier.ROOT_NODE_IDENTIFIER);
    }
}
