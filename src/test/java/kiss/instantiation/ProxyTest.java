/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.instantiation;

import org.junit.Test;

import kiss.I;

/**
 * @version 2017/01/20 9:33:41
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

    @Test(expected = IllegalAccessError.class)
    public void proxyPackagePrivate() {
        I.make(PackagePrivate.class, (instance, method, args) -> "ERROR");
    }

    @Test(expected = IllegalAccessError.class)
    public void proxyPrivate() {
        I.make(Private.class, (instance, method, args) -> "ERROR");
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
}
