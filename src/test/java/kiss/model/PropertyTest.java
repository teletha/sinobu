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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.jupiter.api.Test;

/**
 * @version 2016/05/02 9:36:48
 */
public class PropertyTest {

    @Test
    public void annotation() throws Exception {
        Model model = Model.of(Annotated.class);
        Property property = model.property("name");
        assert property.getAnnotation(KarenBee.class) != null;

        property = model.property("field");
        assert property.getAnnotation(KarenBee.class) != null;
    }

    /**
     * @version 2012/03/16 13:34:44
     */
    @SuppressWarnings("unused")
    private static class Annotated {

        @KarenBee
        public int field;

        private String name;

        /**
         * Get the name property of this {@link PropertyTest.Annotated}.
         * 
         * @return The name property.
         */
        protected String getName() {
            return name;
        }

        /**
         * Set the name property of this {@link PropertyTest.Annotated}.
         * 
         * @param name The name value to set.
         */
        @KarenBee
        protected void setName(String name) {
            this.name = name;
        }
    }

    /**
     * 
     */
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface KarenBee {
    }
}
