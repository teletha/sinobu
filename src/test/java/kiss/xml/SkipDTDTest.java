/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.xml;

import org.junit.Test;

import kiss.I;
import ezunit.Ezunit;

/**
 * @version 2010/02/04 23:09:21
 */
public class SkipDTDTest {

    @Test
    public void skipDTD() {
        I.parse(Ezunit.locateSource("scanner/skipDTD.html"), new XMLScanner());
    }
}
