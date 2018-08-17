package com.espirit.moddev.moduleinstaller;

import com.google.common.base.Strings;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.agency.WebAppId;
import de.espirit.firstspirit.module.WebEnvironment.WebScope;

import static de.espirit.firstspirit.module.WebEnvironment.WebScope.GLOBAL;
import static java.util.Locale.UK;

public interface WebAppIdentifier {

    WebAppId createWebAppId(Project project);
    WebScope getScope();
    boolean isGlobal();

    WebAppIdentifier PREVIEW = forScope(WebScope.PREVIEW);
    WebAppIdentifier STAGING = forScope(WebScope.STAGING);
    WebAppIdentifier WEBEDIT = forScope(WebScope.WEBEDIT);
    WebAppIdentifier LIVE = forScope(WebScope.LIVE);

    WebAppIdentifier FS5_ROOT = forGlobalWebApp("fs5root");

    static WebAppIdentifier forScope(WebScope scope) {
        return forScope(scope, null);
    }

    static WebAppIdentifier forGlobalWebApp(String globalWebAppName) {
        return forScope(GLOBAL, globalWebAppName);
    }

    /**
     * TODO: Make this private/package private
     * @param scope
     * @param globalWebAppName
     * @return
     */
    static WebAppIdentifier forScope(WebScope scope, String globalWebAppName) {
        if(scope == null) {
            throw new IllegalArgumentException("Scope for WebApp identifier shouldn't be null!");
        }

        if(GLOBAL.equals(scope)) {
            if(Strings.isNullOrEmpty(globalWebAppName)) {
                throw new IllegalArgumentException("WebApp name missing for global WebApp.");
            }
            return new GlobalWebAppIdentifier(globalWebAppName);
        } else {
            return new SimpleWebAppIdentifier(scope);
        }


    }

    static boolean isFs5RootWebApp(WebAppId candidate) {
        return FS5_ROOT.createWebAppId(null).equals(candidate);
    }

    class GlobalWebAppIdentifier implements WebAppIdentifier {
        private final String globalWebAppId;

        private GlobalWebAppIdentifier(String globalWebAppId) {
            this.globalWebAppId = globalWebAppId;
        }

        @Override
        public WebAppId createWebAppId(Project project) {
            return WebAppId.Factory.create(globalWebAppId);
        }

        @Override
        public WebScope getScope() {
            return GLOBAL;
        }

        @Override
        public boolean isGlobal() { return true; }


        public String getGlobalWebAppId() {
            return globalWebAppId;
        }

        @Override
        public String toString() {
            return "global(" + globalWebAppId + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            GlobalWebAppIdentifier that = (GlobalWebAppIdentifier) o;

            return globalWebAppId != null ? globalWebAppId.equals(that.globalWebAppId) : that.globalWebAppId == null;
        }

        @Override
        public int hashCode() {
            return globalWebAppId != null ? globalWebAppId.hashCode() : 0;
        }
    }

    class SimpleWebAppIdentifier implements WebAppIdentifier {

        private final WebScope scope;

        private SimpleWebAppIdentifier(WebScope scope) {
            if(scope == null) {
                throw new IllegalArgumentException("Local WebAppIdentifier requires a scope, but given scope is null.");
            }

            this.scope = scope;
        }

        @Override
        public WebAppId createWebAppId(Project project) {
            if(project == null) {
                throw new IllegalArgumentException("Cannot create non global WebAppId with null project!");
            }
            return WebAppId.Factory.create(project, scope);
        }

        @Override
        public WebScope getScope() {
            return scope;
        }

        @Override
        public boolean isGlobal() { return false; }

        @Override
        public String toString() {
            return scope.toString().toLowerCase(UK);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SimpleWebAppIdentifier that = (SimpleWebAppIdentifier) o;

            return scope == that.scope;
        }

        @Override
        public int hashCode() {
            return scope != null ? scope.hashCode() : 0;
        }
    }

}
