/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import static bee.api.License.*;

import javax.lang.model.SourceVersion;

public class Project extends bee.api.Project {

    {
        product("com.github.teletha", "sinobu", ref("version.txt"));
        license(MIT);
        describe("""
                Sinobu is not obsolete framework but utility, which can manipulate objects as a extremely-condensed facade.
                This is extremely lightweight at approximately 120 KB without relying on other libraries, and its various operations are designed to run as fast as other competing libraries.

                This library aims to simplify and highly condense the functions related to domains that are frequently encountered in real-world development projects, making them easier to use.
                - [Dependency Injection](https://en.wikipedia.org/wiki/Dependency_injection)
                - Object lifecycle management
                - Property based object modeling
                - HTTP(S)
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
                """);

        require(SourceVersion.RELEASE_21, SourceVersion.RELEASE_21);

        require("com.github.teletha", "antibug").atTest();
        require("com.pgs-soft", "HttpClientMock").atTest();
        require("io.reactivex.rxjava3", "rxjava").atTest();
        require("javax", "javaee-api").atTest();

        // For JSON benchmark
        require("com.fasterxml.jackson.core", "jackson-databind").atTest();
        require("com.fasterxml.jackson.module", "jackson-module-afterburner").atTest();
        require("com.google.code.gson", "gson").atTest();
        require("com.alibaba.fastjson2", "fastjson2").atTest();

        // For XML benchmark
        require("org.jsoup", "jsoup").atTest();
        require("net.sourceforge.htmlcleaner", "htmlcleaner").atTest();

        // For Mustache benchmark
        require("com.github.spullara.mustache.java", "compiler").atTest();
        require("com.samskivert", "jmustache").atTest();

        // For logging benchmark
        require("org.apache.logging.log4j", "log4j-core").atTest();
        require("com.lmax", "disruptor", "3.4.4").atTest();
        require("org.tinylog", "tinylog-impl").atTest();
        require("ch.qos.logback", "logback-classic").atTest();

        versionControlSystem("https://github.com/teletha/sinobu");
    }

    public static class Jar extends bee.task.Jar {
        {
            removeDebugInfo = true;
            removeTraceInfo = false;
        }
    }
}