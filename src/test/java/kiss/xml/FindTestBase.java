/*
 * Copyright (C) 2025 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.xml;

import java.util.Arrays;
import java.util.stream.Collectors;

import kiss.XML;

public class FindTestBase {

    protected final void validateSelector(XML xml, int expectedCount, String... selectorParts) {
        String selector = Arrays.stream(selectorParts).collect(Collectors.joining(""));
        assert xml.find(selector).size() == expectedCount;

        String spacedSelector = Arrays.stream(selectorParts).map(v -> " " + v + " ").collect(Collectors.joining(""));
        assert xml.find(spacedSelector).size() == expectedCount;

        String tabbedSelector = Arrays.stream(selectorParts).map(v -> "\t" + v + "\t").collect(Collectors.joining(""));
        assert xml.find(tabbedSelector).size() == expectedCount;
    }

    protected final void validateAttributeSelector(XML xml, int expectedCount, String... selectorParts) {
        String selector = Arrays.stream(selectorParts).collect(Collectors.joining(""));
        assert xml.find(selector).size() == expectedCount;

        String[] spaces = {" ", "  ", "\t"};
        for (String s : spaces) {
            String spaced = selector.replaceAll("(\\^=|\\$=|~=|\\|=|\\*=|=)", s + "$1" + s).replace("[", "[" + s).replace("]", s + "]");
            assert xml.find(spaced).size() == expectedCount;
        }
    }
}
