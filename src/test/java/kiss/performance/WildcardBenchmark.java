/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.performance;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;

import kiss.scratchpad.Wildcard;

import org.junit.Rule;
import org.junit.Test;

import antibug.benchmark.Benchmark;
import antibug.benchmark.Benchmark.Code;

/**
 * @version 2012/01/31 16:18:56
 */
public class WildcardBenchmark {

    @Rule
    public static final Benchmark benchmark = new Benchmark();

    Path path = Paths.get(new File("").getAbsolutePath());

    @Test
    public void wild() throws Exception {
        benchmark.measure(new Code() {

            private Wildcard wildcard = new Wildcard("**.java");

            @Override
            public Object measure() throws Throwable {
                return wildcard.match(path.toString());
            }
        });
    }

    @Test
    public void system() throws Exception {
        benchmark.measure(new Code() {

            private PathMatcher wildcard = FileSystems.getDefault().getPathMatcher("glob:*.java");

            @Override
            public Object measure() throws Throwable {
                return wildcard.matches(path);
            }
        });
    }
}
