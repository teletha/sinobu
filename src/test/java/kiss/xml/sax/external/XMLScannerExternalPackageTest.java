/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.xml.sax.external;

import kiss.xml.sax.XMLScanner;

import org.junit.Test;

/**
 * @version 2012/02/18 13:46:51
 */
public class XMLScannerExternalPackageTest {

    @Test
    public void externalPackage() throws Exception {
        assert new PrivateRuleScanner() != null;
    }

    /**
     * @version 2012/02/18 13:46:48
     */
    protected static class PrivateRuleScanner extends XMLScanner {

        public static final String XMLNS = "test";
    }
}
