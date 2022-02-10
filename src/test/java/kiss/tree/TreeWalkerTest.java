/*
 * Copyright (C) 2022 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.tree;

import java.util.List;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.Signal;
import kiss.XML;

class TreeWalkerTest {

    @Test
    void walk() {
        HTML html = new HTML() {
            {
                $("html", () -> {
                    $("body", () -> {
                        $("h1", () -> {
                            $("span");
                            $("span");
                        });
                        $("h2", () -> {
                            $("div");
                        });
                        $("h3");
                    });
                });
            }
        };

        List<String> elements = parse(html).map(XML::name).toList();
        assert elements.size() == 8;
        assert elements.get(0).equals("html");
        assert elements.get(1).equals("body");
        assert elements.get(2).equals("h1");
        assert elements.get(3).equals("h2");
        assert elements.get(4).equals("h3");
        assert elements.get(5).equals("span");
        assert elements.get(6).equals("span");
        assert elements.get(7).equals("div");
    }

    private Signal<XML> parse(HTML html) {
        return I.signal(I.xml(html.toString())).recurseMap(e -> e.flatIterable(XML::children));
    }
}