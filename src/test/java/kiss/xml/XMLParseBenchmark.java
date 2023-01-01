/*
 * Copyright (C) 2023 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.xml;

import org.htmlcleaner.HtmlCleaner;
import org.jsoup.Jsoup;

import antibug.profiler.Benchmark;
import kiss.I;

public class XMLParseBenchmark {

    private static final String html = "<html><head><title>First parse</title></head><body><p>Parsed HTML into a doc.</p></body></html>";

    public static void main(String[] args) {
        Benchmark benchmark = new Benchmark().visualize();

        benchmark.measure("Sinobu", () -> {
            return I.xml(html);
        });

        benchmark.measure("Jsoup", () -> {
            return Jsoup.parse(html);
        });

        HtmlCleaner cleaner = new HtmlCleaner();
        benchmark.measure("HTMLCleaner", () -> {
            return cleaner.clean(html);
        });

        benchmark.perform();
    }
}