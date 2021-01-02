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

import bee.task.Test;

public class Project extends bee.api.Project {

    {
        product("com.github.teletha", "sinobu", "2.1.0");
        producer("Nameless Production Committee");
        describe("Sinobu is not obsolete framework but utility, which can manipulate objects as a extremely-condensed facade.");
        require(SourceVersion.latest(), SourceVersion.RELEASE_11);

        require("com.github.teletha", "antibug").atTest();
        require("com.pgs-soft", "HttpClientMock").atTest();
        require("io.reactivex.rxjava2", "rxjava").atTest();

        // For JSON Parser Benchmark
        require("com.fasterxml.jackson.core", "jackson-databind").atTest();
        require("com.google.code.gson", "gson").atTest();
        require("com.alibaba", "fastjson").atTest();

        versionControlSystem("https://github.com/teletha/sinobu");
    }

    {
        Test.showProlongedTest = 300;
    }
}