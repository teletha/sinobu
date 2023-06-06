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

class TransientPropertyTest {

    @Test
    void transientField() {
        Model model = Model.of(TransientBean.class);
        Property property = model.property("field");
        assert property.transitory;
    }

    @Test
    void noneTransientField() {
        Model model = Model.of(TransientBean.class);
        Property property = model.property("noneField");
        assert !property.transitory;
    }

    @Test
    void transientVariableField() {
        Model model = Model.of(TransientBean.class);
        Property property = model.property("variable");
        assert property.transitory;
    }

    @Test
    void noneTransientVariableField() {
        Model model = Model.of(TransientBean.class);
        Property property = model.property("noneVariable");
        assert !property.transitory;
    }
}