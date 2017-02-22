/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.espirit.moddev.cli.api.parsing.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Pattern;
import org.slf4j.LoggerFactory;

import com.espirit.moddev.cli.api.parsing.identifier.ProjectPropertiesIdentifier;
import com.espirit.moddev.cli.api.parsing.identifier.UidIdentifier;

import de.espirit.firstspirit.transport.PropertiesTransportOptions;

/**
 *
 * @author kohlbrecher
 */
public class ProjectPropertiesParser implements Parser<ProjectPropertiesIdentifier>{
    protected static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ProjectPropertiesParser.class);
    
    private static final Pattern DELIMITER = Pattern.compile("\\s*:\\s*");
    
    private static final String CUSTOM_PREFIX_PROJECT_PROPERTIES = "projectproperty";

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
                        tempEnum.add(PropertiesTransportOptions.ProjectPropertyType.valueOf(secondPart));
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

    @Override
    public boolean appliesTo(String input) {
        String[] splitted = input.split(DELIMITER.pattern());
        if(splitted.length != 2) {
            return false;
        }
        if(splitted[0].toLowerCase(Locale.UK).trim().equals(CUSTOM_PREFIX_PROJECT_PROPERTIES)) {
            return true;
        }
        return false;
    }
    
}
