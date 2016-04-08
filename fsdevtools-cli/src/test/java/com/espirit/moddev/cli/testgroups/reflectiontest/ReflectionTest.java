package com.espirit.moddev.cli.testgroups.reflectiontest;

import com.espirit.moddev.cli.reflection.ReflectionUtils;

import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * @author e-Spirit AG
 */
public class ReflectionTest {

    @Test
    public void readsGroupDescriptionFromAnnotatedMethodTest() {
        assertEquals("Description is expected to be xyz non null from getMyCustomDescription()",
                     "xyz", ReflectionUtils.getDescriptionFromClass(GroupWithDescriptionAnnotation.class));
    }

    @Test
    public void readsGroupDescriptionFromNonAnnotatedMethodWithNamingConventionTest() {
        assertEquals("Description is expected to be abc non null from getDescription()",
                     "abc", ReflectionUtils.getDescriptionFromClass(GroupWithDescriptionMethod.class));
    }

    @Test
    public void readsGroupDescriptionAsEmptyFromDescriptionMethodReturnsVoidTest() {
        assertTrue("Description is expected to be empty from getDescription()",
                   ReflectionUtils.getDescriptionFromClass(GroupWithVoidDescriptionMethod.class).isEmpty());
    }

    @Test
    public void readsGroupDescriptionAsEmptyFromDescriptionMethodReturnsNonStringTest() {
        assertTrue("Description is expected to be empty from getDescription()",
                   ReflectionUtils.getDescriptionFromClass(GroupWithNonStringDescriptionMethod.class).isEmpty());
    }

    @Test
    public void readsGroupWithoutDescriptionAnnotationAndVoidDescriptionMethodTest() {
        assertTrue("Description is expected to be empty from getDescription()",
                   ReflectionUtils.getDescriptionFromClass(GroupWithoutDescriptionAnnotationAndVoidDescriptionMethod.class).isEmpty());
    }
}
