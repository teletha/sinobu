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

import static kiss.core.Group.*;
import static kiss.core.Person.*;

import org.junit.Test;

/**
 * @version 2015/04/17 22:25:35
 */
public class LensTest {

    @Test
    public void single() throws Exception {
        Person bean = new Person("test");

        bean = Lens.set(bean, _name_, "change");
    }

    @Test
    public void two() throws Exception {
        Person person = new Person("test");
        Group group = new Group(person);

        group = Lens.set(group, _leader_, _name_, "change");
    }
}
