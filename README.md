# Overview

A library that supports exposure of build info in [Dropwizard](http://dropwizard.io/) applications. Currently, it is
assumed that your project uses [Git](http://git-scm.com/) for version control. 

# Usage

First, add a dependency to your `build.gradle`.  Releases are published to
[Bintray JCenter](https://bintray.com/bintray/jcenter).  See the [changelog](../CHANGES.md) for the latest version.

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
        attributes "Build-Id": System.getenv("BUILD_ID")
        attributes "Git-Commit": System.getenv("GIT_COMMIT")
        attributes "Git-Branch": System.getenv("GIT_BRANCH")
    }
}
```

Finally, add an instance of `BuildInfoServlet` to your application's `AdminEnvironment`:

```java
public class App extends Application<AppConfiguration> {

    @Override
    public void run(AppConfiguration config, Environment environment) {
        environment.admin().addServlet("buildinfo", new BuildInfoServlet()).addMapping("/buildinfo");
    }
    
}
```

Now you can view the running application's build info at `/buildinfo` on the admin port:

```json
{
    Built-By: "bamboo",
    Build-Jdk: "1.8.0_05",
    Build-Id: "APP-JOB1-1",
    Git-Commit: "444b000aa3336992f073fe0000e3b22dc222226a",
    Git-Branch: "master"
}
```

# License
This library is available under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

(c) All rights reserved Commerce Technologies, Inc.
