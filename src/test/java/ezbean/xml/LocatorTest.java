/*
 * Copyright (C) 2011 Nameless Production Committee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezbean.xml;

import static ezunit.Ezunit.*;

import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

import ezbean.I;

/**
 * @version 2011/03/22 17:24:26
 */
public class LocatorTest {

    /**
     * Locator.
     */
    @Test
    public void testLocator() {
        InputSource source = locateSource("dummy.xml");
        source.setPublicId("dummy.xml");

        Chaser chaser = new Chaser();

        // parse
        I.parse(source, chaser);

        // assert
        assert chaser.locator.getPublicId().equals("dummy.xml");
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/11/23 13:50:48
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
