<p align="center">
    <a href="https://docs.oracle.com/en/java/javase/21/"><img src="https://img.shields.io/badge/Java-Release%2021-green"/></a>
    <span>&nbsp;</span>
    <a href="https://jitpack.io/#teletha/sinobu"><img src="https://img.shields.io/jitpack/v/github/teletha/sinobu?label=Repository&color=green"></a>
    <span>&nbsp;</span>
    <a href="https://teletha.github.io/sinobu"><img src="https://img.shields.io/website.svg?down_color=red&down_message=CLOSE&label=Official%20Site&up_color=green&up_message=OPEN&url=https%3A%2F%2Fteletha.github.io%2Fsinobu"></a>
</p>

## Summary
Sinobu is not obsolete framework but utility, which can manipulate objects as a extremely-condensed facade.
This is extremely lightweight at approximately 120 KB without relying on other libraries, and its various operations are designed to run as fast as other competing libraries.

This library aims to simplify and highly condense the functions related to domains that are frequently encountered in real-world development projects, making them easier to use.
- [Dependency Injection](https://en.wikipedia.org/wiki/Dependency_injection)
- Object lifecycle management
- Property based object modeling
- HTTP(S) Client
- Web Socket Client
- [JSON](https://en.wikipedia.org/wiki/JSON)
- [HTML](https://en.wikipedia.org/wiki/HTML) (including Tag Soup)
- [XML](https://en.wikipedia.org/wiki/XML)
- Reactive Programming ([Rx](http://reactivex.io))
- Asynchronous & Parallel processing
- Multilingualization
- Template Engine ([Mustache](https://mustache.github.io/mustache.5.html))
- Dynamic plug-in mechanism
- Object Persistence
- Logging (Garbage-Free)
- Virtual Job Scheduler
- [Cron](https://en.wikipedia.org/wiki/Cron) Scheduling

With a few exceptions, Sinobu and its APIs are designed to be simple to use and easy to understand by adhering to the following principles.
- Keep it stupid simple
- Less is more
- Type safety
- Refactoring safety
<p align="right"><a href="#top">back to top</a></p>


## Usage
Create instant from type.
```java
class Some {
}

assert I.make(Some.class) != I.make(Some.class);
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

Parse XML/HTML. (accept tag soup)
```java
XML html = I.xml("""
        <html>
            <body>
                <h1>Heading</h1>
                <div class="age">598</div>
                <p>contents</p>
                <div class="author">忍</p>
            </body>
        </html>
        """);

// select the element by CSS selector and read its text content
assert html.find("p").text().equals("contents");
assert html.find(".author").text().equals("忍");
```

Reactive stream. (Rx)
```java
String result = I.signal("This", "is", "reactive", "stream")
        .skip(2)
        .map(String::toUpperCase)
        .scan(Collectors.joining(" "))
        .to()
        .exact();

assert result.equals("REACTIVE STREAM");
```

Evaluate expression language. (Mustache-like syntax)
```java
Person person = new Person();
person.name = "忍";
person.age = 598;

assert I.express("{name} is {age} years old.", person).equals("忍 is 598 years old.");
```

Write log message on console, file and user-defined appender.
```java
I.trace("Default logging level is INFO.");

I.debug("your.logger.name", "Different logger names can be used for different output settings.");

I.info("system", "The default logger name is [system].");

I.warn("""
        The following settings can be changed for each logger:
            * log level
            * displying caller location
            * output directory of log file
            * whether the log file is overwritten or appended
            * the number of days the log file is kept
        """);

I.error((Supplier) () -> "Use a lambda expression to delay message building.");
```

<p align="right"><a href="#top">back to top</a></p>


## Benchmark
### Logging
<img src="/benchmark/LogBenchmark.svg" width="700">

### JSON (Small) Parsing
<img src="/benchmark/JSONParseShortBenchmark.svg" width="700">

### JSON (Large) Parsing
<img src="/benchmark/JSONParseLongBenchmark.svg" width="700">

### JSON (Huge) Parsing
<img src="/benchmark/JSONParseHugeBenchmark.svg" width="700">

### JSON Travesing
<img src="/benchmark/JSONTraverseBenchmark.svg" width="700">

### JSON Mapping
<img src="/benchmark/JSONMappingBenchmark.svg" width="700">

### HTML Parsing
<img src="/benchmark/XMLParseBenchmark.svg" width="700">

### Mastache
<img src="/benchmark/ExpressionBenchmark.svg" width="700">

<p align="right"><a href="#top">back to top</a></p>


## Prerequisites
Sinobu runs on all major operating systems and requires only [Java version 21](https://docs.oracle.com/en/java/javase/21/) or later to run.
To check, please run `java -version` on your terminal.
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
    <version>4.4.0</version>
</dependency>
```
#### [Gradle](https://gradle.org/)
Add JitPack repository at the end of repositories in your build.gradle:
```gradle
repositories {
    maven { url "https://jitpack.io" }
}
```
Add it into the dependencies section like so:
```gradle
dependencies {
    implementation 'com.github.teletha:sinobu:4.4.0'
}
```
#### [SBT](https://www.scala-sbt.org/)
Add JitPack repository at the end of resolvers in your build.sbt:
```scala
resolvers += "jitpack" at "https://jitpack.io"
```
Add it into the libraryDependencies section like so:
```scala
libraryDependencies += "com.github.teletha" % "sinobu" % "4.4.0"
```
#### [Leiningen](https://leiningen.org/)
Add JitPack repository at the end of repositories in your project.clj:
```clj
:repositories [["jitpack" "https://jitpack.io"]]
```
Add it into the dependencies section like so:
```clj
:dependencies [[com.github.teletha/sinobu "4.4.0"]]
```
#### [Bee](https://teletha.github.io/bee)
Add it into your project definition class like so:
```java
require("com.github.teletha", "sinobu", "4.4.0");
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
<p align="right"><a href="#top">back to top</a></p>


## License
Copyright (C) 2024 The SINOBU Development Team

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