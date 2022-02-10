/*
 * Copyright (C) 2022 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.xml;

import org.jsoup.Jsoup;

import antibug.profiler.Benchmark;
import kiss.I;

public class XMLParseBenchmark {

    private static final String html = "<html><head><title>First parse</title></head><body><p>Parsed HTML into a doc.</p></body></html>";

    public static void main(String[] args) {
        Benchmark benchmark = new Benchmark();

        benchmark.measure("Sinobu", () -> {
            return I.xml(html);
        });

        benchmark.measure("Jsoup", () -> {
            return Jsoup.parse(html);
        });

        benchmark.perform();
    }
}
