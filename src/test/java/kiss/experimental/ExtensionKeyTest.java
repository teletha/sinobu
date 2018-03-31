/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.experimental;

import org.junit.jupiter.api.Test;

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
         * @see kiss.experimental.ExtensionKeyTest.ExtensionKeyProvider#key(java.lang.Class,
         *      java.lang.Class)
         */
        @Override
        public String key(Class extensionPoint, Class extension) {
            return null;
        }
    }

    /**
     * @version 2010/02/08 23:42:12
     */
    private static class BExtensionPointKeyProvider implements ExtensionKeyProvider<Class> {

        /**
         * @see kiss.experimental.ExtensionKeyTest.ExtensionKeyProvider#key(java.lang.Class,
         *      java.lang.Class)
         */
        @Override
        public Class key(Class extensionPoint, Class extension) {
            return null;
        }
    }

    /**
     * @version 2010/02/08 23:42:12
     */
    private static class CExtensionPointKeyProvider implements ExtensionKeyProvider<Class> {

        /**
         * @see kiss.experimental.ExtensionKeyTest.ExtensionKeyProvider#key(java.lang.Class,
         *      java.lang.Class)
         */
        @Override
        public Class key(Class extensionPoint, Class extension) {
            return null;
        }
    }

    @Test
    public void user() {
        AExtensionPoint extensionA1 = find(AExtensionPoint.class, "test");
        BExtensionPoint extensionB1 = find(BExtensionPoint.class, String.class);
        CExtensionPoint extensionC1 = find(CExtensionPoint.class, String.class);
        DExtensionPoint extensionD1 = find(DExtensionPoint.class, int.class);

        assert extensionA1 == null;
        assert extensionB1 == null;
        assert extensionC1 == null;
        assert extensionD1 == null;

        // should be compile error
        // find(AExtensionPoint.class, 1);
        // find(AExtensionPoint.class, Class.class);
        // find(BExtensionPoint.class, 1);
        // CExtensionPoint extensionC2 = find(CExtensionPoint.class, 1);
        // DExtensionPoint extensionD2 = find(DExtensionPoint.class, 1);
    }

    /**
     *
     *
     */
    public static <E extends Extension<EKP>, K, EKP extends ExtensionKeyProvider<K>> E find(Class<E> extensionPoint, K key) {
        return null;
    }
}
