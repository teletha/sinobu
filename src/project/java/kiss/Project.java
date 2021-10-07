/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import javax.lang.model.SourceVersion;

public class Project extends bee.api.Project {

    {
        product("com.github.teletha", "sinobu", ref("version.txt"));
        producer("Nameless Production Committee");
        describe("Sinobu is not obsolete framework but utility, which can manipulate objects as a extremely-condensed facade.");

        require(SourceVersion.latest(), SourceVersion.RELEASE_11, SourceVersion.latest());
        require("com.github.teletha", "antibug").atTest();
        require("com.pgs-soft", "HttpClientMock").atTest();
        require("io.reactivex.rxjava3", "rxjava").atTest();

        // For JSON benchmark
        require("com.fasterxml.jackson.core", "jackson-databind").atTest();
        require("com.google.code.gson", "gson").atTest();
        require("com.alibaba", "fastjson").atTest();

        // For logging benchmark
        require("org.apache.logging.log4j", "log4j-core").atTest();
        require("com.lmax", "disruptor").atTest();
        require("org.tinylog", "tinylog-impl").atTest();
        require("ch.qos.logback", "logback-classic").atTest();
        require("org.slf4j", "slf4j-nop").atTest();

        versionControlSystem("https://github.com/teletha/sinobu");
    }
}