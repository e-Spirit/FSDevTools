package com.espirit.moddev.shared;

import org.junit.Test;

import static org.junit.Assert.*;

public class StringUtilsTest {

    @Test
    public void isNullOrEmpty() {
        assertTrue(StringUtils.isNullOrEmpty(null));
        assertTrue(StringUtils.isNullOrEmpty(""));
        assertTrue(StringUtils.isNullOrEmpty(" "));
        assertFalse(StringUtils.isNullOrEmpty("Test"));
    }

    @Test
    public void isEmpty() {
        assertTrue(StringUtils.isNullOrEmpty(""));
        assertTrue(StringUtils.isNullOrEmpty(" "));
        assertFalse(StringUtils.isNullOrEmpty("Test"));
    }

    @Test(expected = NullPointerException.class)
    public void whenExceptionThrown_thenExpectationSatisfied() {
        StringUtils.isEmpty(null);
    }
}