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

import org.junit.Test;

import kiss.sample.bean.TransientBean;

/**
 * @version 2014/07/21 17:07:32
 */
public class TransientPropertyTest {

    @Test
    public void transientGetter() throws Exception {
        Model model = Model.of(TransientBean.class);
        Property property = model.property("onlyGetter");
        assert property.isTransient;
    }

    @Test
    public void transientSetter() throws Exception {
        Model model = Model.of(TransientBean.class);
        Property property = model.property("onlySetter");
        assert property.isTransient;
    }

    @Test
    public void transientBoth() throws Exception {
        Model model = Model.of(TransientBean.class);
        Property property = model.property("both");
        assert property.isTransient;
    }

    @Test
    public void transientInverse() throws Exception {
        Model model = Model.of(TransientBean.class);
        Property property = model.property("inverse");
        assert property.isTransient;
    }

    @Test
    public void transientNone() throws Exception {
        Model model = Model.of(TransientBean.class);
        Property property = model.property("none");
        assert !property.isTransient;
    }
}
