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

public class HTMLSoupTest {

    @Test
    public void noEndTag() {
        XML root = parseAsHTML("<link>");

        assert root.find("link").size() == 1;
    }

    @Test
    public void caseInconsistencyLowerUpper() {
        XML root = parseAsHTML("<div>crazy</DIV>");

        assert root.find("div").size() == 1;
    }

    @Test
    public void caseInconsistencyUpperLower() {
        XML root = parseAsHTML("<DIV>crazy</div>");

        assert root.find("div").size() == 1;
    }

    @Test
    public void rootMultiple() {
        XML root = I.xml("<html><body/></html><other><body/></other>");

        assert root.find("body").size() == 2;
    }

    @Test
    public void slipOut() {
        XML root = parseAsHTML("<a><b>crazy</a></b>");

        assert root.find("a").text().equals("crazy");
        assert root.find("b").text().equals("crazy");
    }

    /**
     * Parse as HTML.
     */
    private XML parseAsHTML(String html) {
        return I.xml("<html>" + html + "</html>");
    }
}