/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2020 e-Spirit AG
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

package com.espirit.moddev.shared.webapp;


import org.junit.Assert;
import org.junit.Test;

import static com.espirit.moddev.shared.webapp.WebAppIdentifier.forGlobalWebApp;
import static com.espirit.moddev.shared.webapp.WebAppIdentifier.forScope;
import static de.espirit.firstspirit.module.WebEnvironment.WebScope.*;

public class WebAppIdentifierTest {

    @Test
    public void testEqualIdentifiers() {
        Assert.assertEquals("Identifiers for Global WebApps with the same WebApp Id should be equal.",
                forGlobalWebApp("fs5root"), forGlobalWebApp("fs5root"));

        Assert.assertNotEquals("Identifiers for Global WebApps with different WebApp Ids should not be equal.",
                forGlobalWebApp("xyz"), forGlobalWebApp("abc"));

        Assert.assertEquals("Identifiers for the same scope should be equal.",
                forScope(PREVIEW), forScope(PREVIEW));

        Assert.assertNotEquals("Identifiers for the same project but different scopes should not be equal.",
                forScope(PREVIEW), forScope(STAGING));
    }
    @Test
    public void testFactoryForSimpleWebAppIdentifier() {
        WebAppIdentifier identifier = forScope(PREVIEW);
        Assert.assertNotNull(identifier);
        Assert.assertEquals("preview", identifier.toString());
    }

    @Test
    public void testFactoryWebAppNameForGlobalWebAppIdentifier() {
        WebAppIdentifier parsed = forGlobalWebApp("fs5root");
        Assert.assertTrue(parsed instanceof WebAppIdentifier.GlobalWebAppIdentifier);
        Assert.assertEquals(GLOBAL, parsed.getScope());
        Assert.assertEquals("fs5root", ((WebAppIdentifier.GlobalWebAppIdentifier) parsed).getGlobalWebAppId());
    }

    @Test
    public void testFactoryForPreviewWebAppIdentifier() {
        WebAppIdentifier identifier = forScope(PREVIEW);
        Assert.assertNotNull(identifier);
        Assert.assertEquals("preview", identifier.toString());
    }
}
