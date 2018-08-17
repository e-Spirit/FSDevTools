package com.espirit.moddev.cli.api.validation;

/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2016 e-Spirit AG
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License"),
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * *********************************************************************
 *
 */

import java.util.Objects;

/**
 * The type Voilation stores a result of a check.
 */
public class Voilation {

    private String field;
    private String message;

    /**
     * Instantiates a new Voilation.
     *
     * @param field   the field
     * @param message the message
     */
    public Voilation(String field, String message) {
        this.field = field;
        this.message = message;
    }

    /**
     * Get the field.
     *
     * @return the field
     */
    public String getField() {
        return field;
    }

    /**
     * Get the message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Voilation)) {
            return false;
        }
        Voilation voilation = (Voilation) o;
        return Objects.equals(field, voilation.field) &&
               Objects.equals(message, voilation.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, message);
    }

    @Override
    public String toString() {
        return field + ' ' + message;
    }
}
