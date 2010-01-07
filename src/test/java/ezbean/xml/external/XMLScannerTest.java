/*
 * Copyright (C) 2010 Nameless Production Committee.
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
package ezbean.xml.external;

import static org.junit.Assert.assertNotNull;


import org.junit.Test;

import ezbean.xml.XMLScanner;

/**
 * DOCUMENT.
 * 
 * @version 2008/11/22 4:02:40
 */
public class XMLScannerTest {

    /**
     * Private rule class. (Out of {@link XMLScanner} package)
     */
    @Test
    public void testPrivateClass() throws Exception {
        assertNotNull(new PrivateRuleScanner());
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/11/22 4:03:45
     */
    protected static class PrivateRuleScanner extends XMLScanner {

        public static final String XMLNS = "test";
    }
}
