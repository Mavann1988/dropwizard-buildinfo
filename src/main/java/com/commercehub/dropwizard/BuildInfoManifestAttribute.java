/*
 * Copyright (C) 2014 Commerce Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
