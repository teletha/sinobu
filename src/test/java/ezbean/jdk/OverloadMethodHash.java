/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package ezbean.jdk;

import java.lang.reflect.Method;
import java.util.Objects;

import org.junit.Test;

/**
 * @version 2011/12/19 0:50:10
 */
public class OverloadMethodHash {

    @Test
    public void hash() throws Exception {
        Method method1 = Target.class.getMethod("test");
        Method method2 = Target.class.getMethod("test", int.class);

        assert method1.hashCode() == method2.hashCode();
        assert Objects.hash(method1.getName(), method1.getParameterTypes()) != Objects.hash(method2.getName(), method2.getParameterTypes());
    }

    /**
     * @version 2011/12/19 0:51:07
     */
    @SuppressWarnings("unused")
    private static class Target {

        public void test() {
        }

        public void test(int index) {
        }
    }
}
