package com.espirit.moddev.shared.webapp;

import com.espirit.moddev.shared.StringUtils;

import de.espirit.common.VisibleForTesting;
import de.espirit.firstspirit.module.WebEnvironment.WebScope;

import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.espirit.firstspirit.module.WebEnvironment.WebScope.GLOBAL;
import static java.util.Locale.UK;
import static java.util.stream.Collectors.toList;

public class WebAppIdentifierParser {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(WebAppIdentifierParser.class);
    private Pattern globalWebAppPattern = Pattern.compile("global\\((.*)\\)");

    public WebAppIdentifier parseSingle(String scopeOrGlobalWebAppId) {
        if(StringUtils.isNullOrEmpty(scopeOrGlobalWebAppId)) {
            throw new IllegalArgumentException("Passed string for scope or global WebAppId is null or empty");
        }
        Matcher globalMatcher = globalWebAppPattern.matcher(scopeOrGlobalWebAppId);

        if(globalMatcher.matches()) {
            String globalWebAppId = globalMatcher.group(1);
            return WebAppIdentifier.forGlobalWebApp(globalWebAppId);
        } else {
            String upperCaseScope = scopeOrGlobalWebAppId.toUpperCase(UK);
            WebScope parsedScope;
            try {
                parsedScope = WebScope.valueOf(upperCaseScope);
            } catch (IllegalArgumentException e) {
                LOGGER.error("Can't find a WebScope for " + upperCaseScope + ". " +
                        "Take a look at the WebScope enum, or pass global(webAppId) to select a global WebApp.", e);
                throw e;
            }
            if(GLOBAL.equals(parsedScope)) {
                throw new IllegalArgumentException("Global WebApp scope has to be passed in the form of 'global(webAppId)'!");
            } else {
                return WebAppIdentifier.forScope(parsedScope);
            }
        }
    }

    @VisibleForTesting
    public List<WebAppIdentifier> extractWebScopes(String webAppScopes) {
        if (StringUtils.isNullOrEmpty(webAppScopes)) {
            return new ArrayList<>();
        }
        try {
            return parseMultiple(webAppScopes);
        } catch (IllegalArgumentException e) {
            LOGGER.error("You passed an illegal argument as webapp scope", e);
        }
        return new ArrayList<>();
    }

    private List<WebAppIdentifier> parseMultiple(String toParse) {
        return Arrays.stream(toParse.split(","))
            .map(this::parseSingle)
            .collect(toList());
    }
}
