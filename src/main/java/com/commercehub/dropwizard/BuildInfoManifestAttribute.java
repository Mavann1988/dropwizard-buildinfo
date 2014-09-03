package com.commercehub.dropwizard;

public enum BuildInfoManifestAttribute {

    BUILT_BY("Built-By"),
    BUILD_JDK("Build-Jdk"),
    BUILD_ID("Build-Id"),
    GIT_COMMIT("Git-Commit"),
    GIT_BRANCH("Git-Branch");

    private final String key;

    BuildInfoManifestAttribute(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

}
