/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.xml.sax;

import static antibug.AntiBug.*;

import java.nio.file.Files;
import java.nio.file.Path;

import kiss.I;

import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

/**
 * @version 2012/02/18 14:58:51
 */
public class LocatorTest {

    @Test
    public void publicId() throws Exception {
        InputSource source = new InputSource(Files.newInputStream(locate("dummy.xml")));
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
     * @version 2012/02/18 14:58:54
     */
    private static class Chaser extends XMLScanner {

        private Locator locator;

        /**
         * {@inheritDoc}
         */
        @Override
        public void setDocumentLocator(Locator locator) {
            this.locator = new LocatorImpl(locator);
        }
    }
}
