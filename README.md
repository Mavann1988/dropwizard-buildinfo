# Overview

A library that supports exposure of build info in [Dropwizard](http://dropwizard.io/) applications. All main attributes in the Manifest will be present.

# Usage

First, add a dependency to your `build.gradle`.  Releases are published to
[Bintray JCenter](https://bintray.com/bintray/jcenter).  See the [changelog](CHANGES.md) for the latest version.

```groovy
dependencies {
    compile "com.commercehub.dropwizard:dropwizard-buildinfo:1.0.0"
}
```

Next, configure the `jar` task to populate your JAR with build info attributes:

```groovy
jar {
    manifest {
        attributes "Built-By": System.getProperty("user.name")
        attributes "Build-Jdk": System.getProperty("java.version")
        [
            "BUILD_ID": "Build-Id",
            "GIT_COMMIT": "Git-Commit",
            "GIT_BRANCH": "Git-Branch"
        ].each { envVar, attrName ->
            def envVal = System.getenv(envVar)
            if (envVal) {
                attributes([(attrName): envVal])
            }
        }
    }
}
```

Finally, add an instance of `BuildInfoServlet` to your application's `AdminEnvironment`:

```java
public class App extends Application<AppConfiguration> {

    @Override
    public void run(AppConfiguration config, Environment environment) {
        environment.admin()
                .addServlet("buildinfo", new BuildInfoServlet())
                .addMapping("/buildinfo/*");
    }
    
}
```

Now you can view the running application's build info at `/buildinfo` on the admin port (with Accept header set to "application/json" you get a json response, otherwise you'll get html):

```json
{
    "Built-By": "bamboo",
    "Build-Jdk": "1.8.0_05",
    "Build-Id": "APP-JOB1-1",
    "Git-Commit": "444b000aa3336992f073fe0000e3b22dc222226a",
    "Git-Branch": "master"
}
```


Now you can also view a particular property by running `/buildinfo/Built-By` on the admin port:

``` content-type:text/plain

    bamboo

```

# Development

## Releasing
Releases are uploaded to [Bintray](https://bintray.com/) via the
[gradle-release](https://github.com/townsfolk/gradle-release) plugin and
[gradle-bintray-plugin](https://github.com/bintray/gradle-bintray-plugin). To upload a new release, you need to be a
member of the [commercehub-oss Bintray organization](https://bintray.com/commercehub-oss). You need to specify your
Bintray username and API key when uploading. Your API key can be found on your
[Bintray user profile page](https://bintray.com/profile/edit). You can put your username and API key in
`~/.gradle/gradle.properties` like so:

    bintrayUserName = johndoe
    bintrayApiKey = 0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef

Then, to upload the release:

    ./gradlew release

Alternatively, you can specify your Bintray username and API key on the command line:

    ./gradlew -PbintrayUserName=johndoe -PbintrayApiKey=0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef release

The `release` task will prompt you to enter the version to be released, and will create and push a release tag for the
specified version. It will also upload the release artifacts to Bintray.

After the release artifacts have been uploaded to Bintray, they must be published to become visible to users. See
Bintray's [Publishing](https://bintray.com/docs/uploads/uploads_publishing.html) documentation for instructions.

After publishing the release on Bintray, it's also nice to create a GitHub release. To do so:
*   Visit the project's [releases](https://github.com/commercehub-oss/dropwizard-buildinfo/releases) page
*   Click the "Draft a new release" button
*   Select the tag that was created by the Gradle `release` task
*   Enter a title; typically, this should match the tag (e.g. "1.2.0")
*   Enter a description of what changed since the previous release (see the [changelog](CHANGES.md))
*   Click the "Publish release" button

# License
This library is available under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

(c) All rights reserved Commerce Technologies, Inc.
