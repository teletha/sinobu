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

import kiss.XML;

public class FindAssetion {

    public static boolean select(XML xml, int expectedCount, String selector) {
        return select(xml, expectedCount, selector, true);
    }

    public static boolean select(XML xml, int expectedCount, String selector, boolean spacer) {
        assert xml.find(selector).size() == expectedCount;

        if (spacer) {
            String[] spaces = {" ", "  ", "\t"};
            for (String s : spaces) {
                String spaced = selector.replaceAll("(\\^=|\\$=|~=|\\|=|\\*=|=|,|<|>|~|\\+)", s + "$1" + s)
                        .replace("[", "[" + s)
                        .replace("]", s + "]");
                assert xml.find(s + spaced + s).size() == expectedCount;
            }
        }
        return true;
    }
}
