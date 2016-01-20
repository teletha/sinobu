/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.module;

import java.security.CodeSource;

import kiss.I;
import kiss.sample.RuntimeAnnotation1;

import org.junit.Test;

/**
 * @version 2013/10/23 11:56:30
 */
public class CodeSourceTest {

    @Test
    public void testCodeSource() {
        Some some = I.make(Some.class);
        assert Some.class != some.getClass();

        CodeSource source1 = some.getClass().getProtectionDomain().getCodeSource();
        CodeSource source2 = Some.class.getProtectionDomain().getCodeSource();
        assert source2.equals(source1);
    }

    /**
     * @version 2013/10/23 11:55:55
     */
    protected static class Some {

        @RuntimeAnnotation1
        public void test() {
        }
    }
}
