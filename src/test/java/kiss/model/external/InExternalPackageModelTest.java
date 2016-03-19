/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.model.external;

import java.util.List;

import kiss.model.Model;
import kiss.model.Property;

import org.junit.Test;

/**
 * @version 2012/04/08 2:02:36
 */
public class InExternalPackageModelTest {

    @Test
    public void fieldPropertyInPrivateClass() throws Exception {
        Model model = Model.of(PrivateClass.class);
        assert model != null;

        List<Property> list = model.properties;
        assert 1 == list.size();
    }

    /**
     * @version 2012/04/07 11:59:27
     */
    private static final class PrivateClass {

        @SuppressWarnings("unused")
        public String property;
    }
}
