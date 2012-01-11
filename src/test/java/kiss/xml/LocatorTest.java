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

import static ezunit.Ezunit.*;

import java.nio.file.Path;

import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

import kiss.I;

/**
 * @version 2011/04/11 11:30:24
 */
public class LocatorTest {

    @Test
    public void publicId() {
        InputSource source = locateSource("dummy.xml");
        source.setPublicId("dummy.xml");

        Chaser chaser = new Chaser();

        // parse
        I.parse(source, chaser);

        // assert
        assert chaser.locator.getPublicId().equals("dummy.xml");
    }

    @Test
    public void publicIdFromPath() {
        Path path = locate("dummy.xml");
        Chaser chaser = new Chaser();

        // parse
        I.parse(path, chaser);

        // assert
        assert chaser.locator.getPublicId().equals(path.toAbsolutePath().toString());
    }

    /**
     * @version 2011/04/11 11:30:28
     */
    private static class Chaser extends XMLScanner {

        private Locator locator;

        /**
         * @see org.xml.sax.helpers.XMLFilterImpl#setDocumentLocator(org.xml.sax.Locator)
         */
        @Override
        public void setDocumentLocator(Locator locator) {
            this.locator = new LocatorImpl(locator);
        }
    }
}
