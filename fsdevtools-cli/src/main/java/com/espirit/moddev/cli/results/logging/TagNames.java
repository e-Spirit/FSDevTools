package com.espirit.moddev.cli.results.logging;

//        TODO: CORE-9421: Remove string comparison and use TagNames-API
public enum TagNames {
    TEMPLATE("TEMPLATE"),
    SECTION("SECTION"),
    PAGE("PAGE"),
    PAGEREF("PAGEREF"),
    SCHEMA("SCHEMA"),
    PAGETEMPLATES("PAGETEMPLATES"),
    MEDIUM("MEDIUM"),
    LINKTEMPLATE("LINKTEMPLATE"),
    FORMATTEMPLATE("FORMATTEMPLATE"),
    MEDIANODE("MEDIANODE"),
    WORKFLOW("WORKFLOW");

    private String name;

    public String getName() {
        return name;
    }

    TagNames(String name) {
        this.name = name;
    }
}
