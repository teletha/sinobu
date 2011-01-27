/**
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
package ezbean.scratchpad;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @version 2010/02/08 23:39:34
 */
public class ExtensionKeyTest {

    /**
     * @version 2010/02/08 23:40:51
     */
    static interface Extension<KP extends ExtensionKeyProvider> {
    }

    /**
     * @version 2010/02/08 23:40:02
     */
    static interface ExtensionKeyProvider<K> {

        K key(Class extensionPoint, Class extension);
    }

    /**
     * @version 2010/02/08 23:41:28
     */
    static interface AExtensionPoint extends Extension<AExtensionPointKeyProvider> {
    }

    /**
     * @version 2010/02/08 23:41:28
     */
    static interface BExtensionPoint extends Extension<BExtensionPointKeyProvider> {
    }

    /**
     * @version 2010/02/08 23:41:28
     */
    static interface CExtensionPoint<T> extends Extension<CExtensionPointKeyProvider> {
    }

    /**
     * @version 2010/02/08 23:41:28
     */
    static interface DExtensionPoint<T> extends Extension {
    }

    /**
     * @version 2010/02/08 23:42:12
     */
    private static class AExtensionPointKeyProvider implements ExtensionKeyProvider<String> {

        /**
         * @see ezbean.scratchpad.ExtensionKeyTest.ExtensionKeyProvider#key(java.lang.Class,
         *      java.lang.Class)
         */
        public String key(Class extensionPoint, Class extension) {
            return null;
        }
    }

    /**
     * @version 2010/02/08 23:42:12
     */
    private static class BExtensionPointKeyProvider implements ExtensionKeyProvider<Class> {

        /**
         * @see ezbean.scratchpad.ExtensionKeyTest.ExtensionKeyProvider#key(java.lang.Class,
         *      java.lang.Class)
         */
        public Class key(Class extensionPoint, Class extension) {
            return null;
        }
    }

    /**
     * @version 2010/02/08 23:42:12
     */
    private static class CExtensionPointKeyProvider implements ExtensionKeyProvider<Class> {

        /**
         * @see ezbean.scratchpad.ExtensionKeyTest.ExtensionKeyProvider#key(java.lang.Class,
         *      java.lang.Class)
         */
        public Class key(Class extensionPoint, Class extension) {
            return null;
        }
    }

    @Test
    public void user() {
        AExtensionPoint extensionA1 = EzbeanScratchpad.find(AExtensionPoint.class, "test");
        BExtensionPoint extensionB1 = EzbeanScratchpad.find(BExtensionPoint.class, String.class);
        CExtensionPoint extensionC1 = EzbeanScratchpad.find(CExtensionPoint.class, String.class);
        DExtensionPoint extensionD1 = EzbeanScratchpad.find(DExtensionPoint.class, int.class);

        assertNull(extensionA1);
        assertNull(extensionB1);
        assertNull(extensionC1);
        assertNull(extensionD1);

        // compile error
        // AExtensionPoint extensionA2 = EzbeanScratchpad.find(AExtensionPoint.class, 1);
        // BExtensionPoint extensionB2 = EzbeanScratchpad.find(BExtensionPoint.class, 1);
        // CExtensionPoint extensionC2 = EzbeanScratchpad.find(CExtensionPoint.class, 1);
        // DExtensionPoint extensionD2 = EzbeanScratchpad.find(DExtensionPoint.class, 1);
    }
}
