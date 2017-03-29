package com.espirit.moddev.cli.api.parsing.parser;

import com.espirit.moddev.cli.api.parsing.identifier.PathIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * @author e-Spirit AG
 */
public class PathIdentifierParser implements Parser<PathIdentifier> {

    private static final Pattern DELIMITER = Pattern.compile("\\s*:\\s*");
    public static final String PATH_PREFIX = "path";


    @Override
    public List<PathIdentifier> parse(List<String> input) {

        final List<PathIdentifier> list = new ArrayList<>(input.size());

        for (final String identifier : input) {
            try(Scanner uidScanner = new Scanner(identifier)) {
                uidScanner.useDelimiter(DELIMITER);
                if (uidScanner.hasNext()) {
                    checkAndAddPath(list, identifier, uidScanner);
                }
            }
        }

        return list;
    }


    private static void checkAndAddPath(List<PathIdentifier> list, String identifier, Scanner uidScanner) {
        final String prefix = uidScanner.next();
        if (! PATH_PREFIX.equalsIgnoreCase(prefix)) {
            // normally checked by #appliesTo
            throw new IllegalArgumentException("invalid prefix - should be 'path'");
        }
        if (uidScanner.hasNext()) {
            final String path = uidScanner.next();
            if (! path.startsWith("/")) {
                throw new IllegalArgumentException("path should start with '/'");
            }
            list.add(new PathIdentifier(path));
        } else {
            throw new IllegalArgumentException("Wrong input format for input string " + identifier);
        }
    }


    @Override
    public boolean appliesTo(String input) {
        final String[] splitted = input.split(DELIMITER.pattern());
        return splitted.length == 2 && splitted[0].toLowerCase(Locale.UK).trim().equals(PATH_PREFIX);
    }
}
