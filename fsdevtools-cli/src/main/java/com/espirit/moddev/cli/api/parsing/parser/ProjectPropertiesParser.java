package com.espirit.moddev.cli.api.parsing.parser;

import java.util.*;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.espirit.moddev.cli.api.parsing.identifier.ProjectPropertiesIdentifier;

import de.espirit.firstspirit.transport.PropertiesTransportOptions;

/**
 *
 * @author kohlbrecher
 */
public class ProjectPropertiesParser implements Parser<ProjectPropertiesIdentifier> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(ProjectPropertiesParser.class);
    
    private static final Pattern DELIMITER = Pattern.compile("\\s*:\\s*");
    
    public static final String CUSTOM_PREFIX_PROJECT_PROPERTIES = "projectproperty";

    /**
     * Special keyword to identify all project properties (projectproperty:ALL)
     */
    public static final String ALL = "ALL";

    @Override
    public List<ProjectPropertiesIdentifier> parse(List<String> input) {
        if (input == null) {
            throw new IllegalArgumentException("input is null!");
        }
        if (input.isEmpty()) {
            return Collections.emptyList();
        }

        EnumSet<PropertiesTransportOptions.ProjectPropertyType> tempEnum = EnumSet.noneOf(PropertiesTransportOptions.ProjectPropertyType.class);
        
        final List<ProjectPropertiesIdentifier> list = new ArrayList<>(input.size());
        for (final String identifier : input) {
            try(Scanner uidScanner = new Scanner(identifier)) {
                uidScanner.useDelimiter(DELIMITER);
                if (uidScanner.hasNext()) {
                    uidScanner.next();
                    if (uidScanner.hasNext()) {
                        final String secondPart = uidScanner.next();
                        if (ALL.equalsIgnoreCase(secondPart)) {
                            // user wants to export all project properties --> ignore already collected properties and skip further collecting
                            tempEnum = EnumSet.allOf(PropertiesTransportOptions.ProjectPropertyType.class);
                            break;
                        } else {
                            tempEnum.add(PropertiesTransportOptions.ProjectPropertyType.valueOf(secondPart.toUpperCase()));
                        }
                    } else {
                        throw new IllegalArgumentException("Wrong input format for input string " + identifier);
                    }
                }
            }
        }
        final ProjectPropertiesIdentifier fqUid = new ProjectPropertiesIdentifier(tempEnum);
        list.add(fqUid);
        return list;
    }


    /**
     * @return Returns a set of all possible values for the related keyword {@link #CUSTOM_PREFIX_PROJECT_PROPERTIES}
     */
    public static Collection<String> getAllPossibleValues() {
        final List<String> result = new ArrayList<>();
        result.add(ALL);
        for (final PropertiesTransportOptions.ProjectPropertyType projectPropertyType : PropertiesTransportOptions.ProjectPropertyType.values()) {
            result.add(projectPropertyType.name());
        }
        return result;
    }


    @Override
    public boolean appliesTo(String input) {
        final String[] splitted = input.split(DELIMITER.pattern());
        return splitted.length == 2 && splitted[0].toLowerCase(Locale.UK).trim().equals(CUSTOM_PREFIX_PROJECT_PROPERTIES);
    }
    
}
