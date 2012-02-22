/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.module;

import java.security.CodeSource;

import org.junit.Test;

import kiss.I;
import kiss.sample.bean.Person;

/**
 * @version 2011/03/22 17:07:03
 */
public class CodeSourceTest {

    @Test
    public void testCodeSource() {
        Person person = I.make(Person.class);
        assert Person.class != person.getClass();

        CodeSource source1 = person.getClass().getProtectionDomain().getCodeSource();
        CodeSource source2 = Person.class.getProtectionDomain().getCodeSource();
        assert source2.equals(source1);
    }
}
