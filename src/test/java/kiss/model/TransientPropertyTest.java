/*
 * Copyright (C) 2023 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.model;

import org.junit.jupiter.api.Test;

import kiss.sample.bean.TransientBean;

/**
 * @version 2018/03/30 1:52:12
 */
public class TransientPropertyTest {

    @Test
    public void transientField() {
        Model model = Model.of(TransientBean.class);
        Property property = model.property("field");
        assert property.transitory;
    }

    @Test
    public void noneTransientField() {
        Model model = Model.of(TransientBean.class);
        Property property = model.property("noneField");
        assert !property.transitory;
    }

    @Test
    public void transientVariableField() {
        Model model = Model.of(TransientBean.class);
        Property property = model.property("variable");
        assert property.transitory;
    }

    @Test
    public void noneTransientVariableField() {
        Model model = Model.of(TransientBean.class);
        Property property = model.property("noneVariable");
        assert !property.transitory;
    }
}