package com.espirit.moddev.cli.api.parsing.parser;

import com.espirit.moddev.cli.api.parsing.identifier.EntitiesIdentifier;
import com.espirit.moddev.cli.api.parsing.identifier.UidIdentifier;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

public class EntitiesIdentifierParser  implements Parser<EntitiesIdentifier> {
    private static final Pattern DELIMITER = Pattern.compile("\\s*:\\s*");

    protected static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(EntitiesIdentifierParser.class);
    public static final String ENTITIES_IDENTIFIER = "entities";

    @Override
    public List<EntitiesIdentifier> parse(List<String> input) {
        if (input == null) {
            throw new IllegalArgumentException("input is null!");
        }
        if (input.isEmpty()) {
            return Collections.emptyList();
        }

        final List<EntitiesIdentifier> list = new ArrayList<>(input.size());
        for (final String identifier : input) {
            try(Scanner uidScanner = new Scanner(identifier)) {
                uidScanner.useDelimiter(DELIMITER);
                if (uidScanner.hasNext()) {
                    final String firstPart = uidScanner.next();
                    if (uidScanner.hasNext()) {
                        final String secondPart = uidScanner.next();
                        final EntitiesIdentifier entitiesIdentifier = new EntitiesIdentifier(secondPart);
                        list.add(entitiesIdentifier);
                    } else {
                        throw new IllegalArgumentException("Wrong input format for input string " + identifier);
                    }
                }
            }
        }
        return list;
    }

    @Override
    public boolean appliesTo(String input) {
        String[] splitted = input.split(DELIMITER.pattern());
        if(splitted.length != 2) {
            return false;
        }

        return splitted[0].toLowerCase(Locale.UK).trim().equals(ENTITIES_IDENTIFIER);
    }
}
