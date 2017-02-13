package com.espirit.moddev.cli.api.parsing.parser;

import com.espirit.moddev.cli.api.parsing.identifier.Identifier;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

public class RegistryBasedParser implements Parser<Identifier> {

    private final List<Parser> registeredParsers = new ArrayList<>();

    public RegistryBasedParser() {
    }

    @Override
    public List<Identifier> parse(List<String> input) {
        List result = new ArrayList(input.size());
        inputLoop:
        for(String currentInput : input) {
            parserLoop:
            for(Parser currentParser : registeredParsers) {
                if(currentParser.appliesTo(currentInput)) {
                    result.addAll(currentParser.parse(Lists.newArrayList(currentInput)));
                    continue inputLoop;
                }
            }
        }
        return result;
    }

    @Override
    public boolean appliesTo(String input) {
        for(Parser current : registeredParsers) {
            if(current.appliesTo(input)) {
                return true;
            }
        }
        return false;
    }

    public boolean registerParser(Parser parser) {
        if(parser == null) {
            throw new IllegalArgumentException("Don't register null parsers!");
        }
        return registeredParsers.add(parser);
    }
    public boolean unregisterParser(Parser parser) {
        return registeredParsers.remove(parser);
    }
}
