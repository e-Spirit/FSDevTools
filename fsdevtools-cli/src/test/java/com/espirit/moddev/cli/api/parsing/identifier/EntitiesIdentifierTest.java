/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2021 e-Spirit GmbH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
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

package com.espirit.moddev.cli.api.parsing.identifier;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class EntitiesIdentifierTest {

    @Test
    public void testNullUid() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new EntitiesIdentifier(null));
    }

    @Test
    public void testEquality() {
        EntitiesIdentifier identifier = new EntitiesIdentifier("products");
        EntitiesIdentifier equalIdentifier = new EntitiesIdentifier("products");

        EntitiesIdentifier anUnequalIdentifier = new EntitiesIdentifier("news");

        assertThat("Expected an entities identifier to be equal to itself", identifier, equalTo(identifier));
        assertThat("Expected two equal entities identifiers for equal uidType", identifier, equalTo(equalIdentifier));
        assertThat("Expected two different entities identifiers to not be equal", identifier, not(equalTo(anUnequalIdentifier)));
    }
}
