/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.xml;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.XML;

class HTMLSoupTest {

    @Test
    void noEndTag() {
        XML xml = parseAsHTML("<link>");
        assert xml.find("link").size() == 1;
    }

    @Test
    void caseInconsistencyLowerUpper() {
        XML xml = parseAsHTML("<div>crazy</DIV>");
        assert xml.find("div").size() == 1;
    }

    @Test
    void caseInconsistencyUpperLower() {
        XML xml = parseAsHTML("<DIV>crazy</div>");
        assert xml.find("div").size() == 1;
    }

    @Test
    void rootMultiple() {
        XML xml = I.xml("<html><body/></html><other><body/></other>");
        assert xml.find("body").size() == 2;
    }

    @Test
    void slipOut() {
        XML xml = parseAsHTML("<a><b>crazy</a></b>");
        assert xml.find("a").text().equals("crazy");
        assert xml.find("b").text().equals("crazy");
    }

    /**
     * Parse as HTML.
     */
    private XML parseAsHTML(String html) {
        return I.xml("<html>" + html + "</html>");
    }
}