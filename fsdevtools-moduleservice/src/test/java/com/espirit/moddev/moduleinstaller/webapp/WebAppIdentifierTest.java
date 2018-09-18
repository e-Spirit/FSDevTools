package com.espirit.moddev.moduleinstaller.webapp;

import com.espirit.moddev.moduleinstaller.WebAppIdentifier;
import com.espirit.moddev.moduleinstaller.WebAppIdentifier.GlobalWebAppIdentifier;
import org.junit.Assert;
import org.junit.Test;

import static com.espirit.moddev.moduleinstaller.WebAppIdentifier.forGlobalWebApp;
import static com.espirit.moddev.moduleinstaller.WebAppIdentifier.forScope;
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
        Assert.assertTrue(parsed instanceof GlobalWebAppIdentifier);
        Assert.assertEquals(GLOBAL, parsed.getScope());
        Assert.assertEquals("fs5root", ((GlobalWebAppIdentifier) parsed).getGlobalWebAppId());
    }

    @Test
    public void testFactoryForPreviewWebAppIdentifier() {
        WebAppIdentifier identifier = forScope(PREVIEW);
        Assert.assertNotNull(identifier);
        Assert.assertEquals("preview", identifier.toString());
    }
}