/*
 * Copyright (C) 2018 Nameless Production Committee
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

/**
 * @version 2017/03/30 12:11:35
 */
public class HTMLSoupTest {

    @Test
    public void noEndTag() throws Exception {
        XML xml = parse("<link>");
        assert xml.find("link").size() == 1;
    }

    @Test
    public void caseInconsistencyLowerUpper() throws Exception {
        XML xml = parse("<div>crazy</DIV>");
        assert xml.find("div").size() == 1;
    }

    @Test
    public void caseInconsistencyUpperLower() throws Exception {
        XML xml = parse("<DIV>crazy</div>");
        assert xml.find("div").size() == 1;
    }

    @Test
    public void rootMultiple() throws Exception {
        XML xml = I.xml("<html><body/></html><other><body/></other>");
        assert xml.find("body").size() == 2;
    }

    /**
     * <p>
     * Parse as HTML.
     * </p>
     */
    private XML parse(String html) {
        return I.xml("<html>" + html + "</html>");
    }
}
