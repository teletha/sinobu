/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package ezbean.model;

import org.junit.Test;

import ezbean.I;
import ezbean.sample.bean.TransientBean;

/**
 * @version 2011/03/22 16:56:26
 */
public class TransientPropertyTest {

    static {
        // When we activates only this test, it throws initialization error, so we have to load I
        // class at first.
        I.locate("");
    }

    @Test
    public void transientGetter() throws Exception {
        Model model = Model.load(TransientBean.class);
        Property property = model.getProperty("onlyGetter");
        assert property.isTransient();
    }

    @Test
    public void transientSetter() throws Exception {
        Model model = Model.load(TransientBean.class);
        Property property = model.getProperty("onlySetter");
        assert property.isTransient();
    }

    @Test
    public void transientBoth() throws Exception {
        Model model = Model.load(TransientBean.class);
        Property property = model.getProperty("both");
        assert property.isTransient();
    }

    @Test
    public void transientInverse() throws Exception {
        Model model = Model.load(TransientBean.class);
        Property property = model.getProperty("inverse");
        assert property.isTransient();
    }

    @Test
    public void transientNone() throws Exception {
        Model model = Model.load(TransientBean.class);
        Property property = model.getProperty("none");
        assert !property.isTransient();
    }
}
