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

/**
 * @version 2017/04/18 21:05:51
 */
public class TreeWalkerTest {

    @Test
    public void walk() throws Exception {
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
        assert elements.get(0) == "html";
        assert elements.get(1) == "body";
        assert elements.get(2) == "h1";
        assert elements.get(3) == "h2";
        assert elements.get(4) == "h3";
        assert elements.get(5) == "span";
        assert elements.get(6) == "span";
        assert elements.get(7) == "div";
    }

    private Signal<XML> parse(HTML html) {
        return I.signal(I.xml(html.toString())).recurseMap(e -> e.flatIterable(XML::children));
    }
}