/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kiss.I;
import kiss.sample.bean.Person;
import kiss.sample.bean.School;

import org.junit.Test;

/**
 * @version 2011/03/22 16:56:52
 */
public class PropertyComparatorTest {

    static {
        // When we activates only this test, it throws initialization error, so we have to load I
        // class at first.
        I.locate("");
    }

    /**
     * Test order.
     */
    @Test
    public void testCompare() {
        List<Property> properties = new ArrayList();

        properties.add(new Property(Model.of(School.class), "aaa"));
        properties.add(new Property(Model.of(String.class), "string"));
        properties.add(new Property(Model.of(int.class), "int"));
        properties.add(new Property(Model.of(Person.class), "abc"));
        properties.add(new Property(Model.of(String.class), "first"));

        // create innocuous BeanModel as property comparator and sort above properties by it
        Collections.sort(properties);

        assert "first" == properties.get(0).name;
        assert "int" == properties.get(1).name;
        assert "string" == properties.get(2).name;
        assert "aaa" == properties.get(3).name;
        assert "abc" == properties.get(4).name;
    }
}
