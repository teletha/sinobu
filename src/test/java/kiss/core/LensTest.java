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
import static kiss.core.PersonOperator.*;
import kiss.I;

import org.junit.Test;

/**
 * @version 2015/04/17 22:25:35
 */
public class LensTest {

    static {
        I.load(LensTest.class, true);
    }

    @Test
    public void single() throws Exception {
        Person bean = new Person("name", 10);
        assert bean.age == 10;

        bean = Lens.set(bean, _age_, 20);
        assert bean.age == 20;

        bean = Lens.set(bean, _age_(30), _name_("set"));
        assert bean.age == 30;
        assert bean.name.equals("set");
    }

    @Test
    public void two() throws Exception {
        Person person = new Person("test", 10);
        Group group = new Group(person);

        group = Lens.set(group, _leader_, _name_, "change");
        assert group.leader.name.equals("change");
    }

    @Test
    public void extend() throws Exception {
        Worker person = new Worker("test", 10, "neko");
        Group group = new Group(person);
        assert group.leader instanceof Worker;

        group = Lens.set(group, _leader_, _name_, "change");
        assert group.leader instanceof Worker;
        assert group.leader.name.equals("change");
    }
}
