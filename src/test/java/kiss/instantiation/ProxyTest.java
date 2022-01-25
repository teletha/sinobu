/*
 * Copyright (C) 2022 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.instantiation;

import org.junit.jupiter.api.Test;

import kiss.I;

/**
 * @version 2017/04/28 23:59:32
 */
public class ProxyTest {

    @Test
    public void proxyPublic() {
        Public proxy = I.make(Public.class, (instance, method, args) -> "OK");
        assert proxy.text().equals("OK");
    }

    @Test
    public void proxyProtected() {
        Protected proxy = I.make(Protected.class, (instance, method, args) -> "OK");
        assert proxy.text().equals("OK");
    }

    @Test
    public void proxyPackagePrivate() {
        PackagePrivate proxy = I.make(PackagePrivate.class, (instance, method, args) -> "OK");
        assert proxy.text().equals("OK");
    }

    @Test
    public void proxyPrivate() {
        Private proxy = I.make(Private.class, (instance, method, args) -> "OK");
        assert proxy.text().equals("OK");
    }

    @Test
    public void proxyDefault() {
        Default proxy = I.make(Default.class, (instance, method, args) -> "OK");
        assert proxy.text().equals("OK");
    }

    /**
     * @version 2017/01/19 11:37:37
     */
    public static interface Public {
        String text();
    }

    /**
     * @version 2017/01/19 11:37:37
     */
    protected static interface Protected {
        String text();
    }

    /**
     * @version 2017/01/19 11:37:37
     */
    static interface PackagePrivate {
        String text();
    }

    /**
     * @version 2017/01/19 11:37:37
     */
    private static interface Private {
        String text();
    }

    /**
     * @version 2017/04/28 23:59:39
     */
    public static interface Default {
        default String text() {
            return "default";
        }
    }
}