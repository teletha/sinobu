/*
 * Copyright (C) 2016 Nameless Production Committee
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
import kiss.sample.bean.ArrayBean;

/**
 * @version 2016/10/11 11:04:56
 */
public class ArrayPropertyTest {

    @Test
    public void property() {
        Model<ArrayBean> model = Model.of(ArrayBean.class);
        Property property = model.property("strings");
        assert property != null;
        assert property.model.type == String[].class;

        property = model.property("doubles");
        assert property != null;
        assert property.model.type == double[].class;
    }

    @Test
    public void get() {
        ArrayBean bean = I.make(ArrayBean.class);
        bean.ints = new int[] {1, 2, 3};

        Model model = Model.of(bean);
        Property property = model.property("ints");
        Object array = model.get(bean, property);
        assert array instanceof int[];

        Property item0 = property.model.property("0");
        Object value0 = property.model.get(array, item0);
        assert value0 instanceof Integer;

    }
}
