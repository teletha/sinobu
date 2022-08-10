<p align="center">
    <a href="https://docs.oracle.com/en/java/javase/11/"><img src="https://img.shields.io/badge/Java-Release%2011-green"/></a>
    <span>&nbsp;</span>
    <a href="https://jitpack.io/#teletha/sinobu"><img src="https://img.shields.io/jitpack/v/github/teletha/sinobu?label=Repository&color=green"></a>
    <span>&nbsp;</span>
    <a href="https://teletha.github.io/sinobu"><img src="https://img.shields.io/website.svg?down_color=red&down_message=CLOSE&label=Official%20Site&up_color=green&up_message=OPEN&url=https%3A%2F%2Fteletha.github.io%2Fsinobu"></a>
</p>


## Summary
Sinobu is not obsolete framework but utility, which can manipulate objects as a extremely-condensed facade.

This library aims to simplify and highly condense the functions related to domains that are frequently encountered in real-world development projects, making them easier to use.
* Dependency Injection
* Object lifecycle management
* JavaBeans-like property based type modeling
* HTTP(S)
* JSON
* HTML(XML)
* Reactive Programming (Rx)
* Asynchronous processing
* Parallel processing
* Multilingualization
* Template Engine (Mustache)
* Dynamic plug-in mechanism
* Domain specific languages
* Object Persistence
* Logging

With a few exceptions, Sinobu and its APIs are designed to be simple to use and easy to understand by adhering to the following principles.
* Keep it stupid simple
* Less is more
* Type safety
* Refactoring safety
<p align="right"><a href="#top">back to top</a></p>


## Usage
Create instance.
```java
class Some {
}

assert I.make(Some.class) != I.make(Some.class);;
```

Create singleton instance. (managed lifestyle)
```java
@Managed(Singleton.class)
class Some {
}

assert I.make(Some.class) == I.make(Some.class);
```

Enable dependency injection without configuration. (constructor injection)
```java
class Injected {
}

class Injectable {
    Injected injected;

    Injectable(Injected injected) {
        this.injected = injected;
    }
}

Injectable Injectable = I.make(Injectable.class);
assert Injectable.injected != null;
```

Read contents from HTTP.
```java
I.http("https://httpstat.us/200", String.class).to(text -> {
    // read as text
});

I.http("https://httpstat.us/200", JSON.class).to(json -> {
    // read as JSON
});

I.http("https://httpstat.us/200", XML.class).to(xml -> {
    // read as XML
});
```

Parse JSON.
```java
JSON json = I.json("""
        {
            "name": "忍",
            "age": 598
        }
        """);

// read value as String (shorthand)
assert json.text("name").equals("忍");

// read value as int
assert json.get("age").as(int.class) == 598;
```

<p align="right"><a href="#top">back to top</a></p>


## Prerequisites
Sinobu runs on all major operating systems and requires only [Java version 11](https://docs.oracle.com/en/java/javase/11/) or later to run.
To check, please run `java -version` from the command line interface. You should see something like this:
```
> java -version
openjdk version "16" 2021-03-16
OpenJDK Runtime Environment (build 16+36-2231)
OpenJDK 64-Bit Server VM (build 16+36-2231, mixed mode, sharing)
```
<p align="right"><a href="#top">back to top</a></p>

## Install
For any code snippet below, please substitute the version given with the version of Sinobu you wish to use.
#### [Maven](https://maven.apache.org/)
Add JitPack repository at the end of repositories element in your build.xml:
```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```
Add it into in the dependencies element like so:
```xml
<dependency>
    <groupId>com.github.teletha</groupId>
    <artifactId>sinobu</artifactId>
    <version>2.17.0</version>
</dependency>
```
#### [Gradle](https://gradle.org/)
Add JitPack repository at the end of repositories in your build.gradle:
```gradle
repositories } url "https://jitpack.io" }
}
```
Add it into the dependencies section like so:
```gradle
dependencies :sinobu:2.17.0'
}
```
#### [SBT](https://www.scala-sbt.org/)
Add JitPack repository at the end of resolvers in your build.sbt:
```scala
resolvers += "jitpack" at "https://jitpack.io"
```
Add it into the libraryDependencies section like so:
```scala
libraryDependencies += "com.github.teletha" % "sinobu" % "2.17.0"
```
#### [Leiningen](https://leiningen.org/)
Add JitPack repository at the end of repositories in your project.clj:
```clj
:repositories [["jitpack" "https://jitpack.io"]]
```
Add it into the dependencies section like so:
```clj
:dependencies [[com.github.teletha/sinobu "2.17.0"]]
```
#### [Bee](https://teletha.github.io/bee)
Add it into your project definition class like so:
```java
require("com.github.teletha", "sinobu", "2.17.0");
```
<p align="right"><a href="#top">back to top</a></p>


## Contributing
Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.
If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also simply open an issue with the tag "enhancement".
Don't forget to give the project a star! Thanks again!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

The overwhelming majority of changes to this project don't add new features at all. Optimizations, tests, documentation, refactorings -- these are all part of making this product meet the highest standards of code quality and usability.
Contributing improvements in these areas is much easier, and much less of a hassle, than contributing code for new features.

### Bug Reports
If you come across a bug, please file a bug report. Warning us of a bug is possibly the most valuable contribution you can make to Sinobu.
If you encounter a bug that hasn't already been filed, [please file a report](https://github.com/teletha/sinobu/issues/new) with an [SSCCE](http://sscce.org/) demonstrating the bug.
If you think something might be a bug, but you're not sure, ask on StackOverflow or on [sinobu-discuss](https://github.com/teletha/sinobu/discussions).
<p align="right"><a href="#top">back to top</a></p>


## Dependency
Sinobu depends on the following products on runtime.
* No Dependency

Sinobu depends on the following products on test.
* [HttpClientMock-1.0.0](https://mvnrepository.com/artifact/com.pgs-soft/HttpClientMock/1.0.0)
* [activation-1.1](https://mvnrepository.com/artifact/javax.activation/activation/1.1)
* [animal-sniffer-annotations-1.18](https://mvnrepository.com/artifact/org.codehaus.mojo/animal-sniffer-annotations/1.18)
* [antibug-1.3.0](https://mvnrepository.com/artifact/com.github.teletha/antibug/1.3.0)
* [apiguardian-api-1.1.2](https://mvnrepository.com/artifact/org.apiguardian/apiguardian-api/1.1.2)
* [byte-buddy-1.12.13](https://mvnrepository.com/artifact/net.bytebuddy/byte-buddy/1.12.13)
* [byte-buddy-agent-1.12.13](https://mvnrepository.com/artifact/net.bytebuddy/byte-buddy-agent/1.12.13)
* [compiler-0.9.10](https://mvnrepository.com/artifact/com.github.spullara.mustache.java/compiler/0.9.10)
* [disruptor-3.4.4](https://mvnrepository.com/artifact/com.lmax/disruptor/3.4.4)
* [fastjson-2.0.11.graal](https://mvnrepository.com/artifact/com.alibaba/fastjson/2.0.11.graal)
* [fastjson2-2.0.11.graal](https://mvnrepository.com/artifact/com.alibaba.fastjson2/fastjson2/2.0.11.graal)
* [fastjson2-extension-2.0.11.graal](https://mvnrepository.com/artifact/com.alibaba.fastjson2/fastjson2-extension/2.0.11.graal)
* [gson-2.9.1](https://mvnrepository.com/artifact/com.google.code.gson/gson/2.9.1)
* [hamcrest-all-1.3](https://mvnrepository.com/artifact/org.hamcrest/hamcrest-all/1.3)
* [jackson-annotations-2.13.3](https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-annotations/2.13.3)
* [jackson-core-2.13.3](https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-core/2.13.3)
* [jackson-databind-2.13.3](https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind/2.13.3)
* [javaee-api-8.0.1](https://mvnrepository.com/artifact/javax/javaee-api/8.0.1)
* [javax.mail-1.6.2](https://mvnrepository.com/artifact/com.sun.mail/javax.mail/1.6.2)
* [jmustache-1.15](https://mvnrepository.com/artifact/com.samskivert/jmustache/1.15)
* [jsoup-1.15.2](https://mvnrepository.com/artifact/org.jsoup/jsoup/1.15.2)
* [junit-jupiter-api-5.9.0-M1](https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-api/5.9.0-M1)
* [junit-jupiter-engine-5.9.0-M1](https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-engine/5.9.0-M1)
* [junit-jupiter-params-5.9.0-M1](https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-params/5.9.0-M1)
* [junit-platform-commons-1.9.0-M1](https://mvnrepository.com/artifact/org.junit.platform/junit-platform-commons/1.9.0-M1)
* [junit-platform-engine-1.9.0](https://mvnrepository.com/artifact/org.junit.platform/junit-platform-engine/1.9.0)
* [junit-platform-launcher-1.9.0](https://mvnrepository.com/artifact/org.junit.platform/junit-platform-launcher/1.9.0)
* [log4j-api-2.18.0](https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-api/2.18.0)
* [log4j-core-2.18.0](https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-core/2.18.0)
* [logback-classic-1.3.0-beta0](https://mvnrepository.com/artifact/ch.qos.logback/logback-classic/1.3.0-beta0)
* [logback-core-1.3.0-beta0](https://mvnrepository.com/artifact/ch.qos.logback/logback-core/1.3.0-beta0)
* [opentest4j-1.2.0](https://mvnrepository.com/artifact/org.opentest4j/opentest4j/1.2.0)
* [reactive-streams-1.0.4](https://mvnrepository.com/artifact/org.reactivestreams/reactive-streams/1.0.4)
* [rxjava-3.1.5](https://mvnrepository.com/artifact/io.reactivex.rxjava3/rxjava/3.1.5)
* [slf4j-api-2.0.0-beta1](https://mvnrepository.com/artifact/org.slf4j/slf4j-api/2.0.0-beta1)
* [slf4j-nop-2.0.0-beta1](https://mvnrepository.com/artifact/org.slf4j/slf4j-nop/2.0.0-beta1)
* [tinylog-api-2.5.0-M2.1](https://mvnrepository.com/artifact/org.tinylog/tinylog-api/2.5.0-M2.1)
* [tinylog-impl-2.5.0-M2.1](https://mvnrepository.com/artifact/org.tinylog/tinylog-impl/2.5.0-M2.1)
<p align="right"><a href="#top">back to top</a></p>


## License
Copyright (C) 2022 The SINOBU Development Team

MIT License

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
<p align="right"><a href="#top">back to top</a></p>