/*
 * Copyright (C) 2012 Nameless Production Committee
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

import org.junit.Test;

import testament.AbstractMicroBenchmarkTest;
import testament.BenchmarkCode;

import kiss.scratchpad.Wildcard;

/**
 * @version 2011/02/19 14:15:17
 */
public class WildcardBenchmark extends AbstractMicroBenchmarkTest {

    Path path = Paths.get(new File("").getAbsolutePath());

    @Test
    public void wild() throws Exception {
        benchmark(new BenchmarkCode() {

            private Wildcard wildcard = new Wildcard("**.java");

            /**
             * @see java.util.concurrent.Callable#call()
             */
            public Boolean call() throws Throwable {
                return wildcard.match(path.toString());
            }
        });
    }

    @Test
    public void system() throws Exception {
        benchmark(new BenchmarkCode() {

            private PathMatcher wildcard = FileSystems.getDefault().getPathMatcher("glob:*.java");

            /**
             * @see java.util.concurrent.Callable#call()
             */
            public Boolean call() throws Throwable {
                return wildcard.matches(path);
            }
        });
    }
}
