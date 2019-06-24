package com.espirit.moddev.shared.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that the visibility of a type or member has been relaxed to make the code serializable.
 */
@Retention(RetentionPolicy.SOURCE)
public @interface RequiredForSerialization {
}