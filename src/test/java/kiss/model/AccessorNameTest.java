/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.model;

import org.junit.Test;

import kiss.I;
import kiss.sample.bean.Person;
import kiss.sample.bean.Primitive;

/**
 * @version 2011/03/22 17:05:12
 */
public class AccessorNameTest {

    static {
        // When we activates only this test, it throws initialization error, so we have to load I
        // class at first.
        I.locate("");
    }

    /**
     * Decide a name of accessor method.
     */
    @Test
    public void normal() throws Exception {
        Model model = Model.load(Person.class);
        assert model != null;

        Property property = model.getProperty("age");
        assert property.accessors[0].getName().equals("getAge");
        assert property.accessors[1].getName().equals("setAge");
    }

    /**
     * Decide a name of accessor method.
     */
    @Test
    public void primitive() throws Exception {
        Model model = Model.load(Primitive.class);
        assert model != null;

        Property property = model.getProperty("boolean");
        assert property.accessors[0].getName().equals("isBoolean");
        assert property.accessors[1].getName().equals("setBoolean");

        property = model.getProperty("int");
        assert property.accessors[0].getName().equals("getInt");
        assert property.accessors[1].getName().equals("setInt");
    }
}
