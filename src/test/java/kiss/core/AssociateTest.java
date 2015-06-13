/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.core;

import kiss.I;
import kiss.sample.bean.Person;

import org.junit.Test;

/**
 * @version 2014/01/30 10:59:19
 */
public class AssociateTest {

    @Test
    public void access() throws Exception {
        Some some = I.make(Some.class);

        Person person = I.associate(some, Person.class);
        assert person != null;

        Person same = I.associate(some, Person.class);
        assert person == same;
    }

    /**
     * @version 2014/01/30 11:01:51
     */
    protected static class Some {
    }
}
