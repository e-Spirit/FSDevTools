package com.espirit.moddev.cli.api.parsing.parser;

import com.espirit.moddev.cli.api.parsing.identifier.Identifier;

import java.util.List;

public interface Parser<T extends Identifier> {
    List<T> parse(List<String> input);

    boolean appliesTo(String input);
}
