/*
 * Copyright (C) 2018 Nameless Production Committee
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
 * @version 2018/03/09 0:48:14
 */
public class TransientPropertyTest {

    @Test
    public void transientGetter() {
        Model model = Model.of(TransientBean.class);
        Property property = model.property("onlyGetter");
        assert property.isTransient;
    }

    @Test
    public void transientSetter() {
        Model model = Model.of(TransientBean.class);
        Property property = model.property("onlySetter");
        assert property.isTransient;
    }

    @Test
    public void transientBoth() {
        Model model = Model.of(TransientBean.class);
        Property property = model.property("both");
        assert property.isTransient;
    }

    @Test
    public void transientInverse() {
        Model model = Model.of(TransientBean.class);
        Property property = model.property("inverse");
        assert property.isTransient;
    }

    @Test
    public void transientNone() {
        Model model = Model.of(TransientBean.class);
        Property property = model.property("none");
        assert !property.isTransient;
    }

    @Test
    public void transientField() {
        Model model = Model.of(TransientBean.class);
        Property property = model.property("field");
        assert property.isTransient;
    }

    @Test
    public void transientNoneField() {
        Model model = Model.of(TransientBean.class);
        Property property = model.property("noneField");
        assert !property.isTransient;
    }
}
